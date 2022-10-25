package check;

public class PansyException extends Exception
{
    protected final ErrorType type;
    protected final int line;

    public PansyException(ErrorType type, int line)
    {
        this.type = type;
        this.line = line;
    }

    /**
     * 用于无法立即确定行数的情况，一般是未定义的符号
     * @param type 错误类型
     */
    public PansyException(ErrorType type)
    {
        this.type = type;
        this.line = -1;
    }

    public ErrorType getType()
    {
        return type;
    }

    public int getLine()
    {
        return line;
    }

    public String toErrorInfo()
    {
        return "at line " + line + " occur " + type;
    }

    public int compareTo(PansyException other)
    {
        return line - other.line;
    }
}
