package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Sub extends BinInstruction
{
    public Sub(int nameNum, BasicBlock parent, Value op1, Value op2)
    {
        super(nameNum, new IntType(32), parent, op1, op2);
    }

    @Override
    public String toString()
    {
        return getName() + " = sub " + getValueType() + " " + getUsedValue(0).getName() + ", " + getUsedValue(1).getName();
    }
}
