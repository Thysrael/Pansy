package exception;

public class LexException extends Exception
{
    private final int line;

    public LexException(int line)
    {
        this.line = line;
    }

    @Override
    public String toString()
    {
        return "Lexer Exception in line " + line + ".\n";
    }
}
