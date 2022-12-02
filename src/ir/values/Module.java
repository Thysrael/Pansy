package ir.values;

import util.MyList;

import java.util.ArrayList;

/**
 * 编译单元，我们的比赛只有一个
 * 一个编译单元由若干个函数与全局变量组成，增加函数与全局变量暂时规定只能加在最后
 * parent->null，name->"Module"，valueType->null
 * 单例模式
 * 暂时不确定是否需要添加符号表，看需求
 */
public class Module extends Value
{
    private static final Module module = new Module();

    /**
     * 这两个list是侵入式链表（OS用的链表），用于函数与全局变量，是Module的符号表
     */
    private final MyList<Function> functions = new MyList<>();
    private final MyList<GlobalVariable> globalVariables = new MyList<>();

    private Module()
    {
        super("Module", null, null);
    }

    public static Module getInstance()
    {
        return module;
    }

    public void addFunction(Function function)
    {
        for (MyList.MyNode<Function> functionNode : functions)
        {
            if (functionNode.getVal().equals(function))
            {
                throw new AssertionError("function is already in!");
            }
        }
        new MyList.MyNode<>(function).insertEnd(functions);
    }

    public void addGlobalVariable(GlobalVariable globalVariable)
    {
        for (MyList.MyNode<GlobalVariable> globalVariableNode : globalVariables)
        {
            if (globalVariableNode.getVal().equals(globalVariable))
            {
                throw new AssertionError("global variable is already in!");
            }
        }
        new MyList.MyNode<>(globalVariable).insertEnd(globalVariables);
    }

    /**
     * 这两个get...s()方法只能由后端调用
     */
    public MyList<Function> getFunctions()
    {
        return functions;
    }

    public MyList<GlobalVariable> getGlobalVariables()
    {
        return globalVariables;
    }

    /**
     * 这两个get()方法前后端都可以调用
     */
    public GlobalVariable getGlobalVariable(String name)
    {
        for (MyList.MyNode<GlobalVariable> globalVariableMyNode : globalVariables)
        {
            GlobalVariable globalVariable = globalVariableMyNode.getVal();
            if (globalVariable.getName().equals(name))
            {
                return globalVariable;
            }
        }
        throw new AssertionError("global variable " + name + " not found!");
    }

    public Function getFunction(String name)
    {
        for (MyList.MyNode<Function> functionMyNode : functions)
        {
            Function function = functionMyNode.getVal();
            if (function.getName().equals(name))
            {
                return function;
            }
        }
        throw new AssertionError("function " + name + " not found!");
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (MyList.MyNode<GlobalVariable> gNode : globalVariables)
        {
            s.append(gNode.getVal()).append('\n');
        }
        s.append("\n");
        for (MyList.MyNode<Function> fNode : functions)
        {
            s.append(fNode.getVal()).append('\n');
        }
        return s.toString();
    }
}

