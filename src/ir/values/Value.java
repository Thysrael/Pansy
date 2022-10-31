package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 最基本的类，llvm ir 的一切元素均是 Value
 * 每个Value有一个唯一的的 id
 * users 列表记录使用过这个 Value 元素的 User
 */
public abstract class Value
{
    /**
     * 身份的唯一标识
     */
    private final int id;
    /**
     * value 的名字，用于打印 llvm ir
     */
    protected String name;
    private final ValueType valueType;
    /**
     * value 的拥有者，注意不是使用者 User
     */
    private Value parent;
    /**
     * 记录使用过这个 Value 的使用者，一个 Value 可以有很多个使用者 User
     */
    private final ArrayList<User> users = new ArrayList<>();
    /**
     * 这里保证了 id 的唯一性
     */
    private static int idCounter = 0;

    public Value(String name, ValueType valueType, Value parent)
    {
        this.id = idCounter++;
        this.name = name;
        this.valueType = valueType;
        this.parent = parent;
    }

    public ValueType getValueType()
    {
        return valueType;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

    public Value getParent()
    {
        return parent;
    }

    public void setParent(Value parent)
    {
        this.parent = parent;
    }

    /**
     * 让 Value 登记自己的使用者
     * @param user 使用者
     */
    public void addUser(User user)
    {
        users.add(user);
    }

    public void setNameNum(int nameNum)
    {
        this.name = "%" + nameNum;
    }

    /**
     * 解除与 user 的使用关系，即不再被 user 使用
     * @param user 使用者
     */
    public void dropUser(User user)
    {
        if (!users.contains(user))
        {
            throw new AssertionError(getId() + " drop nonexistent user: " + user + " " + user.getId());
        }
        users.remove(user);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return id == value.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
