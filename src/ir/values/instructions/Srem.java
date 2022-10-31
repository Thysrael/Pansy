package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Srem extends BinInstruction
{
    public Srem(int nameNum, BasicBlock parent, Value op1, Value op2)
    {
        super(nameNum, new IntType(32), parent, op1, op2);
    }

    @Override
    public String toString()
    {
        return getName() + " = srem " + getValueType() + " " + getUsedValue(0).getName() + ", " + getUsedValue(1).getName();
    }
}
