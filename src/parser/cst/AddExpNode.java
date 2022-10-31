package parser.cst;

import ir.values.Value;
import lexer.token.SyntaxType;

import java.util.ArrayList;

/**
 * AddExp
 *     : MulExp (AddOp MulExp)*
 *     ;
 * AddOp
 *     : PLUS
 *     | MINUS
 *     ;
 */
public class AddExpNode extends CSTNode
{
    private final ArrayList<MulExpNode> mulExps = new ArrayList<>();
    private final ArrayList<TokenNode> addOps = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof MulExpNode)
        {
            mulExps.add((MulExpNode) child);
        }
        else if (child instanceof TokenNode)
        {
            addOps.add((TokenNode) child);
        }
    }

    @Override
    public void buildIr()
    {
        // 如果是可计算的，那么就要算出来
        if (canCalValueDown)
        {
            // 先分析一个
            mulExps.get(0).buildIr();
            int sum = valueIntUp;

            for (int i = 1; i < mulExps.size(); i++)
            {
                mulExps.get(i).buildIr();
                if (addOps.get(i - 1).isSameType(SyntaxType.PLUS))
                {
                    sum += valueIntUp;
                }
                if (addOps.get(i - 1).isSameType(SyntaxType.MINU))
                {
                    sum -= valueIntUp;
                }
            }

            valueIntUp = sum;
        }
        // 是不可直接计算的，要用表达式
        else
        {
            mulExps.get(0).buildIr();
            Value sum = valueUp;

            for (int i = 1; i < mulExps.size(); i++)
            {
                mulExps.get(i).buildIr();
                Value adder = valueUp;
                // 如果类型不对，需要先换类型
                if (sum.getValueType().isI1())
                {
                    sum = irBuilder.buildZext(curBlock, sum);
                }
                if (adder.getValueType().isI1())
                {
                    adder = irBuilder.buildZext(curBlock, adder);
                }

                if (addOps.get(i - 1).isSameType(SyntaxType.PLUS))
                {
                    sum = irBuilder.buildAdd(curBlock, sum, adder);
                }
                if (addOps.get(i - 1).isSameType(SyntaxType.MINU))
                {
                    sum = irBuilder.buildSub(curBlock, sum, adder);
                }
            }

            valueUp = sum;
        }
    }
}
