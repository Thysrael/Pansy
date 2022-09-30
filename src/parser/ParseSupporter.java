package parser;

import exception.PansyException;
import lexer.token.EOFToken;
import lexer.token.SyntaxType;
import lexer.token.Token;
import parser.cst.CSTNode;
import parser.cst.TokenNode;

import java.util.ArrayList;

import static lexer.token.SyntaxType.*;

public class ParseSupporter
{
    private final ArrayList<Token> tokens;
    private final ArrayList<String> parseLog;
    private final ArrayList<PansyException> parseErrors;
    private int cur;

    public ParseSupporter(ArrayList<Token> tokens)
    {
        this.tokens = tokens;
        this.cur = 0;
        this.parseLog = new ArrayList<>();
        this.parseErrors = new ArrayList<>();
    }

    public ParseSupporter(ParseSupporter supporter)
    {
        this.tokens = new ArrayList<>(supporter.tokens);
        this.cur = supporter.cur;
        this.parseLog = new ArrayList<>(supporter.parseLog);
        this.parseErrors = new ArrayList<>(supporter.parseErrors);
    }

    public void addParseLog(String procedure)
    {
        parseLog.add("<" + procedure + ">");
    }

    public ArrayList<String> getParseLog()
    {
        return parseLog;
    }

    public ArrayList<PansyException> getParseErrors()
    {
        return parseErrors;
    }

    public Token lookAhead(int step)
    {
        if (cur + step < tokens.size() && cur + step >= 0)
        {
            return tokens.get(cur + step);
        }
        else
        {
            return new EOFToken(0, null);
        }
    }

    public void advance(int step)
    {
        cur += step;
    }

    public boolean isParseEnd()
    {
        return cur >= tokens.size();
    }

    public boolean isDeclFirst()
    {
        return isConstDecl() || isVarDecl();
    }

    public boolean isConstDecl()
    {
        return lookAhead(0).isSameType(CONSTTK);
    }

    public boolean isVarDecl()
    {
        return lookAhead(0).isSameType(INTTK) && !lookAhead(2).isSameType(LPARENT);
    }

    public boolean isFuncDef()
    {
        return isFuncType() && lookAhead(2).isSameType(LPARENT) && !lookAhead(1).isSameType(MAINTK);
    }
    public boolean isFuncType()
    {
        return lookAhead(0).isSameType(VOIDTK) || lookAhead(0).isSameType(INTTK);
    }

    public boolean isMainFuncDef()
    {
        return lookAhead(0).isSameType(INTTK) && lookAhead(1).isSameType(MAINTK) && lookAhead(2).isSameType(LPARENT);
    }

    public boolean isComma()
    {
        return lookAhead(0).isSameType(COMMA);
    }

    public boolean isLBrackt()
    {
        return lookAhead(0).isSameType(LBRACK);
    }

    public boolean isLBrace()
    {
        return lookAhead(0).isSameType(LBRACE);
    }

    public boolean isFuncFParamsFirst()
    {
        return lookAhead(0).isSameType(INTTK);
    }

    public boolean isIntTk()
    {
        return lookAhead(0).isSameType(INTTK);
    }

    public boolean isVoidTk()
    {
        return lookAhead(0).isSameType(VOIDTK);
    }

    public boolean isAssign()
    {
        return lookAhead(0).isSameType(ASSIGN);
    }

    public boolean isIfTK()
    {
        return lookAhead(0).isSameType(IFTK);
    }

    public boolean isWhileTk()
    {
        return lookAhead(0).isSameType(WHILETK);
    }

    public boolean isBreakTk()
    {
        return lookAhead(0).isSameType(BREAKTK);
    }

    public boolean isContinueTk()
    {
        return lookAhead(0).isSameType(CONTINUETK);
    }

    public boolean isReturnTk()
    {
        return lookAhead(0).isSameType(RETURNTK);
    }

    public boolean isPrintfTk()
    {
        return lookAhead(0).isSameType(PRINTFTK);
    }

    public boolean isAddOp()
    {
        return lookAhead(0).isSameType(PLUS) || lookAhead(0).isSameType(MINU);
    }

    public boolean isLParent()
    {
        return lookAhead(0).isSameType(LPARENT);
    }

    public boolean isSemicolon()
    {
        return lookAhead(0).isSameType(SEMICN);
    }

    public boolean isElseTk()
    {
        return lookAhead(0).isSameType(ELSETK);
    }

    public boolean isMulOp()
    {
        return lookAhead(0).isSameType(MULT) ||
                lookAhead(0).isSameType(DIV) ||
                lookAhead(0).isSameType(MOD);
    }

    public boolean isIdentifier()
    {
        return lookAhead(0).isSameType(IDENFR);
    }

    public boolean isUnaryOp()
    {
        return lookAhead(0).isSameType(PLUS) ||
                lookAhead(0).isSameType(MINU) ||
                lookAhead(0).isSameType(NOT);
    }

    public boolean isCallee()
    {
        return lookAhead(0).isSameType(IDENFR) && lookAhead(1).isSameType(LPARENT);
    }

    public boolean isRelOp()
    {
        return lookAhead(0).isSameType(LEQ) || lookAhead(0).isSameType(GEQ) ||
                lookAhead(0).isSameType(LSS) || lookAhead(0).isSameType(GRE);
    }

    public CSTNode checkToken(SyntaxType type) throws PansyException
    {
        Token token = lookAhead(0);
        if (!token.isSameType(type))
        {
            throw new PansyException(type, token.getLine());
        }

        parseLog.add(token.toString());
        advance(1);
        return new TokenNode(token);
    }
}
