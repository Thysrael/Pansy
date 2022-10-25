package parser.cst;

import check.DataType;
import check.ErrorType;
import check.PansyException;
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
    private final ArrayList<ExpNode> indexes = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ExpNode)
        {
            indexes.add((ExpNode) child);
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
    public DataType getDataType(SymbolTable symbolTable)
    {
        TokenNode identNode = (TokenNode) children.get(0);

        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            // 如果左值是一个常量
            if (symbolInfo instanceof ConstInfo)
            {
                DataType rawType = ((ConstInfo) symbolInfo).getDataType();
                return calcDataType(rawType);
            }
            // 如果左值是一个变量
            else if (symbolInfo instanceof VarInfo)
            {
                DataType rawType = ((VarInfo) symbolInfo).getDataType();
                return calcDataType(rawType);
            }

            return DataType.VOID;
        }
        // 变量未定义，因为如果没有定义，那么就在 LVal.check 的时候报错了，至于这里获得不了真正的信息了，无所谓。
        catch (PansyException e)
        {
            return DataType.VOID;
        }
    }

    private DataType calcDataType(DataType rawType)
    {
        int calDim = indexes.size();
        int rawDim;
        if (rawType.equals(DataType.INT))
        {
            rawDim = 0;
        }
        else if (rawType.equals(DataType.DIM1))
        {
            rawDim = 1;
        }
        else if (rawType.equals(DataType.DIM2))
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
            return DataType.INT;
        }
        else if (trueDim == 1)
        {
            return DataType.DIM1;
        }
        else if (trueDim == 2)
        {
            return DataType.DIM2;
        }
        else
        {
            return DataType.VOID;
        }
    }
}
