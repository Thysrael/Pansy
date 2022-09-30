package lexer.token;

public class EOFToken extends Token
{
    public EOFToken(int line, String content)
    {
        super(line, content);
        this.type = SyntaxType.EOF;
    }
}
