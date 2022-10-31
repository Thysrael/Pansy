package parser.cst;

import middle.symbol.SymbolTable;

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

    @Override
    public void buildIr()
    {
        irSymbolTable.pushBlockLayer();
        blockItems.forEach(CSTNode::buildIr);
        irSymbolTable.popBlockLayer();
    }
}
