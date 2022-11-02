package back.process;

import back.component.ObjBlock;
import back.component.ObjFunction;
import back.component.ObjGlobalVariable;
import back.component.ObjModule;
import back.instruction.*;
import back.operand.*;
import ir.types.*;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.*;
import ir.values.instructions.*;
import util.MyList;
import util.MyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import static back.instruction.ObjBinType.*;
import static back.instruction.ObjCondType.*;

public class IrParser
{
    private final Module irModule;
    private final ObjModule objModule;
    /**
     * 用于提供 ir 函数到 obj 函数的映射
     */
    private final HashMap<Function, ObjFunction> fMap = new HashMap<>();
    /**
     * 用于提供 ir 块到 obj 块的映射
     */
    private final HashMap<BasicBlock, ObjBlock> bMap = new HashMap<>();
    /**
     * 这是一个非常有意思的设计，可以被解析成操作数的 Value 有很多种，这些操作数会被之后的指令使用，
     * 所以我们需要记录下来有哪些 Value 被映射成了 ObjOperand
     * 与此同时，我们并不需要记录所有的可以被解析成 ObjOperand 的 Value，当 Value 是常量的时候，我们需要每次都解析
     * 因为 ir 区分不了两个值一样的常量，所以没法被 HashMap 检索
     * 总而言之，我们会在这个 map 中登记 instr 的目的寄存器，arg 参数
     * 但是不会登记 imm， float，label
     */
    private final HashMap<Value, ObjOperand> operandMap = new HashMap<>();
    /**
     * 这个 map 会根据 被除数和除数对 来查询之前的运算结果
     * 之所以要加上 block 信息，是因为 block 限制了历史，不是所有的 block 都会被运行到的
     */
    private final HashMap<MyPair<ObjBlock, MyPair<ObjOperand, ObjOperand>>, ObjOperand> divMap = new HashMap<>();
    /**
     * key 是基本块的 <前驱，后继> 关系，查询出来的 Arraylist 是需要插入到两个块之间的指令（一般是插入到前驱块尾部），这样可以实现 phi 的选择功能
     */
    private final HashMap<MyPair<ObjBlock, ObjBlock>, ArrayList<ObjInstr>> phiCopysLists = new HashMap<>();

    public IrParser()
    {
        this.irModule = Module.getInstance();
        this.objModule = new ObjModule();
    }

    /**
     * 这是解析的主函数
     *
     * @return 一个组织好的 Module
     */
    public ObjModule parseModule()
    {
        parseGlobalVariables();
        parseFunctions();
        return objModule;
    }

    /**
     * 分析全局变量们
     * TODO 在 mips 中，一个是要维持一个记录地址的东西，因为我要手写 la
     */
    private void parseGlobalVariables()
    {
        MyList<GlobalVariable> irGlobalVars = irModule.getGlobalVariables();

        for (MyList.MyNode<GlobalVariable> node : irGlobalVars)
        {
            GlobalVariable irGlobalVar = node.getVal();
            ObjGlobalVariable objGlobalVariable = parseGlobalVariable(irGlobalVar);
            objModule.addGlobalVariable(objGlobalVariable);
        }
    }

    /**
     * 对于全局变量进行分析，并确定全局变量的大小和内容
     * 全局变量有三种：整数，数组，STR
     * @param irGlobalVar ir 全局变量
     * @return 全局变量
     */
    private ObjGlobalVariable parseGlobalVariable(GlobalVariable irGlobalVar)
    {
        // 用于存放元素的数组
        ArrayList<Integer> elements = new ArrayList<>();
        Constant irInitVal = irGlobalVar.getInitVal();
        // 没有初始化
        if (irInitVal instanceof ZeroInitializer)
        {
            return new ObjGlobalVariable(irGlobalVar.getName(), irInitVal.getValueType().getSize());
        }
        // 是字符串
        else if (irInitVal instanceof ConstStr)
        {
            return new ObjGlobalVariable(irGlobalVar.getName(), ((ConstStr) irInitVal).getContent());
        }
        // 是数组
        else if (irInitVal instanceof ConstArray)
        {
            ArrayList<ConstInt> dataElements = ((ConstArray) irInitVal).getDataElements();
            for (ConstInt dataElement : dataElements)
            {
                elements.add(dataElement.getValue());
            }
            return new ObjGlobalVariable(irGlobalVar.getName(), elements);
        }
        // 单变量
        else
        {
            elements.add(((ConstInt) irInitVal).getValue());
            return new ObjGlobalVariable(irGlobalVar.getName(), elements);
        }
    }

