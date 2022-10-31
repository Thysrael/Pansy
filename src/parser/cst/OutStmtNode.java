package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.values.Function;
import ir.values.GlobalVariable;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.constants.ConstStr;
import ir.values.instructions.GetElementPtr;
import lexer.token.SyntaxType;
import middle.symbol.SymbolTable;
import util.MyPrintf;

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

    @Override
    public void buildIr()
    {
        ArrayList<String> strings = MyPrintf.truncString(formString);
        int argCur = 0;
        for (String string : strings)
        {
            if (string.equals("%d"))
            {
                arguments.get(argCur++).buildIr();
                ArrayList<Value> params = new ArrayList<>();
                params.add(valueUp);
                irBuilder.buildCall(curBlock, Function.putint, params);
            }
            else
            {
                // 全局变量本质是 [len x i8]* 类型
                GlobalVariable globalStr = irBuilder.buildGlobalStr(new ConstStr(string));
                // putstr 的参数是一个 i8*，所以用 GEP 降维指针
                GetElementPtr elementPtr = irBuilder.buildGEP(curBlock, globalStr, ConstInt.ZERO, ConstInt.ZERO);
                ArrayList<Value> params = new ArrayList<>();
                params.add(elementPtr);
                irBuilder.buildCall(curBlock, Function.putstr, params);
            }
        }
    }
}
