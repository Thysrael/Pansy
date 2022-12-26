package pass.refactor;

import ir.IrBuilder;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.*;
import ir.values.Module;
import ir.values.instructions.*;
import pass.Pass;

import java.util.ArrayList;
import java.util.HashMap;

public class InlineFunction implements Pass
{
    private final Module irModule = Module.getInstance();
    private final IrBuilder irBuilder = IrBuilder.getInstance();
    /**
     * 因为函数内联是迭代进行的，所以有一个不动点设计
     */
    private boolean changed = false;
    /**
     * 函数和它调用的函数
     */
    private final HashMap<Function, ArrayList<Function>> callers = new HashMap<>();
    /**
     * 函数和调用它的函数
     */
    private final HashMap<Function, ArrayList<Function>> callees = new HashMap<>();

    @Override
    public void run()
    {
        changed = true;
        // 指示当前函数是否可以被内联，评价标准是内联函数不能递归，不能调用其他函数
        boolean canInlineOtherFunc;
        // 内联函数的集合
        ArrayList<Function> canBeInline = new ArrayList<>();
        while (changed)
        {
            changed = false;
            buildCallGraph();

            // 现在遍历函数是在遍历调用者
            for (Function callerFunc : irModule.getFunctionsArray())
            {
                canInlineOtherFunc = true;
                // 不是内建函数，而且不是 main
                if (!callerFunc.isBuiltin() && !callerFunc.getName().equals("@main"))
                {
                    // 遍历所有的被调用函数
                    for (Function calleeFunc : callers.get(callerFunc))
                    {
                        // 如果调用了其他非内建函数
                        if (!calleeFunc.isBuiltin())
                        {
                            canInlineOtherFunc = false;
                            break;
                        }
                    }
                    // 如果调用了自身
                    if (hasRecursion(callerFunc))
                    {
                        canInlineOtherFunc = false;
                    }

                    // 这里说明只能内联一次
                    if (canInlineOtherFunc)
                    {
                        canBeInline.add(callerFunc);
                    }
                }
            }

            // 进行一次内联
            for (Function inlineFunc : canBeInline)
            {
                inlineFunction(inlineFunc);
            }

            canBeInline.clear();
            // 重新建图
            buildCallGraph();
            // 清空函数
            removeUselessFunction();
        }

    }

    /**
     * 通过现有的调用图，来删除没有被调用的函数
     */
    private void removeUselessFunction()
    {
        // 去掉所有的没有被调用的函数(main)
        for (Function curFunc : irModule.getFunctionsArray())
        {
            if (!curFunc.isBuiltin())
            {
                if (!callees.containsKey(curFunc) && !curFunc.getName().equals("@main"))
                {
                    curFunc.eraseFromParent();
                }
            }
        }
//        // 缩减 phi
//        for (Function curFunc : irModule.getFunctionsArray())
//        {
//            if (!curFunc.isBuiltin())
//            {
//                for (BasicBlock bb : curFunc.getBasicBlocksArray())
//                {
//                    bb.reducePhi(true);
//                }
//            }
//        }
    }

    /**
     * Check if a function has callee to itself
     */
    private boolean hasRecursion(Function function)
    {
        return callers.get(function).contains(function);
    }

