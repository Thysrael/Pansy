package parser.cst;

import lexer.token.Token;

public class TokenNode extends CSTNode
{
    private Token token;

    public TokenNode(Token token)
    {
        this.token = token;
    }
}
