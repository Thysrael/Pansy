package lexer;

import exception.LexException;
import lexer.token.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer
{
    /**
     * 以行的形式存储源文件
     */
    private final String sourceCode;
    /**
     * 表示当前 lex 了到了哪一行
     */
    private int lineCursor;
    /**
     * 表示总的字符串指针到了哪里
     */
    private int strCursor;

    private final ArrayList<Token> tokens;

    private final ArrayList<LexException> exceptions;

    public Lexer(String sourceCode)
    {
        this.sourceCode = sourceCode;
        this.lineCursor = 1;
        this.strCursor = 0;
        this.tokens = new ArrayList<>();
        this.exceptions = new ArrayList<>();
    }

    public ArrayList<Token> run()
    {
        while (!isFileEnd())
        {
            Token token = getToken();

            // 如果没有解析出来，那么就移动一下
            if (token == null)
            {
                strCursor++;
                exceptions.add(new LexException(lineCursor));
            }
            else
            {
                strCursor += token.getContent().length();
                lineCursor += token.getContent().chars().boxed().filter(c -> c == '\n').count();
                if (!isBlankToken(token))
                {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }

    private boolean isFileEnd()
    {
        return strCursor >= sourceCode.length();
    }

    private boolean isBlankToken(Token token)
    {
        SyntaxType type = token.getType();
        return type.equals(SyntaxType.SINGLE_COMMENT) ||
                type.equals(SyntaxType.MULTI_COMMENT) ||
                type.equals(SyntaxType.SPACE);
    }

    private Token getToken()
    {
        // 匹配注释
        int endCursor = Comment.consumeMultiComment(sourceCode, strCursor);
        if (strCursor < endCursor)
        {
            return new Comment(lineCursor, sourceCode.substring(strCursor, endCursor));
        }
        endCursor = Comment.consumeSingleComment(sourceCode, strCursor);
        if (strCursor < endCursor)
        {
            return new Comment(lineCursor, sourceCode.substring(strCursor, endCursor));
        }

        // 匹配保留字
        Pattern pattern = Pattern.compile(Reserved.PATTERN);
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find(strCursor) && matcher.start() == strCursor)
        {
            return new Reserved(lineCursor, matcher.group());
        }

        // 匹配标识符
        pattern = Pattern.compile(Identifier.PATTERN);
        matcher = pattern.matcher(sourceCode);
        // 从 index 处开始匹配，然后检验是否匹配
        if (matcher.find(strCursor) && matcher.start() == strCursor)
        {
            return new Identifier(lineCursor, matcher.group());
        }

        // 匹配字符串
        endCursor = FormString.consumeFormString(sourceCode, strCursor);
        if (strCursor < endCursor)
        {
            return new FormString(lineCursor, sourceCode.substring(strCursor, endCursor));
        }

        // 匹配整形数
        pattern = Pattern.compile(IntConst.PATTERN);
        matcher = pattern.matcher(sourceCode);
        if (matcher.find(strCursor) && matcher.start() == strCursor)
        {
            return new IntConst(lineCursor, matcher.group());
        }

        // 匹配分界符
        pattern = Pattern.compile(Delimiter.PATTERN);
        matcher = pattern.matcher(sourceCode);
        if (matcher.find(strCursor) && matcher.start() == strCursor)
        {
            return new Delimiter(lineCursor, matcher.group());
        }

        // 失败返回 null
        return null;
    }

    public String display()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : tokens)
        {
            stringBuilder.append(token).append("\n");
        }

        return stringBuilder.toString();
    }
}
