package ir.types;

import java.util.ArrayList;

/**
 * 由两个属性组成
 * formalArgs 形参列表，只记录参数类型
 * resultType 返回值类型
 */
public class FunctionType extends ValueType
{
    private final ArrayList<DataType> formalArgs;
    private final DataType returnType;

    /**
     * @param formalArgs 形参列表，只记录参数类型，可以为 PointerType，IntType，FPType，不能是 ArrayType
     *                   void f(int [])中参数是 i32* 指针类型
     *                   void f(int [][10])中参数是 [10 x i32]* 指针类型
     *                   llvm ir 中的 noundef 我们不需要管
     * @param returnType 返回值类型，可以为IntType，FPType，VoidType，不可以是PointerType
     */
    public FunctionType(ArrayList<DataType> formalArgs, DataType returnType)
    {
        this.formalArgs = formalArgs;
        this.returnType = returnType;
    }

    public DataType getReturnType()
    {
        return returnType;
    }

    public ArrayList<DataType> getFormalArgs()
    {
        return formalArgs;
    }

    @Override
    public int getSize()
    {
        System.err.println("get function's size!");
        assert false;
        return 0;
    }
}
