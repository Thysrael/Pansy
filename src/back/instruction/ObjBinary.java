package back.instruction;

import back.operand.ObjImm;
import back.operand.ObjOperand;

import static back.instruction.ObjBinType.*;

public class ObjBinary extends ObjInstr
{
    private final ObjBinType type;
    private ObjOperand dst;
    private ObjOperand src1;
    private ObjOperand src2;

    public ObjBinary(ObjBinType type, ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        this.type = type;
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

    public ObjBinType getType()
    {
        return type;
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

    public boolean isSrc2Imm()
    {
        return src2 instanceof ObjImm;
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
        if (type.equals(SMMUL))
        {
            return "mul\t" + src1 + ",\t" + src2 + "\n" +
                    "\tmfhi\t" + dst + "\n";
        }
        else if (type.equals(DIV))
        {
            return "div\t" + src1 + ",\t" + src2 + "\n" +
                    "\tmflo\t" + dst + "\n";
        }
        else if (type.equals(MOD))
        {
            return "mul\t" + src1 + ",\t" + src2 + "\n" +
                    "\tmfhi\t" + dst + "\n";
        }
        else
        {
            String typeStr = "";
            if (type.equals(ADD))
            {
                typeStr = "add";
            }
            else if (type.equals(SUB))
            {
                typeStr = "sub";
            }
            else if (type.equals(MUL))
            {
                typeStr = "mul";
            }
            return typeStr + "\t" + dst + ",\t" + src1 + ",\t" + src2 + "\n";
        }
    }
}
