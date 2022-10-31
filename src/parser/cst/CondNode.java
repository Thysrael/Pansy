package parser.cst;

import ir.values.BasicBlock;

/**
 * Cond
 *     : LOrExp
 *     ;
 */
public class CondNode extends CSTNode
{
    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock)
    {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock)
    {
        this.falseBlock = falseBlock;
    }

    @Override
    public void buildIr()
    {
        LOrExpNode lOrExp = (LOrExpNode) children.get(0);
        lOrExp.setTrueBlock(trueBlock);
        lOrExp.setFalseBlock(falseBlock);
        lOrExp.buildIr();
    }
}
