package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.CheckDataType;
import ir.values.BasicBlock;
import check.SymbolTable;

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
        // 这个是 return_token 因为报错需要行号
        TokenNode tokenNode = ((TokenNode) children.get(0));
        // 当前函数的类型是 void
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

    /**
     * 这里也有一个和 Break 类似的操作，不知道合不合理
     */
    @Override
    public void buildIr()
    {
        if (exp != null)
        {
            exp.buildIr();
            irBuilder.buildRet(curBlock, valueUp);
        }
        else
        {
            irBuilder.buildRet(curBlock);
        }
        curBlock = new BasicBlock();
    }
}
