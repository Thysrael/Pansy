package parser.cst;

import check.ErrorType;
import check.PansyException;
import middle.symbol.SymbolTable;
import middle.symbol.VarInfo;

/**
 * FuncFParam
 *     : BType IDENFR (L_BRACKT R_BRACKT (L_BRACKT ConstExp R_BRACKT)*)?
 *     ;
 */
public class FuncFParamNode extends CSTNode
{
    /**
     * 需要登记变量
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode identNode = (TokenNode) children.get(1);
        String ident = identNode.getContent();
        // 变量重命名
        if (symbolTable.isSymbolRedefined(ident))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, identNode.getLine()));
        }

        // 登记参数，分为两个部分，首先是作为普通变量，其次是作为函数参数
        VarInfo parameterInfo = new VarInfo(this);
        curFuncInfo.addParameter(parameterInfo);
        symbolTable.addParam(this);

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }
}
