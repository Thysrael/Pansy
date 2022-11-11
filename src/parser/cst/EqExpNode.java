package parser.cst;

import ir.values.Value;
import ir.values.instructions.Icmp;
import lexer.token.SyntaxType;

import java.util.ArrayList;

/**
 * EqExp
 *     : RelExp (EqOp RelExp)*
 *     ;
 *  EqOp
 *     : EQ
 *     | NEQ
 *     ;
 */
public class EqExpNode extends CSTNode
{
    private final ArrayList<RelExpNode> relExps = new ArrayList<>();
    private final ArrayList<TokenNode> eqOps = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof RelExpNode)
        {
            relExps.add((RelExpNode) child);
        }
        if (child instanceof TokenNode)
        {
            eqOps.add((TokenNode) child);
        }
    }

    /**
     * 用 Icmp 将指令连缀起来
     */
    @Override
    public void buildIr()
    {
        relExps.get(0).buildIr();
        Value result = valueUp;

        for (int i = 1; i < relExps.size(); i++)
        {
            i32InRelUp = false;
            relExps.get(i).buildIr();
            if (eqOps.get(i - 1).isSameType(SyntaxType.EQL))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.EQ, result, valueUp);
            }
            else if (eqOps.get(i - 1).isSameType(SyntaxType.NEQ))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.NE, result, valueUp);
            }
        }

        valueUp = result;
    }
}
