package parser.cst;

import check.DataType;
import check.ErrorType;
import check.PansyException;
import middle.symbol.FuncInfo;
import middle.symbol.SymbolTable;
import middle.symbol.VarInfo;

import java.util.ArrayList;

/**
 * Callee
 *     : IDENFR L_PAREN FuncRParams? R_PAREN
 *     ;
 */
public class CalleeNode extends CSTNode
{
    private final ArrayList<ExpNode> arguments = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
    }

    /**
     * 检验函数参数个数不匹配
     * 检验函数类型不匹配
     * 检验函数未定义
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode identNode = ((TokenNode) children.get(0));
        String ident = identNode.getContent();

        // 获得实参表
        if (children.get(2) instanceof FuncRParamsNode)
        {
            ArrayList<CSTNode> tmp = children.get(2).getChildren();
            for (CSTNode cstNode : tmp)
            {
                if (cstNode instanceof ExpNode)
                {
                    arguments.add((ExpNode) cstNode);
                }
            }
        }

        try
        {
            FuncInfo funcInfo = symbolTable.getFuncInfo(ident);
            ArrayList<VarInfo> parameters = funcInfo.getParameters();
            if (parameters.size() != arguments.size())
            {
                errors.add(new PansyException(ErrorType.ARG_NUM_MISMATCH, identNode.getLine()));
            }
            else
            {
                for (int i = 0; i < arguments.size(); i++)
                {
                    VarInfo parameter = parameters.get(i);
                    CSTNode argument = arguments.get(i);
                    // System.out.println(parameter.getDataType());
                    // System.out.println(argument.getDataType(symbolTable));
                    if (!parameter.getDataType().equals(argument.getDataType(symbolTable)))
                    {
                        errors.add(new PansyException(ErrorType.ARG_TYPE_MISMATCH, identNode.getLine()));
                    }
                }
            }
        }
        // 未定义的报错
        catch (PansyException e)
        {
            errors.add(new PansyException(e.getType(), identNode.getLine()));
        }

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    private String getCallFuncName()
    {
        TokenNode identNode = ((TokenNode) children.get(0));

        return identNode.getContent();
    }

    /**
     * 返回函数的类型
     * @param symbolTable 符号表
     * @return 函数的数据类型就是返回类型，不过似乎没有啥用
     */
    @Override
    public DataType getDataType(SymbolTable symbolTable)
    {
        String callFuncName = getCallFuncName();

        try
        {
            FuncInfo funcInfo = symbolTable.getFuncInfo(callFuncName);
            return funcInfo.getReturnType();
        }
        catch (PansyException e)
        {
            return DataType.VOID;
        }
    }
}
