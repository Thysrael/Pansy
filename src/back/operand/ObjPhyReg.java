package back.operand;

import java.util.*;

public class ObjPhyReg extends ObjReg
{
    private final static HashMap<Integer, String> indexToName = new HashMap<>();
    private final static HashMap<String, Integer> nameToIndex = new HashMap<>();
    public final static HashSet<Integer> calleeSavedRegIndex = new HashSet<>();
    public final static HashSet<Integer> canAllocateRegIndex = new HashSet<>();

    static
    {
        // 只能读，不能写，不能用于分配
        indexToName.put(0, "zero");
        // 采用了拓展指令，所以不要动这个寄存器，不能用于分配
        indexToName.put(1, "at");
        // 用作返回值，不需要被调用者保存，可以用于分配，因为 v0 最后一定会被改成返回值，和普通寄存器一样
        indexToName.put(2, "v0");
        // 因为不存在复杂结构，所以这个可以被当成普通的被调用者保存寄存器，可以用于分配
        indexToName.put(3, "v1");
        // 4 个传参寄存器，是调用者保存（甚至不需要保存），可以用于分配，因为即使内容被改写，其父函数只会写这个寄存器，不会读这个寄存器
        indexToName.put(4, "a0");
        indexToName.put(5, "a1");
        indexToName.put(6, "a2");
        indexToName.put(7, "a3");
        // 从 8 ~ 25 毫无疑问，都是被调用者保存，可以被分配
        indexToName.put(8, "t0");
        indexToName.put(9, "t1");
        indexToName.put(10, "t2");
        indexToName.put(11, "t3");
        indexToName.put(12, "t4");
        indexToName.put(13, "t5");
        indexToName.put(14, "t6");
        indexToName.put(15, "t7");
        indexToName.put(16, "s0");
        indexToName.put(17, "s1");
        indexToName.put(18, "s2");
        indexToName.put(19, "s3");
        indexToName.put(20, "s4");
        indexToName.put(21, "s5");
        indexToName.put(22, "s6");
        indexToName.put(23, "s7");
        indexToName.put(24, "t8");
        indexToName.put(25, "t9");
        // 用于 OS 内核，但是 MARS 中没有，所以可以当成被调用者保存，可以被分配
        indexToName.put(26, "k0");
        indexToName.put(27, "k1");
        // gp, fp 同理，都可以因为功能的缺失而被当成普通寄存器，可以被分配
        // sp 是手动维护的，不需要保存，不能被分配
        indexToName.put(28, "gp");
        indexToName.put(29, "sp");
        indexToName.put(30, "fp");
        // 返回地址是需要被调用者保存的，可以被分配
        indexToName.put(31, "ra");

        for (Map.Entry<Integer, String> entry : indexToName.entrySet())
        {
            nameToIndex.put(entry.getValue(), entry.getKey());
        }
        // 只有 zero, at, v0, a0 ~ a3, sp 不需要被调用者保存
        calleeSavedRegIndex.add(3);
        for (int i = 8; i <= 28; i++)
        {
            calleeSavedRegIndex.add(i);
        }
        calleeSavedRegIndex.add(30);
        calleeSavedRegIndex.add(31);
        // 只有 zero, at, sp 不可以
        for (int i = 0; i < 32; i++)
        {
            if (i != 0 && i != 1 && i != 29)
            {
                canAllocateRegIndex.add(i);
            }
        }
    }

    public final static ObjPhyReg ZERO = new ObjPhyReg("zero");
    public final static ObjPhyReg AT = new ObjPhyReg("at");
    public final static ObjPhyReg SP = new ObjPhyReg("sp");
    public final static ObjPhyReg V0 = new ObjPhyReg("v0");
    public final static ObjPhyReg RA = new ObjPhyReg("ra");
    private final int index;
    private final String name;
    private boolean isAllocated;

    public ObjPhyReg(String name)
    {
        this.name = name;
        this.index = nameToIndex.get(name);
        this.isAllocated = false;
    }

    public ObjPhyReg(int index)
    {
        this.name = indexToName.get(index);
        this.index = index;
        this.isAllocated = false;
    }

    public ObjPhyReg(int index, boolean isAllocated)
    {
        this.name = indexToName.get(index);
        this.index = index;
        this.isAllocated = isAllocated;
    }

    public void setAllocated(boolean isAllocated)
    {
        this.isAllocated = isAllocated;
    }

    public int getIndex()
    {
        return index;
    }

    /**
     * 如果一个寄存器是物理寄存器,而且还没有被分配,那么就是需要预着色的
     * 所谓的预着色，可能指的是在图着色中没分配，就已经是物理寄存器的情况
     * 可能这个的意思就是，对于物理寄存器，只有两种状态，没分配的叫预着色，分配的叫 allocated
     * @return true 就是预着色
     */
    @Override
    public boolean isPrecolored()
    {
        return !isAllocated;
    }

    @Override
    public boolean isAllocated()
    {
        return isAllocated;
    }

    /**
     * 对于一个物理寄存器，只要他还没有被分配，那么就是需要着色的
     * @return 若未被分配，那么就是需要着色的
     */
    @Override
    public boolean needsColor()
    {
        return !isAllocated;
    }

    @Override
    public String toString()
    {
        return "$" + name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjPhyReg mipsPhyReg = (ObjPhyReg) o;
        return index == mipsPhyReg.index && isAllocated == mipsPhyReg.isAllocated;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(index, isAllocated);
    }
}
