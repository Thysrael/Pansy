package pass.refactor;

import driver.Config;
import ir.IrBuilder;
import ir.types.DataType;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import ir.values.instructions.*;
import pass.Pass;
import util.MyList;
import util.MyPair;

import java.util.*;

public class Mem2reg implements Pass
{
    /**
     * 当前分析的 alloca 唯一一个 store 指令
     */
    private Store onlyStore;
    /**
     * 如果 store 和 load 在同一个块
     */
    private BasicBlock onlyBlock;
    /**
     * 当前函数的入口块
     */
    private BasicBlock entryBlock;
    /**
     * 对于特定的 alloca 指令，其 load 所在的块
     */
    private final HashSet<BasicBlock> usingBlocks = new HashSet<>();
    /**
     * 对于特定的 alloca 指令，其 store 所在的块
     */
    private final HashSet<BasicBlock> definingBlocks = new HashSet<>();
    private Function function;
    /**
     * 这里面记录当前函数可以被提升的 allocas
     */
    private final ArrayList<Alloca> allocas = new ArrayList<>();
    private final HashMap<Phi, Alloca> phi2Alloca = new HashMap<>();

    @Override
    public void run()
    {
        if (Config.openMem2reg)
        {
            // 遍历每一个函数
            for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
            {
                this.function = funcNode.getVal();
                process();
            }
        }
    }

    private void process()
    {
        // 内建函数不分析
        if (function.isBuiltin())
        {
            return;
        }

        // create alloca list
        entryBlock = function.getBasicBlocks().getHead().getVal();
        // check is we can promote this alloca
        // promote 是一个有趣的概念，说的是可以被消去的 alloca，被叫做“提升”
        for (MyList.MyNode<Instruction> instrNode : entryBlock.getInstructions())
        {
            Instruction instr = instrNode.getVal();
            if (instr instanceof Alloca && ((Alloca) instr).canPromotable())
            {
                allocas.add((Alloca) instr);
            }
        }

        // llvm doesn't include this step, I add it to make things simple
        sweepAllBlocks();

        // promote alloca one by one
        // 遍历 alloca 数组
        Iterator<Alloca> iterator = allocas.iterator();
        while (iterator.hasNext())
        {
            Alloca alloca = iterator.next();
            // is alloca is never used, we can safely delete it
            // 之前的 sweep 会导致 alloca 的使用者减少，那么就可能出现 alloca 没有使用者的情况，此时就可以将其删除了
            if (alloca.getUsers().isEmpty())
            {
                alloca.dropAllOperands();
                alloca.eraseFromParent();
                // 将指令从节点中移出
                iterator.remove();
                // 也不用分析了
                continue;
            }

            // onlyStore
            analyzeAllocaInfo(alloca);

            // prune optimization
            // no store，那么就删除这个 alloca 和与之相关的 load
            if (definingBlocks.size() == 0)
            {
                // 如果没有 store 那么使用者只有 load 了，那么删掉 load 就好了，因为不能读没有的值
                // 之所以在这里 clone， 是因为不然 alloca - load 的关系也没了
                ArrayList<User> loadClone = new ArrayList<>(alloca.getUsers());
                for (User user : loadClone)
                {
                    Load load = (Load) user;
                    load.replaceAllUsesWith(ConstInt.ZERO);
                    load.dropAllOperands();
                    load.eraseFromParent();
                }
                alloca.dropAllOperands();
                alloca.eraseFromParent();
                iterator.remove();
                // 不用再讨论了
                continue;
            }

            // onlyStore
            if (onlyStore != null && dealOnlyStore(alloca))
            {
                iterator.remove();
                continue;
            }

            // store / load in one block
            if (onlyBlock != null)
            {
                dealOneBlock(alloca);
                iterator.remove();
                continue;
            }

            // 所有 definingBlocks 的递归支配边界（递归边界的闭包）都是需要插入 phi 节点的
            HashSet<BasicBlock> phiBlocks = calIDF(definingBlocks);
            // 去掉不需要插入的节点
            phiBlocks.removeIf(block -> !isPhiAlive(alloca, block));

            // insert phi node
            insertPhiNode(alloca, phiBlocks);
        }

        // 如果 alloca 都被删除了（一般表示不需要插入 phi 就可以结束战斗）
        if (allocas.isEmpty())
        {
            return;
        }

        // rename phi node and add incoming <value, block>
        renamePhiNode();

        for (Alloca ai : allocas)
        {
            ai.dropAllOperands();
            ai.eraseFromParent();
        }

        allocas.clear();
        phi2Alloca.clear();
    }

