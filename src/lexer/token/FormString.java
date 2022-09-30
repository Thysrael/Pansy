package lexer.token;

public class FormString extends Token
{
    // 32, 33, 40-126, 其中 92 "\" 必须和 "n" 搭配出现
    public static final String PATTERN = "\"([\\x20\\x21\\x28-\\x5b\\x5d-\\x7e]|(\\\\n)|(%d))*?\"";

    public FormString(int line, String content)
    {
        super(line, content);
        type = SyntaxType.STRCON;
    }

    public static int consumeFormString(String src, int startIndex)
    {
        int endIndex = startIndex;
        if (src.charAt(startIndex) == '\"')
        {
            endIndex++;
            while (endIndex < src.length())
            {
                char c = src.charAt(endIndex);
                if (c == '\"')
                {
                    endIndex++;
                    return endIndex;
                }
                else if (c == 32 || c == 33 || (c >= 40 && c <= 91) || (c >= 93 && c <= 126))
                {
                    endIndex++;
                }
                else if (endIndex + 1 < src.length() &&
                        ((c == '%' && src.charAt(endIndex + 1) == 'd') || (c == '\\' && src.charAt(endIndex + 1) == 'n')))
                {
                    endIndex++;
                }
                else
                {
                    return startIndex;
                }
            }
        }

        return startIndex;
    }
}
