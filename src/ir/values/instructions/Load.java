package ir.values.instructions;

import ir.types.DataType;
import ir.types.PointerType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Load extends MemInstruction
{
    /**
     * 从内存中取出来的值类型，只能为 PointerType 或 IntType
     */
    private final DataType dataType;

    /**
     * @param addr 唯一的操作数，内存地址，其 ValueType 为 PointerType，
     *             只能指向 IntType 或 FPType 或 PointerType（最多双重指针）
     */
    public Load(int nameNum, BasicBlock parent, Value addr)
    {
        super("%v" + nameNum, (DataType) ((PointerType) addr.getValueType()).getPointeeType(), parent, addr);
        this.dataType = (DataType) ((PointerType) addr.getValueType()).getPointeeType();
    }

    public Value getAddr()
    {
        return getUsedValue(0);
    }

    public DataType getDataType()
    {
        return dataType;
    }

    @Override
    public String toString()
    {
        return getName() + " = load " + getValueType() + ", " + getUsedValue(0).getValueType() + " " + getUsedValue(0).getName();
    }
}
