package parser.cst;

import middle.symbol.SymbolTable;

/**
 * FuncRParams
 *     : Exp (COMMA Exp)*
 *     ;
 */
public class FuncRParamsNode extends CSTNode
{
    @Override
    public void check(SymbolTable symbolTable)
    {
        super.check(symbolTable);
    }
}
