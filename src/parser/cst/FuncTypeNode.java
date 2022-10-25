package parser.cst;

import check.DataType;
import lexer.token.SyntaxType;

public class FuncTypeNode extends CSTNode
{
    private DataType dataType;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode)
        {
            if (((TokenNode) child).isSameType(SyntaxType.VOIDTK))
            {
                dataType = DataType.VOID;
            }
            else if (((TokenNode) child).isSameType(SyntaxType.INTTK))
            {
                dataType = DataType.INT;
            }
        }
    }

    public DataType getReturnType()
    {
        return dataType;
    }
}
