package middle.symbol;

import check.CheckDataType;
import parser.cst.*;

import java.util.ArrayList;


public class VarInfo extends SymbolInfo
{
    private final String name;
    private CSTNode dim1 = NumberNode.ZERO;
    private CSTNode dim2 = NumberNode.ZERO;
    public VarInfo(VarDefNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(0);
        this.name = identNode.getContent();

        for (CSTNode child : ctx.getChildren())
        {
            if (child instanceof ConstExpNode)
            {
                if (dim1 == NumberNode.ZERO)
                {
                    dim1 = child;
                }
                else if (dim2 == NumberNode.ZERO)
                {
                    dim2 = child;
                }
            }
        }
    }

    public VarInfo(FuncFParamNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(1);
        this.name = identNode.getContent();
        ArrayList<CSTNode> children = ctx.getChildren();

        // 说明此时参数不是一个单变量
        if (children.size() > 2)
        {
            // 第一维设为无穷
            dim1 = NumberNode.INF;
            // 说明存在第二维
            if (children.size() > 4)
            {
                dim2 = children.get(5);
            }
        }
    }

    public CheckDataType getDataType()
    {
        if (dim1 == NumberNode.ZERO && dim2 == NumberNode.ZERO)
        {
            return CheckDataType.INT;
        }
        else if (dim2 == NumberNode.ZERO)
        {
            return CheckDataType.DIM1;
        }
        else
        {
            return CheckDataType.DIM2;
        }
    }
}
