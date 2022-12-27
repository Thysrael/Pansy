# 错误处理

## 一、总论

​	错误处理和解析生成中间代码的过程是类似的，都可以看做是一个**语法制导翻译的过程**。对于遍历出的节点，触发不同的动作符号，这些动作符号在错误处理中用于检测错误，而在生成中间代码生成中，这些动作符号用于生成中间代码。

​	错误处理会遍历语法生成树，CST 的每个节点都实现了 `check` 方法用于检查有没有错误。

---



## 二、错误的分类

​	并不是所有的错误都是语义错误，常见的语义错误有“变量重命名，变量未定义“等，这些错误是可以通过词法分析和语法分析的，不会造成语法分析的错误。但是有些错误，是没有办法通过词法分析和语法分析的，有如下这些类

| 编号 | 类型           | 原因             |
| ---- | -------------- | ---------------- |
| a    | 非法字符串符号 | 无法通过词法分析 |
| i    | 缺少分号       | 无法通过语法分析 |
| j    | 缺少右小括号   | 无法通过语法分析 |
| k    | 缺少右中括号   | 无法通过语法分析 |

​	对于这样的错误，必须要做的就是当发生这些错误的时候，让词法分析和语法分析正常进行，必须坚持到进行错误处理的步骤。然后有两种思路，一种是在进行词法分析和语法分析的时候就抛出异常并收集，然后在进行错误处理的时候再抛出一些异常，将异常汇总，但是这种方法比较零散。我采用的是在词法分析和语法分析的时候“包容并记录”这种错误，然后在错误处理的时候一并发现。

​	具体的实现思路就是，对于 a 类错误，并不在词法分析过程中检查双引号间有什么，而是直接存储在 token 中，当 `TokenNode` 进行 `check` 的时候，再对其中包含的字符串进行检查。同理，对于三类符号的缺失，可以考虑增强语法分析的功能，使其能够自动补全缺失的符号，然后产生相应的 `TokenNode` ，但是给这些补全的 `TokenNode` 打上缺失标记 `miss`。最终在 `TokenNode` 的 `check` 中一并检出，即下面的逻辑：

```java
/**
 * 用于缺失的时候产生具体的 error
 * 包括三种缺失错误
 * 字符串错误
 * 名字错误本来就不行，这是因为名字错误包括未定义和重复定义两种，没有办法确定要使用哪种
 */
@Override
public void check(SymbolTable symbolTable)
{
    // 检测缺失情况
    if (token.isSameType(SyntaxType.SEMICN))
    {
        if (miss)
        {
            errors.add(new PansyException(ErrorType.MISS_SEMICOLON, token.getLine()));
        }

    }
    else if (token.isSameType(SyntaxType.RBRACK))
    {
        if (miss)
        {
            errors.add(new PansyException(ErrorType.MISS_RBRACK, token.getLine()));
        }
    }
    else if (token.isSameType(SyntaxType.RPARENT))
    {
        if (miss)
        {
            errors.add(new PansyException(ErrorType.MISS_RPARENT, token.getLine()));
        }
    }
    // 检测是否是合法字符串
    else if (token.isSameType(SyntaxType.STRCON))
    {
        String content = token.getContent();
        // 不算两个双引号
        for (int i = 1; i < content.length() - 1; ++i)
        {
            int ascii = content.charAt(i);
            if (ascii == 32 || ascii == 33 || ascii >= 40 && ascii <= 126)
            {
                if (ascii == 92 && content.charAt(i + 1) != 'n')
                {
                    errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                    return;
                }
            }
            else if (ascii == 37)
            {
                if (content.charAt(i + 1) != 'd')
                {
                    errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                    return;
                }

            }
            else
            {
                errors.add(new PansyException(ILLEGAL_SYMBOL, token.getLine()));
                return;
            }
        }
    }
}
```

---



## 三、缺失符号处理

​	正如上文所言，导致语法分析进行不下去的错误一定需要被“包容”，否则就坚持不到错误处理流程了。这种包容设置还有一种“补充”的意味在里面。但是最难的其实是缺失符号对于正常语法解析的影响，缺失符号会让原本就很困难的语法分析变得更加困难，这是因为语法分析本就需要依靠符号确定解析的分支，缺少符号会让这个过程变得模糊不清，这也是想 `i, j, k` 这类错误并不太多的原因，因为一旦多了，就会导致解析困难。

