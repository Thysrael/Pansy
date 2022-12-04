package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * 有左右2个操作数，分别放在 values 列表的 0 号与 1 号位，
 * 操作数可以为常数，也可以为指令
 */
public abstract class BinInstruction extends Instruction
{
    /**
     * @param nameNum 指令名称中的数字，eg: 名称为 %1 的指令的 nameNum 为 1
     * @param op1     第一个操作数
     * @param op2     第二个操作数
     */
    BinInstruction(int nameNum, DataType dataType, BasicBlock parent, Value op1, Value op2)
    {
        super("%v" + nameNum, dataType, parent, op1, op2);
    }

    public Value getOp1()
    {
        return getUsedValue(0);
    }

    public Value getOp2()
    {
        return getUsedValue(1);
    }

    public abstract boolean isCommutative();

    /**
     * 两个指令是否是相同的运算符
     * @param instr1 指令 1
     * @param instr2 指令 2
     * @return 是则为 true
     */
    public static boolean hasSameOp(BinInstruction instr1, BinInstruction instr2)
    {
        if (instr1 instanceof Add && instr2 instanceof Add)
        {
            return true;
        }
        else if (instr1 instanceof Sub && instr2 instanceof Sub)
        {
            return true;
        }
        else if (instr1 instanceof Mul && instr2 instanceof Mul)
        {
            return true;
        }
        else if (instr1 instanceof Sdiv && instr2 instanceof Sdiv)
        {
            return true;
        }
        else if (instr1 instanceof Srem && instr2 instanceof Srem)
        {
            return true;
        }
        else if (instr1 instanceof Icmp && instr2 instanceof Icmp)
        {
            Icmp cmp1 = (Icmp) instr1;
            Icmp cmp2 = (Icmp) instr2;
            return cmp1.getCondition().equals(cmp2.getCondition());
        }
        else
        {
            return false;
        }
    }
}
