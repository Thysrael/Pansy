package ir.types;

/**
 * 起占位作用，不然只能用 null
 */
public class VoidType extends DataType
{
    @Override
    public String toString()
    {
        return "void";
    }

    @Override
    public int getSize()
    {
        throw new AssertionError("get void's size!");
    }
}
