package ir.values.instructions;

import ir.types.DataType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Ret extends TerInstruction
{
    /**
     * 返回值为void，没有操作数
     */
    public Ret(BasicBlock parent)
    {
        super(new VoidType(), parent);
    }

    /**
     * 返回值不为void
     *
     * @param retVal 唯一的操作数，返回值，ValueType为FPType或IntType
     */
    public Ret(BasicBlock parent, Value retVal)
    {
        super((DataType) retVal.getValueType(), parent, retVal);
    }

    public Value getRetValue()
    {
        if (getValueType() instanceof VoidType)
        {
            return null;
        }
        return getUsedValue(0);
    }

    @Override
    public String toString()
    {
        if (getValueType() instanceof VoidType)
        {
            return "ret void";
        }
        else
        {
            return "ret " + getUsedValue(0).getValueType() + " " + getUsedValue(0).getName();
        }
    }
}
