grammar SysY;

/**
* 表示匹配前面的字符 0 个或多个（与 EBNF 的 {} 相同）
+ 表示前面的字符 1 个或多个
? 表示前面的字符 0 个或 1 个（与 EBNF 的 [] 相同）
**/

// 编译单元由三部分组成，分别是值声明、函数声明、main 函数
// 这三部分的顺序大概率是不能换的
compUnit
   : (funcDef | decl)+
   ;

decl
   : constDecl
   | varDecl
   ;

// 支持多个变量同时初始化
constDecl
   : CONST_KW bType constDef (COMMA constDef)* SEMICOLON
   ;

// 只有一种类型，就是 int
bType
   : INT_KW
   ;

// 常量定义，必须要求初始化，可以有数组
constDef
   : IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN constInitVal
   ;

constInitVal
   : constExp
   | (L_BRACE (constInitVal (COMMA constInitVal)*)? R_BRACE)
   ;

varDecl
   : bType varDef (COMMA varDef)* SEMICOLON
   ;

varDef
   : IDENT (L_BRACKT constExp R_BRACKT)* (ASSIGN initVal)?
   ;

initVal
   : exp
   | (L_BRACE (initVal (COMMA initVal)*)? R_BRACE)
   ;

funcDef
   : funcType IDENT L_PAREN funcFParams? R_PAREN block
   ;

funcType
   : VOID_KW
   | INT_KW
   ;

funcFParams
   : funcFParam (COMMA funcFParam)*
   ;

funcFParam
   : bType IDENT (L_BRACKT R_BRACKT (L_BRACKT exp R_BRACKT)*)?
   ;

block
   : L_BRACE blockItem* R_BRACE
   ;

blockItem
   : constDecl
   | varDecl
   | stmt
   ;

stmt
   : assignStmt
   | expStmt
   | block
   | conditionStmt
   | whileStmt
   | breakStmt
   | continueStmt
   | returnStmt
   ;

assignStmt
   : lVal ASSIGN exp SEMICOLON
   ;

expStmt
   : exp? SEMICOLON
   ;

conditionStmt
   : IF_KW L_PAREN cond R_PAREN stmt (ELSE_KW stmt)?
   ;

whileStmt
   : WHILE_KW L_PAREN cond R_PAREN stmt
   ;

breakStmt
   : BREAK_KW SEMICOLON
   ;

continueStmt
   : CONTINUE_KW SEMICOLON
   ;

returnStmt
   : RETURN_KW (exp)? SEMICOLON
   ;

exp
   : addExp
   ;

cond
   : lOrExp
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

primaryExp
   : (L_PAREN exp R_PAREN)
   | lVal
   | number
   ;

number
   : intConst
   ;

intConst
   : DECIMAL_CONST
   ;

unaryExp
   : primaryExp
   | callee
   | (unaryOp unaryExp)
   ;

callee
   : IDENT L_PAREN funcRParams? R_PAREN
   ;

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : exp (COMMA exp)*
   ;

mulExp
   : unaryExp (mulOp unaryExp)*
   ; // eliminate left-recursive

mulOp
   : MUL
   | DIV
   | MOD
   ;

addExp
   : mulExp (addOp mulExp)*
   ; // eliminate left-recursive

addOp
   : PLUS
   | MINUS
   ;

relExp
   : addExp (relOp addExp)*
   ; // eliminate left-recursive

relOp
   : LT
   | GT
   | LE
   | GE
   ;

eqExp
   : relExp (eqOp relExp)*
   ;

eqOp
   : EQ
   | NEQ
   ;

lAndExp
   : eqExp (AND eqExp)*
   ;

lOrExp
   : lAndExp (OR lAndExp)*
   ;

constExp
   : addExp
   ;

CONST_KW
   : 'const'
   ;

INT_KW
   : 'int'
   ;

VOID_KW
   : 'void'
   ;

IF_KW
   : 'if'
   ;

ELSE_KW
   : 'else'
   ;

WHILE_KW
   : 'while'
   ;

BREAK_KW
   : 'break'
   ;

CONTINUE_KW
   : 'continue'
   ;

RETURN_KW
   : 'return'
   ;

IDENT
   : [_a-zA-Z]
   | [_a-zA-Z] [_a-zA-Z0-9]+
   ;

// 整型常数
DECIMAL_CONST
    : [0-9]
    | [1-9] [0-9]+
    ;

// 格式化字符串
FormatString
    : '"' (Char)* '"'
    ;

// 字符
Char
    : FormatChar        // 格式化输出用到的字符
    | NormalChar        // 普通的字符
    ;

// 格式化字符串
FormatChar
    : '%d'
    ;

NormalChar
    : ' '   // 32 空格
    | NOT   // 33 感叹号
    | [(-~] // 40-126
    ;



PLUS
   : '+'
   ;

MINUS
   : '-'
   ;

NOT
   : '!'
   ;

MUL
   : '*'
   ;

DIV
   : '/'
   ;

MOD
   : '%'
   ;

ASSIGN
   : '='
   ;

EQ
   : '=='
   ;

NEQ
   : '!='
   ;

LT
   : '<'
   ;

GT
   : '>'
   ;

LE
   : '<='
   ;

GE
   : '>='
   ;

AND
   : '&&'
   ;

OR
   : '||'
   ;

L_PAREN
   : '('
   ;

R_PAREN
   : ')'
   ;

L_BRACE
   : '{'
   ;

R_BRACE
   : '}'
   ;

L_BRACKT
   : '['
   ;

R_BRACKT
   : ']'
   ;

COMMA
   : ','
   ;

SEMICOLON
   : ';'
   ;

DOUBLE_QUOTE
   : '"'
   ;

// 空白符
WS
   : [ \r\n\t]+ -> skip
   ;

// 单行注释
LINE_COMMENT
   : '//' ~ [\r\n]* -> skip
   ;

// 多行注释
MULTILINE_COMMENT
   : '/*' .*? '*/' -> skip
   ;