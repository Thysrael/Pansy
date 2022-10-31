package parser.cst;

import check.CheckDataType;
import check.ErrorType;
import check.PansyException;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Load;
import lexer.token.SyntaxType;
import middle.symbol.*;

import java.util.ArrayList;

/**
 * LVal
 *     : IDENFR (L_BRACKT Exp R_BRACKT)*
 *     ;
 */
public class LValNode extends CSTNode
{
    /**
     * 用于存储索引信息，进而用于类型判断
     */
    private final ArrayList<ExpNode> exps = new ArrayList<>();
    private TokenNode ident = null;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode && ((TokenNode) child).isSameType(SyntaxType.IDENFR))
        {
            ident = (TokenNode) child;
        }
        if (child instanceof ExpNode)
        {
            exps.add((ExpNode) child);
        }
    }

    /**
     * 需要检测左值是否是一个常量
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        TokenNode identNode = (TokenNode) children.get(0);

        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            // 如果左值是一个函数
            if (symbolInfo instanceof FuncInfo)
            {
                errors.add(new PansyException(ErrorType.UNDEFINED_SYMBOL, identNode.getLine()));
            }
        }
        // 变量未定义
        catch (PansyException e)
        {
            errors.add(new PansyException(e.getType(), identNode.getLine()));
        }

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    @Override
    public CheckDataType getDataType(SymbolTable symbolTable)
    {
        TokenNode identNode = (TokenNode) children.get(0);

        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            // 如果左值是一个常量
            if (symbolInfo instanceof ConstInfo)
            {
                CheckDataType rawType = ((ConstInfo) symbolInfo).getDataType();
                return calcDataType(rawType);
            }
            // 如果左值是一个变量
            else if (symbolInfo instanceof VarInfo)
            {
                CheckDataType rawType = ((VarInfo) symbolInfo).getDataType();
                return calcDataType(rawType);
            }

            return CheckDataType.VOID;
        }
        // 变量未定义，因为如果没有定义，那么就在 LVal.check 的时候报错了，至于这里获得不了真正的信息了，无所谓。
        catch (PansyException e)
        {
            return CheckDataType.VOID;
        }
    }

    private CheckDataType calcDataType(CheckDataType rawType)
    {
        int calDim = exps.size();
        int rawDim;
        if (rawType.equals(CheckDataType.INT))
        {
            rawDim = 0;
        }
        else if (rawType.equals(CheckDataType.DIM1))
        {
            rawDim = 1;
        }
        else if (rawType.equals(CheckDataType.DIM2))
        {
            rawDim = 2;
        }
        else
        {
            rawDim = -1;
        }
        int trueDim = rawDim - calDim;
        if (trueDim == 0)
        {
            return CheckDataType.INT;
        }
        else if (trueDim == 1)
        {
            return CheckDataType.DIM1;
        }
        else if (trueDim == 2)
        {
            return CheckDataType.DIM2;
        }
        else
        {
            return CheckDataType.VOID;
        }
    }

    /**
     * 左值是直接返回指针的，而不是返回指针指向的内容，应当由更高层次的语法树决定是否加载
     */
    @Override
    public void buildIr()
    {
        String name = ident.getContent();
        Value lVal = irSymbolTable.find(name);
        // 这说明 lVal 是一个常量，直接返回就好了
        if (lVal.getValueType() instanceof IntType)
        {
            valueUp = lVal;
        }
        // lVal 的类型是一个 PointerType，说明 lVal 是一个局部变量或者全局变量
        else
        {
            // 三个 boolean 指示了局部变量的类型
            boolean isInt = ((PointerType) lVal.getValueType()).getPointeeType() instanceof IntType;
            boolean isPointer = ((PointerType) lVal.getValueType()).getPointeeType() instanceof PointerType;
            boolean isArray = ((PointerType) lVal.getValueType()).getPointeeType() instanceof ArrayType;
            if (isInt)
            {
                // 可以看到，左值是直接返回指针的，而不是返回指针指向的内容，应当由更高层次的语法树决定是否加载
                valueUp = lVal;
            }
            // 局部变量如果是一个指针，那么这个局部变量就是形参（而且是形参数组 a[], a[][2]）
            else if (isPointer)
            {
                // 这里存着实际的指针
                Load ptr = irBuilder.buildLoad(curBlock, lVal);
                // 没有索引
                // 因为 SySy 中没有没有指针运算，所以当一个形参没有索引就出现的时候，他只能被用当成子函数的实参
                // 那么只需要把这个东西加载出来就好了
                if (exps.isEmpty())
                {
                    valueUp = ptr;
                }
                // 只有一维索引，对于 a[2] 可能是形参 (a[], a[][6]) 的引用，底下这种都适用
                else if (exps.size() == 1)
                {
                    exps.get(0).buildIr();
                    // 根据索引获得一个指针，要维持原有指针的类型
                    valueUp = irBuilder.buildGEP(curBlock, ptr, valueUp);
                }
                // 有两维索引 a[1][2] 只能是形参 a[][6] 的引用
                // TODO 测试一次
                else
                {
                    exps.get(0).buildIr();
                    Value firstIndex = valueUp;
                    exps.get(1).buildIr();
                    Value secondIndex = valueUp;
                    valueUp = irBuilder.buildGEP(curBlock, ptr, firstIndex, secondIndex);
                }
            }
            // 是一个局部数组或者全局数组
            else if (isArray)
            {
//                Value ptr = irBuilder.buildGEP(curBlock, lVal, ConstInt.ZERO, ConstInt.ZERO);
                Value ptr = lVal;
                for (ExpNode exp : exps)
                {
                    exp.buildIr();
                    ptr = irBuilder.buildGEP(curBlock, ptr, ConstInt.ZERO, valueUp);
                }
                // TODO 合理性在哪里？
                if (((PointerType) ptr.getValueType()).getPointeeType() instanceof ArrayType)
                {
                    ptr = irBuilder.buildGEP(curBlock, ptr, ConstInt.ZERO, ConstInt.ZERO);
                }
                valueUp = ptr;
            }
        }
    }
}