    /**
     * 利用不动点法求解支配边界的闭包
     * 也就是支配边界，支配边界的支配边界，支配边界的支配边界的支配边界....
     * @param definingBlocks 拥有 store 的点
     * @return 支配边界的闭包
     */
    private HashSet<BasicBlock> calIDF(HashSet<BasicBlock> definingBlocks)
    {
        HashSet<BasicBlock> ans = new HashSet<>();
        for (BasicBlock definingBlock : definingBlocks)
        {
            ans.addAll(definingBlock.getDominanceFrontier());
        }

        boolean changed = true;
        while (changed)
        {
            changed = false;
            HashSet<BasicBlock> newAns = new HashSet<>(ans);
            for (BasicBlock block : ans)
            {
                newAns.addAll(block.getDominanceFrontier());
            }
            if (newAns.size() > ans.size())
            {
                changed = true;
                ans = newAns;
            }
        }

        return ans;
    }

    /**
     * 填写
     * definingBlocks, usingBlocks, onlyStore, onlyBlock
     * @param alloca 当前分析的 alloca
     */
    private void analyzeAllocaInfo(Alloca alloca)
    {
        // 清空
        definingBlocks.clear();
        usingBlocks.clear();
        onlyBlock = null;
        onlyStore = null;

        // 使用 alloca 的 store 的指令个数
        int storeCnt = 0;
        // 遍历使用 alloca 的指令（就是 load 和 store）
        for (Value user : alloca.getUsers())
        {
            // 如果是 store
            if (user instanceof Store)
            {
                definingBlocks.add(((Store) user).getParent());
                if (storeCnt == 0)
                {
                    onlyStore = (Store) user;
                }
                storeCnt++;
            }
            else if (user instanceof Load)
            {
                usingBlocks.add(((Load) user).getParent());
            }
        }

        if (storeCnt > 1)
        {
            onlyStore = null;
        }

        if (definingBlocks.size() == 1 && definingBlocks.equals(usingBlocks))
        {
            onlyBlock = definingBlocks.iterator().next();
        }
    }

    /**
     * 处理只有一个 store 的情况
     * @param alloca 当前 alloca
     * @return usingBlock 是否为空，本质上是是否需要进行下一步处理，如果是 false 那么就需要继续处理
     */
    private boolean dealOnlyStore(Alloca alloca)
    {
        // construct later
        usingBlocks.clear();
        // replaceValue 是 store 向内存写入的值
        Value replaceValue = onlyStore.getValue();
        ArrayList<User> users = new ArrayList<>(alloca.getUsers());
        // 只有一个 store ，其他都是 load
        for (User user : users)
        {
            if (user instanceof Store)
            {
                if (!user.equals(onlyStore))
                {
                    throw new AssertionError("ai has store user different from onlyStore in dealOnlyStore");
                }
            }
            else
            {
                Load load = (Load) user;
                // 如果 store 所在的块是 load 的支配者，那么就将用到 load 读入值的地方换成 store
                if (onlyStore.getParent() != load.getParent() && onlyStore.getParent().isDominate(load.getParent()))
                {
                    load.replaceAllUsesWith(replaceValue);
                    load.dropAllOperands();
                    load.eraseFromParent();
                }
                // 没有这么好的条件的话，就加入 usingBlocks
                else
                {
                    usingBlocks.add(load.getParent());
                }
            }
        }

        boolean result = usingBlocks.isEmpty();
        // 如果没有比较差的 load，那么 store 就可以删除了，因为都被 replace 了
        if (result)
        {
            onlyStore.dropAllOperands();
            onlyStore.eraseFromParent();
            alloca.dropAllOperands();
            alloca.eraseFromParent();
        }

        return result;
    }

