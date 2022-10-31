package ir.values;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.constants.Constant;

/**
 * 全局简单变量与全局数组
 * 全局变量可以用常量 Constant 初始化, 不能用变量初始化，这是语言规范
 * 本质上 GlobalVariable 和 Function 都属于 GlobalVal 其共同特点是 parent 都是 Module，
 * 但是我没有设计这个，因为懒
 * 还有特点是都是以 @ 开头的
 * GlobalVariable 只持有一个 Constant，也就是其 getUsed(0) 就是持有的常量
 * 全局变量本质上是一个指针，而不是一个实际的变量
 */
public class GlobalVariable extends User
{
    private final boolean isConst;
    private final boolean isInit;

    /**
     * 对于没有初始值的全局变量（一定不会是常量，因为常量一定有初始值），采用 0 初始值
     * @param name 标识符
     * @param valueType 类型
     */
    public GlobalVariable(String name, ValueType valueType)
    {
        // 全局变量本质上是一个指针，所以才有了
        super("@" + name, new PointerType(valueType), Module.getInstance(), Constant.getZeroConstant(valueType));
        isInit = false;
        isConst = false;
    }

    /**
     * 初始化
     * 此时全局变量既可以为常量，也可以不是常量
     *
     * @param initVal 是全局变量使用的唯一 Value
     */
    public GlobalVariable(String name, Constant initVal, boolean isConst)
    {
        super("@" + name, new PointerType(initVal.getValueType()), Module.getInstance(), initVal);
        isInit = true;
        this.isConst = isConst;
    }

    @Override
    public String toString()
    {
        return getName() + " = dso_local " + ((isConst) ? "constant " : "global ") + ((PointerType) getValueType()).getPointeeType() + " " + getUsedValue(0);
    }
}
