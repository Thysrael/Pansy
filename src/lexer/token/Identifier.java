package lexer.token;

public class Identifier extends Token
{
    public static final String PATTERN = "[_a-zA-Z][_a-zA-Z0-9]*";

    public Identifier(int line, String content)
    {
        super(line, content);
        type = TokenType.IDENFR;
    }
}
