package lexer.token;

public class IntConst extends Token
{
    public static final String PATTERN = "([1-9][0-9]*)|(0)";

    public IntConst(int line, String content)
    {
        super(line, content);
        type = TokenType.INTCON;
    }
}
