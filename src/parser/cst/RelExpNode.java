package parser.cst;


import ir.values.Value;
import ir.values.instructions.Icmp;
import lexer.token.SyntaxType;

import java.util.ArrayList;

/**
 * RelExp
 *     : AddExp (RelOp AddExp)*
 *     ;
 * RelOp
 *     : LT
 *     | GT
 *     | LE
 *     | GE
 *     ;
 */
public class RelExpNode extends CSTNode
{
    private final ArrayList<AddExpNode> addExps = new ArrayList<>();
    private final ArrayList<TokenNode> relOps = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof AddExpNode)
        {
            addExps.add((AddExpNode) child);
        }
        if (child instanceof TokenNode)
        {
            relOps.add((TokenNode) child);
        }
    }

    @Override
    public void buildIr()
    {
        addExps.get(0).buildIr();
        Value result = valueUp;

        for (int i = 1; i < addExps.size(); i++)
        {
            i32InRelUp = false;
            addExps.get(i).buildIr();
            Value adder = valueUp;
            // 如果类型不对，需要先换类型
            if (result.getValueType().isI1())
            {
                result = irBuilder.buildZext(curBlock, result);
            }
            if (adder.getValueType().isI1())
            {
                adder = irBuilder.buildZext(curBlock, adder);
            }

            if (relOps.get(i - 1).isSameType(SyntaxType.LEQ))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.LE, result, adder);
            }
            else if (relOps.get(i - 1).isSameType(SyntaxType.GEQ))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.GE, result, adder);
            }
            else if (relOps.get(i - 1).isSameType(SyntaxType.GRE))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.GT, result, adder);
            }
            else if (relOps.get(i - 1).isSameType(SyntaxType.LSS))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.LT, result, adder);
            }
        }

        valueUp = result;
    }
}
