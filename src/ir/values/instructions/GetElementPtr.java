package ir.values.instructions;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

import java.util.ArrayList;

/**
 * 我们规定getelemtnptr只寻址一次，因此只有2个操作数——基址与下标，分别存放在values数组的 0，1号位
 * eg:
 * int c[10][20][30][40];
 * c[1][1][1][1];
 * %10 = getelementptr inbounds [10 x [20 x [30 x [40 x i32]]]], [10 x [20 x [30 x [40 x i32]]]]* %4, i64 0, i64 1
 * %11 = getelementptr inbounds [20 x [30 x [40 x i32]]], [20 x [30 x [40 x i32]]]* %10, i64 0, i64 1
 * %12 = getelementptr inbounds [30 x [40 x i32]], [30 x [40 x i32]]* %11, i64 0, i64 1
 * %13 = getelementptr inbounds [40 x i32], [40 x i32]* %12, i64 0, i64 1
 * %14 = load i32, i32* %13, align 4
 * 可见llvm ir官方用了四条GEP指令寻址四维数组
 * inbounds 表示执行越界检查，从界外掷入界内的，不重要
 * 对于 getelementptr [4 x i32] [4 x i32]* base, i32 A, i32 B
 * A 是第一个 index 他的计算是 sizeof([4 x i32]) * A
 * B 是第二个 index 他的计算是 sizeof(i32) * B
 * 所以一般 B 比较常用，而 A 一般为 0
 * 无论是有 A, B 两个 index，还是只有 A 一个 index。上面的算法都成立
 * 但是 index 的个数不同，GEP 的类型不同
 * 当没有 B 时，因为不涉及定位到 i32，因此 getelementptr 的返回值类型为 [4 x i32]*
 * 当有 B 时，返回值为 i32*
 * 在代码里，有
 * getelementptr baseType baseType* base, A, B;     ret: baseType.getElementType*
 * getelementptr baseType baseType* base, A;        ret: baseType*
 */
public class GetElementPtr extends MemInstruction
{
    private final ValueType baseType;

    /**
     * 只有一个下标，用于 (函数传参int a[][2])，然后 a[2][1]型寻址
     *
     * @param base       第一个操作数，基址（其类型是一个指针）
     * @param firstIndex 第二个操作数，指针寻址下标
     */
    public GetElementPtr(int nameNum, BasicBlock parent, Value base, Value firstIndex)
    {
        super("%v" + nameNum, (PointerType) base.getValueType(), parent, base, firstIndex);
        this.baseType = ((PointerType) base.getValueType()).getPointeeType();
    }


    /**
     * 有两个下标，用于int a[2][3];a[1][2]型正常寻址
     * @param base        第一个操作数，基址（其类型是一个指针）
     * @param firstIndex  第二个操作数，指针寻址下标（通常是 0）
     * @param secondIndex 第三个操作数，数组寻址下标
     */
    public GetElementPtr(int nameNum, BasicBlock parent, Value base, Value firstIndex, Value secondIndex)
    {
        super("%v" + nameNum, new PointerType(((ArrayType) ((PointerType) base.getValueType()).getPointeeType()).getElementType()),
                parent, base, firstIndex, secondIndex);
        this.baseType = ((PointerType) base.getValueType()).getPointeeType();
    }

    public Value getBase()
    {
        return getUsedValue(0);
    }

    public ValueType getBaseType()
    {
        return baseType;
    }

    /**
     * @return 下标列表，包含1个或2个下标
     */
    public ArrayList<Value> getOffset()
    {
        ArrayList<Value> result = new ArrayList<>();
        for (int i = 1; i < getNumOps(); i++)
        {
            result.add(getUsedValue(i));
        }
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder(getName() + " = getelementptr inbounds " + baseType + ", ");
        for (int i = 0; i < getNumOps(); i++)
        {
            s.append(getUsedValue(i).getValueType()).append(" ").append(getUsedValue(i).getName()).append(", ");
        }
        s.delete(s.length() - 2, s.length());
        return s.toString();
    }
}
