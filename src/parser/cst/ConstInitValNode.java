package parser.cst;


import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;

import java.util.ArrayList;

/**
 * ConstInitVal
 *     : ConstExp
 *     | L_BRACE (ConstInitVal (COMMA ConstInitVal)*)? R_BRACE
 *     ;
 */
public class ConstInitValNode extends CSTNode
{
    private ConstExpNode constExp = null;
    private final ArrayList<ConstInitValNode> constInitVals = new ArrayList<>();
    private final ArrayList<Integer> dims = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof ConstExpNode)
        {
            constExp = (ConstExpNode) child;
        }
        else if (child instanceof ConstInitValNode)
        {
            constInitVals.add((ConstInitValNode) child);
        }
    }

    public void setDims(ArrayList<Integer> dims)
    {
        this.dims.addAll(dims);
    }

    /**
     * 会对与 initVal 的三种情况分类讨论并遍历，最后的结果存储在 valueUp 上
     */
    @Override
    public void buildIr()
    {
        // 如果是一个单变量，那么就啥也不用管，直接用就好了
        if (constExp != null)
        {
            constExp.buildIr();
        }
        // 如果是一个数组
        else
        {
            // 全局常量数组
            if (irSymbolTable.isGlobal())
            {
                // 一维数组
                ArrayList<Constant> array = new ArrayList<>();
                if (dims.size() == 1)
                {
                    for (ConstInitValNode element : constInitVals)
                    {
                        element.buildIr();
                        array.add((ConstInt) valueUp);
                    }

                }
                // 二维数组
                else
                {
                    for (ConstInitValNode element : constInitVals)
                    {
                        // 去掉一维
                        element.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                        element.buildIr();
                        array.add((ConstArray) valueUp);
                    }
                }
                valueUp = new ConstArray(array);
            }
            // 局部常量数组，可以看到和变量数组的初始化类似，这是因为局部常量数组本质上也是个局部变量数组，所以方法都一样
            else
            {
                ArrayList<Value> flattenArray = new ArrayList<>();
                // 一维数组
                if (dims.size() == 1)
                {
                    for (ConstInitValNode element : constInitVals)
                    {
                        element.buildIr();
                        flattenArray.add(valueUp);
                    }
                }
                // 二维数组
                else
                {
                    // 此时在遍历每个一维数组
                    for (ConstInitValNode initVal : constInitVals)
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
}
