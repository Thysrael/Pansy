package back.operand;

import java.util.Objects;

public class ObjVirReg extends ObjReg
{
    private static int indexCounter = 0;

    private final String name;

    public ObjVirReg()
    {
        this.name = "vr" + indexCounter++;
    }

    /**
     * 只要是虚拟寄存器，都是需要着色的
     * @return true
     */
    @Override
    public boolean needsColor()
    {
        return true;
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
        ObjVirReg that = (ObjVirReg) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
