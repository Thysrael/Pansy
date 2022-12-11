package pass.refactor;

import driver.Config;
import ir.IrBuilder;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.*;

public class InstructionSimplify
{
    private final static IrBuilder irBuilder = IrBuilder.getInstance();
    /**
     * 主函数，用于对于指令进行分类
     * @param instruction 当前指令
     * @return 被化简的指令，不一定还是一个指令
     */
    public static Value simplify(Instruction instruction)
    {
        if (instruction instanceof Add)
        {
            return simplifyAdd(instruction, !Config.openProduceDeadCode);
        }
        else if (instruction instanceof Sub)
        {
            return simplifySub(instruction, !Config.openProduceDeadCode);
        }
        else if (instruction instanceof Mul)
        {
            return simplifyMul(instruction, !Config.openProduceDeadCode);
        }
        else if (instruction instanceof Sdiv)
        {
            return simplifySdiv(instruction, !Config.openProduceDeadCode);
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
    public static Value simplifyAdd(Instruction instruction, boolean notProduce)
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

        if (notProduce)
        {
            return instruction;
        }

        if (lhs instanceof Add)
        {
            // (x + y) + z = x + (y + z) or (x + z) + y
            Add addInst = (Add) lhs;
            // x
            Value addLhs = addInst.getUsedValue(0);
            // y
            Value addRhs = addInst.getUsedValue(1);
            // tmp = y + z; (x + y) + z
            Add tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    addRhs, rhs, instruction);
            // simplify tmp
            Value simplifyAdd = simplifyAdd(tmp, true);
            if (simplifyAdd != tmp)
            {
                // x + (y + z)
                return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                        addLhs, simplifyAdd, instruction), true);
            }

            tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    addLhs, rhs, instruction);
            simplifyAdd = simplifyAdd(tmp, true);
            if (simplifyAdd != tmp)
            {
                return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                        simplifyAdd, addRhs, instruction), true);
            }
        }

        if (lhs instanceof Sub)
        {
            Sub subInst = (Sub) lhs;
            Value subLhs = subInst.getUsedValue(0);
            Value subRhs = subInst.getUsedValue(1);
            // (y - x) + x = y
            if (subRhs == rhs)
            {
                return subLhs;
            }
            else
            {
                // (x - y) + z = x - (y - z) or (x + z) - y
                // Deal with add first
                // (x + z) - y
                // x + z
                Value tmp = irBuilder.buildAddBefore(instruction.getParent(),
                        subLhs, rhs, instruction);
                Value simplifyAdd = simplifyAdd((Add) tmp, true);
                if (simplifyAdd != tmp)
                {
                    // (x + z) - y
                    return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                            simplifyAdd, subRhs, instruction), true);
                }
                // Then deal with sub
                // x - (y - z)
                // y - z
                tmp = irBuilder.buildSubBefore(instruction.getParent(),
                        subRhs, rhs, instruction);
                Value simplifySub = simplifySub((Sub) tmp, true);
                if (simplifySub != tmp)
                {
                    // x - (y - z)
                    return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                            subLhs, simplifySub, instruction), true);
                }
            }
        }

        if (rhs instanceof Add)
        {
            // x + (y + z) = (x + y) + z or (x + z) + y
            Add addInst = (Add) rhs;
            Value addLhs = addInst.getUsedValue(0);
            Value addRhs = addInst.getUsedValue(1);
            Add tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    lhs, addLhs, instruction);
            Value simplifyAdd = simplifyAdd(tmp, true);
            if (simplifyAdd != tmp)
            {
                return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                        simplifyAdd, addRhs, instruction), true);
            }

            tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    lhs, addRhs, instruction);
            simplifyAdd = simplifyAdd(tmp, true);
            if (simplifyAdd != tmp)
            {
                return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                        simplifyAdd, addLhs, instruction), true);
            }
        }

        if (rhs instanceof Sub)
        {
            Sub subInst = (Sub) rhs;
            Value subLhs = subInst.getUsedValue(0);
            Value subRhs = subInst.getUsedValue(1);
            if (lhs == subRhs)
            {
                // x + (y - x) = y
                return subLhs;
            }
            else
            {
                // x + (y - z) = (x + y) - z or (x - z) + y
                // Deal with add first
                Value tmp = irBuilder.buildAddBefore(instruction.getParent(),
                        lhs, subLhs, instruction);
                Value simplifyAdd = simplifyAdd((Add) tmp, true);
                if (simplifyAdd != tmp)
                {
                    return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                            simplifyAdd, subRhs, instruction), true);
                }
                // Then deal with sub
                tmp = irBuilder.buildSubBefore(instruction.getParent(),
                        lhs, subRhs, instruction);
                Value simplifySub = simplifySub((Sub) tmp, true);
                if (simplifySub != tmp)
                {
                    return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                            simplifySub, subLhs, instruction), true);
                }
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

    public static Value simplifySub(Instruction instruction, boolean notProduce)
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

        if (notProduce)
        {
            return instruction;
        }

        // (x + y) - z = x + (y - z) or y + (x - z) if everything simplifies
        if (lhs instanceof Add)
        {
            Add addInst = (Add) lhs;
            for (int i = 0; i < 2; i++)
            {
                Value curOperand = addInst.getUsedValue(i);
                Sub tmp = irBuilder.buildSubBefore(instruction.getParent(),
                        curOperand, rhs, instruction);
                Value simplifiedSub = simplifySub(tmp, true);
                if (simplifiedSub != tmp)
                {
                    return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                                    addInst.getUsedValue(1 - i), simplifiedSub, instruction),
                            true);
                }
            }
        }

        // (x - y) - z = x - (y + z) or (x - z) - y
        if (lhs instanceof Sub)
        {
            Sub subInst = (Sub) lhs;
            Value subLhs = subInst.getUsedValue(0);
            Value subRhs = subInst.getUsedValue(1);
            // Deal with addInst first
            // x - (y + z)
            Value tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    subRhs, rhs, instruction);
            Value simplifyAdd = simplifyAdd((Add) tmp, true);
            if (simplifyAdd != tmp)
            {
                // x - (y + z)
                return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                        subLhs, simplifyAdd, instruction), true);
            }
            // Then deal with sub
            tmp = irBuilder.buildSubBefore(instruction.getParent(),
                    subLhs, rhs, instruction);
            Value simplifySub = simplifySub((Sub) tmp, true);
            if (simplifySub != tmp)
            {
                return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                        simplifySub, subRhs, instruction), true);
            }
        }

        // x - (y + z) = (x - y) - z or (x - z) - y if everything simplifies
        if (rhs instanceof Add)
        {
            Add addInst = (Add) rhs;
            for (int i = 0; i < 2; i++)
            {
                Value curOperand = addInst.getUsedValue(i);
                Sub tmp = irBuilder.buildSubBefore(instruction.getParent(),
                        lhs, curOperand, instruction);
                Value simplifiedSub = simplifySub(tmp, true);
                if (simplifiedSub != tmp)
                {
                    return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                                    simplifiedSub, addInst.getUsedValue(1 - i), instruction),
                            true);
                }
            }
        }

        // z - (x - y) = (z - x) + y or (z + y) - x if everything simplifies
        if (rhs instanceof Sub)
        {
            Sub subInst = (Sub) rhs;
            Value subLhs = subInst.getUsedValue(0);
            Value subRhs = subInst.getUsedValue(1);
            // Deal with addInst first
            Value tmp = irBuilder.buildAddBefore(instruction.getParent(),
                    lhs, subRhs, instruction);
            Value simplifyAdd = simplifyAdd((Add) tmp, true);
            if (simplifyAdd != tmp)
            {
                return simplifySub(irBuilder.buildSubBefore(instruction.getParent(),
                        simplifyAdd, subLhs, instruction), true);
            }
            // Then deal with sub
            tmp = irBuilder.buildSubBefore(instruction.getParent(),
                    lhs, subLhs, instruction);
            Value simplifySub = simplifySub((Sub) tmp, true);
            // 说明优化有效果
            if (simplifySub != tmp)
            {
                return simplifyAdd(irBuilder.buildAddBefore(instruction.getParent(),
                        simplifySub, subRhs, instruction), true);
            }
        }

        return instruction;
    }

    public static Value simplifyMul(Instruction instruction, boolean notProduce)
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

        if (notProduce)
        {
            return instruction;
        }

        // (x * y) * z = x * (y * z)
        if (lhs instanceof Mul)
        {
            Mul mulInst = (Mul) lhs;
            Value mulLhs = mulInst.getUsedValue(0);
            Value mulRhs = mulInst.getUsedValue(1);
            Mul tmp = irBuilder.buildMulBefore(instruction.getParent(),
                    mulRhs, rhs, instruction);
            Value simplifyMul = simplifyMul(tmp, true);
            if (simplifyMul != tmp)
            {
                return simplifyMul(irBuilder.buildMulBefore(instruction.getParent(),
                        mulLhs, simplifyMul, instruction), true);
            }
        }

        // x * (y * z) = (x * y) * z
        if (rhs instanceof Mul)
        {
            Mul mulInst = (Mul) rhs;
            Value mulLhs = mulInst.getUsedValue(0);
            Value mulRhs = mulInst.getUsedValue(1);
            Mul tmp = irBuilder.buildMulBefore(instruction.getParent(),
                    lhs, mulLhs, instruction);
            Value simplifyMul = simplifyMul(tmp, true);
            if (simplifyMul != tmp)
            {
                return simplifyMul(irBuilder.buildMulBefore(instruction.getParent(),
                        simplifyMul, mulRhs, instruction), true);
            }
        }


        return instruction;
    }

    public static Value simplifySdiv(Instruction instruction, boolean notProduce)
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

        if (notProduce)
        {
            return instruction;
        }

        // if x * y does not overflow, then:
        // (x * y) / y = x
        if (lhs instanceof Mul)
        {
            Mul mulInst = (Mul) lhs;
            Value mulLhs = mulInst.getUsedValue(0);
            Value mulRhs = mulInst.getUsedValue(1);
            if (mulRhs instanceof ConstInt && rhs instanceof ConstInt &&
                    ((ConstInt) mulRhs).getValue() == ((ConstInt) rhs).getValue())
            {
                return mulLhs;
            }
            else if (mulRhs.equals(rhs))
            {
                return mulLhs;
            }
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
