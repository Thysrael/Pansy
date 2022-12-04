package back.process;

import back.component.ObjBlock;
import back.component.ObjFunction;
import back.component.ObjModule;
import back.instruction.ObjInstr;
import back.instruction.ObjLoad;
import back.instruction.ObjMove;
import back.instruction.ObjStore;
import back.operand.*;
import util.MyList;
import util.MyPair;

import java.util.*;
import java.util.stream.Collectors;

import static back.operand.ObjPhyReg.SP;
import static back.process.BlockLiveInfo.livenessAnalysis;

public class RegAllocator
{
    private final ObjModule objModule;

    private final int K = ObjPhyReg.canAllocateRegIndex.size();

    private HashMap<ObjBlock, BlockLiveInfo> liveInfoMap;
    /**
     根据一个节点查询与之相关的节点组
     **/
    private HashMap<ObjOperand, HashSet<ObjOperand>> adjList;
    /**
     * 边的集合
     */
    private HashSet<MyPair<ObjOperand, ObjOperand>> adjSet;
    /**
     * 当一条传送指令 (u,v) 被合并，且 v 已经被放入 coalescedNodes 中，alias(v) = u
     */
    private HashMap<ObjOperand, ObjOperand> alias;
    /**
     * 从一个节点到与该节点相关的 mov 指令之间的映射
     */
    private HashMap<ObjOperand, HashSet<ObjMove>> moveList;
    private HashSet<ObjOperand> simplifyWorklist;
    /**
     * 低度数的，传送有关的节点表
     */
    private HashSet<ObjOperand> freezeWorklist;
    /**
     * 高度数的节点表
     */
    private HashSet<ObjOperand> spillWorklist;
    /**
     * 本轮中要被溢出的节点的集合
     */
    private HashSet<ObjOperand> spilledNodes;
    /**
     * 已合并的节点的集合，比如将 u 合并到 v，那么将 u 加入这里，然后 v 加入其他集合
     */
    private HashSet<ObjOperand> coalescedNodes;
    /**
     * 包含删除的点
     */
    private Stack<ObjOperand> selectStack;
    /**
     * 有可能合并的传送指令集合
     */
    private HashSet<ObjMove> worklistMoves;
    /**
     * 还未做好合并准备的传送指令集合
     */
    private HashSet<ObjMove> activeMoves;
    /**
     * 已经合并的传送指令集合
     */
    private HashSet<ObjInstr> coalescedMoves;
    /**
     *源操作数和目标操作数冲突的传送指令集合
     */
    private HashSet<ObjMove> constrainedMoves;
    /**
     * 不考虑合并的传送指令集合
     */
    private HashSet<ObjMove> frozenMoves;
    /**
     * 节点的度
     */
    private HashMap<ObjOperand, Integer> degree;
    /**
     * 新的虚拟寄存器，用来处理溢出解决时引入的新的虚拟寄存器
     */
    ObjVirReg vReg = null;
    /**
     * 似乎是第一次使用新的寄存器的 store 指令，因为替换是一个先 store，后 load 的过程
     */
    MyList.MyNode<ObjInstr> firstUseNode = null;
    /**
     * 最后一次使用 load 的指令
     */
    MyList.MyNode<ObjInstr> lastDefNode = null;
    /**
     * 存储操作数和所在的基本块对应的循环深度
     */
    HashMap<ObjOperand, Integer> loopDepths = new HashMap<>();

    public RegAllocator(ObjModule objModule)
    {
        this.objModule = objModule;
    }

    /**
     * 这个方法用于初始化一系列的数据结构，并且在 degree 中登记物理寄存器信息
     * @param function 待分析的函数
     */
    private void init(ObjFunction function)
    {
        liveInfoMap = livenessAnalysis(function);
        adjList = new HashMap<>();
        adjSet = new HashSet<>();
        alias = new HashMap<>();
        moveList = new HashMap<>();
        simplifyWorklist = new HashSet<>();
        freezeWorklist = new HashSet<>();
        spillWorklist = new HashSet<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        selectStack = new Stack<>();

        worklistMoves = new HashSet<>();
        activeMoves = new HashSet<>();
        // 下面这三个变量不一定用得到，但是 coalescedMoves 考虑删掉里面所有的 move，似乎是之前代码没有办到的
        coalescedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();

        degree = new HashMap<>();
        // 对于物理寄存器，需要度无限大
        for (int i = 0; i < 32; i++)
        {
            degree.put(new ObjPhyReg(i), Integer.MAX_VALUE);
        }
    }

