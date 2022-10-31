package ir.values.instructions;

import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Store extends MemInstruction
{
    /**
     * 因为 Store 没有返回值，所以连名字也不配拥有
     * @param value     第一个操作数，写入内存的值，ValueType 为IntType
     * @param addr 第二个操作数，写入内存的地址，ValueType 为 PointerType，只能指向 IntType 或 PointerType（最多双重指针）
     */
    public Store(BasicBlock parent, Value value, Value addr)
    {
        super("", new VoidType(), parent, value, addr);
    }

    public Value getValue()
    {
        return getUsedValue(0);
    }

    public Value getAddr()
    {
        return getUsedValue(1);
    }

    @Override
    public String toString()
    {
        return "store " + getUsedValue(0).getValueType() + " " + getUsedValue(0).getName() + ", " +
                getUsedValue(1).getValueType() + " " + getUsedValue(1).getName();
    }
}
