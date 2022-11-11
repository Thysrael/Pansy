package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.SymbolInfo;
import ir.values.Function;
import ir.values.Value;
import ir.values.instructions.Call;
import check.SymbolTable;

import java.util.ArrayList;

/**
 * InStmt
 * 	    : LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
 *     ;
 */
public class InStmtNode extends CSTNode
{
    private LValNode lVal = null;

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof LValNode)
        {
            lVal = (LValNode) child;
        }
    }

    /**
     * 基本上和 assign 的检测一模一样
     * 都是检测 lVal 是否是常量
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode identNode = (TokenNode) lVal.getChildren().get(0);
        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            // 如果修改了常量，那么就要报错
            if (symbolInfo.isConst())
            {
                errors.add(new PansyException(ErrorType.CHANGE_CONST, identNode.getLine()));
            }
        }
        // 没有定义 lVal
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
    public void buildIr()
    {
        lVal.buildIr();
        Value target = valueUp;
        Call source = irBuilder.buildCall(curBlock, Function.getint, new ArrayList<>());

        // 最后是以一个 store 结尾的，说明将其存入内存，就算完成了赋值
        irBuilder.buildStore(curBlock, source, target);
    }
}
