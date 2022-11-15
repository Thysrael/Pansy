package back.instruction;

import back.operand.ObjOperand;

public class ObjCoMove extends ObjInstr
{
    public static ObjCoMove getMthi(ObjOperand src)
    {
        return new ObjCoMove("mthi", null, src);
    }

    public static ObjCoMove getMfhi(ObjOperand dst)
    {
        return new ObjCoMove("mfhi", dst, null);
    }

    private final String type;
    private ObjOperand dst;
    private ObjOperand src;

    public ObjCoMove(String type, ObjOperand dst, ObjOperand src)
    {
        this.type = type;
        setDst(dst);
        setSrc(src);
    }

    public void setDst(ObjOperand dst)
    {
        if (dst != null)
        {
            addDefReg(this.dst, dst);
        }
        this.dst = dst;
    }

    public void setSrc(ObjOperand src)
    {
        if (src != null)
        {
            addUseReg(this.src, src);
        }
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
        if (dst != null)
        {
            if (dst.equals(oldReg))
            {
                setDst(newReg);
            }
        }
        if (src != null)
        {
            if (src.equals(oldReg))
            {
                setSrc(newReg);
            }
        }
    }

    @Override
    public void replaceUseReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (src != null)
        {
            if (src.equals(oldReg))
            {
                setSrc(newReg);
            }
        }
    }

    @Override
    public String toString()
    {
        if (type.equals("mthi"))
        {
            return "mthi " + src + "\n";
        }
        else if (type.equals("mfhi"))
        {
            return "mfhi " + dst + "\n";
        }
        else
        {
            assert false: "wrong coMove\n";
            return "";
        }
    }
}
