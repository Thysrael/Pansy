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

    public String display()
    {
        StringBuilder s = new StringBuilder();
        Set<PansyException> set = new TreeSet<>((o1, o2) -> -o2.compareTo(o1));
        set.addAll(errors);
        for (PansyException error : set)
        {
            s.append(error.getLine()).append(" ").append(errorMap.get(error.getType())).append("\n");
        }
        return s.toString();
    }
}
