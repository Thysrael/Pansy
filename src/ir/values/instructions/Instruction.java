package ir.values.instructions;

import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.User;
import ir.values.Value;
import util.MyList;

/**
 * 一切 llvm ir 指令的父类，会使用 Value 作为操作数
 * 指令的 parent 是 BasicBlock
 */
public abstract class Instruction extends User
{
    /**
     * 因为指令会被组织在 MyList 中，所以需要记录一下当前指令所在的节点
     */
    private MyList.MyNode<Instruction> node;

    /**
     * @param name     指令名称，不是指 "add" "sub"之类的名称，而是指令返回值存放的虚拟寄存器
     *                 eg: %3 = add i32 %1, %2 名称为 "%1"
     *                 store i32 %1, i32* %2 名称为 ""，因为没有用虚拟寄存器
     * @param dataType 指令返回值类型，为 DataType，包括 PointerType，VoidType，IntType
     * @param parent   指令所在基本块
     * @param ops      指令的操作数列表，放在values数组中，从0号位置一次排列。values 数组定义在 User 中
     */
    public Instruction(String name, DataType dataType, BasicBlock parent, Value... ops)
    {
        super(name, dataType, parent, ops);
        if (!name.isEmpty())
        {
            parent.getParent().addFunctionSymbol(this);
        }
    }

    @Override
    public BasicBlock getParent()
    {
        return (BasicBlock) super.getParent();
    }

    public void setNode(MyList.MyNode<Instruction> node)
    {
        this.node = node;
    }

    public MyList.MyNode<Instruction> getNode()
    {
        return node;
    }

    public void eraseFromParent()
    {
        getParent().eraseInstruction(this);
        node = null;
    }
}
