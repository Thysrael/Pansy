package pass.refactor;

import driver.Config;
import ir.values.*;
import ir.values.Module;
import ir.values.instructions.*;
import pass.Pass;
import util.MyList;

import java.util.*;

/**
 * Global Value Numbering 全局值编号
 */
public class GVN implements Pass
{
    /**
     * 这是这个算法的核心，模拟一个 hash 表
     * 之所以不用 HashMap，
     * 是因为为了尽可能的查找（比如说计算指令的相似性），所以我们需要遍历整个表，所有只能用一个 list
     * 之后考虑多做一个 HashMap 来实现 O(1) 查找
     * <key> - <value> 中的 <key> 可以看做是每一条指令或者其他常数之类的东西，
     * <value> 可以看做我们用的那个东西（也就是最先出现的那个表达式）
     */
    private final HashMap<Value, Value> valueNumberTable = new HashMap<>();

    @Override
    public void run()
    {
        if (Config.openGVN)
        {
            for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
            {
                if (!funcNode.getVal().isBuiltin())
                {
                    runGVNOnFunction(funcNode.getVal());
                }
            }
        }
    }

    /**
     * 对于一个函数进行 gvn
     * 首先对函数中的块进行一个前序遍历，至于为啥需要前序遍历，
     * 我们的目的是希望在遍历指令的时候，不会出现未定义就使用的情况，
     * 但是能不能有前序遍历保证，就不一定了
     */
    public void runGVNOnFunction(Function func)
    {
        // 清空做准备
        valueNumberTable.clear();

        // 这里做的是一个压栈，类似与手动 dfs
        Stack<BasicBlock> stack = new Stack<>();
        // pre oder
        ArrayList<BasicBlock> preOder = new ArrayList<>();
        // 后续 visited
        HashSet<BasicBlock> visited = new HashSet<>();
        // 入口块入栈
        stack.push(func.getHeadBlock());
        visited.add(func.getHeadBlock());
        while (!stack.isEmpty())
        {
            BasicBlock curBlock = stack.pop();
            preOder.add(curBlock);
            for (BasicBlock child : curBlock.getSuccessors())
            {
                if (!visited.contains(child))
                {
                    stack.push(child);
                    visited.add(child);
                }
            }
        }

        // 按照这个顺序遍历
        for (BasicBlock curBlock : preOder)
        {
            runGVNOnBlock(curBlock);
        }
    }

    /**
     * 这里面主要完成了对于 phi 函数的化简
     * 和遍历每个指令进行进一步操作
     * @param block 当前块
     */
    private void runGVNOnBlock(BasicBlock block)
    {
        // curInstrNode 是第一个指令
        MyList.MyNode<Instruction> curInstrNode = block.getInstructions().getHead();
        MyList.MyNode<Instruction> endInstNode = block.getInstructions().getTail().getPre();
        // 去掉不必要的 phi
//        block.reducePhi(true);

        // 这个循环是为了处理 phi
        while (curInstrNode != null)
        {
            MyList.MyNode<Instruction> nextInstNode = curInstrNode.getNext();
            Instruction curInst = curInstrNode.getVal();

            // 只有在有 phi 节点继续，消掉一些不必要的 phi
            if (!(curInst instanceof Phi))
            {
                break;
            }
            else
            {
                while (nextInstNode != null)
                {
                    Instruction nxtInst = nextInstNode.getVal();
                    if (!(nxtInst instanceof Phi))
                    {
                        break;
                    }
                    // 是在针对当前 phi 和下一条 phi 进行讨论
                    else if (((Phi) curInst).getPredecessorNum() == ((Phi) nxtInst).getPredecessorNum())
                    {
                        boolean isSame = true;
                        for (int i = ((Phi) curInst).getPredecessorNum();
                             i < ((Phi) curInst).getPredecessorNum() * 2; i++)
                        {
                            BasicBlock bb1 = (BasicBlock) curInst.getUsedValue(i);
                            BasicBlock bb2 = (BasicBlock) nxtInst.getUsedValue(i);
                            if (((Phi) curInst).getInputValForBlock(bb1) !=
                                    ((Phi) nxtInst).getInputValForBlock(bb2))
                            {
                                isSame = false;
                                break;
                            }
                        }
                        // 如果前后两条 phi 相同的话，那么就去掉当前指令
                        if (isSame)
                        {
                            curInst.replaceAllUsesWith(nxtInst);
                            curInst.dropAllOperands();
                            curInst.eraseFromParent();
                            break;
                        }
                    }
                    nextInstNode = nextInstNode.getNext();
                }
            }
            curInstrNode = nextInstNode;
        }

        // 重新遍历，这里才是真正的 runGVNOnInstr，之所以没有用 for，是因为会涉及指令的删除
        curInstrNode = block.getInstructions().getHead();
        while (curInstrNode != null)
        {
            MyList.MyNode<Instruction> nextInstNode = curInstrNode.getNext();
            runGVNOnInstr(curInstrNode.getVal());
            if (curInstrNode.equals(endInstNode))
            {
                break;
            }
            curInstrNode = nextInstNode;
        }
    }

