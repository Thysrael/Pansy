package back.instruction;

import back.component.ObjFunction;
import back.operand.ObjPhyReg;

import java.util.TreeSet;

public class ObjRet extends ObjInstr
{
    private final ObjFunction belongFunc;
    public ObjRet(ObjFunction belongFunc)
    {
        this.belongFunc = belongFunc;
        // 这里只是单纯的借鉴一下，我并不知道为啥
        addUseReg(null, new ObjPhyReg("v0"));
    }

    @Override
    public String toString()
    {
        StringBuilder retSb = new StringBuilder();
        int stackSize = belongFunc.getTotalStackSize();
        // 处理一下 alloc 的部分
        if (stackSize != 0)
        {
            retSb.append("add\t$sp, \t$sp,\t").append(stackSize).append("\n");
        }

        TreeSet<Integer> calleeSavedRegIndexes = belongFunc.getCalleeSavedRegIndexes();
        int stackOffset = -4;
        for (Integer regIndex : calleeSavedRegIndexes)
        {
            retSb.append("\t").append("lw\t").append(new ObjPhyReg(regIndex)).append(",\t")
                    .append(stackOffset).append("($sp)\n");
            // 下移 4
            stackOffset -= 4;
        }

        retSb.append("\tjr\t$ra\n");

        return retSb.toString();
    }
}
