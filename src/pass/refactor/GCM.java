package pass.refactor;

import driver.Config;
import ir.types.PointerType;
import ir.values.*;
import ir.values.Module;
import ir.values.instructions.*;
import pass.Pass;
import pass.analyze.DomInfo;
import util.MyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * 一个指令的位置是由他使用的指令和使用他的指令决定的
 * 我们找到的是一个区间，这个区间上指令可以自由的移动
 * 我们要挑选尽可能靠近支配树根节点和尽可能循环深度比较深的点
 */
public class GCM implements Pass
{
    private final HashSet<Instruction> visited = new HashSet<>();

    /**
     * 似乎是不能提升的意思？又叫做有控制依赖的指令
     * 可以提升的是计算指令，GEP，某些 call 和 zext
     * @param instruction 当前指令
     * @return 是否可以提升
     */
    private boolean isPinned(Instruction instruction)
    {
        return !(instruction instanceof BinInstruction) &&
                !(instruction instanceof Zext) &&
                !(instruction instanceof GetElementPtr) &&
                !(instruction instanceof Call && isPure((Call) instruction));
    }

    @Override
    public void run()
    {
        if (Config.openGCM)
        {
            // 遍历每一个函数
            for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
            {
                Function func = funcNode.getVal();
                if (!func.isBuiltin())
                {
                    runGCM(func);
                }
            }
        }
    }

    public void runGCM(Function func)
    {
        visited.clear();
        // 后序遍历
        ArrayList<BasicBlock> postOder = DomInfo.computeDominanceTreePostOder(func);
        // 逆后续遍历
        Collections.reverse(postOder);
        ArrayList<Instruction> instructions = new ArrayList<>();

        for (BasicBlock bb : postOder)
        {
            instructions.addAll(bb.getInstructionsArray());
        }

        for (Instruction instruction : instructions)
        {
            scheduleEarly(instruction, func);
        }
        // 清空遍历集
        visited.clear();
        // 后序遍历
        Collections.reverse(instructions);
        // 遍历每一个块，进行向后提升
        for (Instruction instruction : instructions)
        {
            scheduleLate(instruction);
        }
    }

    public void scheduleEarly(Instruction instruction, Function curFunction)
    {
        // 如果已经处理过了，或者是无法移动，那么就结束处理
        if (visited.contains(instruction) || isPinned(instruction))
        {
            return;
        }
        // 标记已经处理了
        visited.add(instruction);

        // 将这条指令从当前块移除，然后插入到入口块的最后一条指令之前
        BasicBlock root = curFunction.getHeadBlock();
        instruction.eraseFromParent();
        root.insertBefore(instruction, root.getTailInstr());
        instruction.setParent(root);

        // 遍历该指令用到的操作数
        for (Value input : instruction.getUsedValues())
        {
            // 如果是操作数是个指令，那么应该是一并提升
            if (input instanceof Instruction)
            {
                Instruction inputInst = (Instruction) input;

                scheduleEarly(inputInst, curFunction);
                // 比较两个指令和操作数的支配树深度，如果指令的深度要小于操作数的深度
                if (instruction.getParent().getDomLevel()
                        < inputInst.getParent().getDomLevel())
                {
                    // 将指令插在输入指令的基本块的最后一条指令的前面
                    instruction.eraseFromParent();
                    inputInst.getParent().insertBefore(instruction, inputInst.getParent().getTailInstr());
                    instruction.setParent(inputInst.getParent());
                }
            }
        }
    }

