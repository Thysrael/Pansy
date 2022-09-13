package lexer.token;

import java.util.HashMap;

public class Reserved extends Token
{
    public static final String PATTERN;
    private static final HashMap<String, TokenType> contentToType = new HashMap<>();

    static
    {
        contentToType.put("main", TokenType.MAINTK);
        contentToType.put("const", TokenType.CONSTTK);
        contentToType.put("int", TokenType.INTTK);
        contentToType.put("break", TokenType.BREAKTK);
        contentToType.put("continue", TokenType.CONTINUETK);
        contentToType.put("if", TokenType.IFTK);
        contentToType.put("else", TokenType.ELSETK);
        contentToType.put("while", TokenType.WHILETK);
        contentToType.put("getint", TokenType.GETINTTK);
        contentToType.put("printf", TokenType.PRINTFTK);
        contentToType.put("return", TokenType.RETURNTK);
        contentToType.put("void", TokenType.VOIDTK);

        // 注意这里要按照逆序排列，才能完成匹配
        PATTERN = contentToType.keySet().stream().reduce((s1, s2) -> s1 + "(?![a-zA-Z0-9_])|" + s2).get() + "(?![a-zA-Z0-9_])";
    }

    public Reserved(int line, String content)
    {
        super(line, content);
        type = contentToType.get(content);
    }
}
