package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import util.MyPrintf;

/**
 * char s[10] = "Hello.";
 * \@s = dso_local global [10 x i8] c"Hello.\00\00\00\00", align 1
 */
public class ConstStr extends Constant
{
    private final String content;

    public ConstStr(String str)
    {
        super(new ArrayType(new IntType(8), MyPrintf.llvmStrLen(str) + 1));
        this.content = str;
    }

    @Override
    public String getName()
    {
        return toString();
    }

    /**
     * 这里 llvm 中字符串的标准写法
     * @return 输出
     */
    @Override
    public String toString()
    {
        return "c\"" + content + "\\00\"";
    }
}