    /**
     * 这个函数会初步完成对于所有 ObjBlock 的构造，并且分别登记到 bMap
     * 之所以要在开始解析之前完成这个步骤，是因为对于 block 来说，他需要知悉前驱块，后继块
     * 但是这些块有可能还没有被解析，所以可能无法登记，所以这里先将所有的 block 构造完成，方便之后细致解析
     * 除此之外，还需要进行前驱块的登记，这是因为 obj 前驱块的编号必须与 ir 前驱块编号保持一致
     * （在查询 copy 的时候需要，不过应该可以改掉）
     * 在 mips 中，如果想要化到 ir 中，那么 fMap 在最外层， block 在 function 那一层即可
     */
    private void irMap()
    {
        MyList<Function> irFunctions = irModule.getFunctions();

        for (MyList.MyNode<Function> functionNode : irFunctions)
        {
            Function irFunction = functionNode.getVal();
            ObjFunction objFunction = new ObjFunction(irFunction.getName(), irFunction.isBuiltin());

            fMap.put(irFunction, objFunction);
            objModule.addFunction(objFunction);

            MyList<BasicBlock> irBlocks = irFunction.getBasicBlocks();
            // 建立新块
            for (MyList.MyNode<BasicBlock> blockNode : irBlocks)
            {
                BasicBlock irBlock = blockNode.getVal();
                ObjBlock objBlock = new ObjBlock(irBlock.getName(), irBlock.getLoopDepth());
                bMap.put(irBlock, objBlock);
            }
            // 完成前驱映射
            for (MyList.MyNode<BasicBlock> blockNode : irBlocks)
            {
                BasicBlock irBlock = blockNode.getVal();
                ObjBlock objBlock = bMap.get(irBlock);

                for (BasicBlock predecessor : irBlock.getPredecessors())
                {
                    objBlock.addPred(bMap.get(predecessor));
                }
            }
        }
    }

    /**
     * 首先应该进行 irMap 操作，即进行首次遍历
     * 每个非内建函数都需要先被解析，
     * 然后处理 Phi（插入 Phi 指令）
     * 最后进行块的序列化（即在这里才将 block 加入）
     */
    private void parseFunctions()
    {
        irMap();
        MyList<Function> irFunctions = irModule.getFunctions();
        for (MyList.MyNode<Function> node : irFunctions)
        {
            Function irFunction = node.getVal();
            // 只有非内建函数才需要解析，经过调研，内建函数的意思就是啥都不需要处理的函数
            // 只需要提供一个名字即可（当然还有传参信息）
            if (!irFunction.isBuiltin())
            {
                parseFunction(irFunction);
                parsePhis(irFunction);
                fMap.get(irFunction).blockSerial(bMap.get(irFunction.getBasicBlocks().getHead().getVal()), phiCopysLists);
            }
        }
    }

    /**
     * 即解析每一个块（这么看，上面的东西似乎要移到这里）
     * @param irFunction 函数
     */
    private void parseFunction(Function irFunction)
    {
        MyList<BasicBlock> irBlocks = irFunction.getBasicBlocks();
        for (MyList.MyNode<BasicBlock> blockNode : irBlocks)
        {
            BasicBlock irBlock = blockNode.getVal();
            parseBlock(irBlock, irFunction);
        }
    }

    /**
     * 就是按顺序解析每个指令
     * @param irBlock 当前基本块
     * @param irFunction 当前函数
     */
    private void parseBlock(BasicBlock irBlock, Function irFunction)
    {
        for (MyList.MyNode<Instruction> node : irBlock.getInstructions())
        {
            Instruction instr = node.getVal();
            parseInstruction(instr, irBlock, irFunction);
        }
    }

    /**
     * 根据 ir 指令的类型，来确定解析的方法，到此为止，内化还是很有必要的
     * 但是对于有些指令，不是立刻解析的，因此可能会给内化造成一定的难度
     * @param instr ir 指令
     * @param irBlock ir 基本块
     * @param irFunction ir 函数
     */
    private void parseInstruction(Instruction instr, BasicBlock irBlock, Function irFunction)
    {
        if (instr instanceof Sdiv)
        {
            parseSdiv((Sdiv) instr, irBlock, irFunction);
        }
        else if (instr instanceof Srem)
        {
            parseSrem((Srem) instr, irBlock, irFunction);
        }
        else if (instr instanceof Mul)
        {
            parseMul((Mul) instr, irBlock, irFunction);
        }
        else if (instr instanceof Add)
        {
            parseAdd((Add) instr, irBlock, irFunction);
        }
        else if (instr instanceof Sub)
        {
            parseSub((Sub) instr, irBlock, irFunction);
        }
        if (instr instanceof Br)
        {
            parseBr((Br) instr, irBlock, irFunction);
        }
        else if (instr instanceof Call)
        {
            parseCall((Call) instr, irBlock, irFunction);
        }
        else if (instr instanceof Ret)
        {
            parseRet((Ret) instr, irBlock, irFunction);
        }
        else if (instr instanceof Alloca)
        {
            parseAlloca((Alloca) instr, irBlock, irFunction);
        }
        else if (instr instanceof GetElementPtr)
        {
            parseGEP((GetElementPtr) instr, irBlock, irFunction);
        }
        else if (instr instanceof Load)
        {
            parseLoad((Load) instr, irBlock, irFunction);
        }
        else if (instr instanceof Store)
        {
            parseStore((Store) instr, irBlock, irFunction);
        }
        else if (instr instanceof Zext)
        {
            parseZext((Zext) instr, irBlock, irFunction);
        }
        else
        {
            if (instr instanceof Phi || instr instanceof Icmp)
            {
                return;
            }
            assert false : "I haven't implemented the " + instr.getClass().toString();
        }
    }

