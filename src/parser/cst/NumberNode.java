package parser.cst;

import check.DataType;
import lexer.token.IntConst;
import middle.symbol.SymbolTable;

public class NumberNode extends CSTNode
{
    public static NumberNode ZERO;
    public static NumberNode INF;

    static
    {
        ZERO = new NumberNode(0);
        INF = new NumberNode(Integer.MAX_VALUE);
    }

    private int num;

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode)
        {
            String content = ((TokenNode) child).getContent();
            this.num = Integer.parseInt(content);
        }
    }

    public NumberNode()
    {

    }

    public NumberNode(int num)
    {
        this.num = num;
    }

    @Override
    public DataType getDataType(SymbolTable symbolTable)
    {
        return DataType.INT;
    }
}
