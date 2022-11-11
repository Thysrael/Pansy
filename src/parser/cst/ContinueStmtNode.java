package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.values.BasicBlock;
import check.SymbolTable;

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

    @Override
    public void buildIr()
    {
        irBuilder.buildBr(curBlock, loopCondBlockDown.peek());
        // 这里只用于占位，从此之后，continue 后的语句都会被登记到一个不知道是啥的 block 上去
        curBlock = new BasicBlock();
    }
}
