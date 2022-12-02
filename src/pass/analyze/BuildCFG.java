package pass.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.instructions.Br;
import ir.values.instructions.Instruction;
import pass.Pass;
import util.MyList;

import java.util.HashSet;

public class BuildCFG implements Pass
{
    @Override
    public void run()
    {
        for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
        {
            Function func = funcNode.getVal();
            if (!func.isBuiltin())
            {
                runBBPredSucc(func);
            }
        }
    }

    private void addEdge(BasicBlock pred, BasicBlock succ)
    {
        pred.addSuccessor(succ);
        succ.addPredecessor(pred);
    }

    private void clear(Function func)
    {
        for (MyList.MyNode<BasicBlock> blockNode : func.getBasicBlocks())
        {
            BasicBlock block = blockNode.getVal();
            block.getSuccessors().clear();
            block.getPredecessors().clear();
        }
    }

    public void runBBPredSucc(Function func)
    {
        clear(func);
        BasicBlock entry = func.getBasicBlocks().getHead().getVal();
        dfsBlock(entry);
        clearUselessBlock(func);
    }

    private final HashSet<BasicBlock> visited = new HashSet<>();

    private void dfsBlock(BasicBlock curBlock)
    {
        visited.add(curBlock);
        Instruction instr = curBlock.getInstructions().getTail().getVal();
        if (instr instanceof Br)
        {
            Br br = (Br) instr;
            if (br.hasCondition())
            {
                BasicBlock trueBlock = (BasicBlock) br.getUsedValue(1);
                addEdge(curBlock, trueBlock);
                if (!visited.contains(trueBlock))
                {
                    dfsBlock(trueBlock);
                }
                BasicBlock falseBlock = (BasicBlock) br.getUsedValue(2);
                addEdge(curBlock, falseBlock);
                if (!visited.contains(falseBlock))
                {
                    dfsBlock(falseBlock);
                }
            }
            else
            {
                BasicBlock nextBlock = (BasicBlock) br.getUsedValue(0);
                addEdge(curBlock, nextBlock);
                if (!visited.contains(nextBlock))
                {
                    dfsBlock(nextBlock);
                }
            }
        }
    }

    /**
     * 注意这里的是并不严谨的，只是删除了前驱为 0 的节点，并且更新了其后继的前驱节点
     * 但是如果更新后的后继也成了前驱为 0 的节点，那么就无能为力了，可以考虑用一个不动点去优化
     * 但是我懒了。
     * @param func 当前函数
     */
    private void clearUselessBlock(Function func)
    {
        BasicBlock entry = func.getBasicBlocks().getHead().getVal();

        for (MyList.MyNode<BasicBlock> blockNode : func.getBasicBlocks())
        {
            BasicBlock block = blockNode.getVal();
            if (block.getPredecessors().isEmpty() && block != entry)
            {
                for (BasicBlock successor : block.getSuccessors())
                {
                    successor.getPredecessors().remove(block);
                }
                for (MyList.MyNode<Instruction> instrNode : block.getInstructions())
                {
                    Instruction instr = instrNode.getVal();
                    instr.dropAllOperands();
                    instr.eraseFromParent();
                }
                blockNode.removeSelf();
            }
        }
    }
}
