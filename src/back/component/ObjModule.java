package back.component;

import java.util.ArrayList;

public class ObjModule
{
    // 管理所有的函数
    private final ArrayList<ObjFunction> functions = new ArrayList<>();
    // 管理所有的全局变量
    private final ArrayList<ObjGlobalVariable> globalVariables = new ArrayList<>();

    public void addGlobalVariable(ObjGlobalVariable objGlobalVariable)
    {
        globalVariables.add(objGlobalVariable);
    }

    public void addFunction(ObjFunction function)
    {
        functions.add(function);
    }

    public ArrayList<ObjFunction> getFunctions()
    {
        return functions;
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
        // data segment
        moduleSb.append(".data\n");
        for (ObjGlobalVariable globalVariable : globalVariables)
        {
            moduleSb.append(globalVariable).append("\n");
        }
        // text segment
        // 首先先跳到 main 函数执行
        // TODO 太浪费了，不要 jal 直接宏替换
        moduleSb.append(".text\n");
        moduleSb.append("_start:\n");
        moduleSb.append("\tjal main\n");
        moduleSb.append("\tli\t$v0,\t10\n");
        moduleSb.append("\tsyscall\n\n");
        // putstr
        moduleSb.append("putstr:\n");
        moduleSb.append("\tli\t$v0,\t4\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append("\tjr\t$ra\n\n");
        // putint
        moduleSb.append("putint:\n");
        moduleSb.append("\tli\t$v0,\t1\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append("\tjr\t$ra\n\n");
        // getint
        moduleSb.append("getint:\n");
        moduleSb.append("\tli\t$v0,\t5\n");
        moduleSb.append("\tsyscall\n");
        moduleSb.append("\tjr\t$ra\n\n");
        // 非内建函数打印
        for (ObjFunction function : functions)
        {
            if (!function.isBuiltin())
            {
                moduleSb.append(function);
            }
        }

        return moduleSb.toString();
    }
}