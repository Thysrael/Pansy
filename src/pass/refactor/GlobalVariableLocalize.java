package pass.refactor;

import ir.IrBuilder;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.*;
import ir.values.Module;
import ir.values.instructions.Alloca;
import ir.values.instructions.Call;
import ir.values.instructions.Instruction;
import pass.Pass;
import util.MyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GlobalVariableLocalize implements Pass
{
    /**
     * 记录的是全局变量和调用它的函数列表
     */
    private final HashMap<GlobalVariable, HashSet<Function>> functionUsers = new HashMap<>();
    private final Module irModule = Module.getInstance();
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
        analyzeGlobalUse();
        buildCallGraph();
        localize();
    }

    private void analyzeGlobalUse()
    {
        // 遍历所有的全局向量
        for (MyList.MyNode<GlobalVariable> gvNode : irModule.getGlobalVariables())
        {
            GlobalVariable globalVariable = gvNode.getVal();
            // 遍历所有的使用者
            for (User user : globalVariable.getUsers())
            {
                // 如果使用者是一条指令（应该是显然的）
                if (user instanceof Instruction)
                {
                    Instruction userInst = (Instruction) user;
                    Function userFunc = userInst.getParent().getParent();
                    if (!functionUsers.containsKey(globalVariable))
                    {
                        functionUsers.put(globalVariable, new HashSet<>());
                    }

                    if (!userFunc.equals(Function.LOOP_TRASH))
                    {
                        functionUsers.get(globalVariable).add(userFunc);
                    }
                }
            }
        }
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

    private void localize()
    {
        for (MyList.MyNode<GlobalVariable> gvNode : irModule.getGlobalVariables())
        {
            GlobalVariable curGlobal = gvNode.getVal();

            // 如果没有任何一个函数使用这个全局变量
            if (!functionUsers.containsKey(curGlobal) || functionUsers.get(curGlobal).isEmpty())
            {
                gvNode.removeSelf();
                continue;
            }

            // Only used by one function
            if (functionUsers.containsKey(curGlobal) && functionUsers.get(curGlobal).size() == 1)
            {
                // 获得这个函数
                Function targetFunc = functionUsers.get(curGlobal).iterator().next();
                // 插入头块
                BasicBlock entryBlock = targetFunc.getHeadBlock();

                // 如果是一个目标函数调用其他函数了，也不行
                if (callees.containsKey(targetFunc) || !callees.getOrDefault(targetFunc, new ArrayList<>()).isEmpty())
                {
                    continue;
                }

                ValueType globalType = ((PointerType) curGlobal.getValueType()).getPointeeType();
                if (globalType instanceof IntType)
                {
                    // 建立一个 alloca
                    Alloca alloca = IrBuilder.getInstance().buildAlloca(globalType, entryBlock);
                    for (Instruction beforeInst : entryBlock.getInstructionsArray())
                    {
                        // 建立一个 store
                        if (!(beforeInst instanceof Alloca))
                        {
                            IrBuilder.getInstance().buildStoreBeforeInstr(entryBlock, curGlobal.getInitVal(), alloca, beforeInst);
                            break;
                        }
                    }
                    // 用 alloca 代替
                    curGlobal.replaceAllUsesWith(alloca);
                    gvNode.removeSelf();
                }
            }
        }
    }

}
