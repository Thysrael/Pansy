package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Mul extends BinInstruction
{
    public Mul(int nameNum, BasicBlock parent, Value op1, Value op2)
    {
        super(nameNum, new IntType(32), parent, op1, op2);
    }

    @Override
    public boolean isCommutative()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getName() + " = mul " + getValueType() + " " + getUsedValue(0).getName() + ", " + getUsedValue(1).getName();
    }
}