    public void scheduleLate(Instruction instruction)
    {
        // 依然是筛掉部分指令
        if (visited.contains(instruction) || isPinned(instruction))
        {
            return;
        }
        visited.add(instruction);
        // lca 是所有的 user 的共同祖先
        BasicBlock lca = null;

        // Schedule all uses
        // Pay attention that USE is an edge that describe the relationship
        // between user and usedValue, so we need to discuss
        // both instruction's users and its usedValue
        // 遍历所有使用这个指令的指令
        for (User user : instruction.getUsers())
        {
            if (user instanceof Instruction)
            {
                Instruction userInst = (Instruction) user;
                scheduleLate(userInst);
                BasicBlock useBB;
                // 如果使用指令是 phi
                if (userInst instanceof Phi)
                {
                    // 遍历 phi 指令的所有输入
                    for (int j = 0; j < ((Phi) userInst).getPredecessorNum(); j++)
                    {
                        Value value = userInst.getUsedValue(j);
                        // 刚好是当前指令
                        if (value instanceof Instruction)
                        {
                            // Reverse dependence edge from i to y
                            // Use matching block from CFG
                            if (value.equals(instruction))
                            {
                                // 做得很复杂，找到的是 instruction 作为 phi 的 input 对应的基本块
                                useBB = (BasicBlock) userInst.getUsedValue(j + ((Phi) userInst).getPredecessorNum());
                                // useBB = userInst.getParent().getPredecessors().get(j);
                                lca = findLCA(lca, useBB);
                            }
                        }
                    }
                }
                // 对于非 phi 指令
                else
                {
                    useBB = userInst.getParent();
                    lca = findLCA(lca, useBB);
                }
            }
        }

        // Pick a final position
        if (instruction.getUsers().size() > 0)
        {
            // 他们是有共同祖先的
            assert lca != null;
            // 似乎此时的最佳人选就是共同祖先
            BasicBlock best = lca;
            // 如果共同祖先不是该指令的所在块
            while (!lca.equals(instruction.getParent()))
            {
                lca = lca.getIdomer();
                // 尽量让循环深度变小
                if (lca.getLoopDepth() < best.getLoopDepth())
                {
                    best = lca;
                }
            }

            // best 是最后插入的块
            instruction.eraseFromParent();
            best.insertBefore(instruction, best.getTailInstr());
            instruction.setParent(best);
        }

        BasicBlock best = instruction.getParent();
        // 遍历最终插入的块的指令
        for (Instruction inst : best.getInstructionsArray())
        {
            // 如果不是刚才插入的指令
            if (!inst.equals(instruction))
            {
                // 当前指令用到了这条指令，那么就将这条指令插入到插到当前指令之前
                if (!(inst instanceof Phi) && inst.getUsedValues().contains(instruction))
                {
                    instruction.eraseFromParent();
                    best.insertBefore(instruction, inst);
                    instruction.setParent(best);
                    break;
                }
            }
        }
    }

    /**
     * 这个方法会找到 bb1 和 bb2 的共同祖先
     * LCA Least Common Ancestor
     * @param bb1 基本块 1
     * @param bb2 基本块 2
     * @return 共同祖先
     */
    private BasicBlock findLCA(BasicBlock bb1, BasicBlock bb2)
    {
        if (bb1 == null)
        {
            return bb2;
        }
        while (bb1.getDomLevel() < bb2.getDomLevel())
        {
            bb2 = bb2.getIdomer();
        }
        while (bb2.getDomLevel() < bb1.getDomLevel())
        {
            bb1 = bb1.getIdomer();
        }
        while (!(bb1.equals(bb2)))
        {
            bb1 = bb1.getIdomer();
            bb2 = bb2.getIdomer();
        }
        return bb1;
    }

    /**
     * 是一个像运算符一样简单的 call 指令才可以提升
     */
    private boolean isPure(Call call)
    {
        Function target = call.getFunction();
        // 有副作用的不行
        if (target.hasSideEffect())
        {
            return false;
        }
        if (call.getUsers().isEmpty())
        {
            return false;
        }
        // 遍历实参
        for (Value input : call.getUsedValues())
        {
            // 参数是一个简单的单变量才行
            if (input instanceof GetElementPtr || input instanceof Load || input.getValueType() instanceof PointerType)
            {
                return false;
            }
        }
        return true;
    }
}
