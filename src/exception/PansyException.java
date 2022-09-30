package exception;

import lexer.token.SyntaxType;

public class PansyException extends Exception
{
    protected final ErrorType type;
    protected final int line;

    public PansyException(ErrorType type, int line)
    {
        this.type = type;
        this.line = line;
    }

    public PansyException(SyntaxType type, int line)
    {
        this.type = null;
        this.line = line;
    }

    public ErrorType getType()
    {
        return type;
    }
}
