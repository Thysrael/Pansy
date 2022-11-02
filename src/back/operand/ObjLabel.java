package back.operand;

import java.util.Objects;

public class ObjLabel extends ObjOperand
{
    private String name;

    public ObjLabel(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjLabel objLabel = (ObjLabel) o;
        return Objects.equals(name, objLabel.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