    /**
     * 通过遍历 irModule，获得所有的调用和被调用关系
     * 说白了新建一个调用关系的双向图
     */
    private void buildCallGraph()
    {
        callers.clear();
        callees.clear();

        for (Function curFunc : irModule.getFunctionsArray())
        {
            if (!curFunc.isBuiltin())
            {
                if (!callers.containsKey(curFunc))
                {
                    ArrayList<Function> arrayList = new ArrayList<>();
                    callers.put(curFunc, arrayList);
                }

                // Calculate all the call instruction in this function
                for (BasicBlock curBB : curFunc.getBasicBlocksArray())
                {
                    for (Instruction curInst : curBB.getInstructionsArray())
                    {
                        if (curInst instanceof Call)
                        {
                            Call curCall = (Call) curInst;
                            // 被调用的函数
                            Function callee = curCall.getFunction();
                            // 如果被调用的函数不是内建函数
                            if (!callee.isBuiltin())
                            {
                                // 新建数组
                                if (!callees.containsKey(callee))
                                {
                                    ArrayList<Function> arrayList = new ArrayList<>();
                                    callees.put(callee, arrayList);
                                }
                                if (!callers.get(curFunc).contains(callee))
                                {
                                    callers.get(curFunc).add(callee);
                                }
                                if (!callees.get(callee).contains(curFunc))
                                {
                                    callees.get(callee).add(curFunc);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void inlineFunction(Function inlineFunc)
    {
        // Spread this function in its Caller functions
        ArrayList<Call> callList = new ArrayList<>();
        // 有调用这个函数的函数
        if (callees.containsKey(inlineFunc) && callees.get(inlineFunc).size() != 0)
        {
            // 通过这里赋值为 true，已经进行了内联，所以还需要再分析一遍内联后的成果
            changed = true;
            // 通过遍历，获得所有调用 inlineFunc 的 call
            for (Function targetFunc : callees.get(inlineFunc))
            {
                for (BasicBlock curBB : targetFunc.getBasicBlocksArray())
                {
                    for (Instruction curInst : curBB.getInstructionsArray())
                    {
                        if (curInst instanceof Call)
                        {
                            Call curCall = (Call) curInst;
                            if (curCall.getFunction().getName().equals
                                    (inlineFunc.getName()))
                            {
                                callList.add(curCall);
                            }
                        }
                    }
                }
            }

            // 对所有的 call 进行替换
            for (Call curCall : callList)
            {
                Function target = curCall.getParent().getParent();
                replaceCall(curCall);
            }

            // 去掉调用关系
            for (Function targetFunc : callees.get(inlineFunc))
            {
                callers.get(targetFunc).remove(inlineFunc);
                callers.get(targetFunc).addAll(callers.get(inlineFunc));
            }

            callees.get(inlineFunc).clear();
        }
    }

    /**
     * 完成函数内联
     * @param call 调用指令
     */
    private void replaceCall(Call call)
    {
        Function calleeFunc = call.getFunction();
        Function curFunc = call.getParent().getParent();
        BasicBlock curBlock = call.getParent();
        ValueType retType = calleeFunc.getReturnType();

        // ======================== 这里是为了将 call 指令所在的块分割成两半 ==========================
        // 在当前块（也就是 call 在的那个块）之后再建一个块，用于存放 call 之后的指令
        BasicBlock nextBlock = irBuilder.buildBlockAfter(curFunc, curBlock);
        boolean backInst = false;
        // 遍历当前块
        for (Instruction curInst : curBlock.getInstructionsArray())
        {
            // 应该会到 call 后的指令
            if (!backInst && curInst.equals(call))
            {
                backInst = true;
                continue;
            }
            // 将 call 后的指令插入 nextBlock
            if (backInst)
            {
                curInst.eraseFromParent();
                nextBlock.insertTail(curInst);
                curInst.setParent(nextBlock);
            }
        }

        // 修改后继节点 phi 指令
        for (BasicBlock successor : curBlock.getSuccessors())
        {
            for (Instruction succInstr : successor.getInstructionsArray())
            {
                // 说明是 phi
                if (succInstr instanceof Phi && succInstr.getUsedValues().contains(curBlock))
                {
                    succInstr.getUsedValues().set(succInstr.getUsedValues().indexOf(curBlock), nextBlock);
                    nextBlock.addUser(succInstr);
                    curBlock.getUsers().remove(succInstr);
                }
            }
        }

        // nextBlock 的后继就是 curBlock 的后继块
        nextBlock.getSuccessors().addAll(curBlock.getSuccessors());
        // 修改后继的前驱
        for (BasicBlock succBlock : curBlock.getSuccessors())
        {
            ArrayList<BasicBlock> predBlocks = new ArrayList<>(succBlock.getPredecessors());
            for (BasicBlock preBlock : predBlocks)
            {
                if (preBlock.equals(curBlock))
                {
                    succBlock.getPredecessors().remove(curBlock);
                    succBlock.getPredecessors().add(nextBlock);
                }
            }
        }

        // 清空后继
        curBlock.getSuccessors().clear();
        // ======================== 分割结束 ==========================


        FunctionClone functionCloner = new FunctionClone();
        Function copyFunction = functionCloner.copyFunction(calleeFunc);

        // Then deal with the inlineFunction
        // 这里处理了函数的参数问题
        for (int i = 0; i < copyFunction.getNumArgs(); i++)
        {
            Value curArg = copyFunction.getArguments().get(i);
            // 实参
            Value callArg = call.getUsedValue(i + 1);

            // 如果实参是 int，那么就用实参代替形参
            if (callArg.getValueType() instanceof IntType)
            {
                curArg.replaceAllUsesWith(callArg);
            }
            // 应该是指针
            else
            {
                ArrayList<User> users = new ArrayList<>(curArg.getUsers());
                // 遍历实参的使用者
                for (User user : users)
                {
                    // 如果是 store，应该是把形参存入的 alloca
                    if (user instanceof Store)
                    {
                        Store store = (Store) user;
                        if (store.getUsedValue(1) instanceof Alloca)
                        {
                            Alloca allocaInst = (Alloca) store.getUsedValue(1);
                            allocaInst.eraseFromParent();
                            // 直接去掉所有的 load，然后用实参代替
                            for (User user1 : allocaInst.getUsers())
                            {
                                if (user1 instanceof Load)
                                {
                                    user1.replaceAllUsesWith(callArg);
                                }
                                ((Instruction) user1).eraseFromParent();
                            }
                            allocaInst.dropAllOperands();
                        }
                    }
                    else
                    {
                        curArg.replaceAllUsesWith(callArg);
                    }
                }
            }
        }

        // 让 curBlock 跳掉入口块
        Br toFunc = irBuilder.buildBr(curBlock, copyFunction.getHeadBlock());
        toFunc.eraseFromParent();
        curBlock.insertTail(toFunc);
        toFunc.setParent(curBlock);

        // Then add a Br at the last block of copyFunction(Branch to nextBlock)
        ArrayList<Ret> retList = new ArrayList<>();
        int predecessorNum = 0;
        // 收集所有的 ret 指令
        for (BasicBlock bb : copyFunction.getBasicBlocksArray())
        {
            for (Instruction inst : bb.getInstructionsArray())
            {
                if (inst instanceof Ret)
                {
                    retList.add((Ret) inst);
                    predecessorNum++;
                }
            }
        }

        // 如果是有返回值的，可以用 phi 来代替
        if (retType instanceof IntType)
        {
            Phi phi = irBuilder.buildPhi((IntType) retType, nextBlock, predecessorNum);
            for (Ret ret : retList)
            {
                phi.addIncoming(ret.getRetValue(), ret.getParent());

                ret.getParent().getSuccessors().remove(nextBlock);
                nextBlock.getPredecessors().remove(ret.getParent());

                irBuilder.buildBrBeforeInstr(ret.getParent(), nextBlock, ret);

                ret.dropAllOperands();
                ret.eraseFromParent();
            }
            call.replaceAllUsesWith(phi);
        }
        else if (retType instanceof VoidType)
        {
            for (Ret retInst : retList)
            {
                retInst.getParent().getSuccessors().remove(nextBlock);
                nextBlock.getPredecessors().remove(retInst.getParent());

                irBuilder.buildBrBeforeInstr(retInst.getParent(), nextBlock, retInst);

                retInst.dropAllOperands();
                retInst.eraseFromParent();
            }
        }

        // 清除重新插入
        for (BasicBlock block : copyFunction.getBasicBlocksArray())
        {
            block.eraseFromParent();
            curFunc.insertBefore(block, nextBlock);
            block.setParent(curFunc);
        }

        // 去掉 call
        call.dropAllOperands();
        call.eraseFromParent();
        nextBlock.reducePhi(true);
    }
}
