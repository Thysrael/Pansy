# Pansy Parser

这里是 Pansy 编译器的 parser

## 具体语法树

​	`Parser` 的目的是为了根据语法获得一个**具体语法树**（Concrete Syntax Tree，CST）。这棵语法树的非叶子节点是各个语法成分，而叶子节点则是 `Token` （或者说包含 `Token`）。强调这个是因为我没有意识到可以将 `Token`  与其他语法成分等量齐观。

​	在文法中，我们约定非叶子节点采用首字母大写的驼峰命名法，比如 `CompUnit`，而对于叶子节点，我们采用全大写的形式，比如 `IDENFR` 。

---



## 改写语法

手搓了一份 SysY 的文法，采用正则形式，写在了 `SysY.g4` 文件中。相比于课程组给出的文法，消除了左递归，并且严谨了表述。而且拓展了一部分十分不优雅的语法。

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

可以看到，常量声明可以用逗号分割，同时声明多个。

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

### 常量初值 ConstInitVal

常量初值可以是一个常量表达式，也可以是一个数组初值

```
ConstInitVal → ConstExp
			 | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
```

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

基本上没变。

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

这里的函数定义包括主函数 `main`。

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

这里似乎过于复杂了，所以尝试引入多个来缓解。不过需要注意，即使引入了多个，其实本质还是一个语句，所以语法成分不会增多。

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

#### 赋值语句 AssignStmt

```
AssignStmt
    : LVal ASSIGN Exp SEMICOLON
    ;
```

#### 表达式语句 ExpStmt

```
ExpStmt
    : Exp? SEMICOLON
    ;
```

​	可以发现 `AssignStmt` 和 `ExpStmt` 有很大的重复部分，比如 `a[1][1];` 和 `a[1][1] = 1;` 就是两个语句，但是除非解析到 `=` ，否则根本分不出来到底是啥，所以可能需要前瞻很多东西。

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