package parser.cst;

import ir.types.DataType;
import ir.values.Argument;
import ir.values.instructions.Alloca;

import java.util.ArrayList;

/**
 * FuncFParams
 *     : FuncFParam (COMMA FuncFParam)*
 *     ;
 */
public class FuncFParamsNode extends CSTNode
{
    private final ArrayList<FuncFParamNode> funcFParams = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof FuncFParamNode)
        {
            funcFParams.add((FuncFParamNode) child);
        }
    }

    /**
     * 这个方法会向上传递一个类型数组，也就是说，buildIr 的结果是一组类型信息
     * 其实还算合理，因为形参没有值
     */
    @Override
    public void buildIr()
    {
        ArrayList<DataType> types = new ArrayList<>();
        for (FuncFParamNode funcFParam : funcFParams)
        {
            funcFParam.buildIr();
            types.add(argTypeUp);
        }
        argTypeArrayUp = types;
    }

    /**
     * 这个是为了让形参是 SSA 形式
     * 必须为形参分配空间，然后将指针指向形参
     * 不然形参可能直接被更改
     */
    public void buildFParamsSSA()
    {
        ArrayList<Argument> args = curFunc.getArguments();
        for (int i = 0; i < funcFParams.size(); i++)
        {
            FuncFParamNode funcFParam = funcFParams.get(i);
            Argument argument = args.get(i);
            // 这里建立 alloca 和 store 指令，而且并不需要分类讨论，因为类型在之前已经探讨过了
            Alloca alloca = irBuilder.buildAlloca(argument.getValueType(), curBlock);
            irBuilder.buildStore(curBlock, argument, alloca);
            irSymbolTable.addValue(funcFParam.getName(), alloca);
        }

    }
}
