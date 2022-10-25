package parser.cst;

import check.Checker;
import check.DataType;
import check.PansyException;
import middle.symbol.FuncInfo;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

public abstract class CSTNode
{
    /**
     * 用来存储语义分析中产生的错误
     */
    protected static final ArrayList<PansyException> errors = Checker.errors;
    /**
     * 用来存储检测日志
     */
    protected static final ArrayList<String> checkLog = Checker.checkLog;
    /**
     * 当前函数信息
     */
    protected static FuncInfo curFuncInfo = null;
    /**
     * 是否在循环中
     */
    protected static int inLoop = 0;
    /**
     * 是否在写 LVal
     */
    protected static boolean isWriteLVal = false;


    protected final ArrayList<CSTNode> children = new ArrayList<>();

    public void addChild(CSTNode child)
    {
        children.add(child);
    }

    public ArrayList<CSTNode> getChildren()
    {
        return children;
    }

    protected void addCheckLog()
    {
        checkLog.add("[" + this.getClass() + "]");
    }

    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    /**
     * 获得数据类型
     * 会遍历所有的孩子节点，然后找到最高的
     * 如果没有的话，那么就会变成 VOID
     * 合情合理有没有
     * @param symbolTable 符号表
     * @return 数据类型
     */
    public DataType getDataType(SymbolTable symbolTable)
    {
        DataType type = DataType.VOID;

        for (CSTNode child : children)
        {
            DataType childType = child.getDataType(symbolTable);
            if (childType.compareTo(type) > 0)
            {
                type = childType;
            }
        }

        return type;
    }
}
