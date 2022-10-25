package lexer;

import check.PansyException;
import lexer.token.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static check.ErrorType.LEX_ERROR;

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

    public Lexer(String sourceCode)
    {
        this.sourceCode = sourceCode;
        this.lineCursor = 1;
        this.strCursor = 0;
        this.tokens = new ArrayList<>();
    }

    public ArrayList<Token> run()
    {
        while (!isFileEnd())
        {
            try
            {
                Token token = getToken();
                strCursor += token.getContent().length();
                lineCursor += token.getContent().chars().boxed().filter(c -> c == '\n').count();
                if (!isBlankToken(token))
                {
                    tokens.add(token);
                }
            }
            catch (PansyException error)
            {
                handleUnexpectedErrors(error);
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

    /**
     * 当发生未知的异常的时候，需要打印已经解析出的 tokens，
     * 然后打印报错原因（虽然只有一个）
     * 然后打印栈信息
     * 最后终止程序
     *
     * @param error 未知错误
     */
    private void handleUnexpectedErrors(PansyException error)
    {
        for (Token token : tokens)
        {
            System.err.println(token);
        }
        System.err.println(error.toErrorInfo());
        error.printStackTrace();
        System.exit(1);
    }

    private Token getToken() throws PansyException
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

        // 失败抛出异常
        throw new PansyException(LEX_ERROR, lineCursor);
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
