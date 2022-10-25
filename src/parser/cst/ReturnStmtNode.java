package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.DataType;
import middle.symbol.SymbolTable;

/**
 * ReturnStmt
 *     : RETURN_KW (Exp)? SEMICOLON
 *     ;
 */
public class ReturnStmtNode extends CSTNode
{
    /**
     * 无返回值的函数存在错误的返回值，也就是 return 后有 exp
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode tokenNode = ((TokenNode) children.get(0));
        if (curFuncInfo.getReturnType().equals(DataType.VOID))
        {
            if (children.size() > 2)
            {
                errors.add(new PansyException(ErrorType.VOID_RETURN_VALUE, tokenNode.getLine()));
            }
        }

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }
}
