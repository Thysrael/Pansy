package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.values.BasicBlock;
import check.SymbolTable;

/**
 * BreakStmt
 *     : BREAK_KW SEMICOLON
 *     ;
 */
public class BreakStmtNode extends CSTNode
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

    /**
     * 首先先做一个跳转，来跳到 loop 的下一块
     * 然后，新作了一个块，用于使 break 后面的指令依附其上，然后失效
     */
    @Override
    public void buildIr()
    {
        irBuilder.buildBr(curBlock, loopNextBlockDown.peek());
        curBlock = new BasicBlock();
    }
}
