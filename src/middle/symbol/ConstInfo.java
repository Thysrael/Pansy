package middle.symbol;

import check.DataType;
import parser.cst.*;

// TODO 这里必须重构
public class ConstInfo extends SymbolInfo
{
    private final String name;
    private CSTNode dim1 = NumberNode.ZERO;
    private CSTNode dim2 = NumberNode.ZERO;

    public ConstInfo(ConstDefNode ctx)
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

    public DataType getDataType()
    {
        if (dim1 == NumberNode.ZERO && dim2 == NumberNode.ZERO)
        {
            return DataType.INT;
        }
        else if (dim2 == NumberNode.ZERO)
        {
            return DataType.DIM1;
        }
        else
        {
            return DataType.DIM2;
        }
    }

    public String getName()
    {
        return name;
    }
}
