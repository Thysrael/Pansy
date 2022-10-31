package ir.values.instructions;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;

public class Call extends Instruction
{
    /**
     * 有返回值的call
     *
     * @param function 第一个操作数，被调用的函数，返回值一定不是void
     * @param args     从第二个操作数开始排列，函数参数
     */
    public Call(int nameNum, BasicBlock parent, Function function, ArrayList<Value> args)
    {
        super("%" + nameNum, function.getReturnType(), parent, new ArrayList<Value>()
        {{
            add(function);
            addAll(args);
        }}.toArray(new Value[0]));
    }

    /**
     * 没有返回值的call
     *
     * @param function 同上，一定是void返回值
     * @param args     同上
     */
    public Call(BasicBlock parent, Function function, ArrayList<Value> args)
    {
        super("", function.getReturnType(), parent, new ArrayList<Value>()
        {{
            add(function);
            addAll(args);
        }}.toArray(new Value[0]));
    }

    public Function getFunction()
    {
        return (Function) getUsedValue(0);
    }

    /**
     * @return 获得call指令传递给函数的参数，全部为具体的Value，不是形参
     */
    public ArrayList<Value> getArgs()
    {
        ArrayList<Value> args = new ArrayList<>();
        for (int i = 0; i < ((Function) getUsedValue(0)).getNumArgs(); i++)
        {
            args.add(getUsedValue(i + 1));
        }
        return args;
    }

    @Override
    public String toString()
    {
        Function function = (Function) getUsedValue(0);
        boolean hasReturn = !getName().isEmpty();
        StringBuilder s = new StringBuilder(getName()).append(hasReturn ? " = call " : "call ").append(function.getReturnType()).append(' ')
                .append(function.getName()).append('(');
        int numArgs = function.getNumArgs();
        for (int i = 1; i <= numArgs; i++)
        {
            s.append(getUsedValue(i).getValueType()).append(' ').append(getUsedValue(i).getName()).append(", ");
        }
        if (numArgs > 0)
        {
            s.delete(s.length() - 2, s.length());
        }
        s.append(')');
        return s.toString();
    }
}
