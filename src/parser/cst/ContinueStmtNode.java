package parser.cst;

import check.ErrorType;
import check.PansyException;
import middle.symbol.SymbolTable;

/**
 * ContinueStmt
 *     : CONTINUE_KW SEMICOLON
 *     ;
 */
public class ContinueStmtNode extends CSTNode
{
    /**
     * 检验是否在循环里
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode tokenNode = (TokenNode) children.get(0);

        if (inLoop <= 0)
        {
            errors.add(new PansyException(ErrorType.NON_LOOP_STMT, tokenNode.getLine()));
        }

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }
}
