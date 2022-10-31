package parser.cst;

import ir.values.constants.ConstInt;

/**
 * ConstExp
 *     : AddExp
 *     ;
 */
public class ConstExpNode extends CSTNode
{
    /**
     * 因为常量表达式必须给出初始值，所以一定是可以计算的
     */
    @Override
    public void buildIr()
    {
        canCalValueDown = true;
        children.forEach(CSTNode::buildIr);
        canCalValueDown = false;
        valueUp = new ConstInt(valueIntUp);
    }
}
