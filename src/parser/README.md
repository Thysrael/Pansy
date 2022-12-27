# Pansy Parser

这里是 Pansy 编译器的 parser

## 具体语法树

​	`Parser` 的目的是为了根据语法获得一个**具体语法树**（Concrete Syntax Tree，CST）。这棵语法树的非叶子节点是各个语法成分，而叶子节点则是 `Token` （或者说包含 `Token`）。强调这个是因为我没有意识到可以将 `Token`  与其他语法成分等量齐观。

​	在文法中，我们约定非叶子节点采用首字母大写的驼峰命名法，比如 `CompUnit`，而对于叶子节点，我们采用全大写的形式，比如 `IDENFR` 。

​	相比于各种学长压缩语法树，在语法解析的时候直接获得**抽象语法树**（Abstract Syntax Tree，AST）的设计，我觉得我这个设计是更加适合应试的，因为考试的题目是要依托于文法的，AST 忽略或者说提炼了部分的文法信息，这就导致与原有文法的不契合。而且这种**“将语义信息隐藏在语法中”**的设计思想，和我个人性格更符。

---



## Supporter

​	解析的过程可以看做对于 `tokens` 数组（词法分析的成果）的遍历，只有当解析到最底部的终结符的时候，我们才会移动 `tokens` 指针。为了实现更强的功能（主要是错误处理和前瞻），所以采用了一个 `supporter` 对这个数据结构和动作进行封装。封装好的 `ParseSupporter` 支持前瞻，回退，日志记录、$First$ 判断等多项功能。

---



## 改写语法与解析实现

​	手搓了一份 SysY 的文法，采用正则形式，写在了 `SysY.g4` 文件中。相比于课程组给出的文法，消除了左递归，并且严谨了表述。而且拓展了一部分十分不优雅的语法。

### 编译单元 CompUnit

```
CompUnit -> {Decl} {FuncDef} MainFuncDef
```

编译单元由声明，函数定义和主函数定义组成，而且必须保持这个顺序，我修改成了

```
CompUnit
   : (FuncDef | Decl)+
   ;
```

这样声明和函数可以交错，而且不会有 `MainFuncDef` 这个选项。识别的时候依靠前瞻即可。

在实际操作的时候，发现还是需要 `MainFuncDef` 的时候，因为 `MainFuncDef` 的语法成分和 `FuncDef` 并不相同，两者不能统一处理。`CompUnit` 的解析主要是识别三种符号，然后持续解析直到 `tokens` 被读取完，如果与三种符号都不匹配，那么直接报错退出。 

### 声明 Decl

```
Decl → ConstDecl | VarDecl 
```

声明可以是常量声明或者变量声明

```
Decl
   : ConstDecl
   | VarDecl
   ;
```

这部分没有修改。

在实现中，只需要根据 First 是不是 `const` 就可以区分这两种情况，并不会产生任何的歧义。

### 常量声明 ConstDecl

```
ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' 
```

主要修改是增加了 token 节点，避免了 token 的暴露，让后序遍历更加一致。而且去掉了 `BType`

```
ConstDecl
   : CONST_KW BType ConstDef (COMMA ConstDef)* SEMICOLON
   ;
```

可以看到，常量声明可以用逗号分割，同时声明多个。利用有没有逗号判断 constDef 的个数，是严谨的。

### 常量定义 ConstDef

```
ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
```

修改成

```
ConstDef
   : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
   ;
```

基本上没有任何变化。

在实现中，以左中括号为判断是否存在维度信息的标准，缺少右中括号并不会干扰判断。

### 常量初值 ConstInitVal

常量初值可以是一个常量表达式，也可以是一个数组初值

```
ConstInitVal → ConstExp
			 | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
```

在实现的时候，利用的是，是否是左大括号，如果是进行数组初始化，否则，就是单值常量，这没准会造成某种意义的不严谨。

### 常量表达式 ConstExp

```
ConstExp → AddExp
```

