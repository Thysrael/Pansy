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
import check.SymbolTable;
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

    /**
     * 需要将原有的 printf 拆成多个 putint 和 putstr
     * 需要注意的是，不能拆一个 call 一个，这是因为对于 printf("str:%d", printOther());
     * 如果拆出 "str" 后就立即调用 putstr，那么对于 printOther 的调用会在这个之后，
     * 如果 printOther 输出了一些东西，那么这个就错了
     * 所以应该先对 printOther() buildIr，最后一起调用
     */
    @Override
    public void buildIr()
    {
        ArrayList<String> strings = MyPrintf.truncString(formString);
        ArrayList<Value> putintArgs = new ArrayList<>();
        // 先对参数 buildIr
        for (ExpNode argument : arguments)
        {
            argument.buildIr();
            putintArgs.add(valueUp);
        }
        int argCur = 0;
        for (String string : strings)
        {
            if (string.equals("%d"))
            {
                ArrayList<Value> params = new ArrayList<>();
                params.add(putintArgs.get(argCur++));
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
