package ir.values;


import ir.types.DataType;

/**
 * 函数形参，不带具体数值，只是占位
 * 例如define dso_local void f(i32 %0, [10 x i32]* %1)中 %0 与 %1 是两个形参
 */
public class Argument extends Value
{
    private final int rank;

    /**
     * @param rank   参数编号，从0开始，上面的例子中 %0 的 rank 是0，%1 的 rank 是1
     * @param dataType 参数类型，只能为DataType
     * @param parent   所在函数
     */
    public Argument(int rank, DataType dataType, Function parent)
    {
        super("%a" + rank, dataType, parent);
        this.rank = rank;
    }

    public int getRank()
    {
        return rank;
    }
}
