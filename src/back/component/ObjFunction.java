package back.component;

import back.instruction.ObjBranch;
import back.instruction.ObjCondType;
import back.instruction.ObjInstr;
import back.operand.ObjImm;
import back.operand.ObjPhyReg;
import back.operand.ObjReg;
import back.operand.ObjVirReg;
import util.MyList;
import util.MyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class ObjFunction
{
    private final String name;
    private final MyList<ObjBlock> objBlocks = new MyList<>();
    /**
     * 这里记录着这个函数用到的所有虚拟整型寄存器，最终目的是进行寄存器分配
     */
    private final HashSet<ObjVirReg> usedVirRegs = new HashSet<>();
    /**
     * 包括 2 个部分，alloca 和 spill
     */
    private int allocaSize;
    private int totalStackSize;
    /**
     * 这是函数需要在调用前保存的寄存器
     * 只要函数用到了这个寄存器，而且不是 a0, a1 这样的传参寄存器，那么就都是需要保存的
     */
    private final TreeSet<Integer> calleeSavedRegIndexes = new TreeSet<>();
    /**
     *是一个辅助量，用于在 DFS 序列化的时候作为 visit
     */
    private final HashSet<ObjBlock> hasSerial = new HashSet<>();
    /**
     * 这是该函数需要使用栈上的参数的时候使用到的 mov 指令，来控制 offset
     */
    private final HashSet<ObjImm> argOffsets = new HashSet<>();
    private final boolean isBuiltin;

    public ObjFunction(String name)
    {
        this.name = name;
        this.isBuiltin = false;
        this.allocaSize = 0;
    }

    public ObjFunction(String name, Boolean isBuiltin)
    {
        this.name = name.substring(1);
        this.isBuiltin = isBuiltin;
        this.allocaSize = 0;
    }

    public String getName()
    {
        return name;
    }

    public boolean isBuiltin()
    {
        return isBuiltin;
    }

    public void addUsedVirReg(ObjVirReg objVirReg)
    {
        usedVirRegs.add(objVirReg);
    }

    public void addArgOffset(ObjImm objOffset)
    {
        argOffsets.add(objOffset);
    }

    public int getTotalStackSize()
    {
        return totalStackSize;
    }

    public HashSet<ObjVirReg> getUsedVirRegs()
    {
        return usedVirRegs;
    }

    public void addAllocaSize(int size)
    {
        allocaSize += size;
    }

    public int getAllocaSize()
    {
        return allocaSize;
    }

    public TreeSet<Integer> getCalleeSavedRegIndexes()
    {
        return calleeSavedRegIndexes;
    }

    public MyList<ObjBlock> getObjBlocks()
    {
        return objBlocks;
    }

    /**
     * 栈上的空间，从上到下
     * 调用者保存的寄存器空间
     * 溢出的空间
     * alloca 空间
     * 参数空间
     */
    public void fixStack()
    {
        for (MyList.MyNode<ObjBlock> objBlockNode : objBlocks)
        {
            ObjBlock block = objBlockNode.getVal();
            for (MyList.MyNode<ObjInstr> objInstrNode : block.getInstrs())
            {
                ObjInstr instr = objInstrNode.getVal();
                // 只有写了寄存器才需要保存
                for (ObjReg defReg : instr.getRegDef())
                {
                    int index = ((ObjPhyReg) defReg).getIndex();
                    if (ObjPhyReg.calleeSavedRegIndex.contains(index))
                    {
                        calleeSavedRegIndexes.add(index);
                    }
                }
            }
        }
        int stackRegSize = 4 * calleeSavedRegIndexes.size();

        totalStackSize = stackRegSize + allocaSize;

        for (ObjImm argOffset : argOffsets)
        {
            int newOffset = argOffset.getImmediate() + totalStackSize;
            argOffset.setImmediate(newOffset);
        }
    }

    /**
     * 这个函数用于处理非直接后继块（就是这个后继块我们不打算放在当前块的正后面），在这里是处理 true 后继
     * 对于这种块，我们不能把 copy 指令放到 cur 里，所以要么插入 succ，要么再做一个块
     * 之所以不能的原因是他不会直接放在 cur 的后面
     * @param curBlock 当前块
     * @param succBlock 间接后继
     * @param phiCopys copy
     */
    private void handleTrueCopys(ObjBlock curBlock, ObjBlock succBlock, ArrayList<ObjInstr> phiCopys)
    {
        // 如果没有 copy 的话，就不用费事了
        if (!phiCopys.isEmpty())
        {
            // 如果后继块前只有一个前驱块（当前块），那么就可以直接插入到后继块的最开始
            if (succBlock.getPreds().size() == 1)
            {
                succBlock.insertPhiCopysHead(phiCopys);
            }
            // 如果后继块前有多个前驱块（无法确定从哪个块来），那么就应该新形成一个块
            else
            {
                // 新做出一个中转块
                ObjBlock transferBlock = new ObjBlock(curBlock.getLoopDepth());

                // 把 phiMov 指令放到这里
                transferBlock.insertPhiCopysHead(phiCopys);

                // 做出一个中转块跳转到的指令
                ObjBranch objTransferJump = new ObjBranch(succBlock);
                transferBlock.addInstr(objTransferJump);

                // transfer 登记前驱后继
                transferBlock.setTrueSucc(succBlock);
                transferBlock.addPred(curBlock);

                // succ 登记前驱后继
                succBlock.removePred(curBlock);
                succBlock.addPred(transferBlock);

                // cur 登记前驱后继
                curBlock.setTrueSucc(transferBlock);
                // 修改 cur 的最后一条指令
                ObjBranch tailInstr = (ObjBranch) curBlock.getTailInstr();
                tailInstr.setTarget(transferBlock);
            }
        }
    }

    /**
     * 这里处理的是直接后继块，我们不会做一个新块，而是将 copy 直接插入 cur 的末尾
     * 如果后继还没有放置，那么就放置后继
     * 如果已经放置，那么就再做一个 jump
     * @param curBlock 当前块
     * @param succBlock 后继块
     * @param phiCopys copy
     */
    private void handleFalseCopys(ObjBlock curBlock, ObjBlock succBlock, ArrayList<ObjInstr> phiCopys)
    {
        for (ObjInstr phiCopy : phiCopys)
        {
            curBlock.addInstr(phiCopy);
        }
        // 如果已经序列化了，那么还需要增加一条 branch 指令，跳转到已经序列化的后继块上
        if (hasSerial.contains(succBlock))
        {
            ObjBranch objBranch = new ObjBranch(succBlock);
            curBlock.addInstr(objBranch);
        }
    }

    /**
     * 这个函数用于交换当前块的两个后继，交换操作很简单
     * 是为了让 false 块是未序列化块的几率更大，
     * 或者让 false 与 curBlock 间有 copys (在 false 和 true 均被序列化后)
     * @param curBlock 当前块
     * @param phiWaitLists phi copy
     */
    private void swapSuccBlock(ObjBlock curBlock, HashMap<MyPair<ObjBlock, ObjBlock>, ArrayList<ObjInstr>> phiWaitLists)
    {
        ObjBlock trueSucc = curBlock.getTrueSucc();
        ObjBlock falseSucc = curBlock.getFalseSucc();
        MyPair<ObjBlock, ObjBlock> falseLookUp = new MyPair<>(curBlock, falseSucc);
        if (!hasSerial.contains(trueSucc) ||
                hasSerial.contains(trueSucc) && hasSerial.contains(falseSucc) && (!phiWaitLists.containsKey(falseLookUp) || phiWaitLists.get(falseLookUp).isEmpty()))
        {
            curBlock.setTrueSucc(falseSucc);
            curBlock.setFalseSucc(trueSucc);
            ObjBranch tailBranch = (ObjBranch) curBlock.getTailInstr();
            ObjCondType cond = tailBranch.getCond();
            tailBranch.setCond(ObjCondType.getOppCond(cond));
            // 这里注意，不能直接用 trueBlock
            tailBranch.setTarget(curBlock.getTrueSucc());
        }
    }

    /**
     * 本质是一个
     * @param curBlock 当前块
     * @param phiWaitLists 记录 phi 的表
     */
    public void blockSerial(ObjBlock curBlock, HashMap<MyPair<ObjBlock, ObjBlock>, ArrayList<ObjInstr>> phiWaitLists)
    {
        // 登记
        hasSerial.add(curBlock);
        // 插入当前块,就是序列化当前块
        objBlocks.insertEnd(new MyList.MyNode<>(curBlock));

        // 如果没有后继了,那么就结束
        if (curBlock.getTrueSucc() == null && curBlock.getFalseSucc() == null)
        {
            return;
        }

        // 如果没有错误后继块,说明只有一个后继块，那么就应该考虑与当前块合并
        if (curBlock.getFalseSucc() == null)
        {
            ObjBlock succBlock = curBlock.getTrueSucc();
            // 这个前驱后继关系用于查询有多少个 phiMove 要插入，一个后继块，直接将这些指令插入到跳转之前即可
            MyPair<ObjBlock, ObjBlock> trueLookup = new MyPair<>(curBlock, succBlock);
            curBlock.insertPhiMovesTail(phiWaitLists.getOrDefault(trueLookup, new ArrayList<>()));

            // 合并的条件是后继块还未被序列化，此时只需要将最后一条跳转指令移除掉就好了
            if (!hasSerial.contains(succBlock))
            {
                curBlock.removeTailInstr();
                blockSerial(succBlock, phiWaitLists);
            }
            // 但是不一定能够被合并成功，因为又可以后继块已经被先序列化了，那么就啥都不需要干了
        }
        // 如果有两个后继块
        else
        {
            // 交换块的目的是为了让处理更加快捷
            swapSuccBlock(curBlock, phiWaitLists);

            ObjBlock trueSuccBlock = curBlock.getTrueSucc();
            ObjBlock falseSuccBlock = curBlock.getFalseSucc();
            MyPair<ObjBlock, ObjBlock> trueLookup = new MyPair<>(curBlock, trueSuccBlock);
            MyPair<ObjBlock, ObjBlock> falseLookup = new MyPair<>(curBlock, falseSuccBlock);

            handleTrueCopys(curBlock, trueSuccBlock, phiWaitLists.getOrDefault(trueLookup, new ArrayList<>()));

            handleFalseCopys(curBlock, falseSuccBlock, phiWaitLists.getOrDefault(falseLookup, new ArrayList<>()));

            if (!hasSerial.contains(curBlock.getFalseSucc()))
            {
                blockSerial(curBlock.getFalseSucc(), phiWaitLists);
            }
            if (!hasSerial.contains(curBlock.getTrueSucc()))
            {
                blockSerial(curBlock.getTrueSucc(), phiWaitLists);
            }
        }
    }


    /**
     * 需要打印：
     * 函数 label
     * 保存被调用者寄存器
     * 移动栈指针 sp
     * 所以基本块
     * @return 函数汇编
     */
    @Override
    public String toString()
    {
        StringBuilder funcSb = new StringBuilder();
        funcSb.append(name).append(":\n");
        // 只有非主函数才需要保存寄存器
        if (!name.equals("main"))
        {
            // 调用者保存的寄存器
            int stackOffset = -4;
            for (Integer savedRegIndex : calleeSavedRegIndexes)
            {
                funcSb.append("\t").append("sw ").append(new ObjPhyReg(savedRegIndex)).append(",\t")
                        .append(stackOffset).append("($sp)\n");
                // 下移 4
                stackOffset -= 4;
            }
        }

        // 移动栈指针
        if (totalStackSize != 0)
        {
            funcSb.append("\tsub $sp,\t$sp,\t").append(totalStackSize).append("\n");
        }

        // 遍历所有基本块
        for (MyList.MyNode<ObjBlock> objBlockNode : objBlocks)
        {
            ObjBlock block = objBlockNode.getVal();
            funcSb.append(block);
        }

        return funcSb.toString();
    }
}
