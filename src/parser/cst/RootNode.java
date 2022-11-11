package parser.cst;

import ir.types.*;
import ir.values.Function;

import java.util.ArrayList;

/**
 * CompUnit
 *     : (FuncDef | Decl)+
 *     ;
 */
public class RootNode extends CSTNode
{
    /**
     * 需要加入 3 个 IO 函数
     * 最后还是没有加入符号表，这依赖于程序是正确的
     */
    @Override
    public void buildIr()
    {
        ArrayList<DataType> printfArgs = new ArrayList<>();
        printfArgs.add(new PointerType(new IntType(8)));
        Function.putstr = irBuilder.buildFunction("putstr", new FunctionType(printfArgs, new VoidType()), true);
        ArrayList<DataType> putintArgs = new ArrayList<>();
        putintArgs.add(new IntType(32));
        Function.putint = irBuilder.buildFunction("putint", new FunctionType(putintArgs, new VoidType()), true);
        Function.getint = irBuilder.buildFunction("getint", new FunctionType(new ArrayList<>(), new IntType(32)), true);

        children.forEach(CSTNode::buildIr);
    }
}
