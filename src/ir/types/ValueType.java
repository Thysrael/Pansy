package ir.types;

/**
 * 最基础的标签类
 */
public abstract class ValueType
{
    /**
     * 返回的所占字节数
     * @return 所占字节数
     */
    public abstract int getSize();

    public boolean isI1()
    {
        return false;
    }
}
