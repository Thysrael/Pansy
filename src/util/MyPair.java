package util;

import java.util.Objects;

/**
 * 一个简单的 pair 类
 * @param <A> 第一个参数的类型
 * @param <B> 第二个参数的类型
 */
public class MyPair<A, B>
{
    private A first;
    private B second;

    public MyPair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }

    public A getFirst()
    {
        return first;
    }

    public void setFirst(A first)
    {
        this.first = first;
    }

    public B getSecond()
    {
        return second;
    }

    public void setSecond(B second)
    {
        this.second = second;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPair<?, ?> pair = (MyPair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second);
    }
}
