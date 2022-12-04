package pass.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.instructions.Call;
import ir.values.instructions.Instruction;
import ir.values.instructions.Store;
import pass.Pass;

import java.util.HashMap;

public class SideEffectAnalysis implements Pass
{
    /**
     * 这个表示需要被处理的 func
     */
    private final HashMap<Function, Boolean> func2processed = new HashMap<>();
    /**
     * 应该就是一个简单的函数遍历记录
     */
    private final HashMap<Function, Boolean> func2visited = new HashMap<>();

    @Override
    public void run()
    {
        initCallGraph();
        Function main = Module.getInstance().getFunction("@main");
        process(main);
        main.setHasSideEffect(true);
    }

    /**
     * 初始化一个调用关系图
     */
    private void initCallGraph()
    {
        // 遍历所有的函数
        for (Function function : Module.getInstance().getFunctionsArray())
        {
            // 进行一些清零操作
            function.clearCallees();
            function.setHasSideEffect(false);
            func2visited.put(function, false);

            // 如果一个函数是内建函数，那么就是有副作用的
            if (function.isBuiltin())
            {
                function.setHasSideEffect(true);
                func2processed.put(function, true);
                continue;
            }

            // processed 可能是另一种形式的 visited
            func2processed.put(function, false);

            for (BasicBlock block : function.getBasicBlocksArray())
            {
                for (Instruction instruction : block.getInstructionsArray())
                {
                    // 遍历 call 指令
                    if (instruction instanceof Call)
                    {
                        Function calledFunction = ((Call) instruction).getFunction();
                        // 如果不是内联函数，就加入 callee
                        if (!calledFunction.isBuiltin())
                        {
                            function.addCallee(calledFunction);
                        }
                        // 认为调用内联函数是有影响的
                        else
                        {
                            function.setHasSideEffect(true);
                            func2processed.put(function, true);
                        }
                    }

                    // 如果是 store 那么就是有影响的
                    if (instruction instanceof Store)
                    {
                        function.setHasSideEffect(true);
                        func2processed.put(function, true);
                    }
                }
            }
        }
    }

    private boolean process(Function function)
    {
        boolean hasSideEffect = false;
        func2visited.put(function, true);

        // 如果这个函数已经被处理了
        if (func2processed.get(function))
        {
            hasSideEffect = function.hasSideEffect();
            // 遍历它的调用者
            for (Function callee : function.getCallees())
            {
                if (!func2processed.get(callee) && !func2visited.get(callee))
                {
                    process(callee);
                }
            }
        }
        // 还没有被处理
        else
        {
            for (Function callee : function.getCallees())
            {
                if (callee.hasSideEffect() ||
                        (!func2processed.get(callee) && !func2visited.get(callee) && process(callee)))
                {
                    hasSideEffect = true;
                }
            }
        }

        func2visited.put(function, false);
        function.setHasSideEffect(hasSideEffect);
        func2processed.put(function, true);
        return hasSideEffect;
    }
}
