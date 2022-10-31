package ir.values.instructions;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;

public class Alloca extends MemInstruction
{
    /**
     * 新建一个 alloca 指令，其类型是分配空间类型的指针
     * @param nameNum 对于指令而言，其名称中带有数字，指令名称中的数字，eg: 名称为 %1 的指令的 nameNum 为 1
     * @param allocatedType 分配空间的类型，可能为 PointerType, IntType, ArrayType
     * @param parent 基本块
     */
    public Alloca(int nameNum, ValueType allocatedType, BasicBlock parent)
    {
        // 指针
        super("%" + nameNum, new PointerType(allocatedType), parent);
    }

    public ValueType getAllocatedType()
    {
        return ((PointerType) getValueType()).getPointeeType();
    }

    @Override
    public String toString()
    {
        return getName() + " = alloca " + getAllocatedType();
    }
}
