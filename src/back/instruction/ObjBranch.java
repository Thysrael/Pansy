package back.instruction;

import back.component.ObjBlock;
import back.operand.ObjOperand;

import static back.instruction.ObjCondType.*;

public class ObjBranch extends ObjInstr
{
    private ObjCondType cond;
    private ObjOperand src1 = null;
    private ObjOperand src2 = null;
    private boolean hasNoSrc = false;
    private boolean hasOneSrc = false;
    private boolean hasTwoSrc = false;
    /**
     * 这里没有用 label 是因为懒，我本来想将 parseOperand 的范围扩大一下，后来懒了
     */
    private ObjBlock target;

    /**
     * 无条件跳转 j target
     * @param target 目标块
     */
    public ObjBranch(ObjBlock target)
    {
        this.target = target;
        this.cond = ANY;
        this.hasNoSrc = true;
    }

    /**
     * 与零比较跳转 bgez
     * @param src 源
     * @param target 目标
     */
    public ObjBranch(ObjCondType cond, ObjOperand src, ObjBlock target)
    {
        this.cond = cond;
        setSrc1(src);
        this.target = target;
        this.hasOneSrc = true;
    }

    public ObjBranch(ObjCondType cond, ObjOperand src1, ObjOperand src2, ObjBlock target)
    {
        this.cond = cond;
        setSrc1(src1);
        setSrc2(src2);
        this.target = target;
        this.hasTwoSrc = true;
    }

    public ObjCondType getCond()
    {
        return cond;
    }

    public void setCond(ObjCondType cond)
    {
        this.cond = cond;
    }

    public void setTarget(ObjBlock target)
    {
        this.target = target;
    }

    public void setSrc1(ObjOperand src1)
    {
        addUseReg(this.src1, src1);
        this.src1 = src1;
    }

    public void setSrc2(ObjOperand src2)
    {
        addUseReg(this.src2, src2);
        this.src2 = src2;
    }

    @Override
    public void replaceReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (src1.equals(oldReg))
        {
            setSrc1(newReg);
        }
        if (src2.equals(oldReg))
        {
            setSrc2(newReg);
        }
    }

    @Override
    public void replaceUseReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (src1.equals(oldReg))
        {
            setSrc1(newReg);
        }
        if (src2.equals(oldReg))
        {
            setSrc2(newReg);
        }
    }

    @Override
    public String toString()
    {
        if (hasNoSrc)
        {
            return "j " + target.getName() + "\n";
        }
        if (hasOneSrc)
        {
            return "b" + cond + "z " + target.getName() + "\n";
        }
        if (hasTwoSrc)
        {
            return "b" + cond + " " + src1 + ",\t" + src2 + ",\t" + target.getName() + "\n";
        }
        return "wrong branch\n";
    }
}