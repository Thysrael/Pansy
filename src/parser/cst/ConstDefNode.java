package parser.cst;

import check.ErrorType;
import check.PansyException;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * ConstDef
 *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
 *     ;
 */
public class ConstDefNode extends CSTNode
{
    /**
     * 两种错误，
     * 一种是标识符命名重复
     * 一种是缺失右中括号
     * 这里需要增加对于常量的定义
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode identNode = ((TokenNode) children.get(0));

        // 标识符命名重复
        if (symbolTable.isSymbolRedefined(identNode.getContent()))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, identNode.getLine()));
        }
        // 缺失右中括号
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        // 加入常量的定义，更新符号表
        symbolTable.addConst(this);
    }
}
