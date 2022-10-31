package parser.cst;

import check.ErrorType;
import check.PansyException;
import check.CheckDataType;
import ir.types.DataType;
import ir.types.FunctionType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.constants.ConstInt;
import ir.values.instructions.Br;
import ir.values.instructions.Instruction;
import ir.values.instructions.Ret;
import lexer.token.SyntaxType;
import middle.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * FuncDef
 *     : FuncType IDENFR L_PAREN FuncFParams? R_PAREN Block
 *     ;
 */
public class FuncDefNode extends CSTNode
{
    private FuncTypeNode funcType = null;
    private TokenNode ident = null;
    private FuncFParamsNode funcFParams = null;
    private BlockNode block = null;
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof FuncTypeNode)
        {
            funcType = (FuncTypeNode) child;
        }
        else if (child instanceof TokenNode && ((TokenNode) child).isSameType(SyntaxType.IDENFR))
        {
            ident = (TokenNode) child;
        }
        else if (child instanceof FuncFParamsNode)
        {
            funcFParams = (FuncFParamsNode) child;
        }
        else if (child instanceof BlockNode)
        {
            block = (BlockNode) child;
        }
    }

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
            CheckDataType checkDataType = funcTypeNode.getCheckReturnType();
            curFuncInfo.setReturnType(checkDataType);

            // 有返回值的函数缺少 return 语句
            if (checkDataType.equals(CheckDataType.INT))
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

    @Override
    public void buildIr()
    {
        // get function name
        String funcName = ident.getContent();
        // get function return type
        DataType returnType = funcType.getReturnType();
        // get function params information
        // 此处这是 buildFunc，但是为了 SSA 特性，之后还需要再次遍历 funcFParams 来为形参分配空间
        ArrayList<DataType> argsType = new ArrayList<>();
        if (funcFParams != null)
        {
            funcFParams.buildIr();
            argsType.addAll(argTypeArrayUp);
        }
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
        // 如果参数列表不为空，说明是需要参数 alloc 的
        if (funcFParams != null)
        {
            funcFParams.buildFParamsSSA();
        }
        // 建立函数体
        block.buildIr();

        // 在解析完了函数后，开始处理善后工作
        // 如果没有默认的 return 语句
        Instruction tailInstr = curBlock.getTailInstr();
        // 结尾没有指令或者指令不是跳转指令，null 指令被包含了
        if (!(tailInstr instanceof Ret || tailInstr instanceof Br))
        {
            if (curFunc.getReturnType() instanceof VoidType)
            {
                irBuilder.buildRet(curBlock);
            }
            else
            {
                irBuilder.buildRet(curBlock, ConstInt.ZERO);
            }
        }
        irSymbolTable.popFuncLayer();
    }
}
