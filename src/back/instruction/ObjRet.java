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
    }

    @Override
    public String toString()
    {
        StringBuilder retSb = new StringBuilder();
        int stackSize = belongFunc.getTotalStackSize();
        // 处理一下 alloc 的部分
        if (stackSize != 0)
        {
            retSb.append("add $sp, \t$sp,\t").append(stackSize).append("\n");
        }
        // 如果是主函数就直接结束运行，并且没有保存寄存器的操作
        if (belongFunc.getName().equals("main"))
        {
            retSb.append("\tli\t$v0,\t10\n");
            retSb.append("\tsyscall\n\n");
        }
        else
        {
            TreeSet<Integer> calleeSavedRegIndexes = belongFunc.getCalleeSavedRegIndexes();
            int stackOffset = -4;
            for (Integer regIndex : calleeSavedRegIndexes)
            {
                retSb.append("\t").append("lw ").append(new ObjPhyReg(regIndex)).append(",\t")
                        .append(stackOffset).append("($sp)\n");
                // 下移 4
                stackOffset -= 4;
            }

            retSb.append("\tjr $ra\n");
        }

        return retSb.toString();
    }
}
