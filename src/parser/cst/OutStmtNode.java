package parser.cst;

import check.ErrorType;
import check.PansyException;
import lexer.token.SyntaxType;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * OutStmt
 *     : PRINTFTK L_PAREN FormatString (COMMA Exp)* R_PAREN SEMICN
 *     ;
 */
public class OutStmtNode extends CSTNode
{
    private String formString;
    private final ArrayList<ExpNode> arguments = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode && ((TokenNode) child).isSameType(SyntaxType.STRCON))
        {
            formString = ((TokenNode) child).getContent();
        }
        if (child instanceof ExpNode)
        {
            arguments.add((ExpNode) child);
        }
    }

    /**
     *  要检测格式字符是否有后面的参数匹配
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();

        TokenNode tokenNode = ((TokenNode) children.get(0));

        if (formString.split("%d").length - 1 != arguments.size())
        {
            errors.add(new PansyException(ErrorType.FORM_STRING_MISMATCH, tokenNode.getLine()));
        }

        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
    }
}
