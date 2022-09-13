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
    private static final HashMap<String, TokenType> contentToType = new HashMap<>();
    private static final HashMap<String, String> contentToRegex = new HashMap<>();

    static
    {
        contentToType.put("!", TokenType.NOT);
        contentToType.put("&&", TokenType.AND);
        contentToType.put("||", TokenType.OR);
        contentToType.put("+", TokenType.PLUS);
        contentToType.put("-", TokenType.MINU);
        contentToType.put("*", TokenType.MULT);
        contentToType.put("/", TokenType.DIV);
        contentToType.put("%", TokenType.MOD);
        contentToType.put("<", TokenType.LSS);
        contentToType.put("<=", TokenType.LEQ);
        contentToType.put(">", TokenType.GRE);
        contentToType.put(">=", TokenType.GEQ);
        contentToType.put("==", TokenType.EQL);
        contentToType.put("!=", TokenType.NEQ);
        contentToType.put("=", TokenType.ASSIGN);
        contentToType.put(";", TokenType.SEMICN);
        contentToType.put(",", TokenType.COMMA);
        contentToType.put("(", TokenType.LPARENT);
        contentToType.put(")", TokenType.RPARENT);
        contentToType.put("[", TokenType.LBRACK);
        contentToType.put("]", TokenType.RBRACK);
        contentToType.put("{", TokenType.LBRACE);
        contentToType.put("}", TokenType.RBRACE);

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
            type = TokenType.SPACE;
        }
        else
        {
            type = contentToType.get(content);
        }
    }
}
