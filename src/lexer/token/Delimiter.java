package lexer.token;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Delimiter extends Token
{
    public static final String PATTERN;
    private static final String SPACE_PATTERN = "\\s+";
    private static final Pattern spacePattern = Pattern.compile(SPACE_PATTERN);
    private static final HashMap<String, SyntaxType> contentToType = new HashMap<>();
    private static final HashMap<String, String> contentToRegex = new HashMap<>();

    static
    {
        contentToType.put("!", SyntaxType.NOT);
        contentToType.put("&&", SyntaxType.AND);
        contentToType.put("||", SyntaxType.OR);
        contentToType.put("+", SyntaxType.PLUS);
        contentToType.put("-", SyntaxType.MINU);
        contentToType.put("*", SyntaxType.MULT);
        contentToType.put("/", SyntaxType.DIV);
        contentToType.put("%", SyntaxType.MOD);
        contentToType.put("<", SyntaxType.LSS);
        contentToType.put("<=", SyntaxType.LEQ);
        contentToType.put(">", SyntaxType.GRE);
        contentToType.put(">=", SyntaxType.GEQ);
        contentToType.put("==", SyntaxType.EQL);
        contentToType.put("!=", SyntaxType.NEQ);
        contentToType.put("=", SyntaxType.ASSIGN);
        contentToType.put(";", SyntaxType.SEMICN);
        contentToType.put(",", SyntaxType.COMMA);
        contentToType.put("(", SyntaxType.LPARENT);
        contentToType.put(")", SyntaxType.RPARENT);
        contentToType.put("[", SyntaxType.LBRACK);
        contentToType.put("]", SyntaxType.RBRACK);
        contentToType.put("{", SyntaxType.LBRACE);
        contentToType.put("}", SyntaxType.RBRACE);

        contentToRegex.put("||", "\\|\\|");
        contentToRegex.put("+", "\\+");
        contentToRegex.put("*", "\\*");
        contentToRegex.put("(", "\\(");
        contentToRegex.put(")", "\\)");
        contentToRegex.put("[", "\\[");
        contentToRegex.put("]", "\\]");
        contentToRegex.put("{", "\\{");
        contentToRegex.put("}", "\\}");

        // 注意这里要按照逆序排列，才能完成匹配
        Optional<String> make = contentToType.keySet().stream().sorted(Comparator.comparing(String::length).reversed())
                .map(i -> contentToRegex.get(i) != null ? contentToRegex.get(i) : i)
                .reduce((s1, s2) -> s1 + "|" + s2);
        assert make.isPresent();
        // 首先要把空白符放进去，因为这个东西不好对应
        PATTERN = SPACE_PATTERN + "|" + make.get();
    }

    public Delimiter(int line, String content)
    {
        super(line, content);
        Matcher spaceMatcher = spacePattern.matcher(content);
        if (spaceMatcher.find())
        {
            type = SyntaxType.SPACE;
        }
        else
        {
            type = contentToType.get(content);
        }
    }
}
