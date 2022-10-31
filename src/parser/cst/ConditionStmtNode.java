package parser.cst;

import ir.values.BasicBlock;

import java.util.ArrayList;

/**
 * ConditionStmt
 *     : IF_KW L_PAREN Cond R_PAREN Stmt (ELSE_KW Stmt)?
 *     ;
 */
public class ConditionStmtNode extends CSTNode
{
    private CondNode cond;
    private final ArrayList<StmtNode> stmts = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof CondNode)
        {
            cond = (CondNode) child;
        }
        else if (child instanceof StmtNode)
        {
            stmts.add((StmtNode) child);
        }
    }

    @Override
    public void buildIr()
    {
        BasicBlock trueBlock = irBuilder.buildBlock(curFunc);
        BasicBlock nextBlock = irBuilder.buildBlock(curFunc);
        BasicBlock falseBlock = stmts.size() == 1 ? nextBlock : irBuilder.buildBlock(curFunc);

        cond.setFalseBlock(falseBlock);
        cond.setTrueBlock(trueBlock);

        cond.buildIr();
        curBlock = trueBlock;
        // 遍历 if 块
        stmts.get(0).buildIr();
        // 直接跳转到 nextBlock，这是不言而喻的，因为 trueBlock 执行完就是 nextBlock
        irBuilder.buildBr(curBlock, nextBlock);


        // 对应有 else 的情况
        if (stmts.size() == 2)
        {
            curBlock = falseBlock;
            stmts.get(1).buildIr();
            irBuilder.buildBr(curBlock, nextBlock);
        }

        // 最终到了 nextBlock
        curBlock = nextBlock;
    }
}
