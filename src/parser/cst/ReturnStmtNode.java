package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.CheckDataType;
import ir.values.BasicBlock;
import middle.symbol.SymbolTable;

/**
 * ReturnStmt
 *     : RETURN_KW (Exp)? SEMICOLON
 *     ;
 */
public class ReturnStmtNode extends CSTNode
{
    private ExpNode exp = null;

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ExpNode)
        {
            exp = (ExpNode) child;
        }
    }

    /**
     * 无返回值的函数存在错误的返回值，也就是 return 后有 exp
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode tokenNode = ((TokenNode) children.get(0));
        if (curFuncInfo.getReturnType().equals(CheckDataType.VOID))
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

    @Override
    public void buildIr()
    {
        if (exp != null)
        {
            exp.buildIr();
            irBuilder.buildRet(curBlock, valueUp);
            curBlock = new BasicBlock();
        }
        else
        {
            irBuilder.buildRet(curBlock);
            curBlock = new BasicBlock();
        }
    }
}
