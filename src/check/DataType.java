package check;

/**
 * 在运算的时候，姑且认为存在一个优先级，低优先级与高优先级运算，最后的数据类型会变成高优先级
 * 如 int a[1]，a + 1 会变成 DIM1，但是似乎一维指针与二维指针没有这种关系，不过就勉强认为吧
 * 关于优先级的确定，IntConst 显然是 INT，函数调用要么是 INT，要么是 VOID，LVal 需要查表并且结合其后的索引才能判定
 * 至于其他的 Exp 的 DataType，那么就需要取其子孩子最高的优先级
 */
public enum DataType
{
    // 无返回值
    VOID,
    // 单变量
    INT,
    // 一维指针，比如说 int a[2] 中的 a，就是 DIM1， int a[] 也是 DIM1（本质是传入 a）
    DIM1,
    // 二维指针，比如说 int a[][2] 中的 a，就是 DIM2
    DIM2
}
