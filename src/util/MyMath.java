package util;

public class MyMath
{
    /**
     * Counts the number of Tailing Zeros
     * 计算最低位 1 的位数，如果输入 0，返回 0
     * @param n 待输入的数
     * @return 最低位 1 的位数
     */
    public static int ctz(int n)
    {
        int res = 0;
        n = n >>> 1;
        while (n != 0)
        {
            n = n >>> 1;
            res++;
        }
        return res;
    }
}
