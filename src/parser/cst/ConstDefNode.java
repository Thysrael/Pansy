package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.GlobalVariable;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.Alloca;
import ir.values.instructions.GetElementPtr;
import check.SymbolTable;

import java.util.ArrayList;

/**
 * ConstDef
 *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
 *     ;
 */
public class ConstDefNode extends CSTNode
{
    private TokenNode ident;
    private final ArrayList<ConstExpNode> constExps = new ArrayList<>();
    private ConstInitValNode constInitVal;
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
        if (child instanceof ConstInitValNode)
        {
            constInitVal = (ConstInitValNode) child;
        }
    }

    /**
     * 两种错误，
     * 一种是标识符命名重复
     * 一种是缺失右中括号
     * 这里需要增加对于常量的定义
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        // 标识符命名重复
        if (symbolTable.isSymbolRedefined(ident.getContent()))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, ident.getLine()));
        }
        // 缺失右中括号
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        // 加入常量的定义，更新符号表
        symbolTable.addConst(this);
    }

    /**
     * 没有必要为单常量分配栈空间，但是有必要为常量数组分配空间，
     * 这是因为常量数组是支持变量访存的，而变量是在编译器没法求职的，对于一个 constArray[var]
     * 是没法确定其值的，所以只能将常量当做变量看待。
     */
    private void genConstArray()
    {
        // 解析维数 exp，然后存到 dim 中
        for (ConstExpNode constExp : constExps)
        {
            constExp.buildIr();
            dims.add(((ConstInt) valueUp).getValue());
        }

        // 方便之后的对于 initValue 的分析
        constInitVal.setDims(dims);
        // 分析 initValue
        globalInitDown = true;
        constInitVal.buildIr();
        globalInitDown = false;

        // 如果是全局数组，那么是不需要 alloca 指令的，本质是其在静态区
        if (irSymbolTable.isGlobal())
        {
            // 加入全局变量
            GlobalVariable globalVariable = irBuilder.
                    buildGlobalVariable(ident.getContent(), (Constant) valueUp, true);
            // 登记到符号表中
            irSymbolTable.addValue(ident.getContent(), globalVariable);
        }
        // 如果是局部数组
        else
        {
            // 根据维数信息创建数组标签，之前不用是因为在 constInitVal 中递归生成了
            ArrayType arrayType = new ArrayType(IntType.I32, dims);
            // alloca 指令诞生了！
            // alloca will be moved to first bb in cur function
            // alloca 的指针就是指向这个数组的指针
            Alloca allocArray = irBuilder.buildConstAlloca(arrayType, curBlock, (ConstArray) valueUp);
            // 登记符号表
            irSymbolTable.addValue(ident.getContent(), allocArray);

            // 获得一个指针，这个指针指向初始化数组的一个元素
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
    @Override
    public void buildIr()
    {
        // 获得常量的名字
        String ident = this.ident.getContent();
        // 是单变量
        if (constExps.isEmpty())
        {
            constInitVal.buildIr();
            // 这里很有趣，单变量是不算 GlobalVariable 的，因为没啥意义，只放在符号表中
            // 常量在符号表中对应一个 ConstInt 值，哪怕是在局部，也是不分配栈上空间的
            irSymbolTable.addValue(ident, valueUp);
        }
        // 常量数组
        else
        {
            genConstArray();
        }
    }
}
