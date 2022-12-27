package check;


import parser.cst.CSTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static check.ErrorType.*;


public class Checker
{
    /**
     * 用来存储语义分析中产生的错误
     */
    public static final ArrayList<PansyException> errors = new ArrayList<>();
    /**
     * 用来存储检测日志
     */
    public static final ArrayList<String> checkLog = new ArrayList<>();
    /**
     * 这里将错误类型映射成字母，如果新增添数据类型，那么就需要在这里处理
     */
    private static final HashMap<ErrorType, String> errorMap = new HashMap<>();

    static
    {
        errorMap.put(ILLEGAL_SYMBOL, "a");
        errorMap.put(REDEFINED_SYMBOL, "b");
        errorMap.put(UNDEFINED_SYMBOL, "c");
        errorMap.put(ARG_NUM_MISMATCH, "d");
        errorMap.put(ARG_TYPE_MISMATCH, "e");
        errorMap.put(VOID_RETURN_VALUE, "f");
        errorMap.put(MISS_RETURN, "g");
        errorMap.put(CHANGE_CONST, "h");
        errorMap.put(MISS_SEMICOLON, "i");
        errorMap.put(MISS_RPARENT, "j");
        errorMap.put(MISS_RBRACK, "k");
        errorMap.put(FORM_STRING_MISMATCH, "l");
        errorMap.put(NON_LOOP_STMT, "m");
        errorMap.put(LEX_ERROR, "u");
        errorMap.put(PARSE_ERROR, "u");
    }
    /**
     * 为 CST 的根节点
     */
    private final CSTNode root;
    /**
     * 检测的时候用到的符号表
     */
    private final SymbolTable symbolTable;

    public Checker(CSTNode root)
    {
        this.root = root;
        this.symbolTable = new SymbolTable();
    }

    /**
     * 逻辑与语法分析类似，如果发生了预料之外的异常，那么就退出，并且打印检查顺序
     */
    public void run()
    {
        try
        {
            root.check(symbolTable);
        }
        catch (Exception e)
        {
            System.err.println("Check exception happened.");
            for (String log : checkLog)
            {
                System.err.println(log);
            }
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 这里的逻辑比较复杂，errors 作为一个数组，可能有多个相同的错误（比如 LValNode 检查了一遍，InStmt 又检查了一遍）
     * 而且报错的函数也并不非递减排列的，所以需要利用一个 TreeSet 进行去重和排序
     * 之后输出的是这个 TreeSet 的内容
     * @return 检查的字符串
     */
    public String display()
    {
        StringBuilder s = new StringBuilder();
        // 进行去重和排序
        Set<PansyException> set = new TreeSet<>((o1, o2) -> -o2.compareTo(o1));
        set.addAll(errors);
        for (PansyException error : set)
        {
            s.append(error.getLine()).append(" ").append(errorMap.get(error.getType())).append("\n");
        }
        return s.toString();
    }
}
