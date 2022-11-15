package back.component;

import java.util.ArrayList;

public class ObjModule
{
    // 管理所有的函数
    private final ArrayList<ObjFunction> functions = new ArrayList<>();
    // 管理所有的全局变量
    private final ArrayList<ObjGlobalVariable> globalVariables = new ArrayList<>();
    // 主函数
    private ObjFunction mainFunction;

    public void addGlobalVariable(ObjGlobalVariable objGlobalVariable)
    {
        globalVariables.add(objGlobalVariable);
    }

    public void addFunction(ObjFunction function)
    {
        if (function.getName().equals("main"))
        {
            mainFunction = function;
        }
        functions.add(function);
    }

    public ArrayList<ObjFunction> getFunctions()
    {
        return functions;
    }

    public ArrayList<ObjFunction> getNoBuiltinFunctions()
    {
        ArrayList<ObjFunction> noBuiltins = new ArrayList<>();
        for (ObjFunction function : functions)
        {
            if (!function.isBuiltin())
            {
                noBuiltins.add(function);
            }
        }
        return noBuiltins;
    }

    /**
     * 打印：
     * Pansy 信息
     * 数据段各个全局变量
     * 跳转到 main 的语句和结束（_start）
     * 两个内建函数的打印
     * 非内建函数打印
     * @return 模块汇编
     */
    @Override
    public String toString()
    {
        StringBuilder moduleSb = new StringBuilder("");
        moduleSb.append("# Pansy Say \"Hi~\" to you!\n");
        // macro
        // putstr
        moduleSb.append(".macro putstr\n");
        moduleSb.append("\tli\t$v0,\t4\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append(".end_macro\n\n");
        // putint
        moduleSb.append(".macro putint\n");
        moduleSb.append("\tli\t$v0,\t1\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append(".end_macro\n\n");
        // getint
        moduleSb.append(".macro getint\n");
        moduleSb.append("\tli\t$v0,\t5\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append(".end_macro\n\n");
        // data segment
        moduleSb.append(".data\n");
        for (ObjGlobalVariable globalVariable : globalVariables)
        {
            moduleSb.append(globalVariable).append("\n");
        }
        // text segment
        // 首先先跳到 main 函数执行
        moduleSb.append(".text\n");
        // 打印函数
        // 首先打印主函数
        moduleSb.append(mainFunction);
        // 打印其他函数
        for (ObjFunction function : functions)
        {
            if (!function.isBuiltin() && function != mainFunction)
            {
                moduleSb.append(function).append("\n");
            }
        }

        return moduleSb.toString();
    }
}