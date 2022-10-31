package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * 终结指令，包括 Ret 和 Br
 * 主要特点是没有名字（那么似乎类型也变得不那么重要了）
 */
public class TerInstruction extends Instruction
{
    public TerInstruction(DataType dataType, BasicBlock parent, Value... ops)
    {
        super("", dataType, parent, ops);
    }

}
