package parser.cst;


import java.util.ArrayList;

/**
 * FuncRParams
 *     : Exp (COMMA Exp)*
 *     ;
 */
public class FuncRParamsNode extends CSTNode
{
    private final ArrayList<ExpNode> exps = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ExpNode)
        {
            exps.add((ExpNode) child);
        }
    }

    public ArrayList<ExpNode> getParams()
    {
        return exps;
    }
}
