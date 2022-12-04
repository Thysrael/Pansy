package pass.analyze;

import ir.values.BasicBlock;
import ir.values.Function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * 每个函数拥有一个 LoopInfo
 * 记录着这个循环的基本信息
 * 这么看，无论是 LoopInfo 还是 DomInfo 都应该是全局的东西更加合理
 */
public class LoopInfo
{
    /**
     * 函数的所有循环
     */
    private final ArrayList<Loop> allLoops = new ArrayList<>();
    /**
     * 函数的顶层循环，loopDepth 均为 1
     * 可以有很多个
     */
    private final ArrayList<Loop> topLevelLoops = new ArrayList<>();

    /**
     * 在调用构造器的时候就完成了函数 LoopInfo 的构造
     * @param function 待分析函数
     */
    public LoopInfo(Function function)
    {
        ArrayList<BasicBlock> latchBlocks = new ArrayList<>();
        // 按照后续遍历，是为了先提内循环，后提外循环，这是因为内循环是受到外循环的头块的支配（循环头支配循环体）
        // 这种顺序可以保证循环的树关系不会出错
        ArrayList<BasicBlock> postOder = DomInfo.computeDominanceTreePostOder(function);
        // 本质是在遍历头块（可能的头块）
        for (BasicBlock block : postOder)
        {
            // 遍历前驱节点
            for (BasicBlock predecessor : block.getPredecessors())
            {
                // 如果当前块支配前驱节点
                if (block.isDominate(predecessor))
                {
                    // 前驱就是闩，也就是说，无论怎么样，都会有一个循环的流稳定存在
                    latchBlocks.add(predecessor);
                }
            }
            // 如果 latchBlock 不为空，那么就是有循环了
            if (!latchBlocks.isEmpty())
            {
                // 制作出一个新的 loop
                // 从这里可以看出，此时的 block 就是入口块的意思
                Loop loop = new Loop(block, latchBlocks);
                completeLoop(latchBlocks, loop);
                // 为下一次计算做准备
                latchBlocks.clear();
            }
        }
        // 建立循环与子循环的关系
        addLoopSons(function.getHeadBlock());
    }

    /**
     * 将循环体的块加入循环中
     * 采用的是反转 CFG 图的方式
     * @param latchBlocks 栓块集合
     * @param loop 当前循环
     */
    private void completeLoop(ArrayList<BasicBlock> latchBlocks, Loop loop)
    {
        // 看上去好像是一个 bfs，将所有的闩块加入队列
        ArrayList<BasicBlock> queue = new ArrayList<>(latchBlocks);

        while (!queue.isEmpty())
        {
            // 出队
            BasicBlock block = queue.remove(0);

            // subloop 是当前块所在的循环，最终是目的是 subloop 是最外层循环
            Loop subloop = block.getParentLoop();
            // 当前没有子循环
            if (subloop == null)
            {
                // 设置为传入循环
                block.setParentLoop(loop);
                if (block == loop.getEntryBlock())
                {
                    continue;
                }
                // 这里加入了所有前驱，应该是循环体，除了头块以外，其他的循环体的前驱也是循环体
                queue.addAll(block.getPredecessors());
            }
            // 当前有子循环
            else
            {
                // parent 是 subloop 的外循环
                Loop parent = subloop.getParentLoop();
                // 一直让 subloop 为最外层循环
                while (parent != null)
                {
                    subloop = parent;
                    parent = parent.getParentLoop();
                }
                // loop 是最外层
                if (subloop == loop)
                {
                    continue;
                }
                subloop.setParentLoop(loop);
                // 遍历内循环的头块的前驱，有一部分是在子循环的循环体中的（闩），其他的在外层循环体中
                for (BasicBlock predecessor : subloop.getEntryBlock().getPredecessors())
                {
                    // 不是闩
                    if (predecessor.getParentLoop() != subloop)
                    {
                        queue.add(predecessor);
                    }
                }
            }
        }
    }

    /**
     * 建立外循环对内循环的关系
     * 登记所有的循环
     * 登记循环深度
     * @param root 入口块
     */
    private void addLoopSons(BasicBlock root)
    {
        Stack<BasicBlock> stack = new Stack<>();
        HashSet<BasicBlock> visited = new HashSet<>();
        stack.push(root);

        while (!stack.isEmpty())
        {
            BasicBlock currentBlock = stack.pop();
            visited.add(currentBlock);
            // 是 Header
            Loop subloop = currentBlock.getParentLoop();

            // currentBlock 是循环头块
            if (subloop != null && currentBlock == subloop.getEntryBlock())
            {
                Loop parentLoop = subloop.getParentLoop();
                // subloop 是内层的
                if (parentLoop != null)
                {
                    parentLoop.addSubLoop(subloop);
                    allLoops.add(subloop);
                }
                // 如果没有父循环，说明是顶端循环
                else
                {
                    topLevelLoops.add(subloop);
                    allLoops.add(subloop);
                }

                // 登记循环深度
                // TODO 这里改成 2 会更好，因为没有循环的时候深度都是 1
                int depth = 1;
                Loop tmp = subloop.getParentLoop();
                while (tmp != null)
                {
                    tmp = tmp.getParentLoop();
                    depth++;
                }
                subloop.setLoopDepth(depth);
            }

            while (subloop != null)
            {
                subloop.addBlock(currentBlock);
                subloop = subloop.getParentLoop();
            }

            for (BasicBlock successor : currentBlock.getSuccessors())
            {
                if (!visited.contains(successor))
                {
                    stack.push(successor);
                }
            }
        }
    }

    public ArrayList<Loop> getAllLoops()
    {
        return allLoops;
    }
}
