package parser.cst;

import ir.values.Value;
import ir.values.constants.ConstInt;

import java.util.ArrayList;

/**
 * InitVal
 *     : Exp
 *     | L_BRACE (InitVal (COMMA InitVal)*)? R_BRACE
 *     ;
 */
public class InitValNode extends CSTNode
{
    private ExpNode exp = null;
    private final ArrayList<InitValNode> initVals = new ArrayList<>();
    private final ArrayList<Integer> dims = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ExpNode)
        {
            exp = (ExpNode) child;
        }
        if (child instanceof InitValNode)
        {
            initVals.add((InitValNode) child);
        }
    }

    public void setDims(ArrayList<Integer> dims)
    {
        this.dims.addAll(dims);
    }

    /**
     * 对于单变量初始值，通过 valueUp 返回，
     * 对于数组初始值，通过 valueArrayUp 返回
     * 之所以无法像 ConstInit 都用 valueUp 返回，是因为对于变量初始值，没有一个 ConstArray 这样的结构打包
     * TODO 做一个测试
     */
    @Override
    public void buildIr()
    {
        // 初始值是一个表达式（单变量）
        if (exp != null)
        {
            // 在进行全局单变量初始化
            if (globalInitDown)
            {
                canCalValueDown = true;
                exp.buildIr();
                canCalValueDown = false;
                valueUp = new ConstInt(valueIntUp);
            }
            // 在进行局部变量初始化，没法确定初始值可以直接求值，所以用一个 value 代替
            else
            {
                exp.buildIr();
            }
        }
        // 在进行数组初始化
        else
        {
            ArrayList<Value> flattenArray = new ArrayList<>();
            // 一维数组
            if (dims.size() == 1)
            {
                for (InitValNode element : initVals)
                {
                    // 全局变量数组初始化，这里的值一定是可以被计算出来的
                    if (globalInitDown)
                    {
                        canCalValueDown = true;
                        element.buildIr();
                        canCalValueDown = false;
                        flattenArray.add(new ConstInt(valueIntUp));
                    }
                    else
                    {
                        element.buildIr();
                        flattenArray.add(valueUp);
                    }
                }
            }
            // 二维数组
            else
            {
                // 此时在遍历每个一维数组
                for (InitValNode initVal : initVals)
                {
                    // 先减少一维
                    initVal.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                    initVal.buildIr();
                    flattenArray.addAll(valueArrayUp);
                }
            }
            // 返回
            valueArrayUp = flattenArray;
        }
    }
}
