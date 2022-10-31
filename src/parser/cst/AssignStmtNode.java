package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.values.Value;
import middle.symbol.ConstInfo;
import middle.symbol.SymbolInfo;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * AssignStmt
 *     : LVal ASSIGN Exp SEMICOLON
 *     ;
 */
public class AssignStmtNode extends CSTNode
{
    private LValNode lVal;
    private ExpNode exp;
    private final ArrayList<TokenNode> tokens = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof LValNode)
        {
            lVal = (LValNode) child;
        }
        else if (child instanceof ExpNode)
        {
            exp = (ExpNode) child;
        }
        else
        {
            tokens.add((TokenNode) child);
        }
    }

    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        LValNode lValNode = (LValNode) children.get(0);
        TokenNode identNode = (TokenNode) lValNode.getChildren().get(0);
        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            if (symbolInfo instanceof ConstInfo)
            {
                errors.add(new PansyException(ErrorType.CHANGE_CONST, identNode.getLine()));
            }
        }
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
        exp.buildIr();
        Value source = valueUp;

        // 最后是以一个 store 结尾的，说明将其存入内存，就算完成了赋值
        irBuilder.buildStore(curBlock, source, target);
    }
}