    private void dealOneBlock(Alloca alloca)
    {
        boolean encounterStore = false;
        // 遍历所有的指令
        for (MyList.MyNode<Instruction> instructionMyNode : onlyBlock.getInstructions())
        {
            Instruction instruction = instructionMyNode.getVal();
            // 遇到了还没有 store 就先 load 的情况，直接删掉
            if (instruction instanceof Load && instruction.getUsedValue(0) == alloca && !encounterStore)
            {
                instruction.replaceAllUsesWith(ConstInt.ZERO);
                instruction.dropAllOperands();
                instruction.eraseFromParent();
            }
            else if (instruction instanceof Store && instruction.getUsedValue(1) == alloca)
            {
                if (encounterStore)
                {
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
                else
                {
                    encounterStore = true;
                }
            }
        }
        alloca.dropAllOperands();
        alloca.eraseFromParent();
    }

    /**
     * 按照基本块进行 sweep(打扫)
     */
    private void sweepAllBlocks()
    {
        for (MyList.MyNode<BasicBlock> blockNode : function.getBasicBlocks())
        {
            sweepBlock(blockNode.getVal());
        }
    }

    /**
     * 这是一种指令删除，指的是这种情况
     * 在一个基本块中
     * 对于 store -> alloca -> load 这样的逻辑链条（这样的 store 和 load 一定是在对非数组读写，不然就是 store - gep - load）
     * 用 store 的值代替所有的 load 出的值，然后将 load 删除
     * 之后再对同一个 alloca 的多次 store，简化为最后一次
     * @param block 当前块
     */
    private void sweepBlock(BasicBlock block)
    {
        HashMap<Alloca, Store> alloca2store = new HashMap<>();

        for (MyList.MyNode<Instruction> instrNode : block.getInstructions())
        {
            Instruction instruction = instrNode.getVal();
            // 如果当前指令是 store 指令，而且地址是 alloca 分配的，那么就存到 alloca2store 中
            if (instruction instanceof Store && instruction.getUsedValue(1) instanceof Alloca)
            {
                alloca2store.put((Alloca) instruction.getUsedValue(1), (Store) instruction);
            }
            // 如果当前指令是 load，而且地址是 alloca
            else if (instruction instanceof Load && instruction.getUsedValue(0) instanceof Alloca)
            {
                Alloca alloca = (Alloca) instruction.getUsedValue(0);
                // 如果将 store 取出来
                Store store = alloca2store.get(alloca);
                // 对应的是没有赋初值就 load 的情况，不用考虑这种特殊情况
                if (store == null && block == entryBlock)
                {
                    instruction.replaceAllUsesWith(ConstInt.ZERO);
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
                // 这里才是正文
                else if (store != null)
                {
                    // 这里首先用 store 的要存入的值代替了 load 要读入的值
                    instruction.replaceAllUsesWith(store.getValue());
                    // 将这条 load 指令删除
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
            }
        }
        // 清空对应关系
        alloca2store.clear();

        // 进行倒序遍历
        for (MyList.MyNode<Instruction> instrNode = block.getInstructions().getTail();
             instrNode != null;
             instrNode = instrNode.getPre())
        {
            Instruction instruction = instrNode.getVal();
            // 如果是 store 指令
            if (instruction instanceof Store && instruction.getUsedValue(1) instanceof Alloca)
            {
                Alloca alloca = (Alloca) instruction.getUsedValue(1);
                Store store = alloca2store.get(alloca);
                // 这不是最后一条对于 alloca 这个内存的写，那么就删除
                if (store != null)
                {
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
                // 说明之前没有，那么记录并加入
                else
                {
                    alloca2store.put(alloca, (Store) instruction);
                }
            }
        }
    }

    /**
     * 有插入 phi 的必要：也就是有与 alloca 相关的 load 或者 store
     * @param alloca alloca
     * @param block 当前块
     * @return 需要插入则为 true
     */
    private boolean isPhiAlive(Alloca alloca, BasicBlock block)
    {
        for (MyList.MyNode<Instruction> instrNode : block.getInstructions())
        {
            Instruction instruction = instrNode.getVal();
            if (instruction instanceof Load && instruction.getUsedValue(0) == alloca)
            {
                return true;
            }
            if (instruction instanceof Store && instruction.getUsedValue(1) == alloca)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 将 phi 节点插入基本块之前
     * 并且填写 phi2Alloca 为后续的重命名做准备
     * @param alloca 当前 alloca
     * @param phiBlocks 需要插入 phi 的基本块
     */
    private void insertPhiNode(Alloca alloca, HashSet<BasicBlock> phiBlocks)
    {
        for (BasicBlock phiBlock : phiBlocks)
        {
//            Phi phi = new Phi(phiNameNum++, (DataType) alloca.getAllocatedType(), phiBlock, phiBlock.getPredecessors().size());
//            phiBlock.insertHead(phi);
            Phi phi = IrBuilder.getInstance().buildPhi((DataType) alloca.getAllocatedType(), phiBlock);
            phi2Alloca.put(phi, alloca);
        }
    }

    /**
     * 重命名，完成 phi 的嵌入
     */
    private void renamePhiNode()
    {
        HashMap<BasicBlock, Boolean> visitMap = new HashMap<>();
        HashMap<Alloca, Value> variableVersion = new HashMap<>();

        for (MyList.MyNode<BasicBlock> basicBlockNode : function.getBasicBlocks())
        {
            visitMap.put(basicBlockNode.getVal(), false);
        }

        for (Alloca alloca : allocas)
        {
            // default undef is 0
            variableVersion.put(alloca, ConstInt.ZERO);
        }
        // 手动 dfs
        Stack<MyPair<BasicBlock, HashMap<Alloca, Value>>> bbStack = new Stack<>();
        bbStack.push(new MyPair<>(entryBlock, variableVersion));

        while (!bbStack.isEmpty())
        {
            MyPair<BasicBlock, HashMap<Alloca, Value>> tmp = bbStack.pop();
            BasicBlock currentBlock = tmp.getFirst();
            variableVersion = tmp.getSecond();
            if (visitMap.get(currentBlock))
            {
                continue;
            }

            // main logic
            // 遍历当前块的所有指令
            int i = 0;
            ArrayList<Instruction> instructions = currentBlock.getInstructionsArray();
            while (instructions.get(i) instanceof Phi)
            {
                variableVersion.put(phi2Alloca.get((Phi) instructions.get(i)), instructions.get(i));
                i++;
            }
            while (i < instructions.size())
            {
                Instruction instruction = instructions.get(i);
                if (instruction instanceof Load)
                {
                    Load load = (Load) instruction;
                    if (load.getAddr() instanceof Alloca)
                    {
                        instruction.replaceAllUsesWith(variableVersion.get((Alloca) ((Load) instruction).getAddr()));
                        instruction.dropAllOperands();
                        instruction.eraseFromParent();
                    }
                }
                else if (instruction instanceof Store)
                {
                    Store store = (Store) instruction;
                    if (store.getAddr() instanceof Alloca)
                    {
                        variableVersion.put((Alloca) store.getAddr(), store.getValue());
                        instruction.dropAllOperands();
                        instruction.eraseFromParent();
                    }
                }
                i++;
            }

            for (BasicBlock successor : currentBlock.getSuccessors())
            {
                instructions = successor.getInstructionsArray();
                i = 0;
                while (instructions.get(i) instanceof Phi)
                {
                    Phi phi = (Phi) (instructions.get(i));
                    Alloca ai = phi2Alloca.get(phi);
                    phi.addIncoming(variableVersion.get(ai), currentBlock);
                    i++;
                }
                if (!visitMap.get(successor))
                {
                    bbStack.push(new MyPair<>(successor, new HashMap<>(variableVersion)));
                }
            }

            visitMap.put(currentBlock, true);
        }
    }
}
