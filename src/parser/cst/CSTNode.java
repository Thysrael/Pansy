package parser.cst;

import check.Checker;
import check.CheckDataType;
import check.PansyException;
import ir.IrBuilder;
import ir.IrSymbolTable;
import ir.types.DataType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import check.FuncInfo;
import check.SymbolTable;

import java.util.ArrayList;
import java.util.Stack;

public abstract class CSTNode
{
    /*================================= 错误检测 =================================*/
    /**
     * 用来存储语义分析中产生的错误
     */
    protected static final ArrayList<PansyException> errors = Checker.errors;
    /**
     * 用来存储检测日志
     */
    protected static final ArrayList<String> checkLog = Checker.checkLog;
    /**
     * 当前函数信息
     */
    protected static FuncInfo curFuncInfo = null;
    /**
     * 是否在循环中
     */
    protected static int inLoop = 0;

    /*================================ 中间代码转换 ================================*/
    /**
     * 这两个栈用于方便 break 和 continue 确定自己的跳转目标，因为 loop 可能嵌套，
     * 为了避免外层 loop 的信息被内层 loop 覆盖，所以采用了栈结构
     */
    protected static final Stack<BasicBlock> loopCondBlockDown = new Stack<>();
    protected static final Stack<BasicBlock> loopNextBlockDown = new Stack<>();
    protected static final IrSymbolTable irSymbolTable = new IrSymbolTable();
    protected static final IrBuilder irBuilder = IrBuilder.getInstance();
    /**
     * 综合属性：各种 buildIr 的结果(单值形式)如果会被其更高的节点应用，那么需要利用这个值进行通信
     */
    protected static Value valueUp;
    /**
     * 综合属性：返回值是一个 int ，其实本质上将其包装成 ConstInt 就可以通过 valueUp 返回，但是这样返回更加简便
     * 可以说有的时候不能局限于某种形式的统一性
     */
    protected static int valueIntUp = 0;
    /**
     * 综合属性：各种 buildIr 的结果(数组形式)如果会被其更高的节点应用，那么需要利用这个值进行通信
     */
    protected static ArrayList<Value> valueArrayUp = new ArrayList<>();
    /**
     * 综合属性：函数的参数类型组通过这个上传
     */
    protected static ArrayList<DataType> argTypeArrayUp = new ArrayList<>();
    /**
     * 综合属性：函数的参数类型通过这个上传
     */
    protected static DataType argTypeUp = null;
    /**
     * 综合属性：用来确定当前条件判断中是否是这种情况 if(3)，对于这种情况，需要多加入一条 Icmp
     */
    protected static boolean i32InRelUp;
    /**
     * 继承属性：说明进行全局初始化
     */
    protected static boolean globalInitDown = false;
    /**
     * 继承属性：说明当前表达式可求值，进而可以说明此时的返回值是 valueIntUp
     */
    protected static boolean canCalValueDown = false;
    /**
     * 继承属性：在 build 实参的时候用的，对于 PrimaryExp，会有一个 Load LVal 的动作
     * （默认，因为 PrimaryExp 本质是“读” LVal，而 SySy 中没有指针类型，所以只要是读，所以一定不会有指针类型，所以 Load 就是必要的）
     * 而当 PrimaryExp 作为实参的时候，如果实参需要的是一个指针，那么就不需要 load
     */
    protected static boolean paramDontNeedLoadDown = false;
    /**
     * build 的当前函数
     */
    protected static Function curFunc = null;
    /**
     * build 的当前基本块
     */
    protected static BasicBlock curBlock = null;


    /*================================ 内部属性定义 ================================*/

    protected final ArrayList<CSTNode> children = new ArrayList<>();

    public void addChild(CSTNode child)
    {
        children.add(child);
    }

    public ArrayList<CSTNode> getChildren()
    {
        return children;
    }

    protected void addCheckLog()
    {
        checkLog.add("[" + this.getClass() + "]");
    }

    /**
     * 最基础的检测方法，就是检测让每个子节点进行检测
     * @param symbolTable 符号表
     */
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }

    /**
     * 获得数据类型
     * 会遍历所有的孩子节点，然后找到最高的
     * 如果没有的话，那么就会变成 VOID
     * 合情合理有没有
     * @param symbolTable 符号表
     * @return 数据类型
     */
    public CheckDataType getDataType(SymbolTable symbolTable)
    {
        CheckDataType type = CheckDataType.VOID;

        for (CSTNode child : children)
        {
            CheckDataType childType = child.getDataType(symbolTable);
            if (childType.compareTo(type) > 0)
            {
                type = childType;
            }
        }

        return type;
    }

    public void buildIr()
    {
        children.forEach(CSTNode::buildIr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (CSTNode child : children)
        {
            sb.append(child).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