    /**
     * 依然是根据指令类型进行了一个分类讨论
     * @param instruction 遍历的指令
     */
    private void runGVNOnInstr(Instruction instruction)
    {
        // 如果是计算指令
        if (canBeNumbered(instruction))
        {
            // 首先进行常数化简
            Value simplifier = InstructionSimplify.simplify(instruction);
            // 如果还是指令，说明没有被常数化简，但是还可以进行查表化简
            if (simplifier instanceof Instruction)
            {
                simplifier = findValueNumber(simplifier);
            }
            // 替代值
            replaceValue(instruction, simplifier);
        }
    }

    /**
     * 最关键的方法，查找 valueNumber
     * @param lookUp 查询 value
     * @return 一个可以用的 value
     */
    private Value findValueNumber(Value lookUp)
    {
        // 通过遍历查找
        if (valueNumberTable.containsKey(lookUp))
        {
            return valueNumberTable.get(lookUp);
        }

        // Not found, add
        Value allocatedValue = allocateNumber(lookUp);
        valueNumberTable.put(lookUp, allocatedValue);
        return allocatedValue;
    }


    /**
     * 为没有对应值的 lookUp 分配一个值，
     * 实现的是一个分发函数：
     * 对于 BinaryInstr:
     * 对于 gep:
     * 对于 call:
     * 对于其他：直接返回原值(Zext, Call, load, phi, alloca, constInt)
     * @param lookUp 待分配的键值
     * @return 一个值
     */
    private Value allocateNumber(Value lookUp)
    {
        if (lookUp instanceof BinInstruction)
        {
            return allocateNumberForBinInstr((BinInstruction) lookUp);
        }
        if (lookUp instanceof GetElementPtr)
        {
            return allocateNumberForGEP((GetElementPtr) lookUp);
        }
        else
        {
            return lookUp;
        }
    }

    private Value allocateNumberForBinInstr(BinInstruction lookUp)
    {
        // 查找左操作数
        Value lhs = findValueNumber(lookUp.getOp1());
        // 查找右操作数
        Value rhs = findValueNumber(lookUp.getOp2());

        for (Map.Entry<Value, Value> entry : valueNumberTable.entrySet())
        {
            Value key = entry.getKey();
            Value valueNumber = entry.getValue();

            // 如果同样是二进制指令
            if (key instanceof BinInstruction && !key.equals(lookUp))
            {
                BinInstruction keyInst = (BinInstruction) key;
                // 比较两个类是否是相同的类
                Value lhs2 = findValueNumber(keyInst.getOp1());
                Value rhs2 = findValueNumber(keyInst.getOp2());
                boolean sameOp = BinInstruction.hasSameOp(keyInst, lookUp);
                boolean sameOperand =
                        (lhs.equals(lhs2) && rhs.equals(rhs2)) || (lhs.equals(rhs2) && rhs.equals(lhs2)
                                && lookUp.isCommutative());
                if ((sameOp && sameOperand))
                {
                    return valueNumber;
                }
            }
        }

        // 如果查不到，就返回原值
        return lookUp;
    }

    private Value allocateNumberForGEP(GetElementPtr lookUp)
    {
        for (Map.Entry<Value, Value> entry : valueNumberTable.entrySet())
        {
            Value value = entry.getValue();
            if (value instanceof GetElementPtr)
            {
                User inUser = (User) value;
                if (compareOperands(inUser, lookUp))
                {
                    return value;
                }
            }
        }

        return lookUp;
    }

    private boolean compareOperands(User user1, User user2)
    {
        if (user1.getNumOps() != user2.getNumOps())
        {
            return false;
        }
        else
        {
            for (int i = 0; i < user1.getNumOps(); i++)
            {
                Value op1 = user1.getUsedValues().get(i);
                Value op2 = user2.getUsedValues().get(i);
                if (!op1.equals(op2))
                {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 可以被标号的指令，是一个十分宽泛的范围
     * 只要有使用者就可以被标号
     * 排除了 br 和 store
     * @param instruction 待分析指令
     * @return 是则为 true
     */
    private boolean canBeNumbered(Instruction instruction)
    {
        return instruction.getUsers().size() != 0;
    }

    /**
     * 将 instruction 换成 simplifier，
     * 并且将其从 valueNumberMap 中删除
     * @param instruction 指令
     * @param simplifier 替换的值
     */
    private void replaceValue(Instruction instruction, Value simplifier)
    {
        if (!instruction.equals(simplifier))
        {
            valueNumberTable.remove(instruction);
            instruction.replaceAllUsesWith(simplifier);
            instruction.dropAllOperands();
            instruction.eraseFromParent();
        }
    }
}
