package back.instruction;

import back.operand.ObjLabel;
import back.operand.ObjOperand;

public class ObjLoad extends ObjInstr
{
    private ObjOperand dst;
    private ObjOperand addr;
    private ObjOperand offset;

    public ObjLoad(ObjOperand dst, ObjOperand addr, ObjOperand offset)
    {
        setDst(dst);
        setAddr(addr);
        setOffset(offset);
    }

    public void setDst(ObjOperand dst)
    {
        addDefReg(this.dst, dst);
        this.dst = dst;
    }

    public void setAddr(ObjOperand addr)
    {
        addUseReg(this.addr, addr);
        this.addr = addr;
    }

    public void setOffset(ObjOperand offset)
    {
        addUseReg(this.offset, offset);
        this.offset = offset;
    }

    public ObjOperand getAddr()
    {
        return addr;
    }

    public ObjOperand getOffset()
    {
        return offset;
    }

    public ObjOperand getDst()
    {
        return dst;
    }

    @Override
    public void replaceReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (dst.equals(oldReg))
        {
            setDst(newReg);
        }
        if (addr.equals(oldReg))
        {
            setAddr(newReg);
        }
        if (offset.equals(oldReg))
        {
            setOffset(newReg);
        }
    }

    @Override
    public void replaceUseReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (addr.equals(oldReg))
        {
            setAddr(newReg);
        }
        if (offset.equals(oldReg))
        {
            setOffset(newReg);
        }
    }

    @Override
    public String toString()
    {
        return "lw\t" + dst + ",\t" + offset + "(" + addr + ")\n";
    }
}
