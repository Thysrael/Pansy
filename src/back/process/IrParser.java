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
import util.MyMath;
import util.MyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import static back.instruction.ObjCondType.*;
import static back.operand.ObjPhyReg.*;
import static util.MyMath.canEncodeImm;
import static util.MyMath.ctz;

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
     * 用于记录已经加载过的全局变量，必须局限于基本块内
     */
    private final HashMap<MyPair<ObjBlock, GlobalVariable>, ObjOperand> globalVariableMap = new HashMap<>();
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

    /**
     * 在高级语言中，除法是向 0 取整的，也就是说 3 / 4 = 0， -3 / 4 = 0
     * 但是如果用移位操作来处理的话，除法是向下取整的，即 3 / 4 = 0, -3 / 4 = -1
     * 所以为了适应高级语言，我们需要产生新的被除数，有 newDividend = oldDividend + divisor - 1
     *
     * @param oldDividend 旧的被除数
     * @param abs         除数的绝对值
     * @param irBlock     当前块
     * @param irFunction  当前函数
     * @return 新的被除数
     */
    private ObjOperand genCeilDividend(ObjOperand oldDividend, int abs, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        // l = log2(abs)
        int l = ctz(abs);

        ObjOperand tmp1 = genTmpReg(irFunction);
        ObjShift objShift1 = ObjShift.getSra(tmp1, oldDividend, 31);
        objBlock.addInstr(objShift1);
        // 然后将那一堆 1 或者 0 逻辑右移 32 - l 位
        // 这样就会在 [l-1 : 0] 位获得一堆 1 或者 0，其实就是 2^l - 1 = abs - 1（只有在被除数是负数的时候有效）
        // 最后将这 abs - 1 加到被除数上，完成了针对负数的向上取整操作
        ObjOperand tmp2 = genTmpReg(irFunction);
        ObjShift objShift2 = ObjShift.getSrl(tmp1, tmp1, 32 - l);
        objBlock.addInstr(objShift2);
        ObjBinary objAddu = ObjBinary.getAddu(tmp2, oldDividend, tmp1);
        objBlock.addInstr(objAddu);
        return tmp2;
    }


    private void constDiv(ObjOperand dst, ObjOperand dividend, int divisorImm, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        // 这里之所以取 abs，是在之后如果是负数，那么会有一个取相反数的操作
        int abs = divisorImm > 0 ? divisorImm : -divisorImm;
        // 如果除数是 -1，那么就是取相反数
        if (divisorImm == -1)
        {
            // rsb 是第二个数减去第一个数
            ObjBinary objRsb = ObjBinary.getSubu(dst, ObjPhyReg.ZERO, dividend);
            objBlock.addInstr(objRsb);
            return;
        }
        else if (divisorImm == 1)
        {
            ObjMove objMove = new ObjMove(dst, dividend);
            objBlock.addInstr(objMove);
        }
        // 如果是 2 的幂次
        else if ((abs & (abs - 1)) == 0)
        {
            int l = ctz(abs);
            // 产生新的被除数
            ObjOperand newDividend = genCeilDividend(dividend, abs, irBlock, irFunction);
            // 将被除数右移
            ObjShift objShift = ObjShift.getSra(dst, newDividend, l);
            objBlock.addInstr(objShift);
        }
        // dst = dividend / abs => dst = (dividend * n) >> shift
        else
        {
            // nc = 2^31 - 2^31 % abs - 1
            long nc = ((long) 1 << 31) - (((long) 1 << 31) % abs) - 1;
            long p = 32;
            // 2^p > (2^31 - 2^31 % abs - 1) * (abs - 2^p % abs)
            while (((long) 1 << p) <= nc * (abs - ((long) 1 << p) % abs))
            {
                p++;
            }
            // m = (2^p + abs - 2^p % abs) / abs
            // m 是 2^p / abs 的向上取整
            long m = ((((long) 1 << p) + (long) abs - ((long) 1 << p) % abs) / (long) abs);
            // >>> 是无符号右移的意思，所以 n = m[31:0]
            int n = (int) ((m << 32) >>> 32);
            int shift = (int) (p - 32);

            // tmp0 = n
            ObjOperand tmp0 = genTmpReg(irFunction);
            ObjMove objInstr0 = new ObjMove(tmp0, new ObjImm(n));
            objBlock.addInstr(objInstr0);

            ObjOperand tmp1 = genTmpReg(irFunction);
            // tmp1 = dividend + (dividend * n)[63:32]
            if (m >= 0x80000000L)
            {
                ObjCoMove objMtlo = ObjCoMove.getMthi(dividend);
                objBlock.addInstr(objMtlo);
                // 这里的 madd 要求是有符号的，具体为啥我也不知道
                ObjBinary smmadd = ObjBinary.getSmmadd(tmp1, dividend, tmp0);
                objBlock.addInstr(smmadd);
            }
            // tmp1 = (dividend * n)[63:32]
            else
            {
                // 但是这里的 smmul 则是有符号的
                ObjBinary objInstr1 = ObjBinary.getSmmul(tmp1, dividend, tmp0);
                objBlock.addInstr(objInstr1);
            }

            ObjOperand tmp2 = genTmpReg(irFunction);
            // tmp2 = tmp1 >> shift
            ObjShift objInstr3 = ObjShift.getSra(tmp2, tmp1, shift);
            objBlock.addInstr(objInstr3);
            // dst = tmp2 + dividend >> 31
            ObjShift srl = ObjShift.getSrl(AT, dividend, 31);
            objBlock.addInstr(srl);
            ObjBinary objInstr4 = ObjBinary.getAddu(dst, tmp2, AT);
            objBlock.addInstr(objInstr4);
        }

        // 这里依然是进行了一个取相反数的操作
        if (divisorImm < 0)
        {
            ObjBinary objRsb = ObjBinary.getSubu(dst, ZERO, dst);
            objBlock.addInstr(objRsb);
        }
        divMap.put(new MyPair<>(objBlock, new MyPair<>(dividend, new ObjImm(divisorImm))), dst);
    }

    private void parseSdiv(Sdiv instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment(instr.getOp1().getName() + " div " + instr.getOp2().getName()));

        ObjOperand src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
        // 如果除数是常数，就可以进行除常数优化了
        boolean isSrc2Const = instr.getOp2() instanceof ConstInt;

        if (isSrc2Const)
        {
            // 获得除数常量
            int imm = ((ConstInt) instr.getOp2()).getValue();
            // 如果除数是 1 ,将 ir 映射成被除数
            if (imm == 1)
            {
                operandMap.put(instr, src1);
            }
            else
            {
                MyPair<ObjOperand, ObjOperand> div = new MyPair<>(src1, new ObjImm(imm));
                MyPair<ObjBlock, MyPair<ObjOperand, ObjOperand>> divLookUp = new MyPair<>(objBlock, div);
                if (divMap.containsKey(divLookUp))
                {
                    operandMap.put(instr, divMap.get(divLookUp));
                }
                else
                {
                    ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
                    constDiv(dst, src1, imm, irBlock, irFunction);
                }
            }

        }
        // 无法常数优化
        else
        {
            ObjOperand src2 = parseOperand(instr.getOp2(), false, irFunction, irBlock);

            ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
            ObjBinary objDiv = ObjBinary.getDiv(dst, src1, src2);
            objBlock.addInstr(objDiv);
        }
    }

    /**
     * 只有Mod (-)2^l 才会到后端处理，其余情况在前端处理
     *
     * @param instr      取模指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseSrem(Srem instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        ObjOperand src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
        int imm = ((ConstInt) instr.getOp2()).getValue();
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        int abs = imm > 0 ? imm : -imm;
        assert ((abs & (abs - 1)) == 0);
        int l = ctz(abs);

        MyPair<ObjOperand, ObjImm> div = new MyPair<>(src1, new ObjImm(imm));
        MyPair<ObjBlock, MyPair<ObjOperand, ObjImm>> divLookUp = new MyPair<>(objBlock, div);
        // 取余可以理解进行完乘法后在进行一个减法
        if (divMap.containsKey(divLookUp))
        {
            ObjOperand src2 = divMap.get(divLookUp);
            // src2 << l
            objBlock.addInstr(ObjShift.getSll(AT, src2, l));
            // dst = src1 - src2 << l
            if (imm > 0)
            {
                objBlock.addInstr(ObjBinary.getSubu(dst, src1, AT));
            }
            else
            {
                objBlock.addInstr(ObjBinary.getAddu(dst, src1, AT));
            }
        }
        // 没有先例，需要自己处理，我们的处理方法是先用 bic 指令获得 [31:l] 位的数，然后用原来的数减去这个数
        // 不用 and 的原因是 newDividend 的 [l-1 :0] 与之前的不同，所以没法用了，而且我们还需要 newDividend 的属性
        else
        {
            ObjOperand dividendHi = genTmpReg(irFunction);
            if (abs == 1)
            {
                ObjMove objMove = new ObjMove(dst, ZERO);
                objBlock.addInstr(objMove);
            }
            else
            {
                ObjOperand newDividend = genCeilDividend(src1, abs, irBlock, irFunction);
                // dividendHi = {{newDividend[31:l]}, {l{0}}}
                objBlock.addInstr(ObjShift.getSrl(AT, newDividend, l));
                objBlock.addInstr(ObjShift.getSll(dividendHi, AT, l));

                ObjBinary objSub = ObjBinary.getSubu(dst, src1, dividendHi);
                objBlock.addInstr(objSub);
            }
        }
    }

    private void mulTemplate(ObjOperand dst, Value irOp1, Value irOp2, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment(irOp1.getName() + " mul " + irOp2.getName()));

        boolean isOp1Const = irOp1 instanceof ConstInt;
        boolean isOp2Const = irOp2 instanceof ConstInt;

        ObjOperand src1, src2;
        // 如果有常数
        if (isOp1Const || isOp2Const)
        {
            int imm;
            if (isOp1Const)
            {
                src1 = parseOperand(irOp2, false, irFunction, irBlock);
                imm = ((ConstInt) irOp1).getValue();
            }
            else
            {
                src1 = parseOperand(irOp1, false, irFunction, irBlock);
                imm = ((ConstInt) irOp2).getValue();
            }

            ArrayList<MyPair<Boolean, Integer>> mulOptItems = MyMath.getMulOptItems(imm);
            // 如果是空的，那么就说明无法优化
            if (mulOptItems.isEmpty())
            {
                // 之所以要跟前面的 src1 分开，是因为如果是可以转化成位移指令，那么就会造成 src2 的冗余解析
                if (isOp1Const)
                {
                    src2 = parseOperand(irOp1, false, irFunction, irBlock);
                }
                else
                {
                    src2 = parseOperand(irOp2, false, irFunction, irBlock);
                }
            }
            else
            {
                if (mulOptItems.size() == 1)
                {
                    ObjShift objSll = ObjShift.getSll(dst, src1, mulOptItems.get(0).getSecond());
                    objBlock.addInstr(objSll);
                    if (!mulOptItems.get(0).getFirst())
                    {
                        ObjBinary objSubu = ObjBinary.getSubu(dst, ObjPhyReg.ZERO, dst);
                        objBlock.addInstr(objSubu);
                    }
                }
                else
                {
                    ObjOperand at = AT;

                    // 首先用一个 shift
                    ObjShift objSll = ObjShift.getSll(at, src1, mulOptItems.get(0).getSecond());
                    objBlock.addInstr(objSll);
                    // 检测要不要负数
                    if (!mulOptItems.get(0).getFirst())
                    {
                        ObjBinary objSubu = ObjBinary.getSubu(at, ObjPhyReg.ZERO, at);
                        objBlock.addInstr(objSubu);
                    }
                    // 开始中间，中间运算的结果存储在 at 中
                    for (int i = 1; i < mulOptItems.size() - 1; i++)
                    {
                        if (mulOptItems.get(i).getSecond() == 0)
                        {
                            objBlock.addInstr(mulOptItems.get(i).getFirst() ?
                                    ObjBinary.getAddu(at, at, src1) :
                                    ObjBinary.getSubu(at, at, src1));
                        }
                        else
                        {
                            ObjOperand tmp = genTmpReg(irFunction);
                            objBlock.addInstr(ObjShift.getSll(tmp, src1, mulOptItems.get(i).getSecond()));
                            objBlock.addInstr(mulOptItems.get(i).getFirst() ?
                                    ObjBinary.getAddu(at, at, tmp) :
                                    ObjBinary.getSubu(at, at, tmp));
                        }
                    }
                    // 开始结尾
                    MyPair<Boolean, Integer> last = mulOptItems.get(mulOptItems.size() - 1);
                    if (last.getSecond() == 0)
                    {
                        objBlock.addInstr(last.getFirst() ?
                                ObjBinary.getAddu(dst, at, src1) :
                                ObjBinary.getSubu(dst, at, src1));
                    }
                    else
                    {
                        ObjOperand tmp = genTmpReg(irFunction);
                        objBlock.addInstr(ObjShift.getSll(tmp, src1, last.getSecond()));
                        objBlock.addInstr(last.getFirst() ?
                                ObjBinary.getAddu(dst, at, tmp) :
                                ObjBinary.getSubu(dst, at, tmp));
                    }
                }
                return;
            }
        }
        else
        {
            src1 = parseOperand(irOp1, false, irFunction, irBlock);
            src2 = parseOperand(irOp2, false, irFunction, irBlock);
        }

        ObjBinary objMul = ObjBinary.getMul(dst, src1, src2);
        objBlock.addInstr(objMul);
    }

    /**
     * 采用了 ch 的优秀方法，将一个乘常数分解成了多一个 (+-shift) 的项
     * @param instr      乘法指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseMul(Mul instr, BasicBlock irBlock, Function irFunction)
    {
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        mulTemplate(dst, instr.getOp1(), instr.getOp2(), irBlock, irFunction);
    }

    /**
     * 可以利用的指令是 addu 和 addiu
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

        // 全是常数则直接计算
        if (isOp1Const && isOp2Const)
        {
            int op1Imm = ((ConstInt) instr.getOp1()).getValue();
            int op2Imm = ((ConstInt) instr.getOp2()).getValue();
            ObjMove objMove = new ObjMove(dst, new ObjImm(op1Imm + op2Imm));
            objBlock.addInstr(objMove);
        }
        // 只有 op1 是常数，则交换 op，检验常数是否可以编码的工作，就交给 parseConstInt 了
        else if (isOp1Const)
        {
            src1 = parseOperand(instr.getOp2(), false, irFunction, irBlock);
            src2 = parseOperand(instr.getOp1(), true, irFunction, irBlock);
            ObjBinary objAdd = ObjBinary.getAddu(dst, src1, src2);
            objBlock.addInstr(objAdd);
        }
        // 直接加，是不是常数就不管了
        else
        {
            src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
            src2 = parseOperand(instr.getOp2(), true, irFunction, irBlock);
            ObjBinary objAdd = ObjBinary.getAddu(dst, src1, src2);
            objBlock.addInstr(objAdd);
        }
    }

    /**
     * 只有 sub 这一条指令，在 MARS 中伪指令非常不优雅，故手动拓展
     * 可以考虑当减数是常量的情况，用 addiu 代替（MARS 无法完成这个）
     * @param instr 指令
     * @param irBlock 当前块
     * @param irFunction 当前函数
     */
    private void parseSub(Sub instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        boolean isOp1Const = instr.getOp1() instanceof ConstInt;
        boolean isOp2Const = instr.getOp2() instanceof ConstInt;

        ObjOperand src1, src2;
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        // 如果全是常量，就直接算出来
        if (isOp1Const && isOp2Const)
        {
            int op1Imm = ((ConstInt) instr.getOp1()).getValue();
            int op2Imm = ((ConstInt) instr.getOp2()).getValue();
            ObjMove objMove = new ObjMove(dst, new ObjImm(op1Imm - op2Imm));
            objBlock.addInstr(objMove);
        }
        // 如果减数是常量，那么可以用 addi 来代替
        else if (isOp2Const)
        {
            src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
            int op2Imm = ((ConstInt) instr.getOp2()).getValue();
            src2 = parseConstIntOperand(-op2Imm, true, irFunction, irBlock);
            ObjBinary objAdd = ObjBinary.getAddu(dst, src1, src2);
            objBlock.addInstr(objAdd);
        }
        else
        {
            src1 = parseOperand(instr.getOp1(), false, irFunction, irBlock);
            src2 = parseOperand(instr.getOp2(), true, irFunction, irBlock);
            ObjBinary objSub = ObjBinary.getSubu(dst, src1, src2);
            objBlock.addInstr(objSub);
        }
    }

    /**
     * 本质依然是一个 binary 指令，所以依然有神奇的编码交换
     * 对于 parseIcmp，当两个 OP 均为常数的时候，直接在 OperandMap 中添加
     * 否则就需要 set 指令了
     * 在 MIPS 中，set 类指令只有 slt, slti 两种，其他的指令都是拓展指令
     * 而且在 MARS 提供的伪指令模板中，对于这两种指令的优化并不好，所以我打算手写优化
     * @param instr      比较指令
     * @param irBlock    当前块
     * @param irFunction 当前函数
     */
    private void parseIcmp(Icmp instr, BasicBlock irBlock, Function irFunction)
    {
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);
        ObjCondType cond = genCond(instr.getCondition());
        // 如果均是常数，那么直接比较即可
        if (instr.getOp1() instanceof ConstInt && instr.getOp2() instanceof ConstInt)
        {
            int op1 = ((ConstInt) instr.getOp1()).getValue();
            int op2 = ((ConstInt) instr.getOp2()).getValue();
            dst = new ObjImm(cond.compare(op1, op2) ? 1 : 0);
            operandMap.put(instr, dst);
        }
        else
        {
            // 对不同情况分类讨论，对于前四种情况，是有具体方法对应的，后两种，可以通过交换操作数顺序套用原有模板
            switch (cond)
            {
                case EQ :
                {
                    eqTemplate(dst, instr.getOp1(), instr.getOp2(), irBlock, irFunction);
                    break;
                }
                case NE :
                {
                    neTemplate(dst, instr.getOp1(), instr.getOp2(), irBlock, irFunction);
                    break;
                }
                case LE :
                {
                    leTemplate(dst, instr.getOp1(), instr.getOp2(), irBlock, irFunction);
                    break;
                }
                case LT :
                {
                    ltTemplate(dst, instr.getOp1(), instr.getOp2(), irBlock, irFunction);
                    break;
                }
                case GE :
                {
                    leTemplate(dst, instr.getOp2(), instr.getOp1(), irBlock, irFunction);
                    break;
                }
                case GT:
                {
                    ltTemplate(dst, instr.getOp2(), instr.getOp1(), irBlock, irFunction);
                    break;
                }
            }
        }
    }

    /**
     * 这是其他 set template 的组件
     * 输入一个寄存器和一个 imm，最终会使得 at 寄存器中存入一个值，
     * 如果 src 和 imm 的值相等，那么 at 寄存器为 0，否则不为 0
     * 要求 src 必须是寄存器，不能是立即数
     * @param src 源寄存器
     * @param imm 比较立即数
     * @param irBlock 基本块
     * @param irFunction 函数
     */
    private void basicEqTemplate(ObjOperand src, int imm, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        if (canEncodeImm(-imm, true))
        {
            ObjBinary objAdd = ObjBinary.getAddu(AT, src, new ObjImm(-imm));
            objBlock.addInstr(objAdd);
        }
        else if (canEncodeImm(imm, false))
        {
            ObjBinary objXor = ObjBinary.getXor(AT, src, new ObjImm(imm));
            objBlock.addInstr(objXor);
        }
        else
        {
            // 这里就相当于用 li at, imm
            ObjOperand objAt = parseConstIntOperand(imm, true, irFunction, irBlock);
            // xor at, src, at
            ObjBinary objXor = ObjBinary.getXor(AT, src, objAt);
            objBlock.addInstr(objXor);
        }
    }

    /**
     * 基本思路是这样的，对于 a 和 b 的比较
     * 让 a 和 b xor 或者相减，达到 !(a == b) 的效果
     * 然后 !(a == b) < 1 （也就是 !(a == b) == 0）就可以完成比较
     * eq 具有某种意义上的交换性，所以可以更加方便的调整
     * @param dst 目标寄存器
     * @param op1 第一个操作数
     * @param op2 第二个操作数
     * @param irBlock 基本块
     * @param irFunction 函数
     */
    private void eqTemplate(ObjOperand dst, Value op1, Value op2, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment("eq " + op1.getName() + "\t" + op2.getName()));

        if (op1 instanceof ConstInt)
        {
            ObjOperand src = parseOperand(op2, false, irFunction, irBlock);
            basicEqTemplate(src, ((ConstInt) op1).getValue(), irBlock, irFunction);
            ObjBinary objSltu = ObjBinary.getSltu(dst, AT, new ObjImm(1));
            objBlock.addInstr(objSltu);
        }
        else if (op2 instanceof ConstInt)
        {
            ObjOperand src = parseOperand(op1, false, irFunction, irBlock);
            basicEqTemplate(src, ((ConstInt) op2).getValue(), irBlock, irFunction);
            ObjBinary objSltu = ObjBinary.getSltu(dst, AT, new ObjImm(1));
            objBlock.addInstr(objSltu);
        }
        else
        {
            ObjPhyReg tmpReg = AT;
            // 这两个东西一定不会是在 at 里，因为只有大的 imm 会在里面，而这里显然没有 imm 了
            ObjOperand ojbOp1 = parseOperand(op1, false, irFunction, irBlock);
            ObjOperand objOp2 = parseOperand(op2, false, irFunction, irBlock);
            // at 这个临时寄存器当得好多啊
            ObjBinary objXor = ObjBinary.getXor(tmpReg, ojbOp1, objOp2);
            objBlock.addInstr(objXor);
            ObjBinary objSltu = ObjBinary.getSltu(dst, tmpReg, new ObjImm(1));
            objBlock.addInstr(objSltu);
        }
    }

    /**
     * 思路与 eqTemplate 类似，首先拿到 !(a == b)
     * 然后利用 0 < !(a == b) 完成比较
     */
    private void neTemplate(ObjOperand dst, Value op1, Value op2, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment("ne " + op1.getName() + "\t" + op2.getName()));

        if (op1 instanceof ConstInt)
        {
            ObjOperand src = parseOperand(op2, false, irFunction, irBlock);
            basicEqTemplate(src, ((ConstInt) op1).getValue(), irBlock, irFunction);
            ObjBinary objSltu = ObjBinary.getSltu(dst, ZERO, AT);
            objBlock.addInstr(objSltu);
        }
        else if (op2 instanceof ConstInt)
        {
            ObjOperand src = parseOperand(op1, false, irFunction, irBlock);
            basicEqTemplate(src, ((ConstInt) op2).getValue(), irBlock, irFunction);
            ObjBinary objSltu = ObjBinary.getSltu(dst, ZERO, AT);
            objBlock.addInstr(objSltu);
        }
        else
        {
            ObjPhyReg tmpReg = AT;
            ObjOperand ojbOp1 = parseOperand(op1, false, irFunction, irBlock);
            ObjOperand objOp2 = parseOperand(op2, false, irFunction, irBlock);
            ObjBinary objXor = ObjBinary.getXor(tmpReg, ojbOp1, objOp2);
            objBlock.addInstr(objXor);
            ObjBinary objSltu = ObjBinary.getSltu(dst, ZERO, tmpReg);
            objBlock.addInstr(objSltu);
        }
    }

    /**
     * 主要用到的是 a <= b 就是 !(a > b) 就是 !(b < a) 的特性
     * 但是由于 < 不具有交换性，所以没有办法太好的优化
     * 另外 slti 和 addi 类似，imm 是 16-SE 的
     */
    private void leTemplate(ObjOperand dst, Value op1, Value op2, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment("le " + op1.getName() + "\t" + op2.getName()));

        ObjPhyReg tmp = AT;
        // < 不具有交换性，就和减法一样，只能放弃抵抗
        ObjOperand src1 = parseOperand(op1, true, irFunction, irBlock);
        ObjOperand src2 = parseOperand(op2, false, irFunction, irBlock);
        // 通过交换操作数，达到 > 的目的
        ObjBinary objSlt = ObjBinary.getSlt(tmp, src2, src1);
        objBlock.addInstr(objSlt);
        // 对 > 取反，就变成了 <=
        ObjBinary objXor = ObjBinary.getXor(dst, tmp, new ObjImm(1));
        objBlock.addInstr(objXor);
    }

    /**
     * 简单直白
     */
    private void ltTemplate(ObjOperand dst, Value op1, Value op2, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        objBlock.addInstr(new ObjComment("lt " + op1.getName() + "\t" + op2.getName()));

        // < 不具有交换性，就和减法一样，只能放弃抵抗
        ObjOperand src1 = parseOperand(op1, false, irFunction, irBlock);
        ObjOperand src2 = parseOperand(op2, true, irFunction, irBlock);
        ObjBinary objSlt = ObjBinary.getSlt(dst, src1, src2);
        objBlock.addInstr(objSlt);
    }

    /**
     * 只需要将这条指令与 Icmp 的 dst 对应起来即可，甚至连 move 都不需要
     * @param instr 当前指令
     * @param irBlock 当前块
     * @param irFunction 当前函数
     */
    private void parseZext(Zext instr, BasicBlock irBlock, Function irFunction)
    {
        parseIcmp((Icmp) instr.getSrc(), irBlock, irFunction);
        operandMap.put(instr, operandMap.get(instr.getSrc()));
    }

    /**
     * 不同于 parseIcmp，我没有写大量的模板，而是应用了伪指令，这是因为 MARS 在这个部分做得很好
     * 所以就不自己写了
     */
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

                // 需要交换一下顺序
                boolean needSwap = condition.getOp1() instanceof ConstInt && !(condition.getOp2() instanceof ConstInt);
                if (needSwap)
                {
                    cond = getEqualOppCond(cond);
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
        ObjInstr objCall;
        if (callFunction.isBuiltin())
        {
            objCall = new ObjComment(callFunction.getName(), false);
            // 因为系统调用必然改变 v0
            objCall.addDefReg(null, V0);
        }
        else
        {
            objCall = new ObjCall(callFunction);
        }
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
                ObjStore objStore = new ObjStore(objSrc, SP, new ObjImm(offset));
                objBlock.addInstr(objStore);
            }
        }

        // 这里进行的是栈的生长操作
        if (argc > 4)
        {
            ObjOperand objOffset = parseConstIntOperand(4 * (argc - 4), true, irFunction, irBlock);
            ObjBinary objSub = ObjBinary.getSubu(SP, SP, objOffset);
            objBlock.addInstr(objSub);
        }
        // 到这里才正式把 jal 指令加入
        objBlock.addInstr(objCall);
        // 这里紧接着就是栈的恢复操作
        if (argc > 4)
        {
            ObjOperand objOffset = parseConstIntOperand(4 * (argc - 4), true, irFunction, irBlock);
            ObjBinary objAdd = ObjBinary.getAddu(SP, SP, objOffset);
            objBlock.addInstr(objAdd);
        }

        // 因为寄存器分配是以函数为单位的，所以相当于 call 指令只需要考虑在调用者函数中的影响
        // 那么 call 对应的 bl 指令会修改 lr 和 r0 (如果有返回值的话)
        // 此外，r0 - r3 是调用者保存的寄存器，这会导致可能需要额外的操作 mov ，所以这边考虑全部弄成被调用者保存
        for (int i = 0; i < 4; i++)
        {
            objCall.addDefReg(null, new ObjPhyReg("a" + i));
        }
        // 只有非内建函数需要保存 ra
        if (!callFunction.isBuiltin())
        {
            objCall.addDefReg(null, RA);
        }
        // 这里是处理返回值
        DataType returnType = ((instr.getFunction())).getReturnType();
        // 无论有没有返回值，都需要调用者保存 v0
        objCall.addDefReg(null, V0);
        if (!(returnType instanceof VoidType))
        {
            ObjMove objMove = new ObjMove(parseOperand(instr, false, irFunction, irBlock), V0);
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
            ObjMove objMove = new ObjMove(V0, objRet);
            objBlock.addInstr(objMove);
        }
        // 然后进行弹栈和返回操作
        ObjRet objRet = new ObjRet(objFunction);
        // 这里是为了窥孔优化的正确性，或许放到 readReg 里判断也行
        objRet.addUseReg(null, V0);
        objBlock.addInstr(objRet);
    }

    private void parseAlloca(Alloca instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        ObjFunction objFunction = fMap.get(irFunction);

        // 获得指针指向的类型
        ValueType pointeeType = ((PointerType) instr.getValueType()).getPointeeType();

        objBlock.addInstr(new ObjComment("alloca from the offset: " + objFunction.getAllocaSize() + ", size is: " + pointeeType.getSize()));
        // 这是 alloc 前在栈上已分配出的空间
        ObjOperand offset = parseConstIntOperand(objFunction.getAllocaSize(), true, irFunction, irBlock);
        objFunction.addAllocaSize(pointeeType.getSize());

        // 这里进行的是栈的恢复操作，是因为栈会在 obj 函数一开始就生长出所有 alloc 的空间
        // 这里只需要将 alloc 的空间像一个 heap 一样使用就好了
        ObjOperand dst = parseOperand(instr, true, irFunction, irBlock);
        ObjBinary objAdd = ObjBinary.getAddu(dst, SP, offset);
        objBlock.addInstr(objAdd);
    }

    /**
     * 用于解析然后生成指向特定元素的一个指针
     * 其实本质似乎是可以有多重偏移计算，但是这里被弱化成了 1 重
     */
    private void parseGEP(GetElementPtr instr, BasicBlock irBlock, Function irFunction)
    {
        ObjBlock objBlock = bMap.get(irBlock);

        // 获得数组的基地址
        ObjOperand base = parseOperand(instr.getBase(), false, irFunction, irBlock);
        objBlock.addInstr(new ObjComment("GEP base: " + instr.getBase().getName()));
        ObjOperand dst = parseOperand(instr, false, irFunction, irBlock);

        // 说明此时是一个指向 int 的一维指针(之前的观点了，不过似乎这么做也没啥大错)
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
                    ObjBinary objAdd = ObjBinary.getAddu(dst, base, objTotalOffset);
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
                // ObjOperand objStep = parseConstIntOperand(baseType.getSize(), false, irFunction, irBlock);
                // ObjOperand objOffset = parseOperand(irOffset, false, irFunction, irBlock);
                mulTemplate(dst, irOffset, new ConstInt(baseType.getSize()), irBlock, irFunction);
                //ObjBinary objTotalOffset = ObjBinary.getMul(totalOffset, objStep, objOffset);
                //objBlock.addInstr(objTotalOffset);
                ObjBinary objAdd = ObjBinary.getAddu(dst, dst, base);
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
            objBlock.addInstr(new ObjComment("the first index"));
            // 首先看 A 偏移，有 base += totalOffset0 (offsetA * baseSize)
            if (irOffset0 instanceof ConstInt)
            {
                int totalOffset0 = baseType.getSize() * ((ConstInt) irOffset0).getValue();
                ObjOperand objTotalOffset0 = parseConstIntOperand(totalOffset0, true, irFunction, irBlock);
                ObjBinary objAdd = ObjBinary.getAddu(dst, base, objTotalOffset0);
                objBlock.addInstr(objAdd);
            }
            else
            {
                // ObjOperand objStep0 = parseConstIntOperand(baseType.getSize(), false, irFunction, irBlock);
                // ObjOperand objOffset0 = parseOperand(irOffset0, false, irFunction, irBlock);
                // 此时 dst 为 总偏移量 0
                // ObjBinary objTotalOffset0 = ObjBinary.getMul(dst, objStep0, objOffset0);
                // objBlock.addInstr(objTotalOffset0);
                mulTemplate(dst, irOffset0, new ConstInt(baseType.getSize()), irBlock, irFunction);
                // 此时 dst = base + objStep0 * objOffset0
                ObjBinary objAdd = ObjBinary.getAddu(dst, dst, base);
                objBlock.addInstr(objAdd);
            }
            objBlock.addInstr(new ObjComment("the second index"));
            // 然后看 B 偏移
            if (irOffset1 instanceof ConstInt)
            {
                int totalOffset1 = elementType.getSize() * ((ConstInt) irOffset1).getValue();
                ObjOperand objTotalOffset1 = parseConstIntOperand(totalOffset1, true, irFunction, irBlock);
                ObjBinary objAdd = ObjBinary.getAddu(dst, dst, objTotalOffset1);
                objBlock.addInstr(objAdd);
            }
            else
            {
                // ObjOperand objStep1 = parseConstIntOperand(elementType.getSize(), false, irFunction, irBlock);
                // ObjOperand objOffset1 = parseOperand(irOffset1, false, irFunction, irBlock);
                // 先算 totalOffset1 = objStep1 * objOffset1
                ObjOperand totalOffset1 = AT;
                // ObjBinary objTotalOffset1 = ObjBinary.getMul(totalOffset1, objStep1, objOffset1);
                // objBlock.addInstr(objTotalOffset1);
                mulTemplate(totalOffset1, irOffset1, new ConstInt(elementType.getSize()), irBlock, irFunction);
                // 然后算 dst += totalOffset1
                ObjBinary objAdd = ObjBinary.getAddu(dst, totalOffset1, dst);
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
            ObjOperand objOperand = operandMap.get(irValue);
            // 但是如果是立即数，而逻辑中又不允许立即数，那么就需要重新 move
            if (!canImm && objOperand instanceof ObjImm)
            {
                if (((ObjImm) objOperand).getImmediate() == 0)
                {
                    return ZERO;
                }
                else
                {
                    ObjOperand tmp = genTmpReg(irFunction);
                    ObjMove objMove = new ObjMove(tmp, objOperand);
                    bMap.get(irBlock).addInstr(objMove);
                    return tmp;
                }
            }
            else
            {
                return objOperand;
            }
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
     * 此外，考虑到 at 寄存器不参与分配，所以可以将目的寄存器设置成 at
     * 即使出现 li at, -100000 这种指令，依然在 MARS 中是可以正常工作的
     * 那么需不需要考虑对于 add $t0, $at, $at 这样的指令呢，就是都需要 at 寄存器去加载，
     * 然后就会导致靠前加载的值会被靠后加载的值覆盖
     * 是不会的，因为这种指令就可以直接算出来了
     * 只需要注意分配寄存器的时候，不分配 at 寄存器即可，这样就不会出现 at 被覆盖的情况
     * @param imm        立即数
     * @param irFunction 所在的函数
     * @param irBlock    所在的 block
     * @param canImm     表示允不允许是一个立即数
     * @return 操作数
     */
    private ObjOperand parseConstIntOperand(int imm, boolean canImm, Function irFunction, BasicBlock irBlock)
    {
        ObjImm objImm = new ObjImm(imm);
        // 如果可以直接编码而且允许返回立即数, 那么就直接返回就可以了
        if (canEncodeImm(imm, true) && canImm)
        {
            return objImm;
        }
        // 不可以立即数
        else
        {
            // 如果需要的立即数是 0，那么就直接返回 $zero 寄存器
            if (imm == 0)
            {
                return new ObjPhyReg(0);
            }
            // 否则就用 li $at, imm 来加载
            else
            {
                ObjBlock objBlock = bMap.get(irBlock);
                ObjFunction objFunction = fMap.get(irFunction);
                ObjVirReg dst = new ObjVirReg();
                objFunction.addUsedVirReg(dst);
                ObjMove objMove = new ObjMove(dst, objImm);
                objBlock.addInstr(objMove);
                return dst;
            }
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
        else
        {
            // 创建一个移位指令
            int stackPos = rank - 4;
            ObjImm objOffset = new ObjImm(stackPos * 4);
            // 这里有改成了 offset，因为 mips 支持这样的伪指令
            objFunction.addArgOffset(objOffset);
            // 创建一个加载指令
            ObjLoad objLoad = new ObjLoad(dstVirReg, SP, objOffset);
            // 这个指令需要插入到头部
            firstBlock.addInstrHead(objLoad);
        }
        return dstVirReg;
    }

    /**
     * 全局变量使用前需要加载到一个虚拟寄存器中（直接使用的方法似乎在分段 .data 的时候不成立）
     * 但是我们并没有记录下来，这是因为本质上局部是基本块，所以有的时候别的基本块用的东西，我们即使有，也不能提供
     * @param irGlobal 全局变量
     * @return 操作数
     */
    private ObjOperand parseGlobalOperand(GlobalVariable irGlobal, Function irFunction, BasicBlock irBlock)
    {
        ObjBlock objBlock = bMap.get(irBlock);
        MyPair<ObjBlock, GlobalVariable> globalLoopUp = new MyPair<>(objBlock, irGlobal);
        if (globalVariableMap.containsKey(globalLoopUp))
        {
            return globalVariableMap.get(globalLoopUp);
        }
        else
        {
            // 这里没有使用 at，是因为使用 at 没有意义，我要记录下来，at 的值不容易记
            ObjOperand dst = genTmpReg(irFunction);
            // 这个 move 最后会变成 la
            ObjMove objMove = new ObjMove(dst, new ObjLabel(irGlobal.getName().substring(1)));
            objBlock.addInstr(objMove);
            return dst;
        }
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


