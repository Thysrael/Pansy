package parser.cst;

import lexer.token.SyntaxType;

/**
 * UnaryOp
 *     : PLUS
 *     | MINUS
 *     | NOT
 */
public class UnaryOpNode extends CSTNode
{
    public boolean isPlus()
    {
        return ((TokenNode) children.get(0)).isSameType(SyntaxType.PLUS);
    }

    public boolean isMinus()
    {
        return ((TokenNode) children.get(0)).isSameType(SyntaxType.MINU);
    }

    public boolean isNot()
    {
        return ((TokenNode) children.get(0)).isSameType(SyntaxType.NOT);
    }
}
