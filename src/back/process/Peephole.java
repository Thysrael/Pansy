package back.process;

import back.component.ObjBlock;
import back.component.ObjFunction;
import back.component.ObjModule;
import back.instruction.*;
import back.operand.*;
import driver.Config;
import util.MyList;
import util.MyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Peephole
{
    private final ObjModule objModule;

    public Peephole(ObjModule objModule)
    {
        this.objModule = objModule;
    }

    public void process()
    {
        boolean finished = false;
        while (!finished)
        {
            finished = peephole();
            if (Config.openDataPeepHole)
            {
                finished &= dataFlowPeephole();
            }
        }
    }

    /**
     * 这里的优化都不涉及数据流分析，也就是说，很多的优化其实都是本身指令的变化
     * 当涉及多条指令的作用的时候，就需要用数据流分析了
     * @return 是否被改变
     */
    private boolean peephole()
    {
        // 任何优化都不会发生，那么才叫做 finished
        boolean finished = true;

        for (ObjFunction function : objModule.getFunctions())
        {
            // 有一说一，这里设计的很好，需要强大链表功能的地方就是方便各种奇形怪状的删除
            for (MyList.MyNode<ObjBlock> objBlockNode : function.getObjBlocks())
            {
                ObjBlock objBlock = objBlockNode.getVal();

                for (MyList.MyNode<ObjInstr> curInstrNode : objBlock.getInstrs())
                {
                    finished &= addSubSrc2Zero(curInstrNode);
                    finished &= movSameDstSrc(curInstrNode);
                    finished &= movOverlap(curInstrNode);
                    finished &= branchUselessDelete(curInstrNode, objBlockNode);
                    finished &= loadToMov(curInstrNode);
                }
            }
        }

        return finished;
    }

    /**
     * 处理的是加减法第二个数为 0 的情况，有
     * add r0, r1, 0 => mov r0, r1
     * add r0, r0, 0 => null
     * sub 同理
     */
    private boolean addSubSrc2Zero(MyList.MyNode<ObjInstr> curInstrNode)
    {
        boolean finished = true;

        ObjInstr curInstr = curInstrNode.getVal();

        if (curInstr instanceof ObjBinary)
        {
            ObjBinary instr = (ObjBinary) curInstr;
            String type = instr.getType();
            if (type.equals("addu") || type.equals("subu"))
            {
                boolean isSrc2Zero = (instr.getSrc2() instanceof ObjImm) && (((ObjImm) instr.getSrc2()).getImmediate() == 0);

                if (isSrc2Zero)
                {
                    boolean isDstSrc1Same = instr.getDst().equals(instr.getSrc1());

                    if (isDstSrc1Same)
                    {
                        curInstrNode.removeSelf();
                    }
                    else
                    {
                        ObjMove objMove = new ObjMove(instr.getDst(), instr.getSrc1());
                        MyList.MyNode<ObjInstr> objMoveNode = new MyList.MyNode<>(objMove);
                        objMoveNode.insertBefore(curInstrNode);
                        curInstrNode.removeSelf();
                    }
                    finished = false;
                }
            }
        }

        return finished;
    }

    /**
     * 处理的是 mov 指令源和目的寄存器相同的情况
     * mov r0, r0 => null
     */
    private boolean movSameDstSrc(MyList.MyNode<ObjInstr> curInstrNode)
    {
        boolean finished = true;

        ObjInstr curInstr = curInstrNode.getVal();

        if (curInstr instanceof ObjMove)
        {
            ObjMove objMove = (ObjMove) curInstr;
            // 相等且没有移位
            if (objMove.getDst().equals(objMove.getSrc()))
            {
                curInstrNode.removeSelf();
                finished = false;
            }
        }

        return finished;
    }

    /**
     * 处理的是两个 mov 时的赋值覆盖问题
     * mov r0, r1 (cur, remove)
     * mov r0, r2
     */
    private boolean movOverlap(MyList.MyNode<ObjInstr> curInstrNode)
    {
        boolean finished = true;

        ObjInstr curInstr = curInstrNode.getVal();
        MyList.MyNode<ObjInstr> nextInstrNode = curInstrNode.getNext();

        if (curInstr instanceof ObjMove)
        {
            if (nextInstrNode != null && nextInstrNode.getVal() instanceof ObjMove)
            {
                ObjMove nextInstr = (ObjMove) nextInstrNode.getVal();
                boolean isSameDst = nextInstr.getDst().equals(((ObjMove) curInstr).getDst());
                boolean nextInstrDifferent = !nextInstr.getSrc().equals(nextInstr.getDst());

                if (isSameDst && nextInstrDifferent)
                {
                    curInstrNode.removeSelf();
                    finished = false;
                }
            }
        }

        return finished;
    }

    /**
     * 判断的是
     * j block_label => null
     * block_label:
     */
    private boolean branchUselessDelete(MyList.MyNode<ObjInstr> curInstrNode, MyList.MyNode<ObjBlock> objBlockNode)
    {
        boolean finished = true;
        ObjInstr curInstr = curInstrNode.getVal();
        if (curInstr instanceof ObjBranch)
        {
            ObjBranch objBranch = (ObjBranch) curInstr;

            boolean hasNoCond = objBranch.hasNoCond();
            ObjBlock nextBlock = (objBlockNode.getNext() == null) ? null : objBlockNode.getNext().getVal();
            boolean isNear = objBranch.getTarget().equals(nextBlock);

            if (isNear && hasNoCond)
            {
                curInstrNode.removeSelf();
                finished = false;
            }
        }

        return finished;
    }

    /**
     *   store a, memory
     *   load b, sameMemory
     *   =>
     *   move b, a
     */
    private boolean loadToMov(MyList.MyNode<ObjInstr> curInstrNode)
    {
        boolean finished = true;
        ObjInstr curInstr = curInstrNode.getVal();
        MyList.MyNode<ObjInstr> preInstrNode = curInstrNode.getPre();
        if (curInstr instanceof ObjLoad)
        {
            ObjLoad objLoad = (ObjLoad) curInstr;
            if (preInstrNode != null && preInstrNode.getVal() instanceof ObjStore)
            {
                ObjStore objStore = (ObjStore) preInstrNode.getVal();

                boolean isSameAddr = objStore.getAddr().equals(objLoad.getAddr());
                boolean isSameOffset = objStore.getAddr().equals(objLoad.getOffset());

                if (isSameAddr && isSameOffset)
                {
                    ObjMove objMove = new ObjMove(objLoad.getDst(), objStore.getSrc());
                    MyList.MyNode<ObjInstr> objMoveNode = new MyList.MyNode<>(objMove);
                    objMoveNode.insertAfter(preInstrNode);
                    curInstrNode.removeSelf();
                    finished = false;
                }
            }
        }

        return finished;
    }

    /**
     * 这个数数据流窥孔的最重要的分析方法，它以基本块为单位进行分析
     * 最终我们获得的是一个 writerToReader 的映射，这个映射可以根据一个写指令去查询最后一次的与之相关的读指令
     * 之所以这个映射有用，是因为我们在数据流窥孔中会删除写指令或者改变写指令，
     * 而一旦改变，后面的读指令也应当发生改变，只有当写指令与读指令的距离小于窥孔的直径的时候，才是可以更改的
     * @param objBlock 当前块
     * @return 两个映射
     */
    private MyPair<HashMap<ObjOperand, ObjInstr>, HashMap<ObjInstr, ObjInstr>> getLiveRangeInBlock(ObjBlock objBlock)
    {
        // 这个记录的是每个操作数与它最后一次被写的指令之间的映射
        HashMap<ObjOperand, ObjInstr> lastWriter = new HashMap<>();
        // 这个记录的是 key = writer，value = reader 的映射
        // key -> value 记录的是一个写了某个寄存器的指令到其后读了某个寄存器的指令之间的映射
        // 因为 writerToReader 会持续更新，所以本质是记录着写指令到最后一条相关的读指令之间的映射
        HashMap<ObjInstr, ObjInstr> writerToReader = new HashMap<>();

        // 开始遍历每一条指令
        for (MyList.MyNode<ObjInstr> instrNode : objBlock.getInstrs())
        {
            ObjInstr instr = instrNode.getVal();
            // 不同于 use 和 def，write 和 read 的范围更加大
            ArrayList<ObjReg> writeRegs = instr.getWriteRegs();
            ArrayList<ObjReg> readRegs = instr.getReadRegs();

            // 这里遍历每一个需要用到的寄存器，然后填写 writerToReader
            for (ObjReg readReg : readRegs)
            {
                if (lastWriter.containsKey(readReg))
                {
                    writerToReader.put(lastWriter.get(readReg), instr);
                }
            }

            for (ObjReg writeReg : writeRegs)
            {
                lastWriter.put(writeReg, instr);
            }

            // 这里标记的是不可以删除的指令
            // 但是由于这些寄存器是不写寄存器的，所以很容易被删除，但是显然我们又不能让他们被删除
            // 所以我们会有个妥协（这个妥协是没有数学意义的）
            boolean hasSideEffect = instr instanceof ObjBranch ||
                    instr instanceof ObjCall ||
                    instr instanceof ObjStore ||
                    instr instanceof ObjRet ||
                    instr instanceof ObjComment;

            // 这里很有意思，其实应该是将当前指令对应的 user 登记成 null，然后等待之后的遍历更新
            // 但是有的时候就真的不会用到了，那么就是只定义，没有使用，如果不在其他的基本块中使用，那么就可以删除了
            // 上面的前提是删除的指令定义的寄存器也不会在后续块中使用（这其实是数据流分析窥孔的前提，就是避免分析很多复杂的指令）
            writerToReader.put(instr, hasSideEffect ? instr : null);
        }

        return new MyPair<>(lastWriter, writerToReader);
    }

    /*
     * 底下的这些属性都是为了避免繁琐的传参设计的，其实会一直发生变化
     */
    /**
     * 当前指令作为写指令的时候，所相关的最后一条读指令
     */
    private ObjInstr lastReader = null;
    /**
     * 当前指令是否会对 sp 造成影响
     */
    private boolean notWriteSp = true;
    /**
     * 下一条指令的节点
     */
    MyList.MyNode<ObjInstr> nextInstrNode = null;

    private boolean dataFlowPeephole()
    {
        boolean finished = true;

        for (ObjFunction function : objModule.getFunctions())
        {
            HashMap<ObjBlock, BlockLiveInfo> liveInfoMap = BlockLiveInfo.livenessAnalysis(function);

            for (MyList.MyNode<ObjBlock> objBlockNode : function.getObjBlocks())
            {
                ObjBlock objBlock = objBlockNode.getVal();

                // liveOut 中存着出口活跃的寄存器
                HashSet<ObjReg> liveOut = liveInfoMap.get(objBlock).getLiveOut();
                MyPair<HashMap<ObjOperand, ObjInstr>, HashMap<ObjInstr, ObjInstr>> retPair = getLiveRangeInBlock(objBlock);
                // lastWriter 可以根据寄存器查找上一个写者（最后一次写这个寄存器的指令）
                HashMap<ObjOperand, ObjInstr> lastWriter = retPair.getFirst();
                // 将当前指令作为写指令，writerToReader 可以根据当前指令查询最后一次的读指令
                HashMap<ObjInstr, ObjInstr> writerToReader = retPair.getSecond();

                for (MyList.MyNode<ObjInstr> curInstrNode : objBlock.getInstrs())
                {
                    ObjInstr curInstr = curInstrNode.getVal();

                    // 这个是判断当前指令写的寄存器是不是最后一次被写
                    boolean isLastWriter = curInstr.getRegDef().stream().allMatch(def -> lastWriter.get(def).equals(curInstr));
                    // 这个用来指示当前指令写的寄存器是否在 liveOut 中
                    boolean writeRegInLiveOut = curInstr.getRegDef().stream().anyMatch(liveOut::contains);
                    // 当前指令是否有条件
                    boolean hasNoCond = curInstr.hasNoCond();

                    // 这里是数据流窥孔的精髓，我们考虑删除的指令必须被限定在基本块内，然后再被限定在窥孔内
                    // 其他的指令我们并不考虑
                    if (!(writeRegInLiveOut && isLastWriter) && hasNoCond)
                    {
                        // 进行一波对于指令的提前分析
                        lastReader = writerToReader.get(curInstr);
                        notWriteSp = curInstr.getRegDef().stream().noneMatch(def -> def.equals(ObjPhyReg.SP));
                        nextInstrNode = curInstrNode.getNext();

                        // 正式开始分析
                        if (deleteUselessLastWriter(curInstrNode))
                        {
                            finished = false;
                            continue;
                        }
                        if (movDeleteReplace(curInstrNode))
                        {
                            finished = false;
                        }
                    }
                }
            }
        }

        return finished;
    }


    /**
     * 这个用于删除没有用的写指令
     * some instruction contains a
     * live out doesn't contain a
     * 当一个指令写的寄存器没有被读，那么就是没用的
     * 除此之外还要看是否会改变 sp 指针，这种深远影响是不行的
     */
    private boolean deleteUselessLastWriter(MyList.MyNode<ObjInstr> curInstrNode)
    {
        boolean changed = false;
        if (lastReader == null && notWriteSp)
        {
            curInstrNode.removeSelf();
            changed = true;
        }
        return changed;
    }

    /**
     * mov a, b
     * some instruction(use a)
     * =>
     * some instruction(use b)
     */
    private boolean movDeleteReplace(MyList.MyNode<ObjInstr> curInstrNode)
    {
        ObjInstr curInstr = curInstrNode.getVal();
        if (nextInstrNode == null)
        {
            return false;
        }

        if (curInstr instanceof ObjMove)
        {
            ObjMove objMove = (ObjMove) curInstr;
            // 我们不替换立即数和标签，是因为替换的风险很大
            ObjOperand objSrc = objMove.getSrc();
            if (objSrc instanceof ObjImm || objSrc instanceof ObjLabel)
            {
                return false;
            }

            // 检验是否是可以替换的
            ObjInstr nextInstr = nextInstrNode.getVal();
            boolean nextHasSideEffect =
                    nextInstr instanceof ObjCall ||
                    nextInstr instanceof ObjRet ||
                    nextInstr instanceof ObjComment;

            if (!Objects.equals(lastReader, nextInstr) || nextHasSideEffect)
            {
                return false;
            }

            // 替换指令中的读寄存器
            ObjOperand objDst = objMove.getDst();
            nextInstr.replaceUseReg(objDst, objSrc);

            // 删去当前指令
            curInstrNode.removeSelf();
            return true;
        }

        return false;
    }
}