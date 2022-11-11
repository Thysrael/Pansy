package parser.cst;

import check.SymbolTable;

import java.util.ArrayList;

/**
 * Block
 *     : L_BRACE BlockItem* R_BRACE
 *     ;
 */
public class BlockNode extends CSTNode
{
    private final ArrayList<BlockItemNode> blockItems = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof BlockItemNode)
        {
            blockItems.add((BlockItemNode) child);
        }
    }

    /**
     * 需要为符号表增加一层
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        // 添加了一层之后才可以继续检验
        symbolTable.addBlockLayer();
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        symbolTable.removeBlockLayer();
    }

    /**
     * 增加一层符号表，然后在 build 完后 pop
     */
    @Override
    public void buildIr()
    {
        irSymbolTable.pushBlockLayer();
        blockItems.forEach(CSTNode::buildIr);
        irSymbolTable.popBlockLayer();
    }

    /**
     * 每一项都需要换行
     * blockItem 需要 \t 增加缩进
     * @return block string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (CSTNode child : children)
        {
            if (!(child instanceof TokenNode))
            {
                sb.append("\t");
            }
            sb.append(child).append("\n");
        }

        return sb.toString();
    }
}
