package ir.values;

import ir.types.DataType;
import ir.types.FunctionType;
import pass.analyze.LoopInfo;
import util.MyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    /**
     * 当一个函数向内存中写值的时候，就是有副作用的
     */
    private boolean hasSideEffect = false;
    /**
     * 调用图相关
     * 应该是该函数调用的
     */
    private final HashSet<Function> callees = new HashSet<>();
    private LoopInfo loopInfo;

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
        isBuiltin = true;
        returnType = null;
    }

    public void setHasSideEffect(boolean hasSideEffect)
    {
        this.hasSideEffect = hasSideEffect;
    }

    public boolean hasSideEffect()
    {
        return hasSideEffect;
    }

    public HashSet<Function> getCallees()
    {
        return callees;
    }

    public void clearCallees()
    {
        callees.clear();
    }

    public void addCallee(Function callee)
    {
        callees.add(callee);
    }

    /**
     * @return 用ArrayList形式返回基本块
     */
    public ArrayList<BasicBlock> getBasicBlocksArray()
    {
        ArrayList<BasicBlock> result = new ArrayList<>();
        for (MyList.MyNode<BasicBlock> blockMyNode : blocks)
        {
            result.add(blockMyNode.getVal());
        }
        return result;
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
     * 这显然是低效的，其实应该考虑将 node 记录在案的
     * 否则删除就成了 O(n) 了
     */
    public void eraseFromParent()
    {
        for (MyList.MyNode<Function> functionMyNode : Module.getInstance().getFunctions())
        {
            if (functionMyNode.getVal().equals(this))
            {
                functionMyNode.removeSelf();
                return;
            }
        }
    }

    public LoopInfo getLoopInfo()
    {
        return loopInfo;
    }

    public void analyzeLoop()
    {
        if (isBuiltin)
        {
            return;
        }
        for (MyList.MyNode<BasicBlock> blockMyNode : blocks)
        {
            blockMyNode.getVal().setParentLoop(null);
        }
        // 进行一遍图分析
        loopInfo = new LoopInfo(this);
    }

    public void reducePhi(boolean reducePhi)
    {
        for (BasicBlock block : getBasicBlocksArray())
        {
            block.reducePhi(reducePhi);
        }
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
