package back.process;

import back.component.ObjBlock;
import back.component.ObjFunction;
import back.instruction.ObjInstr;
import back.operand.ObjReg;
import util.MyList;

import java.util.HashMap;
import java.util.HashSet;

public class BlockLiveInfo
{
    private final HashSet<ObjReg> liveUse = new HashSet<>();
    private final HashSet<ObjReg> liveDef = new HashSet<>();
    private HashSet<ObjReg> liveIn = new HashSet<>();
    private HashSet<ObjReg> liveOut = new HashSet<>();

    /**
     * 对于每一个函数都进行一个这样的分析
     * @return 一个每个 block 都对应的一个 info 的 map
     */
    public static HashMap<ObjBlock, BlockLiveInfo> livenessAnalysis(ObjFunction func)
    {
        HashMap<ObjBlock, BlockLiveInfo> liveInfoMap = new HashMap<>();
        // 开始遍历每一个 block
        for (MyList.MyNode<ObjBlock> blockNode : func.getObjBlocks())
        {
            ObjBlock block = blockNode.getVal();

            BlockLiveInfo blockLiveInfo = new BlockLiveInfo();
            liveInfoMap.put(block, blockLiveInfo);
            // 开始遍历 block 中的指令, 跟定义中的一模一样
            for (MyList.MyNode<ObjInstr> instrNode : block.getInstrs())
            {
                ObjInstr instr = instrNode.getVal();
                // 还没定义就被使用，这里是正确的
                instr.getRegUse().stream()
                        .filter(ObjReg::needsColor)
                        .filter(use -> !blockLiveInfo.liveDef.contains(use))
                        .forEach(blockLiveInfo.liveUse::add);
                // 还没使用就被定义，这里应该是错误的，因为定义就是定义，就是杀死，不会因为使用而不杀死
                instr.getRegDef().stream()
                        .filter(ObjReg::needsColor)
                        .forEach(blockLiveInfo.liveDef::add);
            }
            // 这里应该是没有问题的
            blockLiveInfo.liveIn.addAll(blockLiveInfo.liveUse);
        }

        // 香香说这个叫不动点，叫单调有界必收敛
        boolean changed = true;
        while (changed)
        {
            changed = false;
            // 开始遍历 func 中的 block
            for (MyList.MyNode<ObjBlock> blockNode : func.getObjBlocks())
            {
                ObjBlock block = blockNode.getVal();
                BlockLiveInfo blockLiveInfo = liveInfoMap.get(block);
                HashSet<ObjReg> newLiveOut = new HashSet<>();

                // 下面是加入两个后继,这里是正确的，LiveOut 就是 LiveIn 的并集
                if (block.getTrueSucc() != null)
                {
                    BlockLiveInfo succBlockInfo = liveInfoMap.get(block.getTrueSucc());
                    newLiveOut.addAll(succBlockInfo.liveIn);
                }

                if (block.getFalseSucc() != null)
                {
                    BlockLiveInfo succBlockInfo = liveInfoMap.get(block.getFalseSucc());
                    newLiveOut.addAll(succBlockInfo.liveIn);
                }

                // 第一次的时候应该是没有办法 equal 的，这是因为之前 liveOut 并没有被赋值
                if (!newLiveOut.equals(blockLiveInfo.liveOut))
                {
                    changed = true;
                    blockLiveInfo.liveOut = newLiveOut;

                    // 这里模拟的是 LiveUse
                    blockLiveInfo.liveIn = new HashSet<>(blockLiveInfo.liveUse);

                    // liveIn = liveUse + liveOut - liveDef
                    // 这里模拟的是取差集，也是符合的，就是不知道为啥外面要加个循环
                    blockLiveInfo.liveOut.stream()
                            .filter(objOperand -> !blockLiveInfo.liveDef.contains(objOperand))
                            .forEach(blockLiveInfo.liveIn::add);
                }
            }
        }

        return liveInfoMap;
    }

    public HashSet<ObjReg> getLiveOut()
    {
        return liveOut;
    }
}
