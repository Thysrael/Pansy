package pass.refactor;

import driver.Config;
import ir.types.VoidType;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import ir.values.instructions.Call;
import ir.values.instructions.Instruction;
import ir.values.instructions.Ret;
import pass.Pass;

import java.util.ArrayList;
import java.util.HashSet;

public class UselessRetEmit implements Pass
{
    private final Module irModule = Module.getInstance();
    /**
     * 登记的是可以被遍历过的递归函数
     */
    private final HashSet<Value> visited = new HashSet<>();
    /**
     * 里面的函数是可以删除的
     */
    private final ArrayList<Function> canBeDeleted = new ArrayList<>();
    /**
     * 可以删除的 call 指令
     */
    private final ArrayList<Call> uselessCalls = new ArrayList<>();

    /*
     * What to do:
     *   - For all functions, calculate the called cases of
     *     their return values
     *   - For those return values that are not used, set
     *     their return value to 0 directly
     *   - If all the return values of a function are unused,
     *     remove both the function and the instruction that called it
     *   - In this section, we also need to do some dead code removal.
     *     If a variable cannot serve the return value, then all instructions
     *     associated with it should be removed
     * */
    @Override
    public void run()
    {
        if (Config.openURE)
        {
            visited.clear();
            // 似乎因为没有递归删除，所以就选定了 5 轮
            int isFixed = 5;
            while (isFixed != 0)
            {
                uselessCalls.clear();
                canBeDeleted.clear();

                // 遍历每一个函数
                for (Function function : irModule.getFunctionsArray())
                {
                    // 只有非内建函数，不是 main 的函数，有返回值的函数我们才分析
                    if (!function.isBuiltin() &&
                            !function.getName().equals("@main") &&
                            !(function.getReturnType() instanceof VoidType))
                    {
                        deepDCE(function);
                    }
                }

                // 将可以删除的函数删掉
                for (Function function : canBeDeleted)
                {
                    function.eraseFromParent();
                }

                // 将无用的 call 指令删除
                for (Call call : uselessCalls)
                {
                    call.dropAllOperands();
                    call.eraseFromParent();
                }

                visited.clear();

                isFixed--;
            }
        }
    }

    /**
     * 通过分析调用这个函数的 call 语句有没有被用到，和这个函数有没有副作用
     * 来判断这个函数需不要删除
     * @param function 待分析的函数
     */
    private void deepDCE(Function function)
    {
        // 如果没有访问过这个函数
        if (!visited.contains(function))
        {
            // 这个里面装着所有的递归 call 指令
            ArrayList<Call> calls = new ArrayList<>();
            boolean hasRecursion = false;

            // 使用这个函数的 User，大概就是 Call 吧
            for (User user : function.getUsers())
            {
                // 如果是 Call 指令
                if (user instanceof Call)
                {
                    // call 指令属于当前 function，那么就是递归了
                    if (((Call) user).getParent().getParent().equals(function))
                    {
                        hasRecursion = true;
                        calls.add((Call) user);
                    }
                    // 这是常规情况，就是 call 的返回值被使用了
                    else if (!user.getUsers().isEmpty())
                    {
                        return;
                    }
                    // 当 call 指令的返回值没有被使用，那么就考虑他是不是写了内存
                    // 如果没有写内存，那么就是没有用 call（返回值没有被用，而且没有写内存）
                    else if (!((Call) user).getFunction().hasSideEffect())
                    {
                        uselessCalls.add((Call) user);
                    }
                }
            }

            // 能够进行到这里的，一定满足
            // call 没有被返回值使用

            // 如果是递归的
            if (hasRecursion)
            {
                // 有两种情况直接结束分析，一种是 call 被多处使用
                // 另一种是 call 被使用，而且使用者不是 Ret
                for (Call call : calls)
                {
                    if (call.getUsers().size() > 1)
                    {
                        return;
                    }
                    else if (call.getUsers().size() == 1 &&
                            !(call.getUsers().get(0) instanceof Ret))
                    {
                        return;
                    }
                }

                // 进行到这里，是递归函数，而且只有 ret 调用了 call

                // 似乎只有递归函数会被标记这个
                visited.add(function);

                for (BasicBlock bb : function.getBasicBlocksArray())
                {
                    for (Instruction inst : bb.getInstructionsArray())
                    {
                        if (inst instanceof Ret)
                        {
                            // 直接返回 0
                            inst.setUsedValue(0, ConstInt.ZERO);
                        }
                    }
                }
            }
            else
            {
                for (BasicBlock bb : function.getBasicBlocksArray())
                {
                    for (Instruction inst : bb.getInstructionsArray())
                    {
                        if (inst instanceof Ret)
                        {
                            inst.setUsedValue(0, ConstInt.ZERO);
                        }
                    }
                }
            }

            // 这是可以删除的函数
            if (!function.hasSideEffect())
            {
                canBeDeleted.add(function);
            }
        }
    }
}
