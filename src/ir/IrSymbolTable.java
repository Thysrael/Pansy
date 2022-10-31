package ir;

import ir.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IrSymbolTable
{
    private final ArrayList<HashMap<String, Value>> table;
    /**
     * 如果已经 preEnter 了，那么就不用再加一层了
     * 为了区分 function 和 block 的行为
     */
    private boolean preEnter;

    /**
     * 初始化符号表，并且加入 global layer
     */
    public IrSymbolTable()
    {
        this.table = new ArrayList<>();
        this.table.add(new HashMap<>());
        this.preEnter = false;
    }

    /**
     * 返回栈顶符号域
     * @return 栈顶符号域
     */
    public HashMap<String, Value> getTopLayer()
    {
        return table.get(table.size() - 1);
    }

    /**
     * 从栈顶到栈底根据 name 查找 value
     * @param ident 标识符
     * @return value，如果没有找到，则返回 null
     */
    public Value find(String ident)
    {
        for (int i = table.size() - 1; i >= 0; i--)
        {
            if (table.get(i).containsKey(ident))
            {
                return table.get(i).get(ident);
            }
        }
        return null;
    }

    /**
     * 在符号表中登记 Value
     * @param ident 标识符
     * @param value irValue
     */
    public void addValue(String ident, Value value)
    {
        getTopLayer().put(ident, value);
    }

    public void pushFuncLayer()
    {
        table.add(new HashMap<>());
        this.preEnter = true;
    }

    public void popFuncLayer()
    {
        table.remove(table.size() - 1);
        this.preEnter = false;
    }

    public void pushBlockLayer()
    {
        if (preEnter)
        {
            preEnter = false;
        }
        else
        {
            table.add(new HashMap<>());
        }
    }

    public void popBlockLayer()
    {
        // 第 1 层是 global，第 2 层是 function，之后才是 block 层
        if (table.size() > 2)
        {
            table.remove(table.size() - 1);
        }
    }

    /**
     * 第一个 layer 就是 global，所以可以借此判断 layer 的状态
     * @return true 则 global
     */
    public boolean isGlobal()
    {
        return this.table.size() == 1;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        for (int i = table.size() - 1; i >= 0; i--)
        {
            HashMap<String, Value> layer = table.get(i);
            for (Map.Entry<String, Value> entry : layer.entrySet())
            {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            sb.append("\n");
            sb.append("======================================================\n");
        }

        return sb.toString();
    }
}
