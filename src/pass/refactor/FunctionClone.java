package pass.refactor;

import ir.IrBuilder;
import ir.types.DataType;
import ir.types.PointerType;
import ir.values.*;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.*;
import util.MyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FunctionClone
{
    /**
     * 建立 source -> copy 的映射关系，方便在复制过程中查表
     */
    private final HashMap<Value, Value> copyMap = new HashMap<>();
    /**
     * 在进行块的 dfs 的时候需要
     */
    private final HashSet<BasicBlock> visited = new HashSet<>();
    private final IrBuilder irBuilder = IrBuilder.getInstance();
    /**
     * 记录着 src 中的所有 phi
     */
    private final ArrayList<Phi> phis = new ArrayList<>();

    private Value findValue(Value source)
    {
        // 对于这三种变量，是不需要复制的
        if (source instanceof GlobalVariable ||
            source instanceof Constant ||
            source instanceof Function)
        {
            return source;
        }
        else if (copyMap.containsKey(source) && copyMap.get(source) != null)
        {
            return copyMap.get(source);
        }
        else
        {
            assert false : "Don't have copy source: " + source;
            return ConstInt.ZERO;
        }
    }

    private void putValue(Value source, Value copy)
    {
        copyMap.put(source, copy);
    }

    public Function copyFunction(Function srcFunc)
    {
        copyMap.clear();
        visited.clear();
        phis.clear();

        Function copyFunc = new Function(srcFunc.getName() + "_COPY", srcFunc.getValueType(), srcFunc.isBuiltin());

        // 对应参数，在新建函数的时候就自动复制了形参，现在只需要将其对应起来
        int argNum = srcFunc.getArguments().size();
        for (int i = 0; i < argNum; i++)
        {
            Argument argument = srcFunc.getArguments().get(i);
            putValue(argument, copyFunc.getArguments().get(i));
        }
        // 复制块，这是因为后面遍历的时候需要增加前驱后继关系，但是不一定自然而然的满足
        for (MyList.MyNode<BasicBlock> blockNode : srcFunc.getBasicBlocks())
        {
            BasicBlock srcBlock = blockNode.getVal();
            putValue(srcBlock, irBuilder.buildBlock(copyFunc));
        }

        copyBlocks(srcFunc.getHeadBlock());

        // 维护所有的 phi, phi 没有办法在创建的时候就被维护好
        for (Phi phi : phis)
        {
            for (int i = 0; i < phi.getUsedValues().size(); i++)
            {
                ((Phi) findValue(phi)).setUsedValue(i, findValue(phi.getUsedValue(i)));
                findValue(phi.getUsedValue(i)).addUser(((Phi) findValue(phi)));
            }
        }

        return copyFunc;
    }

    void copyBlocks(BasicBlock curBlock)
    {
        for (Instruction srcInstr : curBlock.getInstructionsArray())
        {
            Instruction copyInstr = copyInstr(srcInstr);
            putValue(srcInstr, copyInstr);
        }

        // 对后继进行遍历
        for (BasicBlock successor : curBlock.getSuccessors())
        {
            if (!visited.contains(successor))
            {
                visited.add(successor);
                copyBlocks(successor);
            }
        }
    }

    private Instruction copyInstr(Instruction srcInstr)
    {
        Instruction copyInstr = null;
        BasicBlock copyBlock = (BasicBlock) findValue(srcInstr.getParent());
        if (srcInstr instanceof BinInstruction)
        {
            Value copyOp1 = findValue(((BinInstruction) srcInstr).getOp1());
            Value copyOp2 = findValue(((BinInstruction) srcInstr).getOp2());
            if (srcInstr instanceof Add)
            {
                copyInstr = irBuilder.buildAdd(copyBlock, copyOp1, copyOp2);
            }
            else if (srcInstr instanceof Sub)
            {
                copyInstr = irBuilder.buildSub(copyBlock, copyOp1, copyOp2);
            }
            else if (srcInstr instanceof Mul)
            {
                copyInstr = irBuilder.buildMul(copyBlock, copyOp1, copyOp2);
            }
            else if (srcInstr instanceof Sdiv)
            {
                copyInstr = irBuilder.buildSdiv(copyBlock, copyOp1, copyOp2);
            }
            else if (srcInstr instanceof Srem)
            {
                copyInstr = irBuilder.buildSrem(copyBlock, copyOp1, copyOp2);
            }
            else if (srcInstr instanceof Icmp)
            {
                copyInstr = irBuilder.buildIcmp(copyBlock, ((Icmp) srcInstr).getCondition(),
                        copyOp1, copyOp2);
            }
        }
        else if (srcInstr instanceof Zext)
        {
            copyInstr = irBuilder.buildZext(copyBlock, findValue(((Zext) srcInstr).getSrc()));
        }
        else if (srcInstr instanceof Phi)
        {
            copyInstr = irBuilder.buildPhi((DataType) srcInstr.getValueType(), copyBlock,
                    ((Phi) srcInstr).getPredecessorNum());
            phis.add((Phi) srcInstr);
        }
        else if (srcInstr instanceof Load)
        {
            copyInstr = irBuilder.buildLoad(copyBlock, findValue(((Load) srcInstr).getAddr()));
        }
        else if (srcInstr instanceof Store)
        {
            irBuilder.buildStore(copyBlock,
                    findValue(((Store) srcInstr).getValue()),
                    findValue(((Store) srcInstr).getAddr()));
        }
        else if (srcInstr instanceof Alloca)
        {
            copyInstr = irBuilder.buildAlloca(((PointerType) srcInstr.getValueType()).getPointeeType(),
                    copyBlock);
        }
        else if (srcInstr instanceof GetElementPtr)
        {
            ArrayList<Value> copyIndices = new ArrayList<>();
            for (Value index : ((GetElementPtr) srcInstr).getOffset())
            {
                copyIndices.add(findValue(index));
            }
            Value copyBase = findValue(((GetElementPtr) srcInstr).getBase());
            if (copyIndices.size() == 1)
            {
                copyInstr = irBuilder.buildGEP(copyBlock, copyBase, copyIndices.get(0));
            }
            else
            {
                copyInstr = irBuilder.buildGEP(copyBlock, copyBase, copyIndices.get(0), copyIndices.get(1));
            }
        }
        else if (srcInstr instanceof Call)
        {
            ArrayList<Value> args = new ArrayList<>();
            for (int i = 1; i < srcInstr.getNumOps(); i++)
            {
                args.add(findValue(srcInstr.getUsedValue(i)));
            }
            copyInstr = irBuilder.buildCall(copyBlock, ((Call) srcInstr).getFunction(), args);
        }
        else if (srcInstr instanceof Br)
        {
            if (((Br) srcInstr).hasCondition())
            {
                irBuilder.buildBr(copyBlock, findValue(srcInstr.getUsedValue(0)),
                        (BasicBlock) findValue(srcInstr.getUsedValue(1)),
                        (BasicBlock) findValue(srcInstr.getUsedValue(2)));
            }
            else
            {
                irBuilder.buildBr(copyBlock, (BasicBlock) findValue(srcInstr.getUsedValue(0)));
            }
        }
        else if (srcInstr instanceof Ret)
        {
            if (srcInstr.getUsedValues().size() == 0)
            {
                irBuilder.buildRet(copyBlock);
            }
            else
            {
                irBuilder.buildRet(copyBlock, findValue(((Ret) srcInstr).getRetValue()));
            }
        }

        return copyInstr;
    }
}