最终 `ConstExp` 会变成 `AddExp` 。`Exp` 也是会推出 `AddExp`。其中的区别是 `ConstExp` 里不能有变量。

修改为

```
ConstExp
    : AddExp
    ;
```

事实证明这里没有直接跳过 `ConstExp` 直接 `AddExp` 是一个明智之举，因为这样 `AddExpNode` 就可以继承属性来判断当前是否是 `const` 了。

### 变量声明 VarDecl

```
 VarDecl → BType VarDef { ',' VarDef } ';'
```

修改为

```
VarDecl
    : BType VarDef (COMMA VarDef)* SEMICOLON
    ;
```

根据有无左括号判断是否有维度信息，根据有无等于号判断时是否有初始值，是严谨的。

### 变量定义 VarDef

```
VarDef → Ident { '[' ConstExp ']' } 
	   | Ident { '[' ConstExp ']' } '=' InitVal //包含普通变量、一维数组、二维数组定义
```

相比于常量定义必须有初值，变量定义可以没有初值，修改为

```
VarDef
    : IDENT (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
    ;
```

基本上没变。

### 变量初值 InitVal

```
InitVal → Exp 
	    | '{' [ InitVal { ',' InitVal } ] '}'
```

可以是单变量，也可以是数组。可以修改为

```
InitVal
    : Exp
    | (L_BRACE (InitVal (COMMA InitVal)*)? R_BRACE)
    ;
```

基本上没变。利用的同样是左大括号是否存在来判断分支，在判断的时候还考虑了 '{}' 这种情况的出现，其实是没有必要的，因为 '{}' 是 0 维的，语义约束要求了维度不能为 0。

### 函数定义 FuncDef

```
FuncDef → FuncType Ident '(' [FuncFParams] ')' Block 
```

函数由函数类型，标识符，形参列表和函数体组成，SysY 中没有函数声明，只有函数定义。

```
FuncDef
   : FuncType IDENT L_PAREN funcFParams? R_PAREN block
   ;
```

这里的函数定义并不包括主函数 `main`。根据是否是 INTTK 判断是否有形参表。

### 函数类型 FuncType

```
FuncType → 'void' | 'int' 
```

修改为

```
FuncType
    : VOID_KW
    | INT_KW
    ;
```

### 函数形参表 FuncFParams

```
FuncFParams → FuncFParam { ',' FuncFParam } 
```

函数形参表至少有有一个参数，没参数的情况是直接没有形参表。修改为

```
FuncFParams
    : FuncFParam (COMMA FuncFParam)*
    ;
```

根据是否有逗号确定有几个形参。

### 函数形参 FuncFParam

```
FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] 
```

参数可以是整型，还可以是数组，但是数组的第一维一定是缺失的。

```
FuncFParam
    : BType IDENT (L_BRACKT R_BRACKT (L_BRACKT Exp R_BRACKT)*)?
    ;
```

利用有无左大括号判断是否传入指针。

### 语句块 Block

```
Block → '{' { BlockItem } '}'
```

语句块就是花括号包裹的 0 到多个 `BlockItem` 。修改为

```
Block
    : L_BRACE BlockItem* R_BRACE
    ;
```

### 语句块项 BlockItem

```
BlockItem → Decl | Stmt 
```

语句块项可以是声明和语句。修改为

```
BlockItem
    : VarDecl
    | Stmt
    ;
```

当是声明的时候，走声明分支，否则走语句分支，这是不严谨的。这建立于语句分支的 FIRST 中没有 int 和 const。

### 语句 Stmt

```
Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
| [Exp] ';' // 有无Exp两种情况
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
| 'while' '(' Cond ')' Stmt
| 'break' ';' 
| 'continue' ';'
| 'return' [Exp] ';' // 1.有Exp 2.无Exp
| LVal '=' 'getint''('')'';'
| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
```

这里似乎过于复杂了，所以尝试引入多个来缓解。不过需要注意，这样语法成分就增多了，但是是值得的，而且并不会有太大的影响。

语句总览：

