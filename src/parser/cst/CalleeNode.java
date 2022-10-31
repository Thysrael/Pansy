package parser.cst;

import check.CheckDataType;
import check.ErrorType;
import check.PansyException;
import ir.types.DataType;
import ir.types.IntType;
import ir.values.Function;
import ir.values.Value;
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
    private TokenNode ident = null;
    private FuncRParamsNode funcRParams = null;
    private final ArrayList<ExpNode> arguments = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (children.size() == 1)
        {
            ident = (TokenNode) child;
        }
        else if (child instanceof FuncRParamsNode)
        {
            funcRParams = (FuncRParamsNode) child;
        }
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
    public CheckDataType getDataType(SymbolTable symbolTable)
    {
        String callFuncName = getCallFuncName();

        try
        {
            FuncInfo funcInfo = symbolTable.getFuncInfo(callFuncName);
            return funcInfo.getReturnType();
        }
        catch (PansyException e)
        {
            return CheckDataType.VOID;
        }
    }

    @Override
    public void buildIr()
    {
        // 找到函数
        Function func = (Function) irSymbolTable.find(ident.getContent());
        // 实参表
        ArrayList<Value> paramList = new ArrayList<>();

        if (funcRParams != null)
        {
            ArrayList<ExpNode> params = funcRParams.getParams();
            ArrayList<DataType> formalArgs = func.getValueType().getFormalArgs();

            for (int i = 0; i <params.size(); i++)
            {
                ExpNode param = params.get(i);
                DataType argType = formalArgs.get(i);
                paramDontNeedLoadDown = !(argType instanceof IntType);
                param.buildIr();
                paramDontNeedLoadDown = false;
                paramList.add(valueUp);
            }
        }
        valueUp = irBuilder.buildCall(curBlock, func, paramList);
    }
}
