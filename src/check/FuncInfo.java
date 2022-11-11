package check;

import parser.cst.FuncDefNode;
import parser.cst.MainFuncDefNode;
import parser.cst.TokenNode;

import java.util.ArrayList;

/**
 * 包括两种
 * 普通函数
 * 主函数
 */
public class FuncInfo extends SymbolInfo
{
    private final String name;
    private CheckDataType returnType;
    private final ArrayList<VarInfo> parameters;

    /**
     * 构造普通函数
     * 不过这里不加入参数
     * @param ctx 函数定义
     */
    public FuncInfo(FuncDefNode ctx)
    {
        TokenNode identNode = ((TokenNode) ctx.getChildren().get(1));
        this.name = identNode.getContent();
        this.parameters = new ArrayList<>();
    }

    /**
     * 构造主函数
     * @param ctx 主函数定义
     */
    public FuncInfo(MainFuncDefNode ctx)
    {
        this.name = "main";
        this.parameters = new ArrayList<>();
    }

    public void setReturnType(CheckDataType returnType)
    {
        this.returnType = returnType;
    }

    public CheckDataType getReturnType()
    {
        return returnType;
    }

    public void addParameter(VarInfo parameter)
    {
        parameters.add(parameter);
    }

    public ArrayList<VarInfo> getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType).append(" ").append(name).append("(");
        for (VarInfo parameter : parameters)
        {
            sb.append(parameter).append(", ");
        }
        return sb.substring(0, sb.length() - 2) + ")";
    }
}