```
Stmt
    : AssignStmt
    | ExpStmt
    | Block
    | ConditionStmt
    | WhileStmt
    | BreakStmt
    | ContinueStmt
    | ReturnStmt
    | InStmt
    | OutStmt
    ;
```

对于大部分的语句，都是有很明显的 $FIRST$ 可以将其与其他语句分开。但是对于 AssignStmt, InStmt, ExpStmt，他们的第一个元素都有可能是 LVal，所以这里用到了回退。先尝试解析一个 LVal，然后判断其后的元素是否是 = 号（其实类似于 LAST 的思想）。如果是，那么就从 AssignStmt 和 InStmt 中选择。否则从 ExpStmt 中选择，这里也造成了一定的不严谨性，所有的其他语句都会流入 ExpStmt。

#### 赋值语句 AssignStmt

```
AssignStmt
    : LVal ASSIGN Exp SEMICOLON
    ;
```

通过有没有分号来确定是否到达结尾，缺少分号不会造成问题，因为如果是空语句缺分号，那么就是空行了。

#### 表达式语句 ExpStmt

```
ExpStmt
    : Exp? SEMICOLON
    ;
```

​	可以发现 `AssignStmt` 和 `ExpStmt` 有很大的重复部分，比如 `a[1][1];` 和 `a[1][1] = 1;` 就是两个语句，但是除非解析到 `=` ，否则根本分不出来到底是啥，所以可能需要前瞻很多东西。但是在错误处理中就没有办法使用了。

#### 输入语句 InStmt

```
InStmt
	: LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
```

​	发现似乎需要和 `AssignStmt` 一起处理。应该没事。

#### 输出语句 OutStmt

```
OutStmt
    : PRINTFTK L_PAREN FormatString (COMMA Exp)* R_PAREN
    ;
```

#### 条件语句 ConditionStmt

​	有趣的是，这些复杂的结构，我以为是翻译成 `Block`，但是却被翻译成了 `Stmt`。确实这样表达能力增强了。

```
ConditionStmt
    : IF_KW L_PAREN Cond R_PAREN Stmt (ELSE_KW Stmt)?
    ;
```

#### 循环语句 WhileStmt

```
WhileStmt
    : WHILE_KW L_PAREN Cond R_PAREN Stmt
    ;
```

#### 跳出语句 BreakStmt

```
BreakStmt
    : BREAK_KW SEMICOLON
    ;
```

#### 继续语句 CotinueStmt

```
ContinueStmt
    : CONTINUE_KW SEMICOLON
    ;
```

#### 返回语句 ReturnStmt

```
ReturnStmt
    : RETURN_KW (Exp)? SEMICOLON
    ;
```

依然是需要尝试解析，因为缺少分号会造成“连读”现象（虽然应该被语义约束了）。

### 条件表达式 Cond

```
Cond → LOrExp
```

修改为

```
Cond
    : LOrExp
    ;
```

### 逻辑或表达式 LOrExp

```
LOrExp → LAndExp | LOrExp '||' LAndExp
```

需要消去左递归，修改为

```
LOrExp
   : LAndExp (OR LAndExp)*
   ;
```

这样看就很显然了，因为 `OR` 优先级是低于 `AND` 和 `NOT` 的，所以把他放在最外侧。

### 逻辑与表达式 LAndExp

```
LAndExp → EqExp | LAndExp '&&' EqExp 
```

修改为非左递归形式，如下

```
LAndExp
    : EqExp (AND EqExp)*
    ;
```

这依然维持着优先级的构造顺序，优先级由高到低排列

- 一元运算符：`!, +, -`
- 乘除运算符：`*, /, %`
- 加减运算符：`+,  -`
- 关系运算符：`<, <=, >, >=`
- 相等运算符：`==, !=`
- 逻辑与运算符：`&&`
- 逻辑或运算符：`||`

### 相等性表达式 EqExp

```
EqExp → RelExp | EqExp ('==' | '!=') RelExp 
```

消去左递归，获得

```
EqExp
    : RelExp (EqOp RelExp)*
    ;
    
EqOp
    : EQ
    | NEQ
    ;
```

