package back.operand;

public abstract class ObjOperand
{
    public boolean isPrecolored()
    {
        return false;
    }
    /**
     * 对于物理寄存器，当其是未被分配的，那么他就是需要着色的
     * @return 当这个寄存器是物理寄存器，而且是没有被分配的（就是没有被着色的），着色只会发生在着色环节
     * 这么看上去，似乎这个东西是区分物理寄存器和其他东西的一个方法
     */
    public boolean needsColor()
    {
        return false;
    }

    public boolean isAllocated()
    {
        return false;
    }
}
