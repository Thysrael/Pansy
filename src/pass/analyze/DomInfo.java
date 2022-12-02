package pass.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import pass.Pass;
import util.MyList;

import java.util.ArrayList;
import java.util.BitSet;

public class DomInfo implements Pass
{
    @Override
    public void run()
    {
        Module module = Module.getInstance();
        for (MyList.MyNode<Function> funcNode : module.getFunctions())
        {
            Function func = funcNode.getVal();
            if (!func.isBuiltin())
            {
                computeDominanceInfo(func);
                computeDominanceFrontier(func);
            }
        }
    }

    /**
     * 计算支配信息
     * @param function 待分析的函数
     */
    private void computeDominanceInfo(Function function)
    {
        // entry 入口块
        BasicBlock entry = function.getBasicBlocks().getHead().getVal();
        // blockNum 是基本块的数目
        int blockNum = function.getBasicBlocks().size();
        // domers 是一个 bitSet 的数组，也就是说，每个基本块都有一个 bitSet，用于表示这个块的 domer（支配者）
        // domer 是支配者之意
        ArrayList<BitSet> domers = new ArrayList<>(blockNum);
        // 获得一个块列表，在初始化的时候，会被变成一个基本块列表，我们之后操作这个，因为链表操作起来不太方便
        ArrayList<BasicBlock> blockArray = new ArrayList<>();

        // 作为 block 的索引
        int index = 0;
        // clear existing dominance information and initialize
        for (MyList.MyNode<BasicBlock> basicBlockNode : function.getBasicBlocks())
        {
            // 当前块
            BasicBlock curBlock = basicBlockNode.getVal();
            // 清除原有信息
            curBlock.getDomers().clear();
            curBlock.getIdomees().clear();
            // 登记数组，登记支配者
            blockArray.add(curBlock);
            domers.add(new BitSet());
            // 如果是入口块
            if (curBlock == entry)
            {
                // 说的就是入口块自己被自己支配
                domers.get(index).set(index);
            }
            else
            {
                // 从 0 ~ numNode - 1 全部置 1
                domers.get(index).set(0, blockNum);
            }
            index++;
        }

        // calculate domer
        // 不动点算法
        boolean changed = true;
        while (changed)
        {
            changed = false;

            index = 0;
            // 遍历基本块
            for (MyList.MyNode<BasicBlock> basicBlockNode : function.getBasicBlocks())
            {
                BasicBlock curBlock = basicBlockNode.getVal();
                // 入口块
                if (curBlock != entry)
                {
                    BitSet temp = new BitSet();
                    // 先全部置 1
                    temp.set(0, blockNum);
                    // 就是下面的公式
                    // temp <- {index} \cup (\BigCap_{j \in preds(index)} domer(j) )
                    for (BasicBlock preBlock : curBlock.getPredecessors())
                    {
                        int preIndex = blockArray.indexOf(preBlock);
                        temp.and(domers.get(preIndex));
                    }
                    // 自己也是自己的 domer
                    temp.set(index);

                    // 将 temp 赋给 domer
                    if (!temp.equals(domers.get(index)))
                    {
                        // replace domers[index] with temp
                        domers.get(index).clear();
                        domers.get(index).or(temp);
                        changed = true;
                    }
                }
                index++;
            }
        }
        // 在这个循环里，将 domer 信息存入基本块中
        for (int i = 0; i < blockNum; i++)
        {
            BasicBlock curBlock = blockArray.get(i);
            BitSet domerInfo = domers.get(i);
            // 这个叫做遍历每一个支配者
            for (int domerIndex = domerInfo.nextSetBit(0); domerIndex >= 0;
                 domerIndex = domerInfo.nextSetBit(domerIndex + 1))
            {
                BasicBlock domerBlock = blockArray.get(domerIndex);
                // 添加支配者
                curBlock.getDomers().add(domerBlock);
            }
        }

        // calculate doms and idom
        for (int i = 0; i < blockNum; i++)
        {
            BasicBlock curBlock = blockArray.get(i);
            // 遍历所有的支配者
            for (BasicBlock maybeIdomerbb : curBlock.getDomers())
            {
                // 排除自身
                if (maybeIdomerbb != curBlock)
                {
                    boolean isIdom = true;
                    for (BasicBlock domerbb : curBlock.getDomers())
                    {
                        // 最后一个条件说明并不直接
                        if (domerbb != curBlock && domerbb != maybeIdomerbb && domerbb.getDomers()
                                .contains(maybeIdomerbb))
                        {
                            isIdom = false;
                            break;
                        }
                    }
                    // 说明是直接支配点
                    if (isIdom)
                    {
                        // 双方都需要登记
                        curBlock.setIdomer(maybeIdomerbb);
                        maybeIdomerbb.getIdomees().add(curBlock);
                        break;
                    }
                }
            }
        }

        // calculate dom level
        computeDominanceLevel(entry, 0);
    }

    /**
     * Compute the dominance frontier of all the basic blocks of a function.
     *
     * @param function 当前函数
     */
    private void computeDominanceFrontier(Function function)
    {
        // 清空原来的支配边界
        for (MyList.MyNode<BasicBlock> blockNode : function.getBasicBlocks())
        {
            blockNode.getVal().getDominanceFrontier().clear();
        }

        for (MyList.MyNode<BasicBlock> blockNode : function.getBasicBlocks())
        {
            BasicBlock curBlock = blockNode.getVal();
            for (BasicBlock succBlock : curBlock.getSuccessors())
            {
                // cur 是一个游标，会顺着直接支配者链（也就是支配者树）滑动
                BasicBlock cur = curBlock;
                // 后继块就是 cur 或者是 succBlock 的支配者不包括 cur
                while (cur == succBlock || !succBlock.getDomers().contains(cur))
                {
                    cur.getDominanceFrontier().add(succBlock);
                    // 获得直接支配者，这里说的是，如果 curBlock 的后继不受到 curBlock 的支配，那么 curBlock 的直接支配者的边界也是它
                    cur = cur.getIdomer();
                }
            }
        }

        for (MyList.MyNode<BasicBlock> node : function.getBasicBlocks())
        {
            BasicBlock block = node.getVal();
        }
    }

    /**
     * 通过一个 DFS，获得支配树深度
     * 支配树由直接支配关系组成
     * @param bb 基本块
     * @param domLevel 当前深度
     */
    public static void computeDominanceLevel(BasicBlock bb, Integer domLevel)
    {
        bb.setDomLevel(domLevel);
        for (BasicBlock succ : bb.getIdomees())
        {
            computeDominanceLevel(succ, domLevel + 1);
        }
    }
}
