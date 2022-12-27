package ir.values.constants;

import ir.types.IntType;

import java.util.Objects;

public class ConstInt extends Constant
{
    public static final ConstInt ZERO = new ConstInt(0);
    private final int value;
    private final int bits;

    /**
     * @param bits 整数的位数，只有 32 位与 1 位两类，为了兼容 llvm ir icmp指令返回一位整数
     */
    public ConstInt(int bits, int value)
    {
        // 名字就是值，而且没有从属关系
        super(new IntType(bits));
        this.bits = bits;
        this.value = value;
    }

    public ConstInt(int value)
    {
        super(new IntType(32));
        this.bits = 32;
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    /**
     * 只要 bits 和 value 相同，就认为是同样的值
     * 这里不能直接生成，因为还有一点是比较父类的 equal，显然这是不同的
     * @param o 其他对象
     * @return 相等则为 true
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstInt constInt = (ConstInt) o;
        return value == constInt.value && bits == constInt.bits;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), value, bits);
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
