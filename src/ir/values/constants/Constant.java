package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.User;
import ir.values.Value;

/**
 * 常量包括 ConstInt, ConstArray
 * 对于常量，是没有名字，而且没有 parent 的
 */
public abstract class Constant extends User
{
    /**
     * 不用Value
     */
    Constant(ValueType valueType)
    {
        super(null, valueType, null);
    }

    /**
     * 引用Value
     */
    Constant(ValueType valueType, Value... values)
    {
        super(null, valueType, null, values);
    }

    /**
     * @param constantType Constant种类
     * @return 指定 ValueType的全 0 ConstantData 或者 ConstantArray
     */
    public static Constant getZeroConstant(ValueType constantType)
    {
        if (constantType instanceof IntType)
        {
            return ConstInt.ZERO;
        }
        else
        {
            return ConstArray.getZeroConstantArray((ArrayType) constantType);
        }
    }
}
