package back.operand;

public abstract class ObjOperand
{
    /**
     * 对于非物理寄存器，就是 false 很显然，很合理
     * 对于物理寄存器，他是 !isAllocated
     * 这是因为预着色的寄存器都是在 irParse 阶段分配的，此时的 isAllocated == false，所以是预着色的
     * isAllocated == true 的物理寄存器只会发生在作色阶段
     * @return 如题
     */
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