​	在三种缺失中，以缺少右中括号最为好处理，中括号意味着维度信息，只要有左中括号，那么右中括号一定是可以补上的，基本上无脑就可以了。

​	但是对于缺失分号和小括号，有可能造成解析分支的判断不当。所以需要进行回退处理（如果不回退的话就要改写文法，而且文法改的毫无语义意义）。

​	对于缺失分号，有无法区分赋值语句和表达式的问题。

```c
// 缺失前
i + 1;
a = b + c;

// 缺失后
i + 1
a = b + c;
```

​	这就意味着没法通过前瞻（即“在分号前有一个等于号“）来确定是否是赋值语句。所以只能通过“尝试解析-判断 LAST”的方法进行。

​	对于 `return` 语句，可能也有这种现象，虽然应该被语义约束了

```c
// 缺失前
return;
a;

// 缺失后
return 
a;
```

​	对于 `Callee` 语句，也是这样（应该也被约束了），这里应该是同时缺失了小括号和分号

```c
// 缺失前
f();
a;

// 缺失后
f(
a;
```

---



## 四、动作属性

​	因为是语法制导翻译，所以肯定会涉及动作属性，包括**继承属性和综合属性**，按照理论来讲，可以将这两个东西分别打包成两个类，然后将继承属性当做动作的参数，将综合属性当做动作的返回值，这样是最正统的写法。但是考虑到我设计的时候没有想到，所以我将其全部处理成了全局变量，具体的方法是将其都作为语法树节点基类的**保护静态属性**，然后这样所有的语法树节点就都可以共享这些变量了。

​	对于错误处理，一共有以下变量

```java
/**
 * 用来存储语义分析中产生的错误
 */
protecttatic final ArrayList<PansyException> errors = Checker.errors;
/**
 * 用来存储检测日志
 */
protected static final ArrayList<String> checkLog = Checker.checkLog;
/**
 * 当前函数信息
 */
protected static FuncInfo curFuncInfo = null;
/**
 * 是否在循环中
 */
protected static int inLoop = 0;
```

​	还有一个符号表，我当成参数了。

```java
public void check(SymbolTable symbolTable)
```

---



## 五、错误检测的可重入性

​	可能是由于设计的问题，我会对同一个错误进行多次检测，比如说在 `AssignStmtNode` 和 `LValNode` 中都检测了符号是否存在，只不过 `AssignStmtNode` 还检查了是否更改了常量的值（大概率是设计问题）。

​	但是没有关系，这样依然是可以保证正确性的，只要我们在输出前需要利用一个 TreeSet 进行去重和排序，然后再输出。

​	这个涉及一个很好玩的事情，就是对于任何一棵语法子树，从它的根节点调用 `check` 方法，无论调用多少次，其生成的结果都是一样的。

---



## 六、符号表

​	错误处理同样需要符号表，构建一个栈式符号表即可。可以解决的问题有

| 编号 | 内容         |
| ---- | ------------ |
| b    | 名字重定义   |
| c    | 未定义的名字 |

​	我在实现符号表的时候，没有将函数符号表和变量符号表分开处理，是因为担心有变量和函数重名（其实就是本质就不应该分开）。符号表的表项称为 `SymInfo` ，有两个子类，分别是 `VarInfo` 和 `FuncInfo`，`VarInfo` 又分为“常量，变量，形参”三种类型。

​	对于 `VarInfo` ，需要记录维度信息，因为错误处理对于维度信息的要求较低，所以利用 `CSTNode` 去记录维度信息

| 类型     | dim1     | dim2     | CheckDataType |
| -------- | -------- | -------- | ------------- |
| 单变量   | ZERO     | ZERO     | INT           |
| 一维数组 | ConstExp | ZERO     | DIM1          |
| 二维数组 | ConstExp | ConstExp | DIM2          |
| 一维指针 | INF      | ZERO     | DIM1          |
| 二维指针 | INF      | ConstExp | DIM2          |

​	可以看到记录的信息相当的“粗糙”。

​	实现这两个错误的检测都是很容易的，就不赘述了。

---



## 七、类型

