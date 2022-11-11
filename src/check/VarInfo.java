package check;

import parser.cst.*;

import java.util.ArrayList;

/**
 * 三种类型都在这里存储：
 * 变量
 * 常量
 * 形参
 */
public class VarInfo extends SymbolInfo
{
    private final String name;
    /**
     * 这里用两个 CSTNode 来表示维度信息，可以说是一种权衡了
     * 最开始的时候他们都是 0
     */
    private CSTNode dim1 = NumberNode.ZERO;
    private CSTNode dim2 = NumberNode.ZERO;
    /**
     * 用于表示是否是常量
     */
    private boolean isConst = false;

    /**
     * 根据变量定义进行构造
     * @param ctx 变量定义
     */
    public VarInfo(VarDefNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(0);
        this.name = identNode.getContent();

        for (CSTNode child : ctx.getChildren())
        {
            if (child instanceof ConstExpNode)
            {
                if (dim1 == NumberNode.ZERO)
                {
                    dim1 = child;
                }
                else if (dim2 == NumberNode.ZERO)
                {
                    dim2 = child;
                }
            }
        }
    }

    /**
     * 根据常量定义进行构造
     * @param ctx 常量定义
     */
    public VarInfo(ConstDefNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(0);
        this.name = identNode.getContent();

        for (CSTNode child : ctx.getChildren())
        {
            if (child instanceof ConstExpNode)
            {
                if (dim1 == NumberNode.ZERO)
                {
                    dim1 = child;
                }
                else if (dim2 == NumberNode.ZERO)
                {
                    dim2 = child;
                }
            }
        }
        this.isConst = true;
    }

    /**
     * 根据函数形参进行构造
     * @param ctx 函数形参
     */
    public VarInfo(FuncFParamNode ctx)
    {
        TokenNode identNode = (TokenNode) ctx.getChildren().get(1);
        this.name = identNode.getContent();
        ArrayList<CSTNode> children = ctx.getChildren();

        // 说明此时参数不是一个单变量
        if (children.size() > 2)
        {
            // 第一维设为无穷
            dim1 = NumberNode.INF;
            // 说明存在第二维
            if (children.size() > 4)
            {
                dim2 = children.get(5);
            }
        }
    }

    /**
     * 对于变量和常量，有
     * int a -> INT
     * int a[2] -> DIM1
     * int a[2][2] -> DIM2
     * 对应形参有
     * int a -> INT
     * int a[] -> DIM1
     * int a[][2] -> DIM2
     * 十分巧妙
     * @return 获得 checkDataType
     */
    public CheckDataType getCheckDataType()
    {
        if (dim1 == NumberNode.ZERO && dim2 == NumberNode.ZERO)
        {
            return CheckDataType.INT;
        }
        else if (dim2 == NumberNode.ZERO)
        {
            return CheckDataType.DIM1;
        }
        else
        {
            return CheckDataType.DIM2;
        }
    }

    public boolean isConst()
    {
        return isConst;
    }

    public boolean isVar()
    {
        return !isConst;
    }
}
