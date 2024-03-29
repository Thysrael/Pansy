package back.instruction;

import back.operand.ObjOperand;
import back.operand.ObjPhyReg;
import back.operand.ObjReg;
import util.MyList;

import java.util.ArrayList;

public abstract class ObjInstr
{
    protected MyList.MyNode<ObjInstr> node;
    private final ArrayList<ObjReg> regDef = new ArrayList<>();
    private final ArrayList<ObjReg> regUse = new ArrayList<>();

    public ObjInstr()
    {
        this.node = new MyList.MyNode<>(this);
    }

    public MyList.MyNode<ObjInstr> getNode()
    {
        return node;
    }

    /**
     * 这里之所以要加限制，是因为操作数不一定都是寄存器，只有寄存器我们会进行登记
     * @param reg 寄存器
     */
    private void addUse(ObjOperand reg)
    {
        if (reg instanceof ObjReg)
        {
            regUse.add((ObjReg) reg);
        }
    }

    private void addDef(ObjOperand reg)
    {
        if (reg instanceof ObjReg)
        {
            regDef.add((ObjReg) reg);
        }
    }

    private void removeDef(ObjOperand reg)
    {
        if (reg instanceof ObjReg)
        {
            regDef.remove((ObjReg) reg);
        }
    }

    private void removeUse(ObjOperand reg)
    {
        if (reg instanceof ObjReg)
        {
            regUse.remove((ObjReg) reg);
        }
    }

    /**
     * 这个函数用于登记该指令使用到的寄存器，如果是目的寄存器，那么要登记在 def 中，要是源寄存器，那么要登记在 use 中
     * @param oldReg 之所以有这个设置，是因为可能一个指令的操作数会被二次修改，这个时候就需要修改其他地方了
     * @param newReg 待登记的寄存器
     */
    public void addDefReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (oldReg != null)
        {
            removeDef(oldReg);
        }
        addDef(newReg);
    }

    public void addUseReg(ObjOperand oldReg, ObjOperand newReg)
    {
        if (oldReg != null)
        {
            removeUse(oldReg);
        }
        addUse(newReg);
    }

    /**
     * 只有 branch 指令（条件跳转）时候有这个可能为 false
     * @return 当无条件的时候，返回 true
     */
    public boolean hasNoCond()
    {
        return true;
    }

    /**
     * 表示因此改变的寄存器
     * 可能要比 define 多一些，这是因为寄存器分配只是分析变量
     */
    public ArrayList<ObjReg> getWriteRegs()
    {
        return new ArrayList<>(regDef);
    }

    public ArrayList<ObjReg> getReadRegs()
    {
        ArrayList<ObjReg> readRegs = regUse;

        if (this instanceof ObjCall)
        {
            readRegs.add(ObjPhyReg.SP);
        }

        return readRegs;
    }

    public void replaceReg(ObjOperand oldReg, ObjOperand newReg)
    {}

    public void replaceUseReg(ObjOperand oldReg, ObjOperand newReg)
    {}

    public ArrayList<ObjReg> getRegDef()
    {
        return regDef;
    }

    public ArrayList<ObjReg> getRegUse()
    {
        return regUse;
    }
}
