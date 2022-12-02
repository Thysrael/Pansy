package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 使用 Value 的 Value 元素，
 * 例如 Instruction “%1 = Add i32 %2, 3”中指令 %1 既是 Value 的派生类 Add，
 * 也是使用指令 %2 与常数 3 的 User
 * Function，没有初始化的 GlobalVariable，ConstantData 不会使用 Value，其他 User 会使用至少一个 Value
 */
public abstract class User extends Value
{
    /**
     * 记录自己使用过的 value
     */
    private final ArrayList<Value> operands = new ArrayList<>();

    /**
     * 在初始化的时候不加入自己使用的 value
     */
    public User(String name, ValueType valueType, Value parent)
    {
        super(name, valueType, parent);
    }

    /**
     * 在初始化的时候加入 value
     */
    public User(String name, ValueType valueType, Value parent, Value... operands)
    {
        super(name, valueType, parent);
        this.operands.addAll(Arrays.asList(operands));
        initUse();
    }

    /**
     * 让被使用者绑定关系
     */
    private void initUse()
    {
        for (Value value : operands)
        {
            if (value != null)
            {
                value.addUser(this);
            }
        }
    }

    /**
     * 获取操作数
     *
     * @param index 操作数位置
     */
    public Value getUsedValue(int index)
    {
        return operands.get(index);
    }

    public ArrayList<Value> getUsedValues()
    {
        return operands;
    }

    /**
     * 任何时候，User-used 关系都应该是双向的
     * 这里会去掉原来 index 对应的 Value，并且解除 oldValue 的 use
     * @param index 索引
     * @param newValue 新 Value
     */
    public void setUsedValue(int index, Value newValue)
    {
        Value oldValue = operands.get(index);
        if (oldValue != null)
        {
            oldValue.dropUser(this);
        }
        operands.set(index, newValue);
        newValue.addUser(this);
    }

    /**
     * 替换 User 使用的某个 Value
     * 如果 oldValue 不存在，那么就不进行任何的操作
     * @param oldValue 原有值
     * @param newValue 目标值
     */
    public void replaceUsesWith(Value oldValue, Value newValue)
    {
        for (int i = 0; i < operands.size(); i++)
        {
            if (operands.get(i).equals(oldValue))
            {
                setUsedValue(i, newValue);
            }
        }
    }

    /**
     * @return 使用的操作数数量
     */
    public int getNumOps()
    {
        return operands.size();
    }

    public void dropAllOperands()
    {
        for (Value operand : operands)
        {
            operand.dropUser(this);
        }
    }
}