    /**
     * 在冲突图上添加无向边
     * @param u 第一个节点
     * @param v 第二个节点
     */
    private void addEdge(ObjOperand u, ObjOperand v)
    {
        // 如果没有这条边而且这个边不是自环
        // 从上面就可以看出，adjSet 是个边的集合，边是用 pair 模拟的
        if (!adjSet.contains(new MyPair<>(u, v)) && !u.equals(v))
        {
            // 无向边的加法
            adjSet.add(new MyPair<>(u, v));
            adjSet.add(new MyPair<>(v, u));

            // 操作条件都是没有被预着色
            if (!u.isPrecolored())
            {
                // 从这里看，adjList 是一个可以用节点查询所连接的所有节点的一个结构
                adjList.putIfAbsent(u, new HashSet<>());
                adjList.get(u).add(v);
                // degree.putIfAbsent(u, 0);
                // degree 则是用来表示节点的度的
                degree.compute(u, (key, value) -> value == null ? 0 : value + 1);
            }
            if (!v.isPrecolored())
            {
                adjList.putIfAbsent(v, new HashSet<>());
                adjList.get(v).add(u);
                degree.compute(v, (key, value) -> value == null ? 0 : value + 1);
            }
        }
    }

    /**
     * 通过逆序遍历函数中的所有指令, 生成冲突图
     * live 是每条指令的冲突变量集合
     * @param function 待分析函数
     */
    private void build(ObjFunction function)
    {
        // 这是要倒序遍历 block,是为了确定 range 的范围
        for (MyList.MyNode<ObjBlock> blockNode = function.getObjBlocks().getTail();
             blockNode != null;
             blockNode = blockNode.getPre())
        {
            ObjBlock block = blockNode.getVal();
            // 这里假设他是出口活跃的,似乎还是不一样，但是似乎也是可以理解的，只要最后可以获得即可
            // live 是一个很有意思的东西，他看似一个 block 只有一个，但是因为每条指令都更新它，所以它本质是一个指令颗粒度的东西
            // 我们会根据 live 的内容去构建冲突图
            HashSet<ObjReg> live = new HashSet<>(liveInfoMap.get(block).getLiveOut());

            for(MyList.MyNode<ObjInstr> instrNode = block.getInstrs().getTail();
                instrNode != null;
                instrNode = instrNode.getPre())
            {
                ObjInstr instr = instrNode.getVal();
                ArrayList<ObjReg> regDef = instr.getRegDef();
                ArrayList<ObjReg> regUse = instr.getRegUse();
                // 对于 mov 指令，需要特殊处理
                if (instr instanceof ObjMove)
                {
                    ObjMove objMove = (ObjMove) instr;
                    ObjOperand src = objMove.getSrc();
                    ObjOperand dst = objMove.getDst();

                    if (src.needsColor() && dst.needsColor())
                    {
                        live.remove((ObjReg) src);

                        moveList.putIfAbsent(src, new HashSet<>());
                        moveList.get(src).add(objMove);

                        moveList.putIfAbsent(dst, new HashSet<>());
                        moveList.get(dst).add(objMove);
                        // 此时是有可能被合并的
                        worklistMoves.add(objMove);
                    }
                }

                regDef.stream().filter(ObjReg::needsColor).forEach(live::add);

                // 构建冲突边的时候，只是构建了 def 与 live 的冲突，这样似乎不够
                // 但是其实，是够得，因为在一个个指令的遍历中，能增加边的，只有 def 导致的活跃
                regDef.stream().filter(ObjReg::needsColor).forEach(d -> live.forEach(l -> addEdge(l, d)));

                // 启发式算法的依据，用于后面挑选出溢出节点
                for (ObjReg objReg : regDef)
                {
                    loopDepths.put(objReg, block.getLoopDepth());
                }
                for (ObjReg objReg : regUse)
                {
                    loopDepths.put(objReg, block.getLoopDepth());
                }

                // 这里的删除是为了给前一个指令一个交代（倒序遍历），说明这个指令不再存活了（因为在这个指令被遍历了）
                regDef.stream().filter(ObjReg::needsColor).forEach(live::remove);
                // 这里代表着又活了一个指令
                regUse.stream().filter(ObjReg::needsColor).forEach(live::add);
            }
        }
    }

