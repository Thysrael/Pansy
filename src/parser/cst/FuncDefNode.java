package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.DataType;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * FuncDef
 *     : FuncType IDENFR L_PAREN FuncFParams? R_PAREN Block
 *     ;
 */
public class FuncDefNode extends CSTNode
{
    /**
     * 第一种是函数名重定义，此外，尽管
     * 第二种是有返回值的函数缺少 return 语句
     * 这里有意思的一点是，我没办法把各种东西往底层去推，很多东西必须在这里解决
     * 其实不在这里解决也行，但是需要付出的代价太大了
     * @param symbolTable 符号表
     */
    @Override
    public void check(SymbolTable symbolTable)
    {
        addCheckLog();
        TokenNode identNode = ((TokenNode) children.get(1));

        String name = identNode.getContent();
        // 如果名字重定义
        if (symbolTable.isSymbolRedefined(name))
        {
            errors.add(new PansyException(ErrorType.REDEFINED_SYMBOL, identNode.getLine()));
        }
        // 加入符号表
        symbolTable.addFunc(this);

        // 填写符号表
        try
        {
            curFuncInfo = symbolTable.getFuncInfo(name);

            FuncTypeNode funcTypeNode = (FuncTypeNode) children.get(0);
            // 填写返回类型
            DataType dataType = funcTypeNode.getReturnType();
            curFuncInfo.setReturnType(dataType);

            // 有返回值的函数缺少 return 语句
            if (dataType.equals(DataType.INT))
            {
                BlockNode blockNode = (BlockNode) children.get(children.size() - 1);
                ArrayList<CSTNode> blockChildren = blockNode.getChildren();
                // 这个是大括号节点
                TokenNode tailNode = (TokenNode) blockChildren.get(blockChildren.size() - 1);
                // 这节点可能是 return
                CSTNode blockItemNode = blockChildren.get(blockChildren.size() - 2);
                // 更加可能了
                try
                {
                    CSTNode stmtNode = blockItemNode.getChildren().get(0);
                    CSTNode returnNode = stmtNode.getChildren().get(0);
                    if (!(returnNode instanceof ReturnStmtNode))
                    {
                        errors.add(new PansyException(ErrorType.MISS_RETURN, tailNode.getLine()));
                    }
                }
                // 只要发生了一点异常，大概率都是因为访问越界，越界说明节点错误
                catch (Exception e)
                {
                    errors.add(new PansyException(ErrorType.MISS_RETURN, tailNode.getLine()));
                }
            }
        }
        // 这里是处理获得 FuncInfo 错误的，应该不会有这个错误
        catch (PansyException e)
        {
            errors.add(new PansyException(e.getType(), identNode.getLine()));
        }
        // 这里需要加一层
        symbolTable.addFuncLayer();
        for (CSTNode child : children)
        {
            child.check(symbolTable);
        }
        symbolTable.removeFuncLayer();
    }
}
