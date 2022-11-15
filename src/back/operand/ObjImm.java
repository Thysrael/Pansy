package back.operand;

public class ObjImm extends ObjOperand
{
    private int immediate;

    public ObjImm(int immediate)
    {
        this.immediate = immediate;
    }

    public void setImmediate(int immediate)
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
