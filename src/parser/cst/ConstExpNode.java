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
     * 这里给出了一个很好的解决方法，就是所有的 ConstExp 一定是个 Value 返回，而不是 ValueInt 返回
     * 但是很多时候依然没有用
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
