package lexer.token;

public abstract class Token
{
    /**
     * 所在行
     */
    private final int line;
    /**
     * token 内容
     */
    private final String content;
    /**
     * token 的类型
     */
    protected SyntaxType type;

    protected Token(int line, String content)
    {
        this.line = line;
        this.content = content;
    }

    public SyntaxType getType()
    {
        return type;
    }

    public int getLine()
    {
        return line;
    }

    public String getContent()
    {
        return content;
    }

    public boolean isSameType(SyntaxType type)
    {
        return this.type.equals(type);
    }

    @Override
    public String toString()
    {
        return type.toString() + " " + content;
    }
}
