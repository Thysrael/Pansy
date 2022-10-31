package ir;

import ir.types.FunctionType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.Constant;
import ir.values.instructions.*;
import parser.cst.CSTNode;

import java.util.ArrayList;

public class IrBuilder
{
    private static final IrBuilder irBuilder = new IrBuilder();
    private IrBuilder()
    {}
    public static IrBuilder getInstance()
    {
        return irBuilder;
    }

    /**
     * module 实例，相当于每个 irBuild 方法都是在改变他（或者其子孙）
     */
    public final Module module = Module.getInstance();
    /**
     * 一个起名计数器，对于 instruction 或者 BasicBlock 这样的 Value 是没有本身名字的
     * 所以需要用这个计数器给上面这样的 Value 取一个独一无二的名字
     */
    private static int nameNumCounter = 0;

    private static int strNumCounter = 0;

    public void buildModule(CSTNode root)
    {
        root.buildIr();
    }

    /**
     * 全局变量初始化的时候，一定是用常量初始化的
     * 建造一个全局变量，并将其加入 module
     * @param ident 标识符
     * @param initValue 初始值
     * @param isConst 是否是常量
     */
    public GlobalVariable buildGlobalVariable(String ident, Constant initValue, boolean isConst)
    {
        GlobalVariable globalVariable = new GlobalVariable(ident, initValue, isConst);
        module.addGlobalVariable(globalVariable);
        return globalVariable;
    }

    public GlobalVariable buildGlobalStr(Constant initValue)
    {
        GlobalVariable globalVariable = new GlobalVariable("STR" + strNumCounter++, initValue, true);
        module.addGlobalVariable(globalVariable);
        return globalVariable;
    }

    public Function buildFunction(String ident, FunctionType functionType, boolean isBuiltin)
    {
        Function function = new Function(ident, functionType, isBuiltin);
        module.addFunction(function);
        return function;
    }

    public Function buildFunction(String ident, FunctionType functionType)
    {
        Function function = new Function(ident, functionType, false);
        module.addFunction(function);
        return function;
    }

    public BasicBlock buildBlock(Function function)
    {
        int name = nameNumCounter++;
        BasicBlock ans = new BasicBlock(name, function);
        function.insertTail(ans);
        return ans;
    }
    public Add buildAdd(BasicBlock parentBB, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Add ans = new Add(nameNum, parentBB, src1, src2);
        parentBB.insertTail(ans);
        return ans;
    }

    public Sub buildSub(BasicBlock parentBB, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Sub ans = new Sub(nameNum, parentBB, src1, src2);
        parentBB.insertTail(ans);
        return ans;
    }

    public Mul buildMul(BasicBlock parentBB, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Mul ans = new Mul(nameNum, parentBB, src1, src2);
        parentBB.insertTail(ans);
        return ans;
    }

    public Sdiv buildSdiv(BasicBlock parentBB, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Sdiv ans = new Sdiv(nameNum, parentBB, src1, src2);
        parentBB.insertTail(ans);
        return ans;
    }

    public Srem buildSrem(BasicBlock parent, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Srem ans = new Srem(nameNum, parent, src1, src2);
        parent.insertTail(ans);
        return ans;
    }

    public Icmp buildIcmp(BasicBlock parent, Icmp.Condition condition, Value src1, Value src2)
    {
        int nameNum = nameNumCounter++;
        Icmp ans = new Icmp(nameNum, parent, condition, src1, src2);
        parent.insertTail(ans);
        return ans;
    }

    public Zext buildZext(BasicBlock parent, Value value)
    {
        int nameNum = nameNumCounter++;
        Zext zExt = new Zext(nameNum, parent, value);
        parent.insertTail(zExt);
        return zExt;
    }

    /**
     * 为了方便 mem2reg 优化，约定所有的 Alloca 放到每个函数的入口块处
     * @param allocatedType alloca 空间的类型
     * @param parent 基本块
     * @return Alloca 指令
     */
    public Alloca buildAlloca(ValueType allocatedType, BasicBlock parent)
    {
        int nameNum = nameNumCounter++;
        BasicBlock realParent = parent.getParent().getHeadBlock();
        Alloca ans = new Alloca(nameNum, allocatedType, realParent);
        realParent.insertHead(ans);
        return ans;
    }

    /**
     * 全新的 GEP 指令，可以允许变长的 index
     * @param parent 基本块
     * @param base 基地址（是一个指针）
     * @param indices 变长索引
     * @return 一个新的指针
     */
    public GetElementPtr buildGEP(BasicBlock parent, Value base, Value... indices)
    {
        int nameNum = nameNumCounter++;
        GetElementPtr ans;
        if (indices.length == 1)
        {
            ans = new GetElementPtr(nameNum, parent, base, indices[0]);
        }
        else
        {
            ans = new GetElementPtr(nameNum, parent, base, indices[0], indices[1]);
        }
        parent.insertTail(ans);
        return ans;
    }

    /**
     * @param parent 基本块
     * @param content 存储内容
     * @param addr 地址
     */
    public void buildStore(BasicBlock parent, Value content, Value addr)
    {
        Store ans = new Store(parent, content, addr);
        parent.insertTail(ans);
    }

    public Load buildLoad(BasicBlock parent, Value addr)
    {
        int nameNum = nameNumCounter++;
        Load ans = new Load(nameNum, parent, addr);
        parent.insertTail(ans);
        return ans;
    }

    public void buildRet(BasicBlock parent, Value... retVal)
    {
        Ret ans;
        // 没有返回值
        if (retVal.length == 0)
        {
            ans = new Ret(parent);
        }
        else
        {
            ans = new Ret(parent, retVal[0]);
        }
        parent.insertTail(ans);
    }

    public void buildBr(BasicBlock parent, BasicBlock target)
    {
        Br br = new Br(parent, target);
        parent.insertTail(br);
    }

    public void buildBr(BasicBlock parent, Value condition, BasicBlock trueBlock, BasicBlock falseBlock)
    {
        Br br = new Br(parent, condition, trueBlock, falseBlock);
        parent.insertTail(br);
    }

    public Call buildCall(BasicBlock parent, Function function, ArrayList<Value> args)
    {
        Call ans;
        // 没有返回值
        if (function.getReturnType() instanceof VoidType)
        {
            ans = new Call(parent, function, args);
            parent.insertTail(ans);
        }
        else
        {
            int nameNum = nameNumCounter++;
            ans = new Call(nameNum, parent, function, args);
            parent.insertTail(ans);
        }
        return ans;
    }
}