​	错误处理中涉及的类型问题只有函数的实参和形参不匹配的问题，也就是编号 `e`。类型不匹配只涉及一维和二维和单变量之间的不匹配，并不涉及具体的维度信息，也就是说：

```c
void f(int a[][2])
{}

int main()
{
    int b[1][5] = {{1, 3, 5, 7, 8, 9}};
    f(b)
    return 0;
}
```

  	这种是不会报错的。根据这种语义约束，涉及类型有这样 4 种即可：

```java
public enum CheckDataType
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
```

​	而其实涉及的运算，只有中括号造成的指针升维运算而已。也就是下面的过程

```java
/**
 * 进行指针运算，获得左值真正的类型信息
 * @param rawType 符号表示的类型信息
 * @return 实际类型信息
 */
private CheckDataType calcDataType(CheckDataType rawType)
{
    int calDim = exps.size();
    int rawDim;
    if (rawType.equals(CheckDataType.INT))
    {
        rawDim = 0;
    }
    else if (rawType.equals(CheckDataType.DIM1))
    {
        rawDim = 1;
    }
    else if (rawType.equals(CheckDataType.DIM2))
    {
        rawDim = 2;
    }
    else
    {
        rawDim = -1;
    }
    int trueDim = rawDim - calDim;
    if (trueDim == 0)
    {
        return CheckDataType.INT;
    }
    else if (trueDim == 1)
    {
        return CheckDataType.DIM1;
    }
    else if (trueDim == 2)
    {
        return CheckDataType.DIM2;
    }
    else
    {
        return CheckDataType.VOID;
    }
}
```

​	比如说原本的 `a[1][2]` 这个数组，在符号表存着的时候，符号表指示他是一个 `DIM2` 类型，当我们真的使用他的时候，一般是使用 `a[0][0]` ，那么就是进行了两次升维运算，变成了 `INT` ，如果将 `a` 用于传参，那么它就是一个 `DIM2` 型，而 `a[0]` 则是 `DIM1` 型。

​	对于一个表达式，比如说 `1 + 3 + 2` ，他是 `INT` 型的，究其原因，是 `1, 2, 3` 都是 `INT` 型，这就启发我们，计算一个 `Exp` 的类型，其实要根据它的孩子节点的类型判断，只需要一步步递归，就可以最终溯源到 `UnaryExp`，然后就会发现最底层只有 3 个东西 `Number, LVal, Callee`。这三种的类型如下所示

| 底层节点 | 类型           |
| -------- | -------------- |
| Number   | INT            |
| LVal     | 查表运算后获得 |
| callee   | 查表查返回值   |

​	对于结合，我采用了一个更加让人自我满足的方式，就是选择所有孩子的类型中最大的作为当前节点的类型，所谓“最大”，就是 `DIM2 > DIM1 > INT > VOID` 。这是因为这更符合 C 语言特性，比如说 `a + 1` ，如果 `a` 是一个指针，那么 `a + 1` 也是一个指针，但是指针和指针运算、`INT` 和 `VOID` 运算，就没有那么多道理可讲了，所以只能说是自我安慰。这么做不会导致错误，是因为我们也不允许不同类型的元素做算术。

---



## 八、Return

​	与 `return` 有关的错误有两类，这两类看似相似，但是处理方式却不同。如下所示

| 编号 | 类型                                                         |
| ---- | ------------------------------------------------------------ |
| f    | 无返回值的函数存在不匹配的 `return` 语句                     |
| g    | 有返回值的函数缺少 `return` 语句，只需要考虑函数最后一句即可 |

​	通过记录当前正在处理哪个函数 `curFuncInfo`，是很容易在进行 `ReturnNode` 的时候完成对于 `f` 类错误的检验的，但是对于 `g` 类错误，却只能在 `FuncDefNode` 中处理，因为不能在 `ReturnNode` 中处理，`ReturnNode` 并不知道自己是函数的最后一条指令，所以没有办法在这里处理。

---



## 九、循环

​	只需要用一个 `int` 来对当前的循环深度进行计数，如果循环深度为 0 的时候发生了 `break` 和 `continue` 就会报错。即如下操作

```java
@Override
public void check(SymbolTable symbolTable)
{
    addCheckLog();

    inLoop++;
    for (CSTNode child : children)
    {
        child.check(symbolTable);
    }
    inLoop--;
}
```

---



