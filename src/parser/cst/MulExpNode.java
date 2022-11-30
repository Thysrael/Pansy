package parser.cst;

import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Mul;
import ir.values.instructions.Sdiv;
import lexer.token.SyntaxType;

import java.util.ArrayList;

/**
 * MulExp
 *     : UnaryExp (MulOp UnaryExp)*
 *     ;
 * MulOp
 *     : MUL
 *     | DIV
 *     | MOD
 *     ;
 */
public class MulExpNode extends CSTNode
{
    private final ArrayList<UnaryExpNode> unaryExps = new ArrayList<>();
    private final ArrayList<TokenNode> mulOps = new ArrayList<>();
    @Override
    public void addChild(CSTNode child)
    {
        super.addChild(child);
        if (child instanceof UnaryExpNode)
        {
            unaryExps.add((UnaryExpNode) child);
        }
        if (child instanceof TokenNode)
        {
            mulOps.add((TokenNode) child);
        }
    }

    @Override
    public void buildIr()
    {
        if (canCalValueDown)
        {
            unaryExps.get(0).buildIr();
            int product = valueIntUp;
            for (int i = 1; i < unaryExps.size(); i++)
            {
                unaryExps.get(i).buildIr();
                if (mulOps.get(i - 1).isSameType(SyntaxType.MULT))
                {
                    product *= valueIntUp;
                }
                if (mulOps.get(i - 1).isSameType(SyntaxType.DIV))
                {
                    product /= valueIntUp;
                }
                if (mulOps.get(i - 1).isSameType(SyntaxType.MOD))
                {
                    product %= valueIntUp;
                }
            }
            valueIntUp = product;
            if (!cannotCalValueUp)
            {
                valueUp = new ConstInt(valueIntUp);
            }
        }
        else
        {
            unaryExps.get(0).buildIr();
            Value product = valueUp;
            // cast i1 value 2 i32
            if (product.getValueType().isI1())
            {
                product = irBuilder.buildZext(curBlock, product);
            }
            for (int i = 1; i < unaryExps.size(); i++)
            {
                unaryExps.get(i).buildIr();
                Value multer = valueUp;

                if (multer.getValueType().isI1())
                {
                    multer = irBuilder.buildZext(curBlock, multer);
                }

                if (mulOps.get(i - 1).isSameType(SyntaxType.MULT))
                {
                    product = irBuilder.buildMul(curBlock, product, multer);
                }
                if (mulOps.get(i - 1).isSameType(SyntaxType.DIV))
                {
                    product = irBuilder.buildSdiv(curBlock, product, multer);
                }
                // x % y = x - ( x / y ) * y，这是因为取模优化不太好做
                if (mulOps.get(i - 1).isSameType(SyntaxType.MOD))
                {
                    if (multer instanceof ConstInt)
                    {
                        int num = ((ConstInt) multer).getValue();
                        // 如果绝对值是 1，那么就翻译成 MOD，这就交给后端优化了
                        if (Math.abs(num) == 1)
                        {
                            product = irBuilder.buildSrem(curBlock, product, multer);
                        }
                        // 如果是 2 的幂次
                        else if ((Math.abs(num) & (Math.abs(num) - 1)) == 0)
                        {
                            product = irBuilder.buildSrem(curBlock, product, multer);
                        }
                        // TODO 这里为啥，确实是已一件很难看懂的事情，考虑改掉，或者测试一下
                        else if (num < 0)
                        {
                            Sdiv a = irBuilder.buildSdiv(curBlock, product, multer);
                            Mul b = irBuilder.buildMul(curBlock, a, new ConstInt(32, Math.abs(num)));
                            product = irBuilder.buildSub(curBlock, product, b);
                        }
                        else
                        {
                            Sdiv a = irBuilder.buildSdiv(curBlock, product, multer);
                            Mul b = irBuilder.buildMul(curBlock, a, multer);
                            product = irBuilder.buildSub(curBlock, product, b);
                        }
                    }
                    else
                    {
                        Sdiv a = irBuilder.buildSdiv(curBlock, product, multer);
                        Mul b = irBuilder.buildMul(curBlock, a, multer);
                        product = irBuilder.buildSub(curBlock, product, b);
                    }
                }
            }
            valueUp = product;
        }
    }
}
