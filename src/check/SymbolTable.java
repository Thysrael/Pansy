package check;

import parser.cst.*;

import java.util.ArrayList;
import java.util.HashMap;

import static check.ErrorType.UNDEFINED_SYMBOL;

public class SymbolTable
{
    private final ArrayList<HashMap<String, SymbolInfo>> symbolTable;
    /**
     * 这个变量用于控制层数的添加
     */
    private boolean blockLayerNeedWait = false;

    public SymbolTable()
    {
        symbolTable = new ArrayList<>();
        // 先放上一层
        symbolTable.add(new HashMap<>());
    }

    private HashMap<String, SymbolInfo> getTableTop()
    {
        return symbolTable.get(symbolTable.size() - 1);
    }

    /**
     * 如果 symbolTable 顶层已经有了这个 ident 那么就是重定义
     * @param ident 标识符
     * @return true 则重定义
     */
    public boolean isSymbolRedefined(String ident)
    {
        HashMap<String, SymbolInfo> top = getTableTop();

        return top.containsKey(ident);
    }

    public void addConst(ConstDefNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(0);
        String ident = identNode.getContent();
        HashMap<String, SymbolInfo> top = getTableTop();
        top.put(ident, new VarInfo(ctx));
    }

    public void addVar(VarDefNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(0);
        String ident = identNode.getContent();
        HashMap<String, SymbolInfo> top = getTableTop();
        top.put(ident, new VarInfo(ctx));
    }

    public void addParam(FuncFParamNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(1);
        String ident = identNode.getContent();
        HashMap<String, SymbolInfo> top = getTableTop();
        top.put(ident, new VarInfo(ctx));
    }

    public void addFunc(FuncDefNode ctx)
    {
        TokenNode identNode = ((TokenNode) ctx.getChildren().get(1));
        String name = identNode.getContent();
        HashMap<String, SymbolInfo> top = getTableTop();
        top.put(name, new FuncInfo(ctx));
    }

    public void addMainFunc(MainFuncDefNode ctx)
    {
        String name = "main";
        HashMap<String, SymbolInfo> top = getTableTop();
        top.put(name, new FuncInfo(ctx));
    }

    /**
     * 根据标识符获得 SymbolInfo 信息
     * @param ident 标识符
     * @throws PansyException 如果没有查到，那么就需要报 UNDEFINED_SYMBOL
     */
    public SymbolInfo getSymbolInfo(String ident) throws PansyException
    {
        for (int i = symbolTable.size() - 1; i >= 0; i--)
        {
            HashMap<String, SymbolInfo> layer = symbolTable.get(i);
            if (layer.containsKey(ident))
            {
                return layer.get(ident);
            }
        }

        throw new PansyException(UNDEFINED_SYMBOL);
    }

    /**
     * 对 getSymbolInfo 封装，加入了类型检查
     * @param ident 标识符
     * @return 函数信息表
     * @throws PansyException 如果没有查到，那么就需要报 UNDEFINED_SYMBOL
     */
    public FuncInfo getFuncInfo(String ident) throws PansyException
    {
        SymbolInfo symbolInfo = getSymbolInfo(ident);
        if (symbolInfo instanceof FuncInfo)
        {
            return (FuncInfo) symbolInfo;
        }
        throw new PansyException(UNDEFINED_SYMBOL);
    }

    /**
     * 用于增加一层
     * 需要注意的是，加一层的操作需要由调用者确定，而非被调用者确定，这是因为很多神奇的考虑
     */
    public void addFuncLayer()
    {
        symbolTable.add(new HashMap<>());
        this.blockLayerNeedWait = true;
    }

    /**
     * 用于减少一层
     */
    public void removeFuncLayer()
    {
        symbolTable.remove(symbolTable.size() - 1);
        this.blockLayerNeedWait = false;
    }

    public void addBlockLayer()
    {
        if (blockLayerNeedWait)
        {
            blockLayerNeedWait = false;
        }
        else
        {
            symbolTable.add(new HashMap<>());
        }
    }

    public void removeBlockLayer()
    {
        // 第 1 层是 global，第 2 层是 function，之后才是 block 层
        if (symbolTable.size() > 2)
        {
            symbolTable.remove(symbolTable.size() - 1);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (HashMap<String, SymbolInfo> layer : symbolTable)
        {
            s.append(layer).append("\n");
        }
        return s.toString();
    }
}
