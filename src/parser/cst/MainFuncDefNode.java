package parser.cst;

import check.CheckDataType;
import check.ErrorType;
import check.PansyException;
import ir.types.DataType;
import ir.types.FunctionType;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.constants.ConstInt;
import ir.values.instructions.Br;
import ir.values.instructions.Instruction;
import ir.values.instructions.Ret;
import check.SymbolTable;

import java.util.ArrayList;

/**
 * MainFuncDef
 *      : INT_TK MAIN_TK L_PAREN R_PAREN Block
 *      ;
 */
public class MainFuncDefNode extends CSTNode
{
    private BlockNode block = null;

    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof BlockNode)
        {
            block = (BlockNode) child;
        }
    }

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
            CheckDataType checkDataType = CheckDataType.INT;
            curFuncInfo.setReturnType(checkDataType);

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

    @Override
    public void buildIr()
    {
        // get function name
        String funcName = "main";
        // get function return type
        DataType returnType = new IntType(32);
        // get function params information
        // 此处这是 buildFunc，但是为了 SSA 特性，之后还需要再次遍历 funcFParams 来为形参分配空间
        ArrayList<DataType> argsType = new ArrayList<>();

        // build function object
        curFunc = irBuilder.buildFunction(funcName, new FunctionType(argsType, returnType));
        // add to symbol table
        irSymbolTable.addValue(funcName, curFunc);
        // 在 entryBlock 加入函数的形参
        BasicBlock entryBlock = irBuilder.buildBlock(curFunc);
        // 进入一个函数，就会加一层
        irSymbolTable.pushFuncLayer();
        // visit block and create basic blocks
        // 将函数的形参放到 block 中，将对 Function 的 arg 的初始化 delay 到 visit(ctx.block)
        curBlock = entryBlock;
        // 建立函数体
        block.buildIr();

        // 在解析完了函数后，开始处理善后工作
        // 如果没有默认的 return 语句
        Instruction tailInstr = curBlock.getTailInstr();
        // 结尾没有指令或者指令不是跳转指令，null 指令被包含了
        if (!(tailInstr instanceof Ret || tailInstr instanceof Br))
        {
            irBuilder.buildRet(curBlock, ConstInt.ZERO);
        }
        irSymbolTable.popFuncLayer();
    }
}
