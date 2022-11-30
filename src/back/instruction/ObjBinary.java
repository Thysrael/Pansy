package back.instruction;

import back.operand.ObjImm;
import back.operand.ObjOperand;

/**
 * 包括所有的使用到两个操作数的计算指令，这里也算一个尝试了，放弃使用冗余的 enum
 * 而是使用更加合理的 String，来看看会不会有好效果
 * 乘除法也会包括在这里面，因为多写一个太浪费事情了
 */
public class ObjBinary extends ObjInstr
{
    public static ObjBinary getAddu(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("addu", dst, src1, src2);
    }

    public static ObjBinary getSubu(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("subu", dst, src1, src2);
    }

    public static ObjBinary getXor(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("xor", dst, src1, src2);
    }

    public static ObjBinary getSltu(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("sltu", dst, src1, src2);
    }

    public static ObjBinary getSlt(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("slt", dst, src1, src2);
    }

    public static ObjBinary getMul(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("mul", dst, src1, src2);
    }

    public static ObjBinary getSmmul(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("smmul", dst, src1, src2);
    }

    public static ObjBinary getDiv(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("div", dst, src1, src2);
    }

    public static ObjBinary getSmmadd(ObjOperand dst, ObjOperand src1, ObjOperand src2)
    {
        return new ObjBinary("smmadd", dst, src1, src2);
    }

    private final String type;
    private ObjOperand dst;
    private ObjOperand src1;
    private ObjOperand src2;

    public ObjBinary(String type, ObjOperand dst, ObjOperand src1, ObjOperand src2)
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

    public String getType()
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

    /**
     * 区分是否是 imm 指令
     * 另外乘除法应该也是需要区分的
     * @return 指令字符串
     */
    @Override
    public String toString()
    {
        if (isSrc2Imm())
        {
            // 之所以没有 subiu 是因为这条指令是拓展指令
            if (type.equals("addu"))
            {
                return "addiu " + dst + ",\t" + src1 + ",\t" + src2 + "\n";
            }
            else if (type.equals("subu"))
            {
                return "subiu " + dst + ",\t" + src1 + ",\t" + src2 + "\n";
            }
            else if (type.equals("sltu"))
            {
                return "sltiu " + dst + ",\t" + src1 + ",\t" + src2 + "\n";
            }
            else
            {
                return type + "i " + dst + ",\t" + src1 + ",\t" + src2 + "\n";
            }
        }
        else
        {
            if (type.equals("smmul"))
            {
                return "mult " + src1 + ",\t" + src2 + "\n\t" +
                        "mfhi " + dst + "\n";
            }
            else if (type.equals("div"))
            {
                return "div " + src1 + ",\t" + src2 + "\n\t" +
                        "mflo " + dst + "\n";
            }
            else if (type.equals("smmadd"))
            {
                return "madd " + src1 + ",\t" + src2 + "\n\t" +
                        "mfhi " + dst + "\n";
            }
            else
            {
                return type + " " + dst + ",\t" + src1 + ",\t" + src2 + "\n";
            }
        }
    }
}
