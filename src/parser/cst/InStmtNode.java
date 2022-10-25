package parser.cst;

import middle.symbol.SymbolTable;

/**
 * InStmt
 * 	    : LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
 *     ;
 */
public class InStmtNode extends CSTNode
{
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
}
