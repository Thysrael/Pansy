package lexer.token;

import java.util.HashMap;

public class Reserved extends Token
{
    public static final String PATTERN;
    private static final HashMap<String, SyntaxType> contentToType = new HashMap<>();

    static
    {
        contentToType.put("main", SyntaxType.MAINTK);
        contentToType.put("const", SyntaxType.CONSTTK);
        contentToType.put("int", SyntaxType.INTTK);
        contentToType.put("break", SyntaxType.BREAKTK);
        contentToType.put("continue", SyntaxType.CONTINUETK);
        contentToType.put("if", SyntaxType.IFTK);
        contentToType.put("else", SyntaxType.ELSETK);
        contentToType.put("while", SyntaxType.WHILETK);
        contentToType.put("getint", SyntaxType.GETINTTK);
        contentToType.put("printf", SyntaxType.PRINTFTK);
        contentToType.put("return", SyntaxType.RETURNTK);
        contentToType.put("void", SyntaxType.VOIDTK);

        // 注意这里要按照逆序排列，才能完成匹配
        PATTERN = contentToType.keySet().stream().reduce((s1, s2) -> s1 + "(?![a-zA-Z0-9_])|" + s2).get() + "(?![a-zA-Z0-9_])";
    }

    public Reserved(int line, String content)
    {
        super(line, content);
        type = contentToType.get(content);
    }
}
