package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.GlobalVariable;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.constants.ZeroInitializer;
import ir.values.instructions.Alloca;
import ir.values.instructions.GetElementPtr;
import check.SymbolTable;

import java.util.ArrayList;

/**
 * VarDef
 * : IDENFR (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
 * ;
 */
public class VarDefNode extends CSTNode
{
    private TokenNode ident;
    private final ArrayList<ConstExpNode> constExps = new ArrayList<>();
    private InitValNode initVal = null;
    /**
     * 用于记录数组的维数，比如说 a[1][2] 的 dims 就是 {1, 2}
     */
    private final ArrayList<Integer> dims = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (children.size() == 1)
        {
            ident = (TokenNode) child;
        }
        if (child instanceof ConstExpNode)
        {
            constExps.add((ConstExpNode) child);
        }
        if (child instanceof InitValNode)
        {
            initVal = (InitValNode) child;
        }
    }

    /**
     * 两种错误，
     * 一种是标识符命名重复
     * 一种是缺失右中括号
     *
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        // 标识符命名重复
        if (symbolTable.isSymbolRedefined(ident.getContent()))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, ident.getLine()));
        }
        // 更新符号表
        symbolTable.addVar(this);
        // 缺失右中括号
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    private void genSingleVar()
    {
        // 全局单变量
        if (irSymbolTable.isGlobal())
        {
            // 有初始值的全局单变量
            if (initVal != null)
            {
                globalInitDown = true;
                initVal.buildIr();
                globalInitDown = false;
                // "全局变量声明中指定的初值表达式必须是常量表达式"，所以一定可以转为 ConstInt
                GlobalVariable globalVariable = irBuilder.buildGlobalVariable(ident.getContent(), (ConstInt) valueUp, false);
                irSymbolTable.addValue(ident.getContent(), globalVariable);
            }
            // 没有初始值的全局变量
            else
            {
                // "未显式初始化的全局变量, 其(元素)值均被初始化为 0 "
                GlobalVariable globalVariable = irBuilder.buildGlobalVariable(ident.getContent(), ConstInt.ZERO, false);
                irSymbolTable.addValue(ident.getContent(), globalVariable);
            }
        }
        // 局部单变量
        else
        {
            // 为这个变量分配空间
            Alloca alloca = irBuilder.buildAlloca(IntType.I32, curBlock);
            // 从这里可以看出，可以从符号表这种查询到的东西是一个指针，即 int*
            irSymbolTable.addValue(ident.getContent(), alloca);
            // "当不含有 '=' 和初始值时,其运行时实际初值未定义"
            if (initVal != null)
            {
                initVal.buildIr();
                irBuilder.buildStore(curBlock, valueUp, alloca);
            }
        }
    }

    /**
     * 可以根据展平的初始化数组和 dims 来生成一个合乎常理的全局变量
     *
     * @param flattenArray 展平数组
     */
    private void genGlobalInitArray(ArrayList<Value> flattenArray)
    {
        // 一维数组，将 flattenArray 转变后加入即可
        if (dims.size() == 1)
        {
            ArrayList<Constant> constArray = new ArrayList<>();
            for (Value value : flattenArray)
            {
                constArray.add((ConstInt) value);
            }
            ConstArray initArray = new ConstArray(constArray);
            GlobalVariable globalVariable = irBuilder.buildGlobalVariable(ident.getContent(), initArray, false);
            irSymbolTable.addValue(ident.getContent(), globalVariable);
        }
        // 二维数组
        else
        {
            // 为第一维的数组，其元素为 ConstArray
            ArrayList<Constant> colArray = new ArrayList<>();
            for (int i = 0; i < dims.get(0); i++)
            {
                // 为第二维的数组，其元素为 ConstInt
                ArrayList<Constant> rowArray = new ArrayList<>();
                for (int j = 0; j < dims.get(1); j++)
                {
                    rowArray.add((ConstInt) flattenArray.get(dims.get(1) * i + j));
                }
                colArray.add(new ConstArray(rowArray));
            }
            ConstArray initArray = new ConstArray(colArray);
            GlobalVariable globalVariable = irBuilder.buildGlobalVariable(ident.getContent(), initArray, false);
            irSymbolTable.addValue(ident.getContent(), globalVariable);
        }
    }

    private void genVarArray()
    {
        // 解析维数 exp，然后存到 dim 中
        for (ConstExpNode constExp : constExps)
        {
            constExp.buildIr();
            dims.add(((ConstInt) valueUp).getValue());
        }
        ArrayType arrayType = new ArrayType(IntType.I32, dims);
        // 全局数组 "全局变量声明中指定的初值表达式必须是常量表达式"
        if (irSymbolTable.isGlobal())
        {
            // 全局有初始值的数组
            if (initVal != null)
            {
                initVal.setDims(new ArrayList<>(dims));
                globalInitDown = true;
                initVal.buildIr();
                globalInitDown = false;

                genGlobalInitArray(valueArrayUp);
            }
            // 全局无初始值的数组，那么就初始化为 0
            // TODO 做测试
            else
            {
                ZeroInitializer zeroInitializer = new ZeroInitializer(arrayType);
                GlobalVariable globalVariable = irBuilder.buildGlobalVariable(ident.getContent(), zeroInitializer, false);
                irSymbolTable.addValue(ident.getContent(), globalVariable);
            }
        }
        // 局部数组
        else
        {
            // 分配空间并登记
            Alloca allocArray = irBuilder.buildAlloca(arrayType, curBlock);
            irSymbolTable.addValue(ident.getContent(), allocArray);

            // 有初始值的局部数组
            if (initVal != null)
            {
                initVal.setDims(new ArrayList<>(dims));
                initVal.buildIr();
                GetElementPtr basePtr = irBuilder.buildGEP(curBlock, allocArray, ConstInt.ZERO, ConstInt.ZERO);
                // 如果是一个二维数组，那么就继续 GEP，上面两步之后，basePtr 会变成一个指向具体的 int 的指针，即 int*
                // 同时 basePtr 是指向 allocArray 基地址的
                if (dims.size() > 1)
                {
                    basePtr = irBuilder.buildGEP(curBlock, basePtr, ConstInt.ZERO, ConstInt.ZERO);
                }
                // 利用 store 往内存中存值
                for (int i = 0; i < valueArrayUp.size(); i++)
                {
                    if (i == 0)
                    {
                        irBuilder.buildStore(curBlock, valueArrayUp.get(i), basePtr);
                    }
                    else
                    {
                        // 这里利用的是一维的 GEP，此时的返回值依然是 int*
                        GetElementPtr curPtr = irBuilder.buildGEP(curBlock, basePtr, new ConstInt(i));
                        irBuilder.buildStore(curBlock, valueArrayUp.get(i), curPtr);
                    }
                }
            }
        }
    }

    @Override
    public void buildIr()
    {
        // 单变量
        if (constExps.isEmpty())
        {
            genSingleVar();
        }
        // 数组
        else
        {
            genVarArray();
        }
    }
}
