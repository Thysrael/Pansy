package parser.cst;

import check.CheckDataType;
import ir.types.DataType;
import ir.types.IntType;
import ir.types.VoidType;
import lexer.token.SyntaxType;

public class FuncTypeNode extends CSTNode
{
    private CheckDataType checkDataType;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode)
        {
            if (((TokenNode) child).isSameType(SyntaxType.VOIDTK))
            {
                checkDataType = CheckDataType.VOID;
            }
            else if (((TokenNode) child).isSameType(SyntaxType.INTTK))
            {
                checkDataType = CheckDataType.INT;
            }
        }
    }

    public CheckDataType getCheckReturnType()
    {
        return checkDataType;
    }

    public DataType getReturnType()
    {
        CSTNode child = children.get(0);
        if (child instanceof TokenNode)
        {
            if (((TokenNode) child).isSameType(SyntaxType.VOIDTK))
            {
                return new VoidType();
            }
            else if (((TokenNode) child).isSameType(SyntaxType.INTTK))
            {
                return new IntType(32);
            }
        }
        return new VoidType();
    }
}
