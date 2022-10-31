package ir.types;

public class IntType extends DataType
{
    public static final IntType I32 = new IntType(32);
    private final int bits;

    public IntType(int bits)
    {
        this.bits = bits;
    }

    @Override
    public String toString()
    {
        return "i" + bits;
    }

    @Override
    public int getSize()
    {
        return bits / 8;
    }

    @Override
    public boolean isI1()
    {
        return bits == 1;
    }
}
