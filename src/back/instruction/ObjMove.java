package back.instruction;

import back.operand.ObjImm;
import back.operand.ObjLabel;
import back.operand.ObjOperand;

public class ObjMove extends ObjInstr
{
    private ObjOperand dst;
    private ObjOperand src;

    public ObjMove(ObjOperand dst, ObjOperand src)
    {
        setDst(dst);
        setSrc(src);
    }

    public void setDst(ObjOperand dst)
    {
        addDefReg(this.dst, dst);
        this.dst = dst;
    }

    public void setSrc(ObjOperand src)
    {
        addUseReg(this.src, src);
        this.src = src;
    }

    public ObjOperand getDst()
    {
        return dst;
    }

    public ObjOperand getSrc()
    {
        return src;
    }

    @Override
    public void replaceReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (dst.equals(oldReg))
        {
            setDst(newReg);
        }
        if (src.equals(oldReg))
        {
            setSrc(newReg);
        }
    }

    @Override
    public void replaceUseReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (src.equals(oldReg))
        {
            setSrc(newReg);
        }
    }

    @Override
    public String toString()
    {
        // 如果是一个立即数，那么就用 li
        if (src instanceof ObjImm)
        {
            return "li " + dst + ",\t" + src + "\n";
        }
        else if (src instanceof ObjLabel)
        {
            return "la " + dst + ",\t" + src + "\n";
        }
        else
        {
            return "move " + dst + ",\t" + src + "\n";
        }
    }
}
