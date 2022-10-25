package parser.cst;

import middle.symbol.SymbolTable;

/**
 * Block
 *     : L_BRACE BlockItem* R_BRACE
 *     ;
 */
public class BlockNode extends CSTNode
{
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
}
