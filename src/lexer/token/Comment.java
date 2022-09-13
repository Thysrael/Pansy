package lexer.token;

public class Comment extends Token
{
    private final static String SINGLE_COMMENT_PATTERN = "//.*";
    private final static String MULTI_COMMENT_PATTERN = "/\\*(.|\\n|\\r)*?\\*/";
    public final static String PATTERN = SINGLE_COMMENT_PATTERN + "|" + MULTI_COMMENT_PATTERN;

    public Comment(int line, String content)
    {
        super(line, content);
        this.type = content.charAt(1) == '/' ? TokenType.SINGLE_COMMENT : TokenType.MULTI_COMMENT;
    }
}
