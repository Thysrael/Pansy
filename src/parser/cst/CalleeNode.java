package parser.cst;

import check.CheckDataType;
import check.ErrorType;
import check.PansyException;
import ir.types.DataType;
import ir.types.IntType;
import ir.values.Function;
import ir.values.Value;
import check.FuncInfo;
import check.SymbolTable;
import check.VarInfo;

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

        String funcName = ident.getContent();

        // 获得实参表
        if (funcRParams != null)
        {
            arguments.addAll(funcRParams.getParams());
        }

        try
        {
            // 获得函数信息
            FuncInfo funcInfo = symbolTable.getFuncInfo(funcName);
            ArrayList<VarInfo> parameters = funcInfo.getParameters();
            // 参数的个数不匹配
            if (parameters.size() != arguments.size())
            {
                errors.add(new PansyException(ErrorType.ARG_NUM_MISMATCH, ident.getLine()));
            }
            else
            {
                // 比较函数的类型
                for (int i = 0; i < arguments.size(); i++)
                {
                    VarInfo parameter = parameters.get(i);
                    CSTNode argument = arguments.get(i);
                    if (!parameter.getCheckDataType().equals(argument.getDataType(symbolTable)))
                    {
                        errors.add(new PansyException(ErrorType.ARG_TYPE_MISMATCH, ident.getLine()));
                    }
                }
            }
        }
        // 未定义的报错
        catch (PansyException e)
        {
            errors.add(new PansyException(e.getType(), ident.getLine()));
        }
        // 主要是为了检测右小括号缺失
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    /**
     * 返回函数的类型
     * @param symbolTable 符号表
     * @return 函数的数据类型就是返回类型，不过似乎没有啥用
     */
    @Override
    public CheckDataType getDataType(SymbolTable symbolTable)
    {
        String callFuncName = ident.getContent();
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

    /**
     * 将实参都解析出来，然后调用
     * 在解析实参的时候，需要搭配形参信息，来确定实参是否加载
     */
    @Override
    public void buildIr()
    {
        // 找到函数
        Function func = (Function) irSymbolTable.find(ident.getContent());
        // 实参表
        ArrayList<Value> argList = new ArrayList<>();
        // 如果有实参
        if (funcRParams != null)
        {
            ArrayList<ExpNode> params = funcRParams.getParams();
            ArrayList<DataType> formalArgs = func.getValueType().getFormalArgs();

            for (int i = 0; i <params.size(); i++)
            {
                ExpNode param = params.get(i);
                DataType argType = formalArgs.get(i);
                // 如果传参的是一个指针，那么就不需要加载
                paramDontNeedLoadDown = !(argType instanceof IntType);
                param.buildIr();
                paramDontNeedLoadDown = false;
                argList.add(valueUp);
            }
        }
        valueUp = irBuilder.buildCall(curBlock, func, argList);
    }
}
