package parser.cst;

import check.DataType;
import check.PansyException;
import middle.symbol.SymbolTable;

/**
 * AddExp
 *     : MulExp (AddOp MulExp)*
 *     ; // eliminate left-recursive
 * AddOp
 *     : PLUS
 *     | MINUS
 *     ;
 */
public class AddExpNode extends CSTNode
{
}
