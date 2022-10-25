package parser.cst;

import middle.symbol.SymbolTable;

/**
 * WhileStmt
 *     : WHILE_KW L_PAREN Cond R_PAREN Stmt
 *     ;
 */
public class WhileStmtNode extends CSTNode
{
    /**
     * 之所以需要这个，是因为要看是否在循环中
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        inLoop++;
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        inLoop--;
    }
}
