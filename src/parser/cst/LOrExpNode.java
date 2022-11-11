package parser.cst;

import ir.values.BasicBlock;

import java.util.ArrayList;

/**
 * LOrExp
 *     : LAndExp (OR LAndExp)*
 *     ;
 */
public class LOrExpNode extends CSTNode
{
    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;
    private final ArrayList<LAndExpNode> lAndExps = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof LAndExpNode)
        {
            lAndExps.add((LAndExpNode) child);
        }
    }

    public void setTrueBlock(BasicBlock trueBlock)
    {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock)
    {
        this.falseBlock = falseBlock;
    }

    /**
     * 所谓的短路求值，描述的是这样的一个过程，对于由 && 连接的多个表达式
     * 当某个表达式被求值的时候，如果是 true，那么不会直接进入 trueBlock，
     * 而是会进入一个新的 block，在这个 block 里会判断下一个表达式
     * 但是如果是 false，那么就会直接进入 falseBlock
     * 对于 || 连接的多个表达式
     * 当某个表达式被求值的时候，如果是 true，那么会直接进入 trueBlock，
     * 如果是 false，则会进入一个新的 block 判断下一个表达式
     */
    @Override
    public void buildIr()
    {
        // TODO 突然意识到，break 后接 if 可能导致新的块又被造了出来，这些指令都是没有用的，不过似乎影响不大，起码基本块的性质保证了
        for (int i = 0; i < lAndExps.size() - 1; i++)
        {
            LAndExpNode lAndExp = lAndExps.get(i);
            lAndExp.setTrueBlock(trueBlock);

            BasicBlock nextBlock = irBuilder.buildBlock(curFunc);
            lAndExp.setFalseBlock(nextBlock);
            lAndExp.buildIr();
            curBlock = nextBlock;
        }

        LAndExpNode tailExp = lAndExps.get(lAndExps.size() - 1);
        tailExp.setTrueBlock(trueBlock);
        tailExp.setFalseBlock(falseBlock);
        tailExp.buildIr();
    }
}
