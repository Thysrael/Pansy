package back.instruction;


import static ir.values.instructions.Icmp.*;

public enum ObjCondType
{
    ANY,
    EQ,
    NE,
    GE,
    GT,
    LE,
    LT;

    public static ObjCondType genCond(Condition irCondition)
    {
        switch (irCondition)
        {
            case EQ :
            {
                return ObjCondType.EQ;
            }
            case LE :
            {
                return ObjCondType.LE;
            }
            case LT :
            {
                return ObjCondType.LT;
            }
            case GE :
            {
                return ObjCondType.GE;
            }
            case GT :
            {
                return ObjCondType.GT;
            }
            case NE :
            {
                return ObjCondType.NE;
            }
            default :
            {
                return ObjCondType.ANY;
            }

        }
    }

    public static ObjCondType getEqualOppCond(ObjCondType type)
    {
        switch (type)
        {
            case EQ :
            {
                return ObjCondType.EQ;
            }
            case LE :
            {
                return ObjCondType.GE;
            }
            case LT :
            {
                return ObjCondType.GT;
            }
            case GE :
            {
                return ObjCondType.LE;
            }
            case GT :
            {
                return ObjCondType.LT;
            }
            case NE :
            {
                return ObjCondType.NE;
            }
            default :
            {
                return ObjCondType.ANY;
            }
        }
    }

    public static ObjCondType getOppCond(ObjCondType type)
    {
        switch (type)
        {
            case EQ :
            {
                return NE;
            }
            case LE :
            {
                return GT;
            }
            case LT :
            {
                return GE;
            }
            case GE :
            {
                return LT;
            }
            case GT :
            {
                return LE;
            }
            case NE :
            {
                return EQ;
            }
            default :
            {
                assert false;
                return ObjCondType.ANY;
            }
        }
    }

    public boolean compare(int op1, int op2)
    {
        switch (this)
        {
            case EQ :
            {
                return op1 == op2;
            }
            case NE :
            {
                return op1 != op2;
            }
            case GE :
            {
                return op1 >= op2;
            }
            case GT:
            {
                return op1 > op2;
            }
            case LE:
            {
                return op1 <= op2;
            }
            case LT:
            {
                return op1 < op2;
            }
            default:
            {
                return false;
            }
        }
    }

    @Override
    public String toString()
    {
        switch (this)
        {
            case EQ :
            {
                return "eq";
            }
            case NE :
            {
                return "ne";
            }
            case GE :
            {
                return "ge";
            }
            case GT :
            {
                return "gt";
            }
            case LE :
            {
                return "le";
            }
            case LT :
            {
                return "lt";
            }
            default :
            {
                return "";
            }
        }
    }
}
