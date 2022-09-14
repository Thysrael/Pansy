package lexer.token;

public class Comment extends Token
{
    private final static String SINGLE_COMMENT_PATTERN = "//.*";
    public final static String PATTERN = SINGLE_COMMENT_PATTERN;

    public Comment(int line, String content)
    {
        super(line, content);
        this.type = content.charAt(1) == '/' ? TokenType.SINGLE_COMMENT : TokenType.MULTI_COMMENT;
    }

    public static int consumeSingleComment(String src, int startIndex)
    {
        int endIndex = startIndex;
        if (endIndex + 1 < src.length() &&
                src.charAt(endIndex) == '/' &&
                src.charAt(endIndex + 1) == '/')
        {
            endIndex += 2;
            while (endIndex < src.length())
            {
                if (src.charAt(endIndex) == '\n')
                {
                    endIndex++;
                    return endIndex;
                }
                endIndex++;
            }
            return endIndex;
        }

        return startIndex;
    }

    public static int consumeMultiComment(String src, int startIndex)
    {
        int endIndex = startIndex;
        if (endIndex + 1 < src.length() &&
            src.charAt(endIndex) == '/' &&
            src.charAt(endIndex + 1) == '*')
        {
            endIndex += 2;
            while (endIndex < src.length())
            {
                if (endIndex + 1 < src.length() &&
                        src.charAt(endIndex) == '*' &&
                        src.charAt(endIndex + 1) == '/')
                {
                    endIndex += 2;
                    return endIndex;
                }
                endIndex++;
            }
        }

        return startIndex;
    }
}
