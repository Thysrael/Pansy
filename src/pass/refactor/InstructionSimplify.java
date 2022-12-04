package pass.refactor;

import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.*;

public class InstructionSimplify
{
    /**
     * 主函数，用于对于指令进行分类
     * @param instruction 当前指令
     * @return 被化简的指令，不一定还是一个指令
     */
    public static Value simplify(Instruction instruction)
    {
        if (instruction instanceof Add)
        {
            return simplifyAdd(instruction);
        }
        else if (instruction instanceof Sub)
        {
            return simplifySub(instruction);
        }
        else if (instruction instanceof Mul)
        {
            return simplifyMul(instruction);
        }
        else if (instruction instanceof Sdiv)
        {
            return simplifySdiv(instruction);
        }
        else if (instruction instanceof Srem)
        {
            return simplifySrem(instruction);
        }
        else if (instruction instanceof Icmp)
        {
            return simplifyICmp(instruction);
        }
        else if (instruction instanceof Zext)
        {
            return simplifyZext(instruction);
        }
        else
        {
            return instruction;
        }
    }

    /**
     * 根据当前指令和左右操作数来判断是否可以直接运算
     * 可以直接运算则返回一个 ConstInt
     * 否则返回 null
     * 只能用于两操作数的折叠，没有办法处理 Zext
     * @param instruction 当前指令
     * @param lhs 左操作数
     * @param rhs 右操作数
     * @return 返回值
     */
    private static Value foldConstantInt(Instruction instruction, Value lhs, Value rhs)
    {
        // 如果左右操作数都是常数，那么最好折叠了
        if (lhs instanceof ConstInt && rhs instanceof ConstInt)
        {
            ConstInt num0 = (ConstInt) lhs;
            ConstInt num1 = (ConstInt) rhs;
            int ansVal, ansBit;
            // 只有 Icmp 的运算结果是 i1
            if (instruction instanceof Icmp)
            {
                ansBit = 1;
                Icmp.Condition cond = ((Icmp) instruction).getCondition();
                if (cond == Icmp.Condition.EQ)
                {
                    ansVal = (num0.getValue() == num1.getValue()) ? 1 : 0;
                }
                else if (cond == Icmp.Condition.NE)
                {
                    ansVal = (num0.getValue() != num1.getValue()) ? 1 : 0;
                }
                else if (cond == Icmp.Condition.LT)
                {
                    ansVal = (num0.getValue() < num1.getValue()) ? 1 : 0;
                }
                else if (cond == Icmp.Condition.LE)
                {
                    ansVal = (num0.getValue() <= num1.getValue()) ? 1 : 0;
                }
                else if (cond == Icmp.Condition.GT)
                {
                    ansVal = (num0.getValue() > num1.getValue()) ? 1 : 0;
                }
                else if (cond == Icmp.Condition.GE)
                {
                    ansVal = (num0.getValue() >= num1.getValue()) ? 1 : 0;
                }
                else
                {
                    ansVal = 0;
                    assert false : "Can't fold this instruction";
                }
            }
            else
            {
                ansBit = 32;
                if (instruction instanceof Add)
                {
                    ansVal = num0.getValue() + num1.getValue();
                }
                else if (instruction instanceof Sub)
                {
                    ansVal = num0.getValue() - num1.getValue();
                }
                else if (instruction instanceof Mul)
                {
                    ansVal = num0.getValue() * num1.getValue();
                }
                else if (instruction instanceof Sdiv)
                {
                    // 按照常理推断，这里必然是不可能的，但是因为在函数内联的时候，直接用实参替换了形参，导致出现了这种情况
                    if (num1.getValue() != 0)
                    {
                        ansVal = num0.getValue() / num1.getValue();
                    }
                    else
                    {
                        return instruction;
                    }
                }
                else if (instruction instanceof Srem)
                {
                    if (num1.getValue() != 0)
                    {
                        ansVal = num0.getValue() % num1.getValue();
                    }
                    else
                    {
                        return instruction;
                    }
                }
                else
                {
                    ansVal = 0;
                    assert false : "Can't fold this instruction";
                }
            }
            return new ConstInt(ansBit, ansVal);
        }
        return null;
    }

    /**
     * 化简加法指令
     * 可以返回常数，或者源指令
     * @param instruction 加法指令
     * @return 一个值
     */
    public static Value simplifyAdd(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);

