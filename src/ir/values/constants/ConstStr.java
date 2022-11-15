package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import util.MyPrintf;

import java.util.Objects;

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
     * 在这个部分，要把 llvm ir 习惯的 \0a 换成 MARS 习惯的 \n
     * @return 字符串内容
     */
    public String getContent()
    {
        return content.replace("\\0a", "\\n");
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

    /**
     * 这里规定如果两个字符串的内容相等，那么他们即使相等的
     * 这个方法主要是为了避免过多相同的字符串的出现
     * @param o 另个一元素
     * @return 相等为 true
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConstStr constStr = (ConstStr) o;
        return Objects.equals(content, constStr.content);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), content);
    }
}
