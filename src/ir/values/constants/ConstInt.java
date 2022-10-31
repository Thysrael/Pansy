package ir.values.constants;

import ir.types.IntType;

public class ConstInt extends Constant
{
    public static final ConstInt ZERO = new ConstInt(0);
    private final int value;

    /**
     * @param bits 整数的位数，只有 32 位与 1 位两类，为了兼容 llvm ir icmp指令返回一位整数
     */
    public ConstInt(int bits, int value)
    {
        // 名字就是值，而且没有从属关系
        super(new IntType(bits));
        this.value = value;
    }

    public ConstInt(int value)
    {
        super(new IntType(32));
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String getName()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return Integer.toString(value);
    }
}
