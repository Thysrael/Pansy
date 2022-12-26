package parser.cst;

import check.*;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.values.GlobalVariable;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.Alloca;
import lexer.token.SyntaxType;

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
     * 只有未定义和缺少中括号两种错误，左值常量被修改的问题在 InStmt 和 AssignStmt 中被处理
     * 未定义的错误会反复出现，这也是需要 TreeSet 的原因
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

    /**
     * 左值的类型信息需要进行指针运算才可以得到
     * @param symbolTable 符号表
     * @return 左值的类型
     */
    @Override
    public CheckDataType getDataType(SymbolTable symbolTable)
    {
        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(ident.getContent());
            // 其实这个是必然的，只有 Var （广义）才可以当左值
            if (symbolInfo instanceof VarInfo)
            {
                // 这里只是获得了符号的维度，还需要与后面的中括号进行指针运算
                CheckDataType rawType = ((VarInfo) symbolInfo).getCheckDataType();
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

    /**
     * 进行指针运算，获得左值真正的类型信息
     * @param rawType 符号表示的类型信息
     * @return 实际类型信息
     */
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
     * 左值是直接返回指针的，而不是返回指针指向的内容，
     * 应当由更高层次的语法树（PrimaryExpNode）决定是否加载
     * 左值指向的内容有 3 种类型：
     * 整型：十分显然
     * 指针：
     * 至于为什么会有这么个东西，可以这样举例，比如说 f(int a[])
     * 当我们对 a 进行 buildIr 的时候，a 的类型是 i32*
     * 然后我们为了 SSA（主要是为了整型形参，指针形参属于受害者） ，所以在函数一开始做了一个 alloca-store 操作
     * 那么在之后，我们看 a，就变成了一个 (i32*)*，也就是 lVal 指向一个指针的情况
     * 对于这种情况，我们首先用 load 将其指针去掉一层，目前 a 的类型就和 C 语言一致了，所以对他的一维访存，就是 GEP一个 index
     * 对于二维访存，就是 GEP 两个 index
     * 数组：
     * 后面有写
     */
    @Override
    public void buildIr()
    {
        String name = ident.getContent();
        Value lVal = irSymbolTable.find(name);
        // 这说明 lVal 是一个常量，直接返回就好了
        if (lVal.getValueType() instanceof IntType)
        {
            if (canCalValueDown)
            {
                valueIntUp = ((ConstInt) lVal).getValue();
            }
            valueUp = lVal;
        }
        // lVal 的类型是一个 PointerType，说明 lVal 是一个局部变量或者全局变量
        else
        {
            // 三个 boolean 指示了全局变量或者局部变量的类型
            boolean isInt = ((PointerType) lVal.getValueType()).getPointeeType() instanceof IntType;
            boolean isPointer = ((PointerType) lVal.getValueType()).getPointeeType() instanceof PointerType;
            boolean isArray = ((PointerType) lVal.getValueType()).getPointeeType() instanceof ArrayType;

            if (isInt)
            {
                // 如果是全局变量导致的指针，那么就需要直接把这个量访存出来，在可以计算的情况下
                if (canCalValueDown && lVal instanceof GlobalVariable)
                {
                    ConstInt initVal = (ConstInt) ((GlobalVariable) lVal).getInitVal();
                    valueIntUp = initVal.getValue();
                    valueUp = new ConstInt(valueIntUp);
                }
                else
                {
                    // 可以看到，左值是直接返回指针的，而不是返回指针指向的内容，应当由更高层次的语法树决定是否加载
                    valueUp = lVal;
                }
            }
            // 局部变量如果是一个指针，那么这个局部变量就是形参（而且是形参数组 a[], a[][2]）
            else if (isPointer)
            {
                // 这里存着实际的指针
                Value ptr = irBuilder.buildLoad(curBlock, lVal);
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
                    ptr = irBuilder.buildGEP(curBlock, ptr, valueUp);
                    // 这里和其他的地方一样，我也说不明白为啥了
                    if (((PointerType) ptr.getValueType()).getPointeeType() instanceof ArrayType)
                    {
                        ptr = irBuilder.buildGEP(curBlock, ptr, ConstInt.ZERO, ConstInt.ZERO);
                    }
                    valueUp = ptr;
                }
                // 有两维索引 a[1][2] 只能是形参 a[][6] 的引用
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
                // 当是可以计算的时候，并且是一个全局变量的时候，我们直接将其算出来，而不用 GEP 去做
                // 虽然 GEP 提供了更加统一的观点对待 Alloca 数组和 global 数组
                // 但是 GEP 在全局变量被用在“全局” 和 其他局部数组的 Alloca 时无能为力
                if (canCalValueDown && lVal instanceof GlobalVariable)
                {
                    Constant initVal = ((GlobalVariable) lVal).getInitVal();

                    for (ExpNode exp : exps)
                    {
                        exp.buildIr();
                        initVal = ((ConstArray) initVal).getElementByIndex(valueIntUp);
                    }
                    valueIntUp = ((ConstInt) initVal).getValue();
                }
                // 对于局部常量数组的常量式访问（比如说用于初始化其他常量，当数组的维度，我们不用 gep 访存）
                else if (canCalValueDown && lVal instanceof Alloca)
                {
                    Constant initVal = ((Alloca) lVal).getInitVal();

                    for (ExpNode exp : exps)
                    {
                        exp.buildIr();
                        initVal = ((ConstArray) initVal).getElementByIndex(valueIntUp);
                    }

                    assert initVal instanceof ConstInt;
                    valueIntUp = ((ConstInt) initVal).getValue();
                }
                else
                {
                    Value ptr = lVal;
                    for (ExpNode exp : exps)
                    {
                        exp.buildIr();
                        ptr = irBuilder.buildGEP(curBlock, ptr, ConstInt.ZERO, valueUp);
                    }
                    // 当一个数组符号经过了中括号的运算后，依然指向一个数组，那么说明这个 lVal 一定是指针实参
                    // 否则如果整型实参，这里一定指向的是 INT，但是由于 llvm ir 的数组的指针是高一级的，比如说
                    // int a[2] 在 C 中，a 是指向 int 的指针，而在 llvm ir 中是指向 2 x int 的指针，所以要降级
                    // 至于为啥要降级，是因为在 llvm ir 和 C 中，f(int a[]) 这种写法的 a 都是 “指向 int 的指针”
                    if (((PointerType) ptr.getValueType()).getPointeeType() instanceof ArrayType)
                    {
                        ptr = irBuilder.buildGEP(curBlock, ptr, ConstInt.ZERO, ConstInt.ZERO);
                    }
                    valueUp = ptr;
                }
            }
        }
    }
}
