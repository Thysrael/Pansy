package back.operand;

public class ObjImm extends ObjOperand
{
    private final int immediate;

    public ObjImm(int immediate)
    {
        this.immediate = immediate;
    }

    public int getImmediate()
    {
        return immediate;
    }

    @Override
    public String toString()
    {
        return String.valueOf(immediate);
    }
}
