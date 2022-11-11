package parser.cst;

import check.ErrorType;
import check.PansyException;
import lexer.token.SyntaxType;
import lexer.token.Token;
import check.SymbolTable;


import static check.ErrorType.ILLEGAL_SYMBOL;

public class TokenNode extends CSTNode
{
    private final Token token;
    private final boolean miss;

    public TokenNode(Token token)
    {
        this.token = token;
        this.miss = false;
    }

    public TokenNode(Token token, boolean miss)
    {
        this.token = token;
        this.miss = miss;
    }

    /**
     * 用于缺失的时候产生具体的 error
     * 包括三种缺失错误
     * 字符串错误
     * 名字错误本来就不行，这是因为名字错误包括未定义和重复定义两种，没有办法确定要使用哪种
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        // 检测缺失情况
        if (token.isSameType(SyntaxType.SEMICN))
        {
            if (miss)
            {
                errors.add(new PansyException(ErrorType.MISS_SEMICOLON, token.getLine()));
            }

        }
        else if (token.isSameType(SyntaxType.RBRACK))
        {
            if (miss)
            {
                errors.add(new PansyException(ErrorType.MISS_RBRACK, token.getLine()));
            }
        }
        else if (token.isSameType(SyntaxType.RPARENT))
        {
            if (miss)
            {
                errors.add(new PansyException(ErrorType.MISS_RPARENT, token.getLine()));
            }
        }
        // 检测是否是合法字符串
        else if (token.isSameType(SyntaxType.STRCON))
        {
            String content = token.getContent();
            // 不算两个双引号
            for (int i = 1; i < content.length() - 1; ++i)
            {
                int ascii = content.charAt(i);
                if (ascii == 32 || ascii == 33 || ascii >= 40 && ascii <= 126)
                {
                    if (ascii == 92 && content.charAt(i + 1) != 'n')
                    {
                        errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                        return;
                    }
                }
                else if (ascii == 37)
                {
                    if (content.charAt(i + 1) != 'd')
                    {
                        errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                        return;
                    }

                }
                else
                {
                    errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                    return;
                }
            }
        }
    }

    public int getLine()
    {
        return token.getLine();
    }

    public String getContent()
    {
        return token.getContent();
    }

    public boolean isSameType(SyntaxType type)
    {
        return token.isSameType(type);
    }

    public Token getToken()
    {
        return token;
    }

    @Override
    public String toString()
    {
        return getContent();
    }
}
