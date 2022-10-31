package parser.cst;

import ir.values.BasicBlock;
import middle.symbol.SymbolTable;

/**
 * WhileStmt
 *     : WHILE_KW L_PAREN Cond R_PAREN Stmt
 *     ;
 */
public class WhileStmtNode extends CSTNode
{
    private CondNode cond = null;
    private StmtNode stmt = null;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof CondNode)
        {
            cond = (CondNode) child;
        }
        if (child instanceof StmtNode)
        {
            stmt = (StmtNode) child;
        }
    }

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

    @Override
    public void buildIr()
    {
        // while 涉及 3 个块
        // cond 块负责条件判断和跳转，如果是 true 则进入 bodyBlock，如果是 false 就进入 nextBlock，结束 while 语句
        BasicBlock condBlock = irBuilder.buildBlock(curFunc);
        // body 块是循环的主题
        BasicBlock bodyBlock = irBuilder.buildBlock(curFunc);
        // nextBlock 意味着循环的结束
        BasicBlock nextBlock = irBuilder.buildBlock(curFunc);

        loopCondBlockDown.push(condBlock);
        loopNextBlockDown.push(nextBlock);

        // 先由 curBlock 进入 condBlock
        irBuilder.buildBr(curBlock, condBlock);
        // build condBlock，有趣的是不需要再加入条件 Br，这是因为这个 Br 在 LAndExp 短路求值的时候加了
        cond.setTrueBlock(bodyBlock);
        cond.setFalseBlock(nextBlock);
        curBlock = condBlock;
        cond.buildIr();

        curBlock = bodyBlock;
        stmt.buildIr();
        irBuilder.buildBr(curBlock, condBlock);

        loopNextBlockDown.pop();
        loopCondBlockDown.pop();

        curBlock = nextBlock;
    }
}
