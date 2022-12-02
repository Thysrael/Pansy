package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Value;

public class Phi extends Instruction
{
    private int predecessorNum;

    public Phi(int nameNum, DataType dataType, BasicBlock parent, int predecessorNum)
    {
        super("%p" + nameNum, dataType, parent, new Value[predecessorNum * 2]);
        this.predecessorNum = predecessorNum;
    }

    public int getPredecessorNum()
    {
        return predecessorNum;
    }

    public void addIncoming(Value value, BasicBlock block)
    {
        int i = 0;
        while (i < predecessorNum && getUsedValue(i) != null)
        {
            i++;
        }
        if (i < predecessorNum)
        {
            setUsedValue(i, value);
            setUsedValue(i + predecessorNum, block);
        }
        else
        {
            getUsedValues().add(predecessorNum, value);
            predecessorNum++;
            getUsedValues().add(block);
        }
        value.addUser(this);
        block.addUser(this);
    }

    /**
     * 按照索引获得可能的操作数，比如上面那个例子，如果 index = 0， 则应该返回 1 (对应 %btrue 块，是第一个)
     *
     * @param index 索引
     * @return 操作数
     */
    public Value getInputVal(int index)
    {
        return getUsedValue(index);
    }

    public void removeIfRedundant()
    {
        removeIfRedundant(true);
    }

    public void removeIfRedundant(boolean reducePhi)
    {
        if (getUsers().isEmpty())
        {
            dropAllOperands();
            eraseFromParent();
            return;
        }
        if (predecessorNum == 0)
        {
            throw new AssertionError(this + "'s predecessorNum = 0!");
        }
        Value commonValue = getUsedValue(0);
        for (int i = 1; i < predecessorNum; i++)
        {
            if (commonValue != getUsedValue(i))
            {
                return;
            }
        }
        if (!reducePhi && commonValue instanceof Instruction)
        {
            return;
        }
        replaceAllUsesWith(commonValue);
        dropAllOperands();
        eraseFromParent();
    }

    public Value getInputValForBlock(BasicBlock block)
    {
        for (int i = 0; i < predecessorNum; i++)
        {
            if (getUsedValue(i + predecessorNum) == block)
            {
                return getUsedValue(i);
            }
        }

        throw new AssertionError("block not found for phi!");
    }

    public BasicBlock getBlockForVal(Value value, int rank)
    {
        for (int i = 0; i < predecessorNum; i++)
        {
            if (getUsedValue(i) == value)
            {
                if (rank-- == 0)
                {
                    return (BasicBlock) getUsedValue(i + predecessorNum);
                }
            }
        }
        throw new AssertionError(String.format("%d-th value not found!", rank));
    }

    public void removeIncoming(BasicBlock block)
    {
        int index = getUsedValues().indexOf(block);
        getUsedValues().get(index - predecessorNum).dropUser(this);
        block.dropUser(this);
        getUsedValues().remove(index);
        getUsedValues().remove(index - predecessorNum);
        predecessorNum--;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder(getName() + " = phi ").append(getValueType());
        for (int i = 0; i < predecessorNum; i++)
        {
            if (getUsedValue(i) == null) break;
            s.append(" [ ").append(getUsedValue(i).getName()).append(", ")
                    .append(getUsedValue(i + predecessorNum).getName()).append(" ], ");
        }
        s.delete(s.length() - 2, s.length());
        return s.toString();
    }
}
