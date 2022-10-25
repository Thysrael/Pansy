package check;

public enum ErrorType
{
    ILLEGAL_SYMBOL,     // a 非法符号
    REDEFINED_SYMBOL,   // b 名字重定义
    UNDEFINED_SYMBOL,   // c 未定义的名字
    ARG_NUM_MISMATCH,   // d 参数个数不匹配
    ARG_TYPE_MISMATCH,  // e 参数类型不匹配
    VOID_RETURN_VALUE,  // f 无返回值的函数存在不匹配的 return 语句
    MISS_RETURN,        // g 有返回值的函数缺少 return 语句
    CHANGE_CONST,       // h 不能改变常量的值
    MISS_SEMICOLON,     // i 缺少分号
    MISS_RPARENT,       // j 缺少右小括号
    MISS_RBRACK,        // k 缺少中括号
    FORM_STRING_MISMATCH, // l printf 中格式字符与表达式个数不匹配
    NON_LOOP_STMT,      // m 在非循环块中使用 break 和 continue 语句


    LEX_ERROR,          // 未知的 lex 错误
    PARSE_ERROR         // 未知的 parse 错误
}
