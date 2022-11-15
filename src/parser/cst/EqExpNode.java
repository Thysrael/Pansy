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
     * EqExp 是最高级的表达式，因为 LOr 和 LAnd 被翻译成了短路求值，也就是利用了 Br
     * 而 Br 要求输入是 i1，所以这就要求 EqExp 的输出是 i1
     * 与此同时，EqExp 的输入可能是 i32(AddExp 及以下) 或者是 i1 (RelExp)
     * 而翻译 EqExp 需要利用 icmp i32，所以这里才是真正需要 Zext 的地方，感觉 AddExp 反而不需要
     * RelExp 也不需要，因为它的输入一定是来自 AddExp i32
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

            if (eqOps.get(i - 1).isSameType(SyntaxType.EQL))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.EQ, result, adder);
            }
            else if (eqOps.get(i - 1).isSameType(SyntaxType.NEQ))
            {
                result = irBuilder.buildIcmp(curBlock, Icmp.Condition.NE, result, adder);
            }
        }

        valueUp = result;
    }
}
