package parser.cst;

import check.ErrorType;
import check.PansyException;
import middle.symbol.ConstInfo;
import middle.symbol.SymbolInfo;
import middle.symbol.SymbolTable;

/**
 * AssignStmt
 *     : LVal ASSIGN Exp SEMICOLON
 *     ;
 */
public class AssignStmtNode extends CSTNode
{
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
}
