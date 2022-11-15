package ir.values;

import ir.types.DataType;
import ir.types.FunctionType;
import util.MyList;

import java.util.ArrayList;
import java.util.HashMap;

public class Function extends Value
{
    public static Function putstr = null;
    public static Function putint = null;
    public static Function getint = null;
    public static Function LOOP_TRASH = new Function();
    /**
     * 是否是内建函数
     */
    private final boolean isBuiltin;
    /**
     * 函数的形参列表
     */
    private final ArrayList<Argument> arguments = new ArrayList<>();
    /**
     * 函数使用基本块
     */
    private final MyList<BasicBlock> blocks = new MyList<>();
    private final DataType returnType;
    /**
     * 存放 name-value 键值对
     * Value包括argument，basic block，有返回值的instruction
     */
    private final HashMap<String, Value> valueSymTab = new HashMap<>();

    public Function(String name, FunctionType functionType, boolean isBuiltin)
    {
        super("@" + name, functionType, Module.getInstance());
        this.isBuiltin = isBuiltin;
        this.returnType = getValueType().getReturnType();
        for (int i = 0; i < getNumArgs(); i++)
        {
            Argument argument = new Argument(i, functionType.getFormalArgs().get(i), this);
            arguments.add(argument);
            addFunctionSymbol(argument);
        }
    }

    /**
     * 这个同样是为了解决 loop 的问题
     */
    private Function()
    {
        super("@LOOP_TMP", null, null);
        isBuiltin = false;
        returnType = null;
    }

    public boolean isBuiltin()
    {
        return isBuiltin;
    }

    public MyList<BasicBlock> getBasicBlocks()
    {
        return blocks;
    }

    /**
     * 非常有意思的处理，一部分的信息被转移到了 type 中去，而 Value 实体只保留一小部分信息
     * @return 参数个数
     */
    public int getNumArgs()
    {
        return getValueType().getFormalArgs().size();
    }

    /**
     * @param value Argument | BasicBlock | Instruction(带返回值)
     */
    public void addFunctionSymbol(Value value)
    {
        valueSymTab.put(value.getName(), value);
    }

    /**
     * @return 函数开头基本块
     */
    public BasicBlock getHeadBlock()
    {
        return blocks.getHead().getVal();
    }

    public void insertTail(BasicBlock block)
    {
        blocks.insertEnd(new MyList.MyNode<>(block));
    }

    public ArrayList<Argument> getArguments()
    {
        return arguments;
    }

    public DataType getReturnType()
    {
        return returnType;
    }

    @Override
    public FunctionType getValueType()
    {
        return (FunctionType) super.getValueType();
    }

    /**
     * 编译器可以假设标记为 dso_local 的函数或变量将解析为同一链接单元中的符号。
     * 即使定义不在这个编译单元内，也会生成直接访问
     * @return 一个函数的 ir
     */
    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder(isBuiltin ? "declare dso_local " : "define dso_local ").
                append(returnType).append(" ").append(getName()).append('(');
        for (Argument argument : arguments)
        {
            s.append(argument.getValueType()).append(" ").append(argument.getName()).append(", ");
        }
        if (!arguments.isEmpty())
        {
            s.delete(s.length() - 2, s.length());
        }
        s.append(")");
        if (isBuiltin)
        {
            return s.toString();
        }
        s.append(" {\n");

        for (MyList.MyNode<BasicBlock> blockMyNode : blocks)
        {
            s.append(blockMyNode.getVal()).append('\n');
        }

        s.append("}");
        return s.toString();
    }
}
