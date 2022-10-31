package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * 有左右2个操作数，分别放在 values 列表的 0 号与 1 号位，
 * 操作数可以为常数，也可以为指令
 */
public abstract class BinInstruction extends Instruction
{
    /**
     * @param nameNum 指令名称中的数字，eg: 名称为 %1 的指令的 nameNum 为 1
     * @param op1     第一个操作数
     * @param op2     第二个操作数
     */
    BinInstruction(int nameNum, DataType dataType, BasicBlock parent, Value op1, Value op2)
    {
        super("%" + nameNum, dataType, parent, op1, op2);
    }

    public Value getOp1()
    {
        return getUsedValue(0);
    }

    public Value getOp2()
    {
        return getUsedValue(1);
    }
}