    /**
     * 遍历所有的节点（错，只是非预着色点）, 把这些节点分配加入不同的 workList
     * 但是预着色点依然在这里面存在
     * @param function 待分析的函数
     */
    private void makeWorklist(ObjFunction function)
    {
        // 这里似乎是在把一个函数中用到的所有虚拟寄存器都提出来，注意这里面并不包括物理寄存器（也就是预着色的点）
        for (ObjVirReg virReg : function.getUsedVirRegs())
        {
            // 如果度是大于 K 的，那么就要加入 spillWorklist 了，他们是可能发生实际溢出的
            if (degree.getOrDefault(virReg, 0) >= K)
            {
                spillWorklist.add(virReg);
            }
            // 说白了就是跟 mov 指令相关的操作数，那么就加入 freezeWorklist
            else if (moveRelated(virReg))
            {
                freezeWorklist.add(virReg);
            }
            // 否则就要加到 simplifyWorklist 中，就是可以进行化简的
            else
            {
                simplifyWorklist.add(virReg);
            }
        }
    }

    /**
     * 这个函数从 adjList 中取出对应的点，但是有一些条件，比如 selectStack 和 coalesceNode 里的不取
     * 因为是没有删除边的操作的, 所以对于一些节点, 比如已经删掉或者合并的, 就需要从这里去掉
     * @param u 一个节点
     * @return 与这个节点相连的节点组
     */
    private Set<ObjOperand> getAdjacent(ObjOperand u)
    {
        return adjList.getOrDefault(u, new HashSet<>()).stream()
                .filter(v -> !(selectStack.contains(v) || coalescedNodes.contains(v)))
                .collect(Collectors.toSet());
    }

    /**
     * 这个会根据节点取出一些 Move 指令，必须在 activeMoves 和 workListMoves 中有
     * 至于为啥要在这个里面有, 可能是因为这两个是需要考虑的
     * @param u 待检测的节点
     * @return mov 的集合
     */
    private Set<ObjMove> nodeMoves(ObjOperand u)
    {
        return moveList.getOrDefault(u, new HashSet<>()).stream()
                .filter(objMove -> activeMoves.contains(objMove) || worklistMoves.contains(objMove))
                .collect(Collectors.toSet());
    }

    /**
     * 这里进行了一个节点 u 和其相连的节点将 activeMoves 删去，然后加入到 workListMoves 的操作
     * 也就是将这个节点和与其相连的 mov 节点都从“不能合并”状态转换为“能合并”状态
     * 从这里可以看出，能合并要求度是 K - 1 以下
     * @param u 节点
     */
    private void enableMoves(ObjOperand u)
    {
        nodeMoves(u).stream()
                .filter(activeMoves::contains)
                .forEach(m ->
                {
                    activeMoves.remove(m);
                    worklistMoves.add(m);
                });

        getAdjacent(u).forEach(a -> nodeMoves(a).stream()
                .filter(activeMoves::contains)
                .forEach(m ->
                {
                    activeMoves.remove(m);
                    worklistMoves.add(m);
                }));
    }


    /**
     * 这个方法用来一个寄存器是不是 mov 指令的操作数
     * @param u 节点
     * @return 如果是, 那么是 true
     */
    private boolean moveRelated(ObjOperand u)
    {
        return !nodeMoves(u).isEmpty();
    }

    /**
     * 这里描述的是, 当简化一个节点的时候, 与其相连的节点都需要进行一定的改动
     * 最简单的就是降低度,
     * 此外, 随着度的降低, 有些节点会从某个 list 移动到另一个 list
     * @param u 相连的节点
     */
    private void decreaseDegree(ObjOperand u)
    {
        int d = degree.get(u);
        degree.put(u, d - 1);

        // 当未修改的度是 K 的时候，那么修改过后就是 K - 1， 那么此时就需要特殊处理
        if (d == K)
        {
            enableMoves(u);
            spillWorklist.remove(u);
            if (moveRelated(u))
            {
                freezeWorklist.add(u);
            }
            else
            {
                simplifyWorklist.add(u);
            }
        }
    }

