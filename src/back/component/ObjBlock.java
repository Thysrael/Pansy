package back.component;

import back.instruction.ObjInstr;
import util.MyList;

import java.util.ArrayList;

public class ObjBlock
{
    // 用来给 ObjBlock 一个独有的名字
    private static int index = 0;
    private final String name;
    // 组成 block 的 instr
    private final MyList<ObjInstr> instrs = new MyList<>();
    // 用来表示该 block 所属的 function

    // 如果最后一条指令是有条件跳转指令，那falseSucc就是直接后继块。false指条件跳转中不满足条件下继续执行的基本块
    private ObjBlock falseSucc = null;
    // 一个基本块最多两个后继块，如果基本块只有一个后继，那么falseSucc是null，trueSucc不是null
    private ObjBlock trueSucc = null;
    // 前驱块
    private final ArrayList<ObjBlock> preds = new ArrayList<>();
    // 记录 ir 传来的循环深度
    private final int loopDepth;

    public ObjBlock(String name, int loopDepth)
    {
        this.name = "Basic_" +  name.substring(1) + "_" + index;
        index++;
        this.loopDepth = loopDepth;
    }

    /**
     * 有的 ObjBlock 可能没有对应的 irBlock，而是由 phi 的需要生长出来的，所以没有名字
     */
    public ObjBlock(int loopDepth)
    {
        this.name = "transfer_" + index;
        index++;
        this.loopDepth = loopDepth;
    }

    public void addPred(ObjBlock pred)
    {
        this.preds.add(pred);
    }

    public void removePred(ObjBlock pred)
    {
        this.preds.remove(pred);
    }

    public ArrayList<ObjBlock> getPreds()
    {
        return preds;
    }

    public void addInstr(ObjInstr armInstr)
    {
        instrs.insertEnd(armInstr.getNode());
    }

    public void addInstrHead(ObjInstr armInstr)
    {
        instrs.insertBeforeHead(armInstr.getNode());
    }

    public void setFalseSucc(ObjBlock falseSucc)
    {
        this.falseSucc = falseSucc;
    }

    public void setTrueSucc(ObjBlock trueSucc)
    {
        this.trueSucc = trueSucc;
    }

    public ObjBlock getFalseSucc()
    {
        return falseSucc;
    }

    public ObjBlock getTrueSucc()
    {
        return trueSucc;
    }

    public String getName()
    {
        return name;
    }

    public int getLoopDepth()
    {
        return loopDepth;
    }

    public MyList<ObjInstr> getInstrs()
    {
        return instrs;
    }

    /**
     *  phi 指令解析的时候会产生一大堆没有归属的 mov 指令
     *  如果这个块只有一个后继块，那么我们需要把这些 mov 指令插入到最后一条跳转指令之前，这样就可以完成 phi 的更新
     * @param phiMoves 一大堆没有归属的 move 指令
     */
    public void insertPhiMovesTail(ArrayList<ObjInstr> phiMoves)
    {
        for (ObjInstr phiMove : phiMoves)
        {
            instrs.insertBeforeTail(phiMove.getNode());
        }
    }

    /**
     * phiMoves 的顺序已经是正确的了，所以这个方法会确保 phiMoves 按照其原来的顺序插入到 block 的头部
     * @param phiMoves 待插入的 copy 序列
     */
    public void insertPhiCopysHead(ArrayList<ObjInstr> phiMoves)
    {
        for (int i = phiMoves.size() - 1; i >= 0; i--)
        {
            instrs.insertBeforeHead(phiMoves.get(i).getNode());
        }
    }

    /**
     * 这个指令一般用于去掉末尾的跳转指令，然后可以与下一个块合并
     */
    public void removeTailInstr()
    {
        instrs.getTail().removeSelf();
    }

    public ObjInstr getTailInstr()
    {
        return instrs.getTail().getVal();
    }


    @Override
    public String toString()
    {
        StringBuilder blockSb = new StringBuilder();
        // 块标签
        blockSb.append(name).append(":\n");

        for (MyList.MyNode<ObjInstr> node : instrs)
        {
            ObjInstr objInstr = node.getVal();
            blockSb.append("\t").append(objInstr);
        }

        return blockSb.toString();
    }
}