### 关系表达式 RelExp

```
RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp 
```

消除左递归，变为

```
RelExp
    : AddExp (RelOp AddExp)*
    ; // eliminate left-recursive

RelOp
    : LT
    | GT
    | LE
    | GE
    ;
```

### 表达式 Exp

```
Exp → AddExp 
```

表达式直接为 AddExp，是一件十分神奇的事情，即使引入了浮点数计算，这里依然维持一个的对应关系。

```
Exp
    : AddExp
    ;
```

### 加减表达式 AddExp

```
AddExp → MulExp | AddExp ('+' | '−') MulExp 
```

消除左递归，修改为

```
AddExp
    : MulExp (AddOp MulExp)*
    ; // eliminate left-recursive

AddOp
    : PLUS
    | MINUS
    ;
```

### 乘除表达式 MulExp

```
MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
```

消除左递归，修改为

```
MulExp
    : UnaryExp (MulOp UnaryExp)*
    ; // eliminate left-recursive

MulOp
    : MUL
    | DIV
    | MOD
    ;
```

### 一元表达式 UnaryExp

```
UnaryExp → PrimaryExp 
		 | Ident '(' [FuncRParams] ')'
         | UnaryOp UnaryExp 

FuncRParams → Exp { ',' Exp } 
```

一元表达式并非分解的重点，因为还有一元运算符

```
UnaryExp
    : PrimaryExp
    | Callee
    | UnaryOp UnaryExp
    ;
    
Callee
    : IDENFR L_PAREN FuncRParams? R_PAREN
    ;
```

利用的是 UnaryOp 和 Callee 的 FIRST 判断，如果都不是就是 UnaryExp，这应该也是不严谨的。

### 基本表达式 PrimaryExp

```
PrimaryExp → '(' Exp ')' | LVal | Number
```

到这里，可以说开始递归了。基本表达式没有任何运算符，基准情况只有左值和字面量两种。

```
PrimaryExp
    : L_PAREN Exp R_PAREN
    | LVal
    | Number
    ;
```

先判断 Exp 和 LVal 的 FIRST，否则是 LVal，这应该会造成一定程度的不严谨。其实应该很好修，因为这仨的 FIRST 都是显然的。

### 一元运算符 UnaryOp

```
UnaryOp → '+' | '−' | '!' 
```

修改为

```
UnaryOp
    : PLUS
    | MINUS
    | NOT
    ;
```

### 左值表达式 LVal

```
LVal → Ident {'[' Exp ']'} 
```

修改为

```
LVal
    : IDENFR (L_BRACKT Exp R_BRACKT)*
    ;
```

利用中括号判断维数信息，没有歧义。

### 函数实参表 FuncRParams

```
FuncRParams → Exp { ',' Exp } 
```

修改为

```
FuncRParams
    : Exp (COMMA Exp)*
    ;
```

---



## 表达式优先级

SysY 语言和 C 语言的优先级一致，我们在递归下降的时候，会利用优先级，如下所示

| 运算符         | 体现                                                         |
| -------------- | ------------------------------------------------------------ |
| `[], ()`       | 修饰 PrimaryExp，LVal 的一部分是 `[]`，Callee 的一部分是 `()` |
| `!, -, +`      | 修饰 UnaryExp                                                |
| `*, /, %`      | 修饰 MulExp                                                  |
| `+, -`         | 修饰 AddExp                                                  |
| `<, <=, >, >=` | 修饰 RelExp                                                  |
| `==, !=`       | 修饰 EqExp                                                   |
| `&&`           | 修饰 LAndExp                                                 |
| `||`           | 修饰 LOrExp                                                  |



---



## 偏移

​	在一开始设计的时候，我打算完全按照文法构造，也就是每个非终结符都对应一个非叶子节点，但是这样太愚蠢了，所以我放弃了。也就说“我们题目的要求应该是输出具体语法树的后序遍历”这个想法是错误的。



## 错误处理

`Error` 也可以考虑作为一个节点。