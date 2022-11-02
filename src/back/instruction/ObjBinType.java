package back.instruction;

public enum ObjBinType
{
    DIV,
    MOD,
    MUL,
    ADD,
    SUB,
    AND,
    OR,
    XOR,
    // 乘法结果的高 32 位
    SMMUL,
    // 即 AND NOT
    BIC,

}
