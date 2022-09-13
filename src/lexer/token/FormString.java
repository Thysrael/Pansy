package lexer.token;

public class FormString extends Token
{
    // 32, 33, 40-126, 其中 92 "\" 必须和 "n" 搭配出现
    public static final String PATTERN = "\"([\\x20\\x21\\x28-\\x5b\\x5d-\\x7e]|(\\\\n)|(%d))*?\"";

    public FormString(int line, String content)
    {
        super(line, content);
        type = TokenType.STRCON;
    }
}
