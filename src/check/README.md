# 错误处理

## 一、总论

​	错误处理和解析生成中间代码的过程是类似的，都可以看做是一个**语法制导翻译的过程**。对于遍历出的节点，触发不同的动作符号，这些动作符号在错误处理中用于检测错误，而在生成中间代码生成中，这些动作符号用于生成中间代码。

## 二、处理细节

### 2.1 输出顺序

​	因为最后要求是要按照错误所在行的递增排列，并且要求保证每行只会出现一个错误，所以我用了 `TreeSet` 去实现排序和去重功能。

​	首先在 `PansyException` （我的错误类）中定义比较行数的函数

```java
/**
* 这里为了错误输出时按照顺序输出，所以需要比较所在行的大小
* @param other 另一个 exception
* @return 行数比较
*/
public int compareTo(PansyException other)
{
    return line - other.line;
}
```

​	然后将原来的 `error` 数组重新放到 `TreeSet` 中，然后输出：

```java
public String display()
{
    StringBuilder s = new StringBuilder();
    Set<PansyException> set = new TreeSet<>((o1, o2) -> -o2.compareTo(o1));
    set.addAll(errors);
    for (PansyException error : set)
    {
        s.append(error.getLine()).append(" ").append(errorMap.get(error.getType())).append("\n");
    }
    return s.toString();
}
```

​	另外其实如果不要求每行只有一个错误，那么其实可以直接对 `ArrayList` 进行排序

```java
//对list数组降序排列
public class ArrayListTest {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(60);
        list.add(20);
        list.add(99);
        list.add(10);
        Collections.sort(list, new  Comparator<Integer>(){ // 排序
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        System.out.println(list);
    }
}
```

### 2.2 推迟检验

​	有些错误在语法分析或者词法分析的时候就可以检测出来，但是为了检测的统一性，这些检测真正的错误输出会被统一集中到 `Checker` 中进行处理。这就意味着，这些错误必须以某种“姿势”活到 `check` 这一步。可以采用在具象语法树中增加属性的方法：

```java
public class TokenNode extends CSTNode
{
    private final Token token;
    private final boolean miss;
    ...
}
```

​	其中的 `miss` 属性指定了是否是缺失了这个 `token` ，即是否是 `i, j, k` 型错误。

### 2.3 解析调整



## 三、实现细节

```java
/**
* 改变常量值
* 符号未定义
* 缺符号
* @param symbolTable 符号表
*/
@Override
public void check(SymbolTable symbolTable)
{
    addCheckLog();
    TokenNode identNode = (TokenNode) lVal.getChildren().get(0);
    try
    {
        SymbolInfo symbolInfo = symbolTable.getSymbolInfo(identNode.getContent());
        if (symbolInfo instanceof ConstInfo)
        {
            errors.add(new PansyException(ErrorType.CHANGE_CONST, identNode.getLine()));
        }
    }
    catch (PansyException e)
    {
        errors.add(new PansyException(e.getType(), identNode.getLine()));
    }
    // 检测是否缺分号和右中括号
    for (TokenNode token : tokens)
    {
        token.check(symbolTable);
    }
}
```

