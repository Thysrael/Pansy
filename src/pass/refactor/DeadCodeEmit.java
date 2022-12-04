package pass.refactor;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.Value;
import ir.values.instructions.*;
import pass.Pass;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 死代码删除
 * 利用的对于有用指令的闭包，删掉闭包之外的指令
 */
public class DeadCodeEmit implements Pass
{
    /**
     * 有用指令的闭包，有用的评判标准是是不是访存指令或者与其他类似的感觉
     * 被这些指令需要的指令，这些指令的指令...就构成了有用指令的闭包
     */
    private final HashSet<Instruction> usefulInstrClosure = new HashSet<>();
    @Override
    public void run()
    {
        ArrayList<Function> functions = Module.getInstance().getFunctionsArray();
        for (Function function : functions)
        {
            if (!function.isBuiltin())
            {
                deleteUselessInstructions(function);
            }
        }
    }

    /**
     * 一共有 4 种
     * 跳转：Br,Ret
     * 内存写 store
     * 有副作用的 call
     * @param instruction 指令
     * @return 有用则为 true
     */
    public boolean isUseful(Instruction instruction)
    {
        return instruction instanceof Br ||
                instruction instanceof Ret ||
                instruction instanceof Store ||
                (instruction instanceof Call &&
                        ((Call) instruction).getFunction().hasSideEffect());
    }

    private void deleteUselessInstructions(Function curFunc)
    {
        usefulInstrClosure.clear();
        for (BasicBlock basicBlock : curFunc.getBasicBlocksArray())
        {
            for (Instruction instruction : basicBlock.getInstructionsArray())
            {
                if (isUseful(instruction))
                {
                    findUsefulClosure(instruction);
                }
            }
        }

        // 删除不在闭包内的指令
        for (BasicBlock basicBlock : curFunc.getBasicBlocksArray())
        {
            for (Instruction instruction : basicBlock.getInstructionsArray())
            {
                if (!usefulInstrClosure.contains(instruction))
                {
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
            }
        }
    }

    /**
     * 求解闭包的过程，是一个 DFS
     */
    private void findUsefulClosure(Instruction instr)
    {
        if (!usefulInstrClosure.contains(instr))
        {
            // 记录所有用到的指令
            usefulInstrClosure.add(instr);
            for (Value operand : instr.getUsedValues())
            {
                if (operand instanceof Instruction)
                {
                    findUsefulClosure((Instruction) operand);
                }
            }
        }
    }
}
