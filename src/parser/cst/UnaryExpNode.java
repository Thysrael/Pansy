package parser.cst;

import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Icmp;

/**
 * UnaryExp
 *     : PrimaryExp
 *     | Callee
 *     | UnaryOp UnaryExp
 *     ;
 */
public class UnaryExpNode extends CSTNode
{
    private PrimaryExpNode primaryExp = null;
    private UnaryOpNode unaryOp = null;
    private UnaryExpNode unaryExp = null;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof PrimaryExpNode)
        {
            primaryExp = (PrimaryExpNode) child;
        }
        if (child instanceof UnaryOpNode)
        {
            unaryOp = (UnaryOpNode) child;
        }
        if (child instanceof UnaryExpNode)
        {
            unaryExp = (UnaryExpNode) child;
        }
    }

    @Override
    public void buildIr()
    {
        // 可计算的情况只有 unaryExp 和 PrimaryExp 两种
        if (canCalValueDown)
        {
            // 处理符号即可
            if (unaryExp != null)
            {
                unaryExp.buildIr();
                if (unaryOp.isMinus())
                {
                    valueIntUp = -valueIntUp;
                }
                if (unaryOp.isNot())
                {
                    valueIntUp = valueIntUp == 0 ? 1 : 0;
                }
            }
            // 那么就是 primaryExp 的情况了，已经不需要管了
            else
            {
                primaryExp.buildIr();
            }
        }
        // 不可计算的情况
        else
        {
            if (unaryExp != null)
            {
                unaryExp.buildIr();
                Value unaryValue = valueUp;
                // 先拓展
                if (unaryValue.getValueType().isI1())
                {
                    unaryValue = irBuilder.buildZext(curBlock, unaryValue);
                }

                if (unaryOp.isNot())
                {
                    valueUp = irBuilder.buildIcmp(curBlock, Icmp.Condition.EQ, unaryValue, ConstInt.ZERO);
                }
                else if (unaryOp.isMinus())
                {
                    valueUp = irBuilder.buildSub(curBlock, ConstInt.ZERO, unaryValue);
                }
            }
            // callee 和 primary 两种情况不需要考虑
            else
            {
                children.forEach(CSTNode::buildIr);
            }
        }
    }
}
