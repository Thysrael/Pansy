package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.values.Value;
import check.SymbolInfo;
import check.SymbolTable;

import java.util.ArrayList;

/**
 * AssignStmt
 *     : LVal ASSIGN Exp SEMICOLON
 *     ;
 */
public class AssignStmtNode extends CSTNode
{
    private LValNode lVal;
    private ExpNode exp;
    private final ArrayList<TokenNode> tokens = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof LValNode)
        {
            lVal = (LValNode) child;
        }
        else if (child instanceof ExpNode)
        {
            exp = (ExpNode) child;
        }
        else
        {
            tokens.add((TokenNode) child);
        }
    }

    /**
     * 改变常量值
     * 符号未定义
     * 缺符号
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        TokenNode identNode = (TokenNode) lVal.getChildren().get(0);
        try
        {
            SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
            // 如果修改了常量，那么就要报错
            if (symbolInfo.isConst())
            {
                errors.add(new PansyException(ErrorType.CHANGE_CONST, identNode.getLine()));
            }
        }
        catch (PansyException e)
        {
            errors.add(new PansyException(e.getType(), identNode.getLine()));
        }
        // 检测是否缺分号和右中括号
        for (TokenNode token : tokens)
        {
            token.check(symbolTable);
        }
    }

    /**
     * lVal build 后是一个指针，所以只需要 build lVal，然后 build exp
     * 最用用以 store 将二者联系在一起即可
     */
    @Override
    public void buildIr()
    {
        lVal.buildIr();
        Value target = valueUp;
        exp.buildIr();
        Value source = valueUp;

        // 最后是以一个 store 结尾的，说明将其存入内存，就算完成了赋值
        irBuilder.buildStore(curBlock, source, target);
    }
}
