package middle.symbol;

import check.DataType;
import parser.cst.FuncDefNode;
import parser.cst.MainFuncDefNode;
import parser.cst.TokenNode;

import java.util.ArrayList;

public class FuncInfo extends SymbolInfo
{
    private final String name;
    private DataType dataType;

    private final ArrayList<VarInfo> parameters;
    public FuncInfo(FuncDefNode ctx)
    {
        TokenNode identNode = ((TokenNode) ctx.getChildren().get(1));
        this.name = identNode.getContent();
        this.parameters = new ArrayList<>();
    }

    public FuncInfo(MainFuncDefNode ctx)
    {
        this.name = "main";
        this.parameters = new ArrayList<>();
    }

    public void setReturnType(DataType dataType)
    {
        this.dataType = dataType;
    }

    public DataType getReturnType()
    {
        return dataType;
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
        return name + " " + dataType + " " + parameters;
    }
}
