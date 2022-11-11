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

    /**
     * 这里为了错误输出时按照顺序输出，所以需要比较所在行的大小
     * @param other 另一个 exception
     * @return 行数比较
     */
    public int compareTo(PansyException other)
    {
        return line - other.line;
    }
}
