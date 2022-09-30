grammar SysY;

/**
* 表示匹配前面的字符 0 个或多个（与 EBNF 的 {} 相同）
+ 表示前面的字符 1 个或多个
? 表示前面的字符 0 个或 1 个（与 EBNF 的 [] 相同）
**/

CompUnit
    : (FuncDef | Decl)+
    ;

Decl
    : ConstDecl
    | VarDecl
    ;

// 支持多个变量同时初始化
ConstDecl
    : CONST_KW BType ConstDef (COMMA ConstDef)* SEMICOLON
    ;

// 常量定义，必须要求初始化，可以有数组
ConstDef
    : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
    ;

ConstInitVal
    : ConstExp
    | L_BRACE (ConstInitVal (COMMA ConstInitVal)*)? R_BRACE
    ;

// ConstExp 的定义和 Exp 的定义一样，都是加减法连缀的表达式
ConstExp
    : AddExp
    ;

VarDecl
    : BType VarDef (COMMA VarDef)* SEMICOLON
    ;

VarDef
    : IDENFR (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
    ;

InitVal
    : Exp
    | L_BRACE (InitVal (COMMA InitVal)*)? R_BRACE
    ;

FuncDef
    : FuncType IDENFR L_PAREN FuncFParams? R_PAREN Block
    ;

FuncType
    : VOID_KW
    | INT_KW
    ;

FuncFParams
    : FuncFParam (COMMA FuncFParam)*
    ;

FuncFParam
    : BType IDENFR (L_BRACKT R_BRACKT (L_BRACKT ConstExp R_BRACKT)*)?
    ;

Block
    : L_BRACE BlockItem* R_BRACE
    ;

BlockItem
    : Decl
    | Stmt
    ;

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

AssignStmt
    : LVal ASSIGN Exp SEMICOLON
    ;

ExpStmt
    : Exp? SEMICOLON
    ;

InStmt
	: LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
    ;

ConditionStmt
    : IF_KW L_PAREN Cond R_PAREN Stmt (ELSE_KW Stmt)?
    ;

WhileStmt
    : WHILE_KW L_PAREN Cond R_PAREN Stmt
    ;

BreakStmt
    : BREAK_KW SEMICOLON
    ;

ContinueStmt
    : CONTINUE_KW SEMICOLON
    ;

ReturnStmt
    : RETURN_KW (Exp)? SEMICOLON
    ;

OutStmt
    : PRINTFTK L_PAREN FormatString (COMMA Exp)* R_PAREN SEMICOLON
    ;

Exp
    : AddExp
    ;

Cond
    : LOrExp
    ;

LVal
    : IDENFR (L_BRACKT Exp R_BRACKT)*
    ;

PrimaryExp
    : L_PAREN Exp R_PAREN
    | LVal
    | Number
    ;

Number
    : IntConst
    ;

IntConst
    : DECIMAL_CONST
    ;

UnaryExp
    : PrimaryExp
    | Callee
    | UnaryOp UnaryExp
    ;

Callee
    : IDENFR L_PAREN FuncRParams? R_PAREN
    ;

UnaryOp
    : PLUS
    | MINUS
    | NOT
    ;

FuncRParams
    : Exp (COMMA Exp)*
    ;

MulExp
    : UnaryExp (MulOp UnaryExp)*
    ; // eliminate left-recursive

MulOp
    : MUL
    | DIV
    | MOD
    ;

AddExp
    : MulExp (AddOp MulExp)*
    ; // eliminate left-recursive

AddOp
    : PLUS
    | MINUS
    ;

RelExp
    : AddExp (RelOp AddExp)*
    ; // eliminate left-recursive

RelOp
    : LT
    | GT
    | LE
    | GE
    ;

EqExp
    : RelExp (EqOp RelExp)*
    ;

EqOp
    : EQ
    | NEQ
    ;

LAndExp
    : EqExp (AND EqExp)*
    ;

LOrExp
    : LAndExp (OR LAndExp)*
    ;

BType
    : INT_KW
    ;

PRINTFTK
    : 'printf'
    ;

GETINTTK
	: 'getint'
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

IDENFR
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