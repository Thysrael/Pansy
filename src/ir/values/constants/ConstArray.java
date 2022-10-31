package ir.values.constants;

import ir.types.ArrayType;
import ir.values.Value;

import java.util.ArrayList;

public class ConstArray extends Constant
{
    public static ConstArray getZeroConstantArray(ArrayType arrayType)
    {
        ArrayList<Constant> elements = new ArrayList<>();
        for (int i = 0; i < arrayType.getElementNum(); i++)
        {
            elements.add(Constant.getZeroConstant(arrayType.getElementType()));
        }
        return new ConstArray(elements);
    }

    /**
     * 一个数组，包含该 ConstantArray 中的所有元素（非展开）
     */
    private final ArrayList<Constant> elements = new ArrayList<>();

    /**
     * 虽然这个构造方法很长，但是本质上说了一个很简单的事情：
     * element 是常量数组，里面有一堆常量，可能是 ConstInt，也可能是 ConstArray
     * 我们构造它的类型的时候，需要元素的信息，我们选择了第一个元素
     * @param elements 常量数组
     */
    public ConstArray(ArrayList<Constant> elements)
    {
        super(new ArrayType(elements.get(0).getValueType(), elements.size()),
                elements.toArray(new Value[0]));
        this.elements.addAll(elements);
    }

    /**
     * @return 数组展开后的一维数组，每个元素都是一个 ConstInt
     */
    public ArrayList<ConstInt> getDataElements()
    {
        ArrayList<ConstInt> result = new ArrayList<>();
        // 当前数组是一维数组
        if (elements.get(0) instanceof ConstInt)
        {
            for (Constant element : elements)
            {
                result.add((ConstInt) element);
            }
        }
        // 二维数组
        else
        {
            for (Constant element : elements)
            {
                result.addAll(((ConstArray) element).getDataElements());
            }
        }

        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("[");
        // 这里之所以这么繁琐，是因为 ConstArray 里面没有保存信息，所以只能依赖于标签信息
        for (int i = 0; i < ((ArrayType) getValueType()).getElementNum(); i++)
        {
            s.append(getUsedValue(i).getValueType()).append(" ").append(getUsedValue(i)).append(", ");
        }
        s.delete(s.length() - 2, s.length());
        s.append("]");
        return s.toString();
    }
}
