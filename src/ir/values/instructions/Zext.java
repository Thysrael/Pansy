package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * %8 = zext i1 %7 to i32
 */
public class Zext extends Instruction
{
    /**
     *
     * @param parent 基本块
     * @param value 被转变的值
     */
    public Zext(int nameNum, BasicBlock parent, Value value)
    {
        super("%v" + nameNum, new IntType(32), parent, value);
    }

    public Value getSrc()
    {
        return getUsedValue(0);
    }

    @Override
    public String toString()
    {
        return this.getName() + " = zext i1 " + getUsedValue(0).getName() + " to i32";
    }
}
