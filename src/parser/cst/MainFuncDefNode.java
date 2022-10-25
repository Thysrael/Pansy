package parser.cst;

import check.DataType;
import check.ErrorType;
import check.PansyException;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

public class MainFuncDefNode extends CSTNode
{
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
        symbolTable.addMainFunc(this);

        try
        {
            curFuncInfo = symbolTable.getFuncInfo(name);
            // 填写返回类型
            DataType dataType = DataType.INT;
            curFuncInfo.setReturnType(dataType);

            // 有返回值的函数缺少 return 语句
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