    /**
     * 这个函数会从 simplifyWorklist 中节点，然后加入到 selectStack 中
     * 与此同时，需要修改与这个节点相关的节点的度
     */
    private void simplify()
    {
        ObjOperand n = simplifyWorklist.iterator().next();
        // 从可以简化的列表中取出一个节点
        simplifyWorklist.remove(n);
        // 从这里可以看出，selectStack 就是图着色时用的栈
        selectStack.push(n);
        // 把与这个删掉的点有关的点的度都降低
        getAdjacent(n).forEach(this::decreaseDegree);
    }

    /**
     * alias 是别名的意思，这个说的是，对于一个合并的节点，他可以有两个名字，所以可以检索它的另一个名字
     * @param u 被合并节点
     * @return 被合并的另一个节点
     */
    private ObjOperand getAlias(ObjOperand u)
    {
        while (coalescedNodes.contains(u))
        {
            u = alias.get(u);
        }
        return u;
    }

    /**
     * 这个函数实现的是将一个节点从 freezeWorklist 移动到 simplifyWorklist 中
     * 这是一个 coalesce 过程的子方法，主要用于合并
     * @param u  待合并的节点
     */
    private void addWorklist(ObjOperand u)
    {
        if (!u.isPrecolored() && !moveRelated(u) && degree.getOrDefault(u, 0) < K)
        {
            freezeWorklist.remove(u);
            simplifyWorklist.add(u);
        }
    }

    /**
     * 这是用来判断 v，u 是否可以合并的
     * 判断方法是考虑 v 的临边关系，这种判断方法被称为 George
     * @param v 一定是虚拟寄存器
     * @param u 可能是物理寄存器
     * @return 可以合并则为 true
     */
    private boolean adjOk(ObjOperand v, ObjOperand u)
    {
        return getAdjacent(v).stream().allMatch(t -> ok(t, u));
    }

    /**
     * 具教材说，这是一个合并一个预着色寄存器的时候用到的启发式函数
     * 其中 t 是待合并的虚拟寄存器的邻接点，r 是待合并的预着色寄存器
     * 这三个条件满足一个就可以合并
     * @param t 合并的虚拟寄存器的邻接点
     * @param r 待合并的预着色寄存器
     * @return 可以合并就是 true
     */
    private boolean ok(ObjOperand t, ObjOperand r)
    {
        return degree.get(t) < K || t.isPrecolored() || adjSet.contains(new MyPair<>(t, r));
    }

    /**
     * 这是另一种保守地判断可不可以合并的方法，有一说一，我没看出为啥要用两种方法来
     * 被称为 briggs 法
     * @param u 待合并的节点 1
     * @param v 待合并的节点 2
     * @return 可以合并就是 true
     */
    private boolean conservative(ObjOperand u, ObjOperand v)
    {
        Set<ObjOperand> uAdjacent = getAdjacent(u);
        Set<ObjOperand> vAdjacent = getAdjacent(v);
        uAdjacent.addAll(vAdjacent);
        long count = uAdjacent.stream().filter(n -> degree.get(n) >= K).count();
        return count < K;
    }

