package back.instruction;

/**
 * 包括两个东西，一个是宏调用，一个是注释
 */
public class ObjComment extends ObjInstr
{
    private final String content;
    private final boolean isComment;

    public ObjComment(String content)
    {
        this.content = content;
        this.isComment = true;
    }

    public ObjComment(String content, boolean isComment)
    {
        this.content = content;
        this.isComment = isComment;
    }

    @Override
    public String toString()
    {
        if (isComment)
        {
            return "# " + content + "\n";
        }
        else
        {
            return content + "\n";
        }
    }
}
