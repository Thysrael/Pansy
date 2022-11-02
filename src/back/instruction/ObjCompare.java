package back.instruction;

import back.operand.ObjOperand;

public class ObjCompare extends ObjInstr
{
    private final ObjCondType cond;
    private ObjOperand dst;
    private ObjOperand src1;
    private ObjOperand src2;

    public ObjCompare(ObjCondType cond, ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        this.cond = cond;
        setDst(dst);
        setSrc1(src1);
        setSrc2(src2);
    }

    public void setDst(ObjOperand dst)
    {
        addDefReg(this.dst, dst);
        this.dst = dst;
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
    public ObjOperand getDst()
    {
        return dst;
    }

    public ObjOperand getSrc1()
    {
        return src1;
    }

    public ObjOperand getSrc2()
    {
        return src2;
    }

    @Override
    public void replaceReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (dst.equals(oldReg))
        {
            setDst(newReg);
        }
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
        return "s" + cond.toString().toLowerCase() + "\t" + dst + ",\t" + src1 + ",\t" + src2 + "\n";
    }
}
