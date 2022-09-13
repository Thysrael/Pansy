package lexer.token;

public enum TokenType
{
    // comment
    SINGLE_COMMENT, MULTI_COMMENT,
    // reserved
    MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK, WHILETK, RETURNTK, VOIDTK,
    GETINTTK, PRINTFTK,
    // delimiter
    NOT, AND, OR, PLUS, MINU, MULT, DIV, MOD,
    LSS, LEQ, GRE, GEQ, EQL, NEQ,
    ASSIGN, SEMICN, COMMA, SPACE,
    LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
    // IntConst
    INTCON,
    // FormatString
    STRCON,
    // Identifier
    IDENFR
}
