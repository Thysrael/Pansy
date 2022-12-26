package ir.values.instructions;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.User;
import ir.values.constants.ConstArray;

public class Alloca extends MemInstruction
{
    /**
     * 这是参考 GlobalVariable 的设计，主要是为了局部常量数组准备的
     * 这里记录着局部常量数组的值，
     * 但是我没有将其视为 user，不知道对不对
     * TODO 这里启发我们，如果一个常量数组只被当成常量使用，也就是没有 const[var] 这种状况
     * 可以在后续的优化中将所有的 store 删掉
     */
    private final ConstArray initVal;
    /**
     * 新建一个 alloca 指令，其类型是分配空间类型的指针
     * @param nameNum 对于指令而言，其名称中带有数字，指令名称中的数字，eg: 名称为 %1 的指令的 nameNum 为 1
     * @param allocatedType 分配空间的类型，可能为 PointerType, IntType, ArrayType
     * @param parent 基本块
     */
    public Alloca(int nameNum, ValueType allocatedType, BasicBlock parent)
    {
        // 指针
        super("%v" + nameNum, new PointerType(allocatedType), parent);
        initVal = null;
    }

    public Alloca(int nameNum, ValueType allocatedType, BasicBlock parent, ConstArray initVal)
    {
        // 指针
        super("%v" + nameNum, new PointerType(allocatedType), parent);
        this.initVal = initVal;
    }

    public ValueType getAllocatedType()
    {
        return ((PointerType) getValueType()).getPointeeType();
    }

    public ConstArray getInitVal()
    {
        return initVal;
    }

    /**
     * 可以被提升，本质是只要是没有使用 gep 的，都可以被提升
     * 直观理解，就是和数组不挂钩的，都可以在 mem2reg 中被提升
     * @return 可提升，则为 true
     */
    public boolean canPromotable()
    {
        // 没有使用
        if (getUsers().isEmpty())
        {
            return true;
        }
        // 使用者中有 GEP ，则与数组有关，否则一般使用者都是 load，store 之类的
        for (User user : getUsers())
        {
            if (user instanceof GetElementPtr)
            {
                GetElementPtr getElementPtr = (GetElementPtr) user;
                // promotable alloca must be a single data
                if (getElementPtr.getUsedValue(0) == this)
                {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return getName() + " = alloca " + getAllocatedType();
    }
}
