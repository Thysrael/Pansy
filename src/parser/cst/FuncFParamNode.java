package parser.cst;

import check.ErrorType;
import check.PansyException;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import lexer.token.SyntaxType;
import check.SymbolTable;
import check.VarInfo;

import java.util.ArrayList;

/**
 * FuncFParam
 *     : BType IDENFR (L_BRACKT R_BRACKT (L_BRACKT ConstExp R_BRACKT)*)?
 *     ;
 */
public class FuncFParamNode extends CSTNode
{
    private TokenNode ident = null;
    private final ArrayList<TokenNode> leftBrackets = new ArrayList<>();
    private final ArrayList<ConstExpNode> constExps = new ArrayList<>();

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof TokenNode && ((TokenNode) child).isSameType(SyntaxType.IDENFR))
        {
            ident = (TokenNode) child;
        }
        else if (child instanceof TokenNode && ((TokenNode) child).isSameType(SyntaxType.LBRACK))
        {
            leftBrackets.add((TokenNode) child);
        }
        else if (child instanceof ConstExpNode)
        {
            constExps.add((ConstExpNode) child);
        }
    }

    /**
     * 需要登记变量，需要登记到函数和符号表两个地方
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

    /**
     * int a 此时的类型是 i32
     * int a[] 此时的类型是 i32*
     * int a[][2] 此时的类型是 [i32 x 2]*
     * 这个函数最后会向上传递一个综合属性 argTypeUp，是当前形参的类型信息
     */
    @Override
    public void buildIr()
    {
        // 单变量
        if (leftBrackets.isEmpty())
        {
            argTypeUp = new IntType(32);
        }
        // 指针
        else
        {
            ValueType argType = new IntType(32);
            // 先倒序遍历（其实应该最多只有一个）
            for (int i = constExps.size() - 1; i >= 0; i--)
            {
                canCalValueDown = true;
                constExps.get(i).buildIr();
                canCalValueDown = false;
                argType = new ArrayType(argType, valueIntUp);
            }
            // 最终做一个指针，和 C 语言逻辑一模一样
            argType = new PointerType(argType);
            argTypeUp = (PointerType) argType;
        }
    }

    public String getName()
    {
        return ident.getContent();
    }
}
