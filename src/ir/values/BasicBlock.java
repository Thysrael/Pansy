package ir.values;

import ir.types.LabelType;
import ir.values.instructions.Instruction;
import ir.values.instructions.Phi;
import pass.analyze.Loop;
import util.MyList;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 一个基本块由若干指令组成，最后一条指令一定为终结指令（ret / br）
 * 支持 3 种插入指令的方式(插在末尾，插在某个指令之前，插在头部)
 * BasicBlock 不是 User 他并不使用 Instruction
 */
public class BasicBlock extends Value
{
    private final MyList<Instruction> instructions = new MyList<>();
    /**
     * 前驱与后继基本块，不讲求顺序，因此不用链表
     */
    private final HashSet<BasicBlock> predecessors = new HashSet<>();
    private final HashSet<BasicBlock> successors = new HashSet<>();
    /**
     * 这个表示该基本块的支配者块
     */
    private final ArrayList<BasicBlock> domers = new ArrayList<>();
    /**
     * 表示该基本块直接支配的基本块
     */
    private final ArrayList<BasicBlock> idomees = new ArrayList<>();
    /**
     * 表示直接支配该基本块的基本块
     */
    private BasicBlock Idomer;
    /**
     * 在支配树中的深度
     */
    private int domLevel;
    /**
     * 支配边际，即刚好不被当前基本块支配的基本块
     */
    private final HashSet<BasicBlock> dominanceFrontier = new HashSet<>();
    /**
     * 当前块所在的循环
     * 如果为 null，那么说明当前块不在循环中
     */
    private Loop parentLoop;
    /**
     * @param nameNum 基本块的名字，一定为数字编号
     * @param parent  基本块所在函数
     */
    public BasicBlock(int nameNum, Function parent)
    {
        super("%b" + nameNum, new LabelType(), parent);
        parent.addFunctionSymbol(this);
    }

    /**
     * 这个 block 主要用于控制 break 和 continue 后的语句
     */
    public BasicBlock()
    {
        super("%LOOP_TMP", new LabelType(), Function.LOOP_TRASH);
    }

    /**
     * 从这里可以看出，BasicBlock 的父亲一定是 Function
     * @return Function Parent
     */
    @Override
    public Function getParent()
    {
        return (Function) super.getParent();
    }

    public HashSet<BasicBlock> getPredecessors()
    {
        return predecessors;
    }

    public void insertTail(Instruction instruction)
    {
        if (findNode(instruction) != null)
        {
            throw new AssertionError("instruction is already in!");
        }
        MyList.MyNode<Instruction> node = new MyList.MyNode<>(instruction);
        node.insertEnd(instructions);
        instruction.setNode(node);
    }

    public void insertBefore(Instruction instruction, Instruction before)
    {
        if (findNode(instruction) != null)
        {
            throw new AssertionError("instruction is already in!");
        }
        MyList.MyNode<Instruction> beforeNode = findNode(before);
        if (beforeNode == null)
        {
            throw new AssertionError("can't find before instruction!");
        }
        MyList.MyNode<Instruction> node = new MyList.MyNode<>(instruction);
        node.insertBefore(beforeNode);
        instruction.setNode(node);
    }

    public void insertHead(Instruction instruction)
    {
        if (findNode(instruction) != null)
        {
            throw new AssertionError("instruction is already in!");
        }
        MyList.MyNode<Instruction> node = new MyList.MyNode<>(instruction);
        node.insertHead(instructions);
        instruction.setNode(node);
    }

    public void eraseInstruction(Instruction instruction)
    {
        MyList.MyNode<Instruction> node = findNode(instruction);
        if (node == null)
        {
            throw new AssertionError("can't find instruction: " + instruction);
        }
        node.removeSelf();
    }

    private MyList.MyNode<Instruction> findNode(Instruction instruction)
    {
        return instruction.getNode();
    }

    public void addPredecessor(BasicBlock predecessor)
    {
        predecessors.add(predecessor);
    }

    public void addSuccessor(BasicBlock successor)
    {
        successors.add(successor);
    }

    /**
     * pred - succ 也是一对双向关系
     * @param oldBlock 原有 block
     * @param newBlock 现有 block
     */
    public void replaceSuccessor(BasicBlock oldBlock, BasicBlock newBlock)
    {
        successors.remove(oldBlock);
        oldBlock.predecessors.remove(this);
        successors.add(newBlock);
    }

    public MyList<Instruction> getInstructions()
    {
        return instructions;
    }

    /**
     * 获得结尾的指令，如果结尾没有指令，那么返回 null
     * @return 结尾指令
     */
    public Instruction getTailInstr()
    {
        if (instructions.isEmpty())
        {
            return null;
        }
        else
        {
            return instructions.getTail().getVal();
        }
    }

    public ArrayList<BasicBlock> getDomers()
    {
        return domers;
    }

    /**
     * 返回当前块是否是 other 的支配者
     * @param other 另一个块
     * @return 是则为 true
     */
    public boolean isDominate(BasicBlock other)
    {
        return other.domers.contains(this);
    }

    public ArrayList<BasicBlock> getIdomees()
    {
        return idomees;
    }

    public void setIdomer(BasicBlock idomer)
    {
        Idomer = idomer;
    }

    public void setDomLevel(int domLevel)
    {
        this.domLevel = domLevel;
    }

    public int getDomLevel()
    {
        return domLevel;
    }

    public HashSet<BasicBlock> getDominanceFrontier()
    {
        return dominanceFrontier;
    }

    public HashSet<BasicBlock> getSuccessors()
    {
        return successors;
    }

    public BasicBlock getIdomer()
    {
        return Idomer;
    }

    public ArrayList<Instruction> getInstructionsArray()
    {
        ArrayList<Instruction> instructionArrayList = new ArrayList<>();
        for (MyList.MyNode<Instruction> instructionMyNode : instructions)
        {
            instructionArrayList.add(instructionMyNode.getVal());
        }
        return instructionArrayList;
    }

    /**
     * 为 Function 的 renumber 服务
     */
    public int renumber(int startNameNum)
    {
        for (MyList.MyNode<Instruction> instructionNode : instructions)
        {
            Instruction instruction = instructionNode.getVal();
            if (!instruction.getName().equals(""))
            {
                instruction.setNameNum(startNameNum++);
                getParent().addFunctionSymbol(instruction);
            }
        }
        return startNameNum;
    }

    /**
     * 获得循环深度
     * 如果不在循环中，则深度为 1
     * @return 循环深度
     */
    public int getLoopDepth()
    {
        if (parentLoop == null)
        {
            return 0;
        }
        return parentLoop.getLoopDepth();
    }

    public void setParentLoop(Loop parentLoop)
    {
        this.parentLoop = parentLoop;
    }

    public Loop getParentLoop()
    {
        return parentLoop;
    }

    public void reducePhi(boolean reducePhi)
    {
        for (Instruction instruction : getInstructionsArray())
        {
            if (instruction instanceof Phi)
            {
                ((Phi) instruction).removeIfRedundant(reducePhi);
            }
        }
    }

    /**
     * 移除基本块
     */
    public void eraseFromParent()
    {
        getParent().removeBlock(this);
    }

    @Override
    public String toString()
    {
        // 将 % 去掉，因为只有在跳转指令里需要加上这个
        StringBuilder s = new StringBuilder(getName().substring(1)).append(":\n");
        for (MyList.MyNode<Instruction> instructionNode : instructions)
        {
            s.append('\t').append(instructionNode.getVal()).append('\n');
        }
        if (!instructions.isEmpty())
        {
            s.deleteCharAt(s.length() - 1);
        }
        return s.toString();
    }

}
