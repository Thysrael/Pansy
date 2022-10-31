package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * 这个类本质上没有啥实际实现的东西，可能更像是一种分类
 * 包括 load, store, alloca, GEP
 */
public abstract class MemInstruction extends Instruction
{
    MemInstruction(String name, DataType dataType, BasicBlock parent, Value... ops)
    {
        super(name, dataType, parent, ops);
    }
}
