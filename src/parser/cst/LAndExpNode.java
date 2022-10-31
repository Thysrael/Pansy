package parser.cst;

import ir.values.BasicBlock;
import ir.values.constants.ConstInt;
import ir.values.instructions.Icmp;

import java.util.ArrayList;

/**
 * LAndExp
 *     : EqExp (AND EqExp)*
 *     ;
 */
public class LAndExpNode extends CSTNode
{
    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;
    private final ArrayList<EqExpNode> eqExps = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof EqExpNode)
        {
            eqExps.add((EqExpNode) child);
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

    @Override
    public void buildIr()
    {
        for (EqExpNode eqExp : eqExps)
        {
            BasicBlock nextBlock = irBuilder.buildBlock(curFunc);
            i32InRelUp = true;
            eqExp.buildIr();
            // 在这里，将某个为 I32 的 eqExp 变成 I1
            if (i32InRelUp)
            {
                i32InRelUp = false;
                valueUp = irBuilder.buildIcmp(curBlock, Icmp.Condition.NE, valueUp, ConstInt.ZERO);
            }
            // 错了就直接进入 falseBlock
            irBuilder.buildBr(curBlock, valueUp, nextBlock, falseBlock);
            curBlock = nextBlock;
        }
        irBuilder.buildBr(curBlock, trueBlock);
    }
}
