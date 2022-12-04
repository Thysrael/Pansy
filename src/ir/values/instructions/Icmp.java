package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Icmp extends BinInstruction
{
    public enum Condition
    {
        EQ,
        LE,
        LT,
        GE,
        GT,
        NE;

        @Override
        public String toString()
        {
            if (this == Condition.EQ)
            {
                return "eq";
            }
            else if (this == Condition.GE)
            {
                return "sge";
            }
            else if (this == Condition.GT)
            {
                return "sgt";
            }
            else if (this == Condition.LE)
            {
                return "sle";
            }
            else if (this == Condition.LT)
            {
                return "slt";
            }
            return "ne";
        }
    }

    private final Condition condition;

    /**
     * @param condition 判断类型
     * @param op1       第一个操作数
     * @param op2       第二个操作数
     */
    public Icmp(int nameNum, BasicBlock parent, Condition condition, Value op1, Value op2)
    {
        super(nameNum, new IntType(1), parent, op1, op2);
        this.condition = condition;
    }

    public Condition getCondition()
    {
        return condition;
    }

    @Override
    public boolean isCommutative()
    {
        return condition.equals(Condition.NE) || condition.equals(Condition.EQ);
    }

    @Override
    public String toString()
    {
        return getName() + " = icmp " + condition.toString() + " " +
                getUsedValue(0).getValueType() + " " + getUsedValue(0).getName() + ", " + getUsedValue(1).getName();
    }
}