    /**
     * 这是合并操作
     * @param u 待合并的节点 1
     * @param v 待合并的节点 2
     */
    private void combine(ObjOperand u, ObjOperand v)
    {
        // 这里做的是把他们从原有的 worklist 中移出
        if (freezeWorklist.contains(v))
        {
            freezeWorklist.remove(v);
        }
        else
        {
            spillWorklist.remove(v);
        }

        coalescedNodes.add(v);
        // 这里没有问题，相当于 alias 的 key 是虚拟寄存器，而 value 是物理寄存器
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));
        // enableMoves.accept(v);
        getAdjacent(v).forEach(t ->
        {
            addEdge(t, u);
            decreaseDegree(t);
        });

        if (degree.getOrDefault(u, 0) >= K && freezeWorklist.contains(u))
        {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }

    /**
     * 用于合并节点
     */
    private void coalesce()
    {
        ObjMove objMove = worklistMoves.iterator().next();
        ObjOperand u = getAlias(objMove.getDst());
        ObjOperand v = getAlias(objMove.getSrc());

        // 如果 v 是物理寄存器，那么就需要交换一下，最后的结果就是如果有物理寄存器的话，那么一定是 u
        // 之所以这么操作，是因为合并也是一种着色，我们需要让合并后剩下的那个节点，是预着色点
        if (v.isPrecolored())
        {
            ObjOperand tmp = u;
            u = v;
            v = tmp;
        }

        worklistMoves.remove(objMove);

        // 这个对应可以要进行合并了
        if (u.equals(v))
        {
            coalescedMoves.add(objMove);
            addWorklist(u);
        }
        // 对应源操作数和目的操作数冲突的情况，此时的 mov 就是受到抑制的，也就是
        else if (v.isPrecolored() || adjSet.contains(new MyPair<>(u, v)))
        {
            constrainedMoves.add(objMove);
            addWorklist(u);
            addWorklist(v);
        }
        //
        else if ((u.isPrecolored() && adjOk(v, u)) ||
                (!u.isPrecolored() && conservative(u, v)))
        {
            coalescedMoves.add(objMove);
            combine(u, v);
            addWorklist(u);
        }
        else
        {
            activeMoves.add(objMove);
        }
    }

    /**
     * 这个会遍历每一条与 u 有关的 mov 指令，然后将这些 mov 指令从 active 和 worklist 中移出
     * 这就意味着他们不会再被考虑合并
     * @param u 待冻结的节点
     */
    private void freezeMoves(ObjOperand u)
    {
        for (ObjMove objMove : nodeMoves(u))
        {
            if (activeMoves.contains(objMove))
            {
                activeMoves.remove(objMove);
            }
            else
            {
                worklistMoves.remove(objMove);
            }

            frozenMoves.add(objMove);
            ObjOperand v = getAlias(objMove.getDst()).equals(getAlias(u)) ? getAlias(objMove.getSrc()) : getAlias(objMove.getDst());

            if (!moveRelated(v) && degree.getOrDefault(v, 0) < K)
            {
                freezeWorklist.remove(v);
                simplifyWorklist.add(v);
            }
        }
    }

    /**
     * 当 simplify 无法进行：没有低度数的，无关 mov 的点了
     * 当 coalesce 无法进行：没有符合要求可以合并的点了
     * 那么进行 freeze，就是放弃一个低度数的 mov 的点，这样就可以 simplify 了
     */
    private void freeze()
    {
        ObjOperand u = freezeWorklist.iterator().next();
        freezeWorklist.remove(u);
        simplifyWorklist.add(u);
        freezeMoves(u);
    }

    /**
     * 这里用到了启发式算法，因为没有 loopDepth 所以只采用一个很简单的方式，调出一个需要溢出的节点，
     * 这个节点的性质是溢出后边会大幅减少
     */
    private void selectSpill()
    {
        // TODO 这里太慢了，要不然直接挑第一个吧
        ObjOperand m = spillWorklist.stream().max((l, r) ->
        {
            double value1 = degree.getOrDefault(l, 0).doubleValue() / Math.pow(1.414, loopDepths.getOrDefault(l, 0));
            double value2 = degree.getOrDefault(r, 0).doubleValue() / Math.pow(1.414, loopDepths.getOrDefault(l, 0));

            return Double.compare(value1, value2);
        }).get();
        // ObjOperand m = spillWorklist.iterator().next();
        simplifyWorklist.add(m);
        freezeMoves(m);
        spillWorklist.remove(m);
    }

    private void assignColors(ObjFunction func)
    {
        // colored 是记录虚拟寄存器到物理寄存器的映射关系的
        HashMap<ObjOperand, ObjOperand> colored = new HashMap<>();

        while (!selectStack.isEmpty())
        {
            // 从栈上弹出一个节点
            ObjOperand n = selectStack.pop();
            // 这里做了一个包含所有待分配颜色的数组，可以看到是对于每个弹出节点，都会有这样的一个集合，表示这个节点可能的颜色
            // 这个集合会通过与其邻接点比对而不断缩小
            HashSet<Integer> okColors = new HashSet<>(ObjPhyReg.canAllocateRegIndex);
            // 遍历与这个弹出的节点
            for (ObjOperand w : adjList.getOrDefault(n, new HashSet<>()))
            {
                ObjOperand a = getAlias(w);
                // 如果这个邻接点是物理寄存器，那么就要移除掉
                if (a.isAllocated() || a.isPrecolored())
                {
                    okColors.remove(((ObjPhyReg) a).getIndex());
                }
                // 如果邻接点是一个虚拟寄存器，而且已经被着色了
                else if (a instanceof ObjVirReg)
                {
                    if (colored.containsKey(a))
                    {
                        ObjOperand color = colored.get(a);
                        okColors.remove(((ObjPhyReg) color).getIndex());
                    }
                }
            }
            // 如果没有备选颜色，那么就发生实际溢出
            if (okColors.isEmpty())
            {
                spilledNodes.add(n);
            }
            else
            {
                Integer color = okColors.iterator().next();
                colored.put(n, new ObjPhyReg(color, true));
            }
        }

        if (!spilledNodes.isEmpty())
        {
            return;
        }
        // 当处理完 stack 后如果还没有问题，那么就可以处理合并节点了
        // 这里的原理相当于在一开始 stack 中只压入部分点（另一些点由栈中的点代表）
        for (ObjOperand coalescedNode : coalescedNodes)
        {
            ObjOperand alias = getAlias(coalescedNode);
            // 如果合并的节点里有物理寄存器，而且还是一个预着色寄存器
            if (alias.isPrecolored())
            {
                colored.put(coalescedNode, alias);
            }
            // 如果全是虚拟寄存器
            else
            {
                colored.put(coalescedNode, colored.get(alias));
            }
        }

        // 这里完成了替换
        for (MyList.MyNode<ObjBlock> blockNode : func.getObjBlocks())
        {
            ObjBlock block = blockNode.getVal();

            for (MyList.MyNode<ObjInstr> instrEntry : block.getInstrs())
            {
                ObjInstr instr = instrEntry.getVal();
                ArrayList<ObjReg> defs = new ArrayList<>(instr.getRegDef());
                ArrayList<ObjReg> uses = new ArrayList<>(instr.getRegUse());

                for (ObjReg def : defs)
                {
                    if (colored.containsKey(def))
                    {
                        instr.replaceReg(def, colored.get(def));
                    }
                }
                for (ObjReg use : uses)
                {
                    if (colored.containsKey(use))
                    {
                        instr.replaceReg(use, colored.get(use));
                    }
                }
            }

        }
    }

    /**
     * 这个方法用于确定加入溢出 load 和 store 的 offset
     * @param func 函数
     * @param instrNode load 或者 store 节点
     */
    private void fixOffset(ObjFunction func, MyList.MyNode<ObjInstr> instrNode)
    {
        ObjInstr instr = instrNode.getVal();
        int offset = func.getAllocaSize();
        ObjImm objOffset = new ObjImm(offset);
        // offset 的编码规则与之前不同
        if (instr instanceof ObjLoad)
        {
            ObjLoad objLoad = (ObjLoad) instr;
            objLoad.setOffset(objOffset);
        }
        else if (instr instanceof ObjStore)
        {
            ObjStore objStore = (ObjStore) instr;
            objStore.setOffset(objOffset);
        }
    }

    /**
     * 用于完成将新的，处理溢出的临时变量插入到基本块中的功能
     * @param func 函数
     */
    private void checkPoint(ObjFunction func)
    {
        if (firstUseNode != null)
        {
            ObjLoad objLoad = new ObjLoad(vReg, SP, null);
            MyList.MyNode<ObjInstr> objLoadNode = new MyList.MyNode<>(objLoad);
            objLoadNode.insertBefore(firstUseNode);
            fixOffset(func, objLoadNode);

            firstUseNode = null;
        }

        if (lastDefNode != null)
        {
            ObjStore objStore = new ObjStore(vReg, SP, null);
            MyList.MyNode<ObjInstr> objStoreNode = new MyList.MyNode<>(objStore);
            objStoreNode.insertAfter(lastDefNode);
            fixOffset(func, objStoreNode);

            lastDefNode = null;
        }

        vReg = null;
    }

    private void rewriteProgram(ObjFunction func)
    {
        for (ObjOperand n : spilledNodes)
        {
            for (MyList.MyNode<ObjBlock> blockNode : func.getObjBlocks())
            {
                ObjBlock block = blockNode.getVal();

                vReg = null;
                firstUseNode = null;
                lastDefNode = null;
                // cntInstr 是 block 中已经处理的指令的个数
                int cntInstr = 0;
                for (MyList.MyNode<ObjInstr> instrNode : block.getInstrs())
                {
                    ObjInstr instr = instrNode.getVal();
                    HashSet<ObjReg> defs = new HashSet<>(instr.getRegDef());
                    HashSet<ObjReg> uses = new HashSet<>(instr.getRegUse());

                    for (ObjReg use : uses)
                    {
                        if (use.equals(n))
                        {
                            if (vReg == null)
                            {
                                vReg = new ObjVirReg();
                                func.addUsedVirReg(vReg);
                            }
                            instr.replaceReg(use, vReg);

                            if (firstUseNode == null && lastDefNode == null)
                            {
                                firstUseNode = instrNode;
                            }
                        }
                    }
                    // n 是最外层遍历的实际溢出的节点
                    // 这里说的是如果这个溢出的节点是目的寄存器
                    for (ObjReg def : defs)
                    {
                        // 这里似乎只针对第一个等于 n 的目的寄存器
                        // 因为显然只需要一个虚拟寄存器来保存溢出节点
                        if (def.equals(n))
                        {
                            if (vReg == null)
                            {
                                vReg = new ObjVirReg();
                                func.addUsedVirReg(vReg);
                            }
                            instr.replaceReg(def, vReg);
                            lastDefNode = instrNode;
                        }
                    }

                    // TODO 这里其实是一个权衡，改这里会不会让时间变快
                    if (cntInstr > 30)
                    {
                        checkPoint(func);
                    }

                    cntInstr++;
                }

                checkPoint(func);
            }

            // 为这个临时变量在栈上分配空间
            func.addAllocaSize(4);
        }
    }

    private void clearPhyRegState()
    {
        for (ObjFunction function : objModule.getFunctions())
        {
            for (MyList.MyNode<ObjBlock> objBlockNode : function.getObjBlocks())
            {
                ObjBlock objBlock = objBlockNode.getVal();
                for (MyList.MyNode<ObjInstr> instrNode : objBlock.getInstrs())
                {
                    ObjInstr instr = instrNode.getVal();
                    for (ObjReg objReg : instr.getRegDef())
                    {
                        if (objReg instanceof ObjPhyReg)
                        {
                            ((ObjPhyReg) objReg).setAllocated(false);
                        }
                    }

                    for (ObjReg objReg : instr.getRegUse())
                    {
                        if (objReg instanceof ObjPhyReg)
                        {
                            ((ObjPhyReg) objReg).setAllocated(false);
                        }
                    }
                }
            }
        }
    }

    public void process()
    {
        for (ObjFunction function : objModule.getNoBuiltinFunctions())
        {
            boolean finished = false;

            while (!finished)
            {
                init(function);
                build(function);
                makeWorklist(function);

                do
                {
                    if (!simplifyWorklist.isEmpty())
                    {
                        simplify();
                    }
                    if (!worklistMoves.isEmpty())
                    {
                        coalesce();
                    }
                    if (!freezeWorklist.isEmpty())
                    {
                        freeze();
                    }
                    if (!spillWorklist.isEmpty())
                    {
                        selectSpill();
                    }
                } while (!(simplifyWorklist.isEmpty() && worklistMoves.isEmpty() &&
                        freezeWorklist.isEmpty() && spillWorklist.isEmpty()));
                assignColors(function);

                // 这里看一下实际溢出的节点
                if (spilledNodes.isEmpty())
                {
                    finished = true;
                }
                // 存在实际溢出的点
                else
                {
                    rewriteProgram(function);
                }
            }
        }


        // 因为在 color 的时候，会把 isAllocated 设置成 true，这个函数的功能就是设置成 false
        // 应该是为了避免物理寄存器在判定 equals 时的错误
        clearPhyRegState();

        for (ObjFunction function : objModule.getNoBuiltinFunctions())
        {
            function.fixStack();
        }
    }
}
