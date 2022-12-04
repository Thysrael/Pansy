package ir.values.instructions;

import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

import java.util.ArrayList;

/**
 * FIXME
 * 最失败的设计，如果让 Br 在新建的时候就维护 CFG 图，就会出现过于耦联的结果
 * 比如说 break 的时候会导致一个正常的块有一个 TMP_BLOCK 的前驱。
 */
public class Br extends TerInstruction
{
    private final boolean hasCondition;

    /**
     * 无条件跳转
     *
     * @param target 唯一的操作数，目标基本块
     */
    public Br(BasicBlock parent, BasicBlock target)
    {
        super(new VoidType(), parent, target);
        hasCondition = false;
        if (target != null)
        {
            parent.addSuccessor(target);
            target.addPredecessor(parent);
        }
    }

    /**
     * 有条件跳转
     *
     * @param condition  第一个操作数，条件
     * @param trueBlock  第二个操作数，条件成立目标
     * @param falseBlock 第三个操作数，条件不成立目标
     */
    public Br(BasicBlock parent, Value condition, BasicBlock trueBlock, BasicBlock falseBlock)
    {
        super(new VoidType(), parent, condition, trueBlock, falseBlock);
        hasCondition = true;
        if (trueBlock != null)
        {
            parent.addSuccessor(trueBlock);
            trueBlock.addPredecessor(parent);
        }
        if (falseBlock != null)
        {
            parent.addSuccessor(falseBlock);
            falseBlock.addPredecessor(parent);
        }
    }

    public boolean hasCondition()
    {
        return hasCondition;
    }

    /**
     * @return 指令的全部操作数，使用前需要用hasCondition()判定是否为有条件跳转
     */
    public ArrayList<Value> getOps()
    {
        ArrayList<Value> result = new ArrayList<>();
        if (hasCondition)
        {
            result.add(getUsedValue(0));
            result.add(getUsedValue(1));
            result.add(getUsedValue(2));
        }
        else
        {
            result.add(getUsedValue(0));
        }
        return result;
    }

    /**
     * 重载了 setUsedValue 方法，如果设置的是跳转基本块，这个方法还会自动更新所属 Block 的 successor
     * @param index 索引
     * @param newValue 新 Value
     */
    @Override
    public void setUsedValue(int index, Value newValue)
    {
        if (!hasCondition)
        {
            BasicBlock oldBlock = (BasicBlock) getUsedValue(index);
            getParent().replaceSuccessor(oldBlock, (BasicBlock) newValue);
        }
        else
        {
            if (index > 0)
            {
                BasicBlock oldBlock = (BasicBlock) getUsedValue(index);
                getParent().replaceSuccessor(oldBlock, (BasicBlock) newValue);
            }
        }
        super.setUsedValue(index, newValue);
    }

    public String toString()
    {
        if (hasCondition)
        {
            return "br " + getUsedValue(0).getValueType() + " " + getUsedValue(0).getName() + ", " +
                    getUsedValue(1).getValueType() + " " + getUsedValue(1).getName() + ", " +
                    getUsedValue(2).getValueType() + " " + getUsedValue(2).getName();
        }
        return "br " + getUsedValue(0).getValueType() + " " + getUsedValue(0).getName();
    }
}
