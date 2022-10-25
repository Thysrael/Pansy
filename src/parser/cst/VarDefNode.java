package parser.cst;

import check.ErrorType;
import check.PansyException;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * VarDef
 *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
 *     ;
 */
public class VarDefNode extends CSTNode
{
    /**
     * 两种错误，
     * 一种是标识符命名重复
     * 一种是缺失右中括号
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        TokenNode identNode = ((TokenNode) children.get(0));

        // 标识符命名重复
        if (symbolTable.isSymbolRedefined(identNode.getContent()))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, identNode.getLine()));
        }
        // 更新符号表
        symbolTable.addVar(this);
        // 缺失右中括号
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }
}
