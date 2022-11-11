package parser.cst;

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

    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        isWriteLVal = true;
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        isWriteLVal = false;
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