    private void parseSdiv(Sdiv instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);

        // 无法常数优化
        ObjOperand src2 = parseOperand(instr.getOp2(), false, irFunction, irBlock);

        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        ObjBinary objDiv = new ObjBinary(DIV, dst, src1, src2);
        objBlock.addInstr(objDiv);
    }

    /**
     * 只有Mod (-)2^n 才会到后端处理，其余情况在前端处理
     *
     * @param instr      取模指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseSrem(Srem instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
        ObjOperand src2 = parseOperand(instr.getOp2(), false, irFunction, irBlock);
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        ObjBinary objMod = new ObjBinary(MOD, dst, src1, src2);
        objBlock.addInstr(objMod);
    }

    /**
     * mul 是无法使用立即数的，此外，对于幂次情况也应该有优化
     * 但是这里只做了整幂次，其实还可以更好
     * y = x * C    (2^{n - 1} <= C < 2^n)
     * =>
     * y = (x << (n - 1)) + x * (C - 2^{n - 1})     (add)
     * y = (x << n) - x * (2^n - C)                 (rsb)
     * @param instr      乘法指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseMul(Mul instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1, src2;
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
        src2 = parseOperand(instr.getOp2(), false, irFunction, irBlock);

        ObjBinary objMul = new ObjBinary(MUL, dst, src1, src2);
        objBlock.addInstr(objMul);
    }

    /**
     * 到加减法这里基本上就是利用第二个源操作数可以是立即数这一个特征了
     * 其实一两条指令并不显，只能说是寓教于乐了
     * b = 3 + a
     * add b a, 3
     * addi imm
     * @param instr      加法指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseAdd(Add instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        boolean isOp1Const = instr.getOp1() instanceof ConstInt;
        boolean isOp2Const = instr.getOp2() instanceof ConstInt;

        ObjOperand src1, src2;
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        if (isOp1Const && !isOp2Const)
        {
            src1 = parseOperand(instr.getOp2(), false, irFunction, irBlock);
            src2 = parseOperand(instr.getOp1(), true, irFunction, irBlock);
            ObjBinary objAdd = new ObjBinary(ADD, dst, src1, src2);
            objBlock.addInstr(objAdd);
        }
        else
        {
            src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
            src2 = parseOperand(instr.getOp2(), true, irFunction, irBlock);
            ObjBinary objAdd = new ObjBinary(ADD, dst, src1, src2);
            objBlock.addInstr(objAdd);
        }
    }

    /**
     * 因为没有 rsb 指令，似乎一点化简都做不了
     * @param instr 指令
     * @param irBlock 当前块
     * @param irFunction 当前函数
     */
    private void parseSub(Sub instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1, src2;
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        // 如果左边是常量并且ir是减法，那改成rsb，两个操作数交换位置，这是因为常量可以在 obj 的第二个 src
        // 交换两个操作数后不需要考虑编码问题，因为减法无法逆转
        src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
        src2 = parseOperand(instr.getOp2(), true, irFunction, irBlock);
        ObjBinary objAdd = new ObjBinary(SUB, dst, src1, src2);
        objBlock.addInstr(objAdd);
    }

    /**
     * 本质依然是一个 binary 指令，所以依然有神奇的编码交换
     * 有一个递归，因为会同时出现多个 cmp 指令，所以对于多余一个的情况，需要将自己构造
     *
     * @param instr      比较指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     * @return 条件
     */
    private ObjCondType parseIcmp(Icmp instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1, src2;
        Value irSrc1, irSrc2;
        ObjCondType cond = genCond(instr.getCondition());

        if (instr.getOp1() instanceof ConstInt && !(instr.getOp2() instanceof ConstInt))
        {
            irSrc1 = instr.getOp2();
            irSrc2 = instr.getOp1();
            cond = getEqualOppCond(cond);
        }
        else
        {
            irSrc1 = instr.getOp1();
            irSrc2 = instr.getOp2();
        }

        // 正式加入比较指令
        src1 = parseOperand(irSrc1, false, irFunction, irBlock);
        src2 = parseOperand(irSrc2, true, irFunction, irBlock);
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        ObjCompare objCompare = new ObjCompare(genCond(instr.getCondition()), dst, src1, src2);
        objBlock.addInstr(objCompare);

        return cond;
    }

    // TODO 1 == 2 == 3 测试一下
    /**
     * 只需要将这条指令与 Icmp 的 dst 对应起来即可
     * 这里还要和基本块的布局一起考虑 TODO
     * @param instr 当前指令
     * @param irBlock 当前块
     * @param irFunction 当前函数
     */
    private void parseZext(Zext instr, BasicBlock irBlock, Function irFunction)
    {
        parseIcmp((Icmp) instr.getSrc(), irBlock, irFunction);
        operandMap.put(instr, operandMap.get(instr.getSrc()));
    }

    private void parseBr(Br instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        // 对应有条件跳转
        if (instr.hasCondition())
        {
            Value irCondition = instr.getOps().get(0);
            BasicBlock irTrueBlock = (BasicBlock) instr.getOps().get(1);
            BasicBlock irFalseBlock = (BasicBlock) instr.getOps().get(2);

            // 如果条件是一个字面值，说明可以无条件跳转了
            if (irCondition instanceof ConstInt)
            {
                int condImm = ((ConstInt) irCondition).getValue();
                if (condImm > 0)
                {
                    ObjBranch objBranch = new ObjBranch(bMap.get(irTrueBlock));
                    objBlock.addInstr(objBranch);
                    objBlock.setTrueSucc(bMap.get(irTrueBlock));
                }
                else
                {
                    ObjBranch objBranch = new ObjBranch(bMap.get(irFalseBlock));
                    objBlock.addInstr(objBranch);
                    objBlock.setTrueSucc(bMap.get(irFalseBlock));
                }
            }
            // 如果条件是 icmp
            else if (irCondition instanceof Icmp)
            {
                Icmp condition = (Icmp) irCondition;
                ObjBlock objTrueBlock = bMap.get((BasicBlock) instr.getOps().get(1));
                ObjBlock objFalseBlock = bMap.get((BasicBlock) instr.getOps().get(2));
                ObjOperand src1, src2;
                ObjCondType cond = genCond(condition.getCondition());
                boolean needSwap = condition.getOp1() instanceof ConstInt && !(condition.getOp2() instanceof ConstInt);
                if (needSwap)
                {
                    cond = getOppCond(cond);
                    objTrueBlock = bMap.get((BasicBlock) instr.getOps().get(2));
                    objFalseBlock = bMap.get((BasicBlock) instr.getOps().get(1));
                    src1 = parseOperand(condition.getOp2(), false, irFunction, irBlock);
                    src2 = parseOperand(condition.getOp1(), true, irFunction, irBlock);
                }
                else
                {
                    src1 = parseOperand(condition.getOp1(), false, irFunction, irBlock);
                    src2 = parseOperand(condition.getOp2(), true, irFunction, irBlock);
                }

                // set true block to branch target
                ObjBranch objBranch = new ObjBranch(cond, src1, src2, objTrueBlock);
                objBlock.addInstr(objBranch);
                // parseBr 的一个重要功能，登记后继
                objBlock.setTrueSucc(objTrueBlock);
                objBlock.setFalseSucc(objFalseBlock);
            }
            else
            {
                assert false;
            }
        }
        // 对应无条件跳转
        else
        {
            ObjBlock objTargetBlock = bMap.get((BasicBlock) instr.getOps().get(0));
            ObjBranch objJump = new ObjBranch(objTargetBlock);
            objBlock.addInstr(objJump);
            objBlock.setTrueSucc(objTargetBlock);
        }
    }

    private void parseCall(Call instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        ObjFunction callFunction = fMap.get(instr.getFunction());
        ObjCall objCall = new ObjCall(callFunction);
        // 获取调用函数的参数数量,这里进行的是传参操作
        int argc = instr.getArgs().size();
        for (int i = 0; i < argc; i++)
        {
            Value irArg = instr.getArgs().get(i);
            ObjOperand objSrc;
            if (i < 4)
            {
                objSrc = parseOperand(irArg, true, irFunction, irBlock);
                ObjMove objMove = new ObjMove(new ObjPhyReg("a" + i), objSrc);
                objBlock.addInstr(objMove);
                // 防止寄存器分配消除掉这些move
                objCall.addUseReg(null, objMove.getDst());
            }
            else
            {
                // 和上面的区别在于，这里不允许立即数的出现，必须是寄存器
                objSrc = parseOperand(irArg, false, irFunction, irBlock);

                int offset = -(argc - i) * 4;
                ObjStore objStore = new ObjStore(objSrc, new ObjPhyReg("sp"), new ObjImm(offset));
                objBlock.addInstr(objStore);
            }
        }

        // 这里进行的是栈的生长操作
        if (argc > 4)
        {
            ObjOperand objOffset = parseConstIntOperand(4 * (argc - 4), true, irFunction, irBlock);
            ObjBinary objSub = new ObjBinary(SUB, new ObjPhyReg("sp"), new ObjPhyReg("sp"), objOffset);
            objBlock.addInstr(objSub);
        }
        // 到这里才正式把 jal 指令加入
        objBlock.addInstr(objCall);
        // 这里紧接着就是栈的恢复操作
        if (argc > 4)
        {
            ObjOperand objOffset = parseConstIntOperand(4 * (argc - 4), true, irFunction, irBlock);
            ObjBinary objAdd = new ObjBinary(ObjBinType.ADD, new ObjPhyReg("sp"), new ObjPhyReg("sp"), objOffset);
            objBlock.addInstr(objAdd);
        }

        // 因为寄存器分配是以函数为单位的，所以相当于 call 指令只需要考虑在调用者函数中的影响
        // 那么 call 对应的 bl 指令会修改 lr 和 r0 (如果有返回值的话)
        // 此外，r0 - r3 是调用者保存的寄存器，这会导致可能需要额外的操作 mov ，所以这边考虑全部弄成被调用者保存
        for (int i = 0; i < 4; i++)
        {
            objCall.addDefReg(null, new ObjPhyReg(i));
        }
        // 保存 ra
        objCall.addDefReg(null, new ObjPhyReg("ra"));

        // 这里是处理返回值
        DataType returnType = ((instr.getFunction())).getReturnType();
        if (!(returnType instanceof VoidType))
        {
            objCall.addDefReg(null, new ObjPhyReg("v0"));
            ObjMove objMove = new ObjMove(parseOperand(instr, false, irFunction, irBlock), new ObjPhyReg("v0"));
            objBlock.addInstr(objMove);
        }
    }

    private void parseRet(Ret instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        ObjFunction objFunction = fMap.get(irFunction);

        Value irRetValue = instr.getRetValue();
        // 如果有返回值，就把返回值移入 v0
        if (irRetValue != null)
        {
            ObjOperand objRet = parseOperand(irRetValue, true, irFunction, irBlock);
            // 如果返回值是浮点数，那么要从 s0 返回
            ObjMove objMove = new ObjMove(new ObjPhyReg("v0"), objRet);
            objBlock.addInstr(objMove);
        }
        // 然后进行弹栈和返回操作
        ObjRet objRet = new ObjRet(objFunction);
        // 这里是为了窥孔优化的正确性，或许放到 readReg 里判断也行
        objRet.addUseReg(null, new ObjPhyReg("v0"));
        objBlock.addInstr(objRet);
    }

    private void parseAlloca(Alloca instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        ObjFunction objFunction = fMap.get(irFunction);

        // 获得指针指向的类型
        ValueType pointeeType = ((PointerType) instr.getValueType()).getPointeeType();

        // 这是 alloc 前在栈上已分配出的空间
        ObjOperand offset = parseConstIntOperand(objFunction.getAllocaSize(), true, irFunction, irBlock);
        objFunction.addAllocaSize(pointeeType.getSize());

        // 这里进行的是栈的恢复操作，是因为栈会在 obj 函数一开始就生长出所有 alloc 的空间
        // 这里只需要将 alloc 的空间像一个 heap 一样使用就好了
        ObjOperand dst = parseOperand(instr, true, irFunction, irBlock);
        ObjBinary objAdd = new ObjBinary(ObjBinType.ADD, dst, new ObjPhyReg("sp"), offset);
        objBlock.addInstr(objAdd);
    }

    /**
     * 用于解析然后生成指向特定元素的一个指针
     * 其实本质似乎是可以有多重偏移计算，但是这里被弱化成了 1 重
     */
    //
    private void parseGEP(GetElementPtr instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        // 获得数组的基地址
        ObjOperand base = parseOperand(instr.getBase(), false, irFunction, irBlock);
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        // 说明此时是一个指向 int 的一维指针
        if (instr.getNumOps() == 2)
        {
            ValueType baseType = instr.getBaseType();
            Value irOffset = instr.getOffset().get(0);
            // 对于常数，是可以进行优化，直接用一个 add 算出来
            if (irOffset instanceof ConstInt)
            {
                int totalOffset = baseType.getSize() * ((ConstInt) irOffset).getValue();
                if (totalOffset != 0)
                {
                    ObjOperand objTotalOffset = parseConstIntOperand(totalOffset, true, irFunction, irBlock);
                    ObjBinary objAdd = new ObjBinary(ADD, dst, base, objTotalOffset);
                    objBlock.addInstr(objAdd);
                }
                else
                {
                    operandMap.put(instr, base);
                }
            }
            // 如果是变量，那么就需要用 mla
            else
            {
                ObjOperand objStep = parseConstIntOperand(baseType.getSize(), false, irFunction, irBlock);
                ObjOperand objOffset = parseOperand(irOffset, false, irFunction, irBlock);
                ObjOperand totalOffset = genTmpReg(irFunction);
                ObjBinary objTotalOffset = new ObjBinary(MUL, totalOffset, objStep, objOffset);
                objBlock.addInstr(objTotalOffset);
                ObjBinary objAdd = new ObjBinary(ADD, dst, totalOffset, base);
                objBlock.addInstr(objAdd);
            }
        }
        // 指向一个数组
        else if (instr.getNumOps() == 3)
        {
            // 获得指针指向的类型，应该是一个数组类型
            ArrayType baseType = (ArrayType) instr.getBaseType();
            // 获得数组元素类型
            ValueType elementType = baseType.getElementType();
            Value irOffset0 = instr.getOffset().get(0);
            Value irOffset1 = instr.getOffset().get(1);

            ObjOperand tmp = genTmpReg(irFunction);
            if (irOffset0 instanceof ConstInt)
            {
                int totalOffset0 = baseType.getSize() * ((ConstInt) irOffset0).getValue();
                ObjOperand objTotalOffset0 = parseConstIntOperand(totalOffset0, true, irFunction, irBlock);
                ObjBinary objAdd = new ObjBinary(ADD, tmp, base, objTotalOffset0);
                objBlock.addInstr(objAdd);
            }
            else
            {
                ObjOperand objStep0 = parseConstIntOperand(baseType.getSize(), false, irFunction, irBlock);
                ObjOperand objOffset0 = parseOperand(irOffset0, false, irFunction, irBlock);
                ObjOperand totalOffset0 = genTmpReg(irFunction);
                ObjBinary objTotalOffset0 = new ObjBinary(MUL, totalOffset0, objStep0, objOffset0);
                objBlock.addInstr(objTotalOffset0);
                ObjBinary objAdd = new ObjBinary(ADD, dst, totalOffset0, base);
                objBlock.addInstr(objAdd);
            }

            if (irOffset1 instanceof ConstInt)
            {
                int totalOffset1 = elementType.getSize() * ((ConstInt) irOffset1).getValue();
                ObjOperand objTotalOffset1 = parseConstIntOperand(totalOffset1, true, irFunction, irBlock);
                ObjBinary objAdd = new ObjBinary(ADD, dst, tmp, objTotalOffset1);
                objBlock.addInstr(objAdd);
            }
            else
            {
                ObjOperand objStep1 = parseConstIntOperand(elementType.getSize(), false, irFunction, irBlock);
                ObjOperand objOffset1 = parseOperand(irOffset1, false, irFunction, irBlock);
                ObjOperand totalOffset1 = genTmpReg(irFunction);
                ObjBinary objTotalOffset1 = new ObjBinary(MUL, totalOffset1, objStep1, objOffset1);
                objBlock.addInstr(objTotalOffset1);
                ObjBinary objAdd = new ObjBinary(ADD, dst, totalOffset1, base);
                objBlock.addInstr(objAdd);
            }
        }
    }

    private void parseLoad(Load instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        Value irAddr = instr.getAddr();
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        //如果load的地址是二重指针，那么登记后就可以返回了,等价于这个 ir 指令没有对应任何 obj 指令
        ObjOperand addr = parseOperand(irAddr, false, irFunction, irBlock);
        ObjOperand offset = parseConstIntOperand(0, true, irFunction, irBlock);
        ObjLoad objLoad = new ObjLoad(dst, addr, offset);
        objBlock.addInstr(objLoad);
    }

    private void parseStore(Store instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        Value irAddr = instr.getAddr();
        ObjOperand src = parseOperand(instr.getValue(), false, irFunction, irBlock);

        ObjOperand addr = parseOperand(irAddr, false, irFunction, irBlock);
        ObjOperand offset = parseConstIntOperand(0, true, irFunction, irBlock);
        ObjStore objStore = new ObjStore(src, addr, offset);
        objBlock.addInstr(objStore);
    }

    private void handleNoCyclePath(Stack<ObjOperand> path, ObjOperand begin, ArrayList<ObjInstr> copys, HashMap<ObjOperand, ObjOperand> graph)
    {
        ObjOperand phiSrc = begin;
        while (!path.isEmpty())
        {
            ObjOperand phiTarget = path.pop();
            ObjInstr objMove = new ObjMove(phiTarget, phiSrc);
            // 这里需要采用头插法，因为如果尾插法就会产生 src 先于 target 更新的情况，与并行不符
            // 如果是 a <- b, b <- c 。那么就应当排列成 b <- c, a <-b
            // 如果是第一种排序，那么就会导致最终 a 是 c 的值，而其实 a 应该是 b 原本的值，也就是并行的意思
            copys.add(0, objMove);
            phiSrc = phiTarget;
            graph.remove(phiTarget);
        }
    }

    private void handleCyclePath(ObjFunction objFunction, Stack<ObjOperand> path, ObjOperand begin, ArrayList<ObjInstr> copys, HashMap<ObjOperand, ObjOperand> graph)
    {
        ObjVirReg tmp = new ObjVirReg();
        objFunction.addUsedVirReg(tmp);

        ObjMove objMove = new ObjMove(null, null);
        objMove.setDst(tmp);
        while (path.contains(begin))
        {
            ObjOperand r = path.pop();
            objMove.setSrc(r);
            copys.add(objMove);
            objMove = new ObjMove(null, null);
            objMove.setDst(r);
            graph.remove(r);
        }
        objMove.setSrc(tmp);

    }

    /**
     * 这个函数会根据当前块和其某个前驱块，生成要插入这个前驱块的 mov 指令（通过 phi 翻译获得，我们称为 copy）
     * 因为 phi 具有并行的特性，所以在排列顺序的时候，我们需要注意
     *
     * @param phis       当前块的 phi 指令集合
     * @param irFunction 当前 ir 函数
     * @param irBlock    当前区块
     * @return 一堆待插入的 copy 指令
     */
    private ArrayList<ObjInstr> genPhiCopys(ArrayList<Phi> phis, BasicBlock irPreBlock, Function irFunction, BasicBlock irBlock)
    {
        ObjFunction objFunction = fMap.get(irFunction);
        // 通过构建一个图来检验是否成环
        HashMap<ObjOperand, ObjOperand> graph = new HashMap<>();

        ArrayList<ObjInstr> copys = new ArrayList<>();

        // 构建一个图
        for (Phi phi : phis)
        {
            ObjOperand phiTarget = parseOperand(phi, false, irFunction, irBlock);
            // phiSrc phi 目的寄存器可能的一个值
            Value inputValue = phi.getInputValForBlock(irPreBlock);
            // 这里进行了一个复杂的讨论，这是因为一般的 parseOperand 在分析立即数的时候，可能会引入
            // 其他指令，而这些指令会跟在当前块上，而不是随意移动的（我们需要他们随意移动）
            ObjOperand phiSrc;
            if (inputValue instanceof ConstInt)
            {
                phiSrc = new ObjImm(((ConstInt) inputValue).getValue());
            }
            else
            {
                phiSrc = parseOperand(inputValue, true, irFunction, irBlock);
            }
            graph.put(phiTarget, phiSrc);
        }


        while (!graph.isEmpty())
        {
            Stack<ObjOperand> path = new Stack<>();
            ObjOperand cur;
            // 对这个图进行 DFS 遍历来获得成环信息, DFS 发生了不止一次，而是每次检测到一个环就会处理一次
            for (cur = graph.entrySet().iterator().next().getKey(); graph.containsKey(cur); cur = graph.get(cur))
            {
                // 这就说明成环了，也就是会有 swap 问题
                if (path.contains(cur))
                {
                    break;
                }
                else
                {
                    path.push(cur);
                }
            }

            //如果以该点出发没有环路
            if (!graph.containsKey(cur))
            {
                handleNoCyclePath(path, cur, copys, graph);
            }
            else
            {
                handleCyclePath(objFunction, path, cur, copys, graph);
                handleNoCyclePath(path, cur, copys, graph);
            }
        }

        return copys;
    }

    /**
     * 因为对于 phi 的处理要涉及多个块，所以没有办法在一个块内处理
     */
    private void parsePhis(Function irFunction)
    {
        // 遍历函数中的每个块
        for (MyList.MyNode<BasicBlock> blockNode : irFunction.getBasicBlocks())
        {
            BasicBlock irBlock = blockNode.getVal();
            ObjBlock objBlock = bMap.get(irBlock);

            HashSet<BasicBlock> predBlocks = irBlock.getPredecessors();
            int predNum = predBlocks.size();
            // 如果只有一个前驱,那么就不用处理了
            // 不用处理的原因是，如果只有一个前驱块，那么这个块里应该就没有 phi 指令了吧
            if (predNum <= 1)
            {
                continue;
            }
            // 收集基本块中的 phi 指令
            ArrayList<Phi> phis = new ArrayList<>();
            for (MyList.MyNode<Instruction> instrNode : irBlock.getInstructions())
            {
                Instruction instr = instrNode.getVal();
                if (instr instanceof Phi)
                {
                    phis.add((Phi) instr);
                }
                else
                {
                    break;
                }
            }

            for (BasicBlock irPreBlock : predBlocks)
            {
                // 建立基本的前驱后继查找关系
                MyPair<ObjBlock, ObjBlock> pair = new MyPair<>(bMap.get(irPreBlock), objBlock);
                phiCopysLists.put(pair, genPhiCopys(phis, irPreBlock, irFunction, irBlock));
            }
        }
    }

    /**
     * 生成一个临时中转的寄存器，来翻译诸如 1 + 2 * 3 这种无法一个 obj 指令完成的操作
     *
     * @return 一个虚拟寄存器
     */
    private ObjOperand genTmpReg(Function irFunction)
    {
        ObjFunction objFunction = fMap.get(irFunction);
        ObjVirReg tmpReg = new ObjVirReg();
        objFunction.addUsedVirReg(tmpReg);
        return tmpReg;
    }

    /**
     * 这应该就是最有用的函数，用于将 ir 中发挥 Operand 作用的 Value 转成一个 ObjOperand
     * 为啥要这么长，是因为对于一个操作数 ，她可能是一个字面值，也可能是指令的执行结果，也可能是函数的参数，还可能是全局变量
     * 进一步的理解，操作数是 obj 数据结构的最底层，它应该是所有逻辑的起点，几乎所有的 Value 都可以成为操作数
     *
     * @return 一个操作数
     */
    private ObjOperand parseOperand(Value irValue, boolean canImm, Function irFunction, BasicBlock irBlock)
    {
        // 如果已经被解析了，那么就不需要再解析了
        if (operandMap.containsKey(irValue))
        {
            return operandMap.get(irValue);
        }
        if (irValue instanceof Argument && irFunction.getArguments().contains(irValue))
        {
            return parseArgOperand((Argument) irValue, irFunction);
        }
        else if (irValue instanceof GlobalVariable)
        {
            return parseGlobalOperand((GlobalVariable) irValue, irFunction, irBlock);
        }
        // 如果是整型常数
        else if (irValue instanceof ConstInt)
        {
            return parseConstIntOperand(((ConstInt) irValue).getValue(), canImm, irFunction, irBlock);
        }
        // 如果是指令，那么需要生成一个目的寄存器
        else
        {
            return genDstOperand(irValue, irFunction);
        }
    }

    /**
     * 这是最神奇的一个方法，有两个讨论，
     * 如果允许返回立即数，那么就返回立即数，因为对于 mips 来说，有足够多的伪指令可以使得直接处理 32 位数
     * 如果不允许返回立即数，那么就返回一个 li 的结果
     * @param imm        立即数
     * @param irFunction 所在的函数
     * @param irBlock    所在的block
     * @param canImm     表示允不允许是一个立即数
     * @return 操作数
     */
    private ObjOperand parseConstIntOperand(int imm, boolean canImm, Function irFunction, BasicBlock irBlock)
    {
        ObjImm objImm = new ObjImm(imm);
        // 如果可以直接编码而且允许返回立即数, 那么就直接返回就可以了
        if (canImm)
        {
            return objImm;
        }
        else
        {
            ObjFunction objFunction = fMap.get(irFunction);
            ObjBlock objBlock = bMap.get(irBlock);
            ObjVirReg dstReg = new ObjVirReg();
            objFunction.addUsedVirReg(dstReg);

            ObjMove objMove = new ObjMove(dstReg, objImm);
            objBlock.addInstr(objMove);
            return dstReg;
        }
    }

    /**
     * 这个方法的特殊之处在于会将指令插入到函数的头部
     *
     * @param irArgument 参数
     * @param irFunction 函数
     * @return 拥有函数参数的寄存器
     */
    private ObjOperand parseArgOperand(Argument irArgument, Function irFunction)
    {
        ObjFunction objFunction = fMap.get(irFunction);
        // 是第几个参数
        int rank = irArgument.getRank();
        ObjBlock firstBlock = bMap.get(irFunction.getBasicBlocks().getHead().getVal());
        // 如果是浮点数，那么就需要用浮点数传参，因为浮点传参是 s0 - s15，所以我不打算考虑到栈的情况了
        ObjVirReg dstVirReg = new ObjVirReg();
        operandMap.put(irArgument, dstVirReg);
        objFunction.addUsedVirReg(dstVirReg);

        if (rank < 4)
        {
            // 创建一个移位指令
            ObjMove objMove = new ObjMove(dstVirReg, new ObjPhyReg("a" + rank));
            firstBlock.addInstrHead(objMove);
        }
        // 这时需要从栈上加载
        // TODO 测试一下
        else
        {
            // 创建一个移位指令
            int stackPos = rank - 4;
            ObjOperand objOffsetDst = genTmpReg(irFunction);
            // 这里之所以看似冗余的，但是是因为 fixStack 的时候修正 offset，如果采用立即数形式，可能导致无法编码 TODO
            ObjMove objMove = new ObjMove(objOffsetDst, new ObjImm(stackPos * 4));
            objFunction.addArgOffsetMove(objMove);
            // 创建一个加载指令
            ObjLoad objLoad = new ObjLoad(dstVirReg, new ObjPhyReg("sp"), objOffsetDst);
            firstBlock.addInstrHead(objLoad);
            firstBlock.addInstrHead(objMove);
        }
        return dstVirReg;
    }

    /**
     * 全局变量使用前需要加载到一个虚拟寄存器中（直接使用的方法似乎在分段 .data 的时候不成立）
     * 但是我们并没有记录下来，这是因为本质上局部是基本块，所以有的时候别的基本块用的东西，我们即使有，也不能提供
     * TODO 所以这里没准可以记下来
     * @param irGlobal 全局变量
     * @return 操作数
     */
    private ObjOperand parseGlobalOperand(GlobalVariable irGlobal, Function irFunction, BasicBlock irBlock)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        ObjOperand dst = genTmpReg(irFunction);
        ObjMove objMove = new ObjMove(dst, new ObjLabel(irGlobal.getName().substring(1)));
        objBlock.addInstr(objMove);
        return dst;
    }

    /**
     * 用于生成目的寄存器,其实同样可以看做是解析指令的结果
     * 最明显的是，这个指令的类型决定了目的寄存器的类型
     * @param irValue    应该是指令
     * @param irFunction 所在的函数
     * @return 目的寄存器
     */
    private ObjOperand genDstOperand(Value irValue, Function irFunction)
    {
        assert irValue instanceof Instruction : "Wrong Operand.";
        ObjFunction objFunction = fMap.get(irFunction);
        ObjVirReg dstReg = new ObjVirReg();
        objFunction.addUsedVirReg(dstReg);
        operandMap.put(irValue, dstReg);
        return dstReg;
    }
}