        // 看看是不是可以常数折叠，如果可以
        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }
        // Swap，确实，保证让右操作数尽量为常数，为了后面的讨论
        if (lhs instanceof ConstInt)
        {
            // instruction.dropAllReferences();
            instruction.setUsedValue(0, rhs);
            instruction.setUsedValue(1, lhs);
            lhs = instruction.getUsedValue(0);
            rhs = instruction.getUsedValue(1);
        }

        // x + 0 = x
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 0)
        {
            return lhs;
        }

        // If two operands are negative, return 0
        // x + (0 - x) = 0, x is a instruction (Or it has been folded)
        if (checkNegativeAdd(rhs, lhs))
        {
            return ConstInt.ZERO;
        }
        // (0 - x) + x = 0, x is a instruction (Or it has been folded)
        if (checkNegativeAdd(lhs, rhs))
        {
            return ConstInt.ZERO;
        }
        // (a - b) + (b - a) = 0
        if (lhs instanceof Sub && rhs instanceof Sub)
        {
            Value lhsOp0 = ((Sub) lhs).getUsedValue(0);
            Value lhsOp1 = ((Sub) lhs).getUsedValue(1);
            Value rhsOp0 = ((Sub) rhs).getUsedValue(0);
            Value rhsOp1 = ((Sub) rhs).getUsedValue(1);
            if (lhsOp0 == rhsOp1 && lhsOp1 == rhsOp0)
            {
                return ConstInt.ZERO;
            }
        }

        return instruction;
    }

    /**
     * 乐这个优化好无聊
     * 看的是这里是不是相反数
     * @param lhs 左操作数
     * @param rhs 右操作数
     * @return 是则为 true
     */
    private static boolean checkNegativeAdd(Value lhs, Value rhs)
    {
        if (rhs instanceof Instruction && lhs instanceof Sub)
        {
            Value op0 = ((Sub) lhs).getUsedValue(0);
            Value op1 = ((Sub) lhs).getUsedValue(1);
            if (op0 instanceof ConstInt && ((ConstInt) op0).getValue() == 0)
            {
                return op1 == rhs;
            }
        }
        return false;
    }

    public static Value simplifySub(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);

        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }

        // x - 0 = x
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 0)
        {
            return lhs;
        }

        // x - x = 0
        if (lhs.equals(rhs))
        {
            return ConstInt.ZERO;
        }

        return instruction;
    }

    public static Value simplifyMul(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);

        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }

        if (lhs instanceof ConstInt)
        {
            // instruction.dropAllReferences();
            instruction.setUsedValue(0, rhs);
            instruction.setUsedValue(1, lhs);
            lhs = instruction.getUsedValue(0);
            rhs = instruction.getUsedValue(1);
        }

        // x * 0 = 0
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 0)
        {
            return ConstInt.ZERO;
        }

        // x * 1 = x
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 1)
        {
            return lhs;
        }

        return instruction;
    }

    public static Value simplifySdiv(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);

        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }

        // 0 / x = 0
        if (lhs instanceof ConstInt && ((ConstInt) lhs).getValue() == 0)
        {
            return ConstInt.ZERO;
        }

        // x / x = 1
        if (lhs.equals(rhs))
        {
            return new ConstInt(1);
        }

        // x / 1 = x
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 1)
        {
            return lhs;
        }

        return instruction;
    }

    public static Value simplifySrem(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);

        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }

        // 0 % x = 0
        if (lhs instanceof ConstInt && ((ConstInt) lhs).getValue() == 0)
        {
            return ConstInt.ZERO;
        }

        // x % x = 0
        if (lhs.equals(rhs))
        {
            return ConstInt.ZERO;
        }

        // x % 1 = 0
        if (rhs instanceof ConstInt && ((ConstInt) rhs).getValue() == 1)
        {
            return ConstInt.ZERO;
        }

        return instruction;
    }

    public static Value simplifyICmp(Instruction instruction)
    {
        Value lhs = instruction.getUsedValue(0);
        Value rhs = instruction.getUsedValue(1);


        Value constantVal = foldConstantInt(instruction, lhs, rhs);
        if (constantVal != null)
        {
            return constantVal;
        }

        Icmp.Condition condition = ((Icmp) instruction).getCondition();
        // 只有 x == x 可以特判
        if (condition == Icmp.Condition.EQ && lhs.equals(rhs))
        {
            return new ConstInt(1, 1);
        }

        return instruction;
    }

    public static Value simplifyZext(Instruction instruction)
    {
        Value src = instruction.getUsedValue(0);
        // 源是常量
        if (src instanceof ConstInt)
        {
            int value = ((ConstInt) src).getValue();
            return new ConstInt(value);
        }
        return instruction;
    }
}
