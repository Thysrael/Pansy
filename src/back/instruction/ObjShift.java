package back.instruction;

import back.operand.ObjOperand;

import static back.instruction.ObjShift.ShiftType.*;

public class ObjShift extends ObjInstr
{
    public static ObjShift getSll(ObjOperand dst, ObjOperand src, int shift)
    {
        return new ObjShift(LSL, dst, src, shift);
    }

    public static ObjShift getSra(ObjOperand dst, ObjOperand src, int shift)
    {
        return new ObjShift(ASR, dst, src, shift);
    }

    public static ObjShift getSrl(ObjOperand dst, ObjOperand src, int shift)
    {
        return new ObjShift(LSR, dst, src, shift);
    }

    public enum ShiftType
    {
        //arithmetic right
        ASR,
        //logic left
        LSL,
        //logic right,
        LSR
    }

    private ObjOperand dst;
    private ObjOperand src;
    private final ShiftType type;
    private final int shift;

    public ObjShift(ShiftType type, ObjOperand dst, ObjOperand src, int shift)
    {
        this.type = type;
        setDst(dst);
        setSrc(src);
        this.shift = shift;
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
        if (type.equals(ASR))
        {
            return "sra " + dst + ",\t" + src + ",\t" + shift + "\n";
        }
        else if (type.equals(LSR))
        {
            return "srl " + dst + ",\t" + src + ",\t" + shift + "\n";
        }
        else
        {
            return "sll " + dst + ",\t" + src + ",\t" + shift + "\n";
        }
    }
}
