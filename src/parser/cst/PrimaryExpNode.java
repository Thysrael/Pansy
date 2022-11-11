package parser.cst;

import ir.types.IntType;

/**
 * PrimaryExp
 *     : L_PAREN Exp R_PAREN
 *     | LVal
 *     | Number
 *     ;
 */
public class PrimaryExpNode extends CSTNode
{
    private ExpNode exp = null;
    private LValNode lVal = null;
    private NumberNode number = null;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ExpNode)
        {
            exp = (ExpNode) child;
        }
        if (child instanceof LValNode)
        {
            lVal = (LValNode) child;
        }
        if (child instanceof NumberNode)
        {
            number = (NumberNode) child;
        }
    }

    /**
     * 如果是可以计算的，那么忽视这一层即可
     * 如果是不可计算的，那么大概率有两种情况
     * (Exp)，那么就直接继续递归即可
     * lVal，那么需要在这里完成加载（将指针指向的内容搞出来）
     */
    @Override
    public void buildIr()
    {
        if (canCalValueDown)
        {
            children.forEach(CSTNode::buildIr);
        }
        else
        {
            if (exp != null)
            {
                exp.buildIr();
            }
            else if (lVal != null)
            {
                // 这个变量控制不要加载
                if (paramDontNeedLoadDown)
                {
                    paramDontNeedLoadDown = false;
                    lVal.buildIr();
                }
                else
                {
                    lVal.buildIr();
                    // 如果左值是一个 int 常量，那么就不用加载了
                    // 现在这种情况，说明是个指针，指针一般说明是局部变量，那么此时需要加载了
                    if (!(valueUp.getValueType() instanceof IntType))
                    {
                        valueUp = irBuilder.buildLoad(curBlock, valueUp);
                    }
                }
            }
            else if (number != null)
            {
                number.buildIr();
            }
        }
    }
}
