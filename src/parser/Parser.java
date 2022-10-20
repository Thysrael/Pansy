package parser;

import exception.PansyException;
import lexer.token.SyntaxType;
import lexer.token.Token;
import parser.cst.*;

import java.util.ArrayList;

public class Parser
{
    private ParseSupporter supporter;
    public Parser(ArrayList<Token> tokens)
    {
        this.supporter = new ParseSupporter(tokens);
    }

    public void addParseLog(String procedure)
    {
        supporter.addParseLog(procedure);
    }

    public CSTNode run()
    {
        try
        {
            return parseCompUnit();
        }
        catch (PansyException e)
        {
            System.err.println(display());
            e.printStackTrace();
            System.err.println(e.getType());
            System.exit(1);
        }

        return null;
    }

    public String display()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (String log : supporter.getParseLog())
        {
            stringBuilder.append(log).append("\n");
        }
        return stringBuilder.toString();
    }

    public CSTNode assertToken(SyntaxType type)
    {
        try
        {
            return supporter.checkToken(type);
        }
        catch (PansyException e)
        {
            supporter.getParseErrors().add(e);
            return null;
        }
    }

    /**
     * CompUnit
     *     : (FuncDef | Decl)+
     *     ;
     * @return node
     */
    private RootNode parseCompUnit() throws PansyException
    {
        RootNode rootNode = new RootNode();

        while (!supporter.isParseEnd())
        {
           if (supporter.isDeclFirst())
           {
               rootNode.addChild(parseDecl());
           }
           else if (supporter.isFuncDef())
           {
               rootNode.addChild(parseFuncDef());
           }
           else if (supporter.isMainFuncDef())
           {
               rootNode.addChild(parseMainFuncDef());
           }
        }
        addParseLog("CompUnit");
        return rootNode;
    }

    /**
     * Decl
     *     : ConstDecl
     *     | VarDecl
     *     ;
     * @return decl
     */
    private CSTNode parseDecl() throws PansyException
    {
        CSTNode declNode = new DeclNode();
        // 常量
        if (supporter.isConstDecl())
        {
            declNode.addChild(parseConstDecl());
        }
        else
        {
            declNode.addChild(parseVarDecl());
        }

        return declNode;
    }

    /**
     * ConstDecl
     *     : CONST_KW BType ConstDef (COMMA ConstDef)* SEMICOLON
     *     ;
     * @return 声明节点
     */
    private ConstDeclNode parseConstDecl() throws PansyException
    {
        ConstDeclNode constDeclNode = new ConstDeclNode();

        constDeclNode.addChild(supporter.checkToken(SyntaxType.CONSTTK));
        constDeclNode.addChild(parseBType());
        constDeclNode.addChild(parseConstDef());
        while (!supporter.isParseEnd() && supporter.isComma())
        {
            constDeclNode.addChild(supporter.checkToken(SyntaxType.COMMA));
            constDeclNode.addChild(parseConstDef());
        }
        constDeclNode.addChild(assertToken(SyntaxType.SEMICN));

        addParseLog("ConstDecl");
        return constDeclNode;
    }

    /**
     * BType
     *     : INT_KW
     *     ;
     * @return jj
     * @throws PansyException jj
     */
    private BTypeNode parseBType() throws PansyException
    {
        BTypeNode bTypeNode = new BTypeNode();
        bTypeNode.addChild(supporter.checkToken(SyntaxType.INTTK));

        return bTypeNode;
    }

    /**
     * ConstDef
     *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
     *     ;
     * @return jj
     */
    private ConstDefNode parseConstDef() throws PansyException
    {
        ConstDefNode constDefNode = new ConstDefNode();
        constDefNode.addChild(supporter.checkToken(SyntaxType.IDENFR));
        while (supporter.isLBrackt())
        {
            constDefNode.addChild(supporter.checkToken(SyntaxType.LBRACK));
            constDefNode.addChild(parseConstExp());
            constDefNode.addChild(assertToken(SyntaxType.RBRACK));
        }
        constDefNode.addChild(supporter.checkToken(SyntaxType.ASSIGN));
        constDefNode.addChild(parseConstInitVal());

        addParseLog("ConstDef");
        return constDefNode;
    }

    /**
     * ConstInitVal
     *     : ConstExp
     *     | L_BRACE (ConstInitVal (COMMA ConstInitVal)*)? R_BRACE
     *     ;
     * @return jj
     */
    private ConstInitValNode parseConstInitVal() throws PansyException
    {
        ConstInitValNode constInitValNode = new ConstInitValNode();
        if (supporter.isLBrace())
        {
            constInitValNode.addChild(supporter.checkToken(SyntaxType.LBRACE));
            if (!supporter.lookAhead(0).isSameType(SyntaxType.RBRACE))
            {
                constInitValNode.addChild(parseConstInitVal());
                while (supporter.isComma())
                {
                    constInitValNode.addChild(supporter.checkToken(SyntaxType.COMMA));
                    constInitValNode.addChild(parseConstInitVal());
                }
            }
            constInitValNode.addChild(supporter.checkToken(SyntaxType.RBRACE));
        }
        else
        {
            constInitValNode.addChild(parseConstExp());
        }

        addParseLog("ConstInitVal");
        return constInitValNode;
    }

    /**
     * ConstExp
     *     : AddExp
     *     ;
     * @return jj
     */
    private ConstExpNode parseConstExp() throws PansyException
    {
        ConstExpNode constExpNode = new ConstExpNode();
        constExpNode.addChild(parseAddExp());

        addParseLog("ConstExp");
        return constExpNode;
    }

    /**
     * VarDecl
     *     : BType VarDef (COMMA VarDef)* SEMICOLON
     *     ;
     * @return jj
     */
    private VarDeclNode parseVarDecl() throws PansyException
    {
        VarDeclNode varDeclNode = new VarDeclNode();
        varDeclNode.addChild(parseBType());
        varDeclNode.addChild(parseVarDef());

        while(supporter.isComma())
        {
            varDeclNode.addChild(supporter.checkToken(SyntaxType.COMMA));
            varDeclNode.addChild(parseVarDef());
        }

        varDeclNode.addChild(assertToken(SyntaxType.SEMICN));

        addParseLog("VarDecl");
        return varDeclNode;
    }

    /**
     * VarDef
     *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
     *     ;
     * @return jj
     * @throws PansyException jj
     */
    private VarDefNode parseVarDef() throws PansyException
    {
        VarDefNode varDefNode = new VarDefNode();
        varDefNode.addChild(supporter.checkToken(SyntaxType.IDENFR));

        while (supporter.isLBrackt())
        {
            varDefNode.addChild(supporter.checkToken(SyntaxType.LBRACK));
            varDefNode.addChild(parseConstExp());
            varDefNode.addChild(assertToken(SyntaxType.RBRACK));
        }

        if (supporter.isAssign())
        {
            varDefNode.addChild(supporter.checkToken(SyntaxType.ASSIGN));
            varDefNode.addChild(parseInitVal());
        }

        addParseLog("VarDef");
        return varDefNode;
    }

    /**
     * InitVal
     *     : Exp
     *     | L_BRACE (InitVal (COMMA InitVal)*)? R_BRACE
     *     ;
     * @return jj
     * @throws PansyException jj
     */
    private InitValNode parseInitVal() throws PansyException
    {
        InitValNode initValNode = new InitValNode();

        if (supporter.isLBrace())
        {
            initValNode.addChild(supporter.checkToken(SyntaxType.LBRACE));
            if (!supporter.lookAhead(0).isSameType(SyntaxType.RBRACE))
            {
                initValNode.addChild(parseInitVal());

                while (supporter.isComma())
                {
                    initValNode.addChild(supporter.checkToken(SyntaxType.COMMA));
                    initValNode.addChild(parseInitVal());
                }

                initValNode.addChild(supporter.checkToken(SyntaxType.RBRACE));
            }
        }
        else
        {
            initValNode.addChild(parseExp());
        }

        addParseLog("InitVal");
        return initValNode;
    }

    /**
     * FuncDef
     *     : FuncType IDENFR L_PAREN FuncFParams? R_PAREN Block
     *     ;
     * @return jj
     */
    private FuncDefNode parseFuncDef() throws PansyException
    {
        FuncDefNode funcDefNode = new FuncDefNode();

        funcDefNode.addChild(parseFuncType());
        funcDefNode.addChild(supporter.checkToken(SyntaxType.IDENFR));
        funcDefNode.addChild(supporter.checkToken(SyntaxType.LPARENT));

        if (supporter.isFuncFParamsFirst())
        {
            funcDefNode.addChild(parseFuncFParams());
        }

        funcDefNode.addChild(assertToken(SyntaxType.RPARENT));
        funcDefNode.addChild(parseBlock());

        addParseLog("FuncDef");
        return funcDefNode;
    }

    /**
     * FuncType
     *     : VOID_KW
     *     | INT_KW
     *     ;
     * @return jj
     */
    public FuncTypeNode parseFuncType() throws PansyException
    {
        FuncTypeNode funcTypeNode = new FuncTypeNode();
        if (supporter.isIntTk())
        {
            funcTypeNode.addChild(supporter.checkToken(SyntaxType.INTTK));
        }
        else if (supporter.isVoidTk())
        {
            funcTypeNode.addChild(supporter.checkToken(SyntaxType.VOIDTK));
        }

        addParseLog("FuncType");
        return funcTypeNode;
    }

    /**
     * FuncFParams
     *     : FuncFParam (COMMA FuncFParam)*
     *     ;
     * @return jj
     */
    private FuncFParamsNode parseFuncFParams() throws PansyException
    {
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode();
        funcFParamsNode.addChild(parseFuncFParam());

        while (supporter.isComma())
        {
            funcFParamsNode.addChild(supporter.checkToken(SyntaxType.COMMA));
            funcFParamsNode.addChild(parseFuncFParam());
        }

        addParseLog("FuncFParams");
        return funcFParamsNode;
    }

    /**
     * FuncFParam
     *     : BType IDENFR (L_BRACKT R_BRACKT (L_BRACKT ConstExp R_BRACKT)*)?
     *     ;
     * @return jj
     */
    private FuncFParamNode parseFuncFParam() throws PansyException
    {
        FuncFParamNode funcFParamNode = new FuncFParamNode();
        funcFParamNode.addChild(parseBType());
        funcFParamNode.addChild(supporter.checkToken(SyntaxType.IDENFR));

        if (supporter.isLBrackt())
        {
            funcFParamNode.addChild(supporter.checkToken(SyntaxType.LBRACK));
            funcFParamNode.addChild(assertToken(SyntaxType.RBRACK));

            while (supporter.isLBrackt())
            {
                funcFParamNode.addChild(supporter.checkToken(SyntaxType.LBRACK));
                funcFParamNode.addChild(parseConstExp());
                funcFParamNode.addChild(assertToken(SyntaxType.RBRACK));
            }
        }

        addParseLog("FuncFParam");
        return funcFParamNode;
    }

    /**
     * Block
     *     : L_BRACE BlockItem* R_BRACE
     *     ;
     * @return jj
     */
    private BlockNode parseBlock() throws PansyException
    {
        BlockNode blockNode = new BlockNode();

        blockNode.addChild(supporter.checkToken(SyntaxType.LBRACE));

        while (!supporter.lookAhead(0).isSameType(SyntaxType.RBRACE))
        {
            blockNode.addChild(parseBlockItem());
        }

        blockNode.addChild(supporter.checkToken(SyntaxType.RBRACE));

        addParseLog("Block");
        return blockNode;
    }

    /**
     * BlockItem
     *     : Decl
     *     | Stmt
     *     ;
     * @return jj
     */
    private BlockItemNode parseBlockItem() throws PansyException
    {
        BlockItemNode blockItemNode = new BlockItemNode();

        if (supporter.isDeclFirst())
        {
            blockItemNode.addChild(parseDecl());
        }
        else
        {
            blockItemNode.addChild(parseStmt());
        }

        return blockItemNode;
    }

    /**
     * Stmt
     *     : AssignStmt
     *     | ExpStmt
     *     | Block
     *     | ConditionStmt
     *     | WhileStmt
     *     | BreakStmt
     *     | ContinueStmt
     *     | ReturnStmt
     *     | InStmt
     *     | OutStmt
     *     ;
     * @return jj
     */
    private StmtNode parseStmt() throws PansyException
    {
        StmtNode stmtNode = new StmtNode();

        if (supporter.isLBrace())
        {
            stmtNode.addChild(parseBlock());
        }
        else if (supporter.isIfTK())
        {
            stmtNode.addChild(parseConditionStmt());
        }
        else if (supporter.isWhileTk())
        {
            stmtNode.addChild(parseWhileStmt());
        }
        else if (supporter.isBreakTk())
        {
            stmtNode.addChild(parseBreakStmt());
        }
        else if (supporter.isContinueTk())
        {
            stmtNode.addChild(parseContinueStmt());
        }
        else if (supporter.isReturnTk())
        {
            stmtNode.addChild(parseReturnStmt());
        }
        else if (supporter.isPrintfTk())
        {
            stmtNode.addChild(parseOutStmt());
        }
        else
        {
            ParseSupporter oldSupporter = this.supporter;
            this.supporter = new ParseSupporter(oldSupporter);
            try
            {
                parseLVal();
                supporter.checkToken(SyntaxType.ASSIGN);

                if (supporter.lookAhead(0).isSameType(SyntaxType.GETINTTK))
                {
                    this.supporter = oldSupporter;
                    stmtNode.addChild(parseInStmt());
                }
                else
                {
                    this.supporter = oldSupporter;
                    stmtNode.addChild(parseAssignStmt());
                }
            }
            catch (PansyException e)
            {
                this.supporter = oldSupporter;
                stmtNode.addChild(parseExpStmt());
            }
        }

        addParseLog("Stmt");
        return stmtNode;
    }

    /**
     * AssignStmt
     *     : LVal ASSIGN Exp SEMICOLON
     *     ;
     * @return jj
     */
    private AssignStmtNode parseAssignStmt() throws PansyException
    {
        AssignStmtNode assignStmtNode = new AssignStmtNode();

        assignStmtNode.addChild(parseLVal());
        assignStmtNode.addChild(supporter.checkToken(SyntaxType.ASSIGN));
        assignStmtNode.addChild(parseExp());
        assignStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return assignStmtNode;
    }

    /**
     * ExpStmt
     *     : Exp? SEMICOLON
     *     ;
     * @return jj
     */
    private ExpStmtNode parseExpStmt() throws PansyException
    {
        ExpStmtNode expStmtNode = new ExpStmtNode();

        if (!supporter.isSemicolon())
        {
            expStmtNode.addChild(parseExp());
        }

        expStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return expStmtNode;
    }

    /**
     * InStmt
     * 	    : LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
     *     ;
     * @return jj
     */
    private InStmtNode parseInStmt() throws PansyException
    {
        InStmtNode inStmtNode = new InStmtNode();

        inStmtNode.addChild(parseLVal());
        inStmtNode.addChild(supporter.checkToken(SyntaxType.ASSIGN));
        inStmtNode.addChild(supporter.checkToken(SyntaxType.GETINTTK));
        inStmtNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
        inStmtNode.addChild(assertToken(SyntaxType.RPARENT));
        inStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return inStmtNode;
    }

    /**
     * ConditionStmt
     *     : IF_KW L_PAREN Cond R_PAREN Stmt (ELSE_KW Stmt)?
     *     ;
     * @return jj
     */
    private ConditionStmtNode parseConditionStmt() throws PansyException
    {
        ConditionStmtNode conditionStmtNode = new ConditionStmtNode();

        conditionStmtNode.addChild(supporter.checkToken(SyntaxType.IFTK));
        conditionStmtNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
        conditionStmtNode.addChild(parseCond());
        conditionStmtNode.addChild(assertToken(SyntaxType.RPARENT));
        conditionStmtNode.addChild(parseStmt());

        if (supporter.isElseTk())
        {
            conditionStmtNode.addChild(supporter.checkToken(SyntaxType.ELSETK));
            conditionStmtNode.addChild(parseStmt());
        }

        return conditionStmtNode;
    }

    /**
     * WhileStmt
     *     : WHILE_KW L_PAREN Cond R_PAREN Stmt
     *     ;
     * @return jj
     */
    private WhileStmtNode parseWhileStmt() throws PansyException
    {
        WhileStmtNode whileStmtNode = new WhileStmtNode();

        whileStmtNode.addChild(supporter.checkToken(SyntaxType.WHILETK));
        whileStmtNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
        whileStmtNode.addChild(parseCond());
        whileStmtNode.addChild(assertToken(SyntaxType.RPARENT));
        whileStmtNode.addChild(parseStmt());

        return whileStmtNode;
    }

    /**
     * BreakStmt
     *     : BREAK_KW SEMICOLON
     *     ;
     * @return jj
     */
    private BreakStmtNode parseBreakStmt() throws PansyException
    {
        BreakStmtNode breakStmtNode = new BreakStmtNode();

        breakStmtNode.addChild(supporter.checkToken(SyntaxType.BREAKTK));
        breakStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return breakStmtNode;
    }

    /**
     * ContinueStmt
     *     : CONTINUE_KW SEMICOLON
     *     ;
     * @return jj
     */
    private ContinueStmtNode parseContinueStmt() throws PansyException
    {
        ContinueStmtNode continueStmtNode = new ContinueStmtNode();

        continueStmtNode.addChild(supporter.checkToken(SyntaxType.CONTINUETK));
        continueStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return continueStmtNode;
    }

    /**
     * ReturnStmt
     *     : RETURN_KW (Exp)? SEMICOLON
     *     ;
     * @return jj
     */
    private ReturnStmtNode parseReturnStmt() throws PansyException
    {
        ReturnStmtNode returnStmtNode = new ReturnStmtNode();

        returnStmtNode.addChild(supporter.checkToken(SyntaxType.RETURNTK));

        ParseSupporter oldSupporter = this.supporter;
        try
        {
            this.supporter = new ParseSupporter(oldSupporter);
            parseExp();
            this.supporter = oldSupporter;
            returnStmtNode.addChild(parseExp());
        }
        catch (PansyException ignored)
        {}

        returnStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return returnStmtNode;
    }

    /**
     * OutStmt
     *     : PRINTFTK L_PAREN FormatString (COMMA Exp)* R_PAREN SEMICN
     *     ;
     * @return jj
     */
    private OutStmtNode parseOutStmt() throws PansyException
    {
        OutStmtNode outStmtNode = new OutStmtNode();

        outStmtNode.addChild(supporter.checkToken(SyntaxType.PRINTFTK));
        outStmtNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
        outStmtNode.addChild(supporter.checkToken(SyntaxType.STRCON));

        while (supporter.isComma())
        {
            outStmtNode.addChild(supporter.checkToken(SyntaxType.COMMA));
            outStmtNode.addChild(parseExp());
        }

        outStmtNode.addChild(assertToken(SyntaxType.RPARENT));
        outStmtNode.addChild(assertToken(SyntaxType.SEMICN));

        return outStmtNode;
    }

    /**
     * Exp
     *     : AddExp
     *     ;
     * @return jj
     */
    private ExpNode parseExp() throws PansyException
    {
        ExpNode expNode = new ExpNode();

        expNode.addChild(parseAddExp());

        addParseLog("Exp");
        return expNode;
    }

    /**
     * Cond
     *     : LOrExp
     *     ;
     * @return jj
     */
    private CondNode parseCond() throws PansyException
    {
        CondNode condNode = new CondNode();

        condNode.addChild(parseLOrExp());

        addParseLog("Cond");
        return condNode;
    }

    /**
     * LVal
     *     : IDENFR (L_BRACKT Exp R_BRACKT)*
     *     ;
     * @return jj
     */
    private LValNode parseLVal() throws PansyException
    {
        LValNode lValNode = new LValNode();

        lValNode.addChild(supporter.checkToken(SyntaxType.IDENFR));

        while (supporter.isLBrackt())
        {
            lValNode.addChild(supporter.checkToken(SyntaxType.LBRACK));
            lValNode.addChild(parseExp());
            lValNode.addChild(assertToken(SyntaxType.RBRACK));
        }

        addParseLog("LVal");
        return lValNode;
    }

    /**
     * LOrExp
     *     : LAndExp (OR LAndExp)*
     *     ;
     * @return jj
     */
    private LOrExpNode parseLOrExp() throws PansyException
    {
        LOrExpNode lOrExpNode = new LOrExpNode();
        lOrExpNode.addChild(parseLAndExp());
        addParseLog("LOrExp");

        while (supporter.lookAhead(0).isSameType(SyntaxType.OR))
        {
            lOrExpNode.addChild(supporter.checkToken(SyntaxType.OR));
            lOrExpNode.addChild(parseLAndExp());
            addParseLog("LOrExp");
        }

        return lOrExpNode;
    }

    /**
     * LAndExp
     *     : EqExp (AND EqExp)*
     *     ;
     * @return jj
     */
    private LAndExpNode parseLAndExp() throws PansyException
    {
        LAndExpNode lAndExpNode = new LAndExpNode();

        lAndExpNode.addChild(parseEqExp());
        addParseLog("LAndExp");
        while (supporter.lookAhead(0).isSameType(SyntaxType.AND))
        {
            lAndExpNode.addChild(supporter.checkToken(SyntaxType.AND));
            lAndExpNode.addChild(parseEqExp());
            addParseLog("LAndExp");
        }

        return lAndExpNode;
    }

    /**
     * EqExp
     *     : RelExp (EqOp RelExp)*
     *     ;
     *  EqOp
     *     : EQ
     *     | NEQ
     *     ;
     * @return jj
     */
    private EqExpNode parseEqExp() throws PansyException
    {
        EqExpNode eqExpNode = new EqExpNode();

        eqExpNode.addChild(parseRelExp());
        addParseLog("EqExp");

        while (supporter.lookAhead(0).isSameType(SyntaxType.EQL) ||
                supporter.lookAhead(0).isSameType(SyntaxType.NEQ))
        {
            eqExpNode.addChild(supporter.checkToken(supporter.lookAhead(0).getType()));
            eqExpNode.addChild(parseRelExp());
            addParseLog("EqExp");
        }

        return eqExpNode;
    }

    /**
     * RelExp
     *     : AddExp (RelOp AddExp)*
     *     ; // eliminate left-recursive
     * RelOp
     *     : LT
     *     | GT
     *     | LE
     *     | GE
     *     ;
     * @return jj
     */
    private RelExpNode parseRelExp() throws PansyException
    {
        RelExpNode relExpNode = new RelExpNode();

        relExpNode.addChild(parseAddExp());
        addParseLog("RelExp");

        while (supporter.isRelOp())
        {
            relExpNode.addChild(supporter.checkToken(supporter.lookAhead(0).getType()));
            relExpNode.addChild(parseAddExp());
            addParseLog("RelExp");
        }

        return relExpNode;
    }

    /**
     * AddExp
     *     : MulExp (AddOp MulExp)*
     *     ; // eliminate left-recursive
     * AddOp
     *     : PLUS
     *     | MINUS
     *     ;
     * @return jj
     * @throws PansyException jj
     */
    private AddExpNode parseAddExp() throws PansyException
    {
        AddExpNode addExpNode = new AddExpNode();
        addExpNode.addChild(parseMulExp());
        addParseLog("AddExp");

        while (supporter.isAddOp())
        {
            addExpNode.addChild(supporter.checkToken(supporter.lookAhead(0).getType()));
            addExpNode.addChild(parseMulExp());
            addParseLog("AddExp");
        }

        return addExpNode;
    }

    /**
     * MulExp
     *     : UnaryExp (MulOp UnaryExp)*
     *     ; // eliminate left-recursive
     * MulOp
     *     : MUL
     *     | DIV
     *     | MOD
     *     ;
     * @return jj
     */
    private MulExpNode parseMulExp() throws PansyException
    {
        MulExpNode mulExpNode = new MulExpNode();
        mulExpNode.addChild(parseUnaryExp());
        addParseLog("MulExp");

        while (supporter.isMulOp())
        {
            mulExpNode.addChild(supporter.checkToken(supporter.lookAhead(0).getType()));
            mulExpNode.addChild(parseUnaryExp());
            addParseLog("MulExp");
        }

        return mulExpNode;
    }

    /**
     * UnaryExp
     *     : PrimaryExp
     *     | Callee
     *     | UnaryOp UnaryExp
     *     ;
     * @return jj
     */
    private UnaryExpNode parseUnaryExp() throws PansyException
    {
        UnaryExpNode unaryExpNode = new UnaryExpNode();

        if (supporter.isUnaryOp())
        {
            unaryExpNode.addChild(parseUnaryOp());
            unaryExpNode.addChild(parseUnaryExp());
        }
        else if (supporter.isCallee())
        {
            unaryExpNode.addChild(parseCallee());
        }
        else
        {
            unaryExpNode.addChild(parsePrimaryExp());
        }

        addParseLog("UnaryExp");
        return unaryExpNode;
    }

    /**
     * UnaryOp
     *     : PLUS
     *     | MINUS
     *     | NOT
     *     ;
     */
    private UnaryOpNode parseUnaryOp() throws PansyException
    {
        UnaryOpNode unaryOpNode = new UnaryOpNode();
        unaryOpNode.addChild(supporter.checkToken(supporter.lookAhead(0).getType()));

        addParseLog("UnaryOp");
        return unaryOpNode;
    }

    /**
     * Callee
     *     : IDENFR L_PAREN FuncRParams? R_PAREN
     *     ;
     * @return jj
     */
    private CalleeNode parseCallee() throws PansyException
    {
        CalleeNode calleeNode = new CalleeNode();

        calleeNode.addChild(supporter.checkToken(SyntaxType.IDENFR));
        calleeNode.addChild(supporter.checkToken(SyntaxType.LPARENT));

        ParseSupporter oldSupporter = this.supporter;
        try
        {
            // try
            this.supporter = new ParseSupporter(oldSupporter);
            parseExp();
            // no exception, continue
            this.supporter = oldSupporter;
            calleeNode.addChild(parseFuncRParams());
        }
        catch (PansyException ignored)
        {}

        calleeNode.addChild(assertToken(SyntaxType.RPARENT));

        return calleeNode;
    }

    /**
     * FuncRParams
     *     : Exp (COMMA Exp)*
     *     ;
     * @return jj
     */
    private FuncRParamsNode parseFuncRParams() throws PansyException
    {
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode();

        funcRParamsNode.addChild(parseExp());

        while (supporter.isComma())
        {
            funcRParamsNode.addChild(supporter.checkToken(SyntaxType.COMMA));
            funcRParamsNode.addChild(parseExp());
        }

        addParseLog("FuncRParams");
        return funcRParamsNode;
    }

    /**
     * PrimaryExp
     *     : L_PAREN Exp R_PAREN
     *     | LVal
     *     | Number
     *     ;
     * @return jj
     */
    private PrimaryExpNode parsePrimaryExp() throws PansyException
    {
        PrimaryExpNode primaryExpNode = new PrimaryExpNode();

        if (supporter.isLParent())
        {
            primaryExpNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
            primaryExpNode.addChild(parseExp());
            primaryExpNode.addChild(assertToken(SyntaxType.RPARENT));
        }
        else if (supporter.isIdentifier())
        {
            primaryExpNode.addChild(parseLVal());
        }
        else
        {
            primaryExpNode.addChild(parseNumber());
        }

        addParseLog("PrimaryExp");
        return primaryExpNode;
    }

    private NumberNode parseNumber() throws PansyException
    {
        NumberNode numberNode = new NumberNode();

        numberNode.addChild(supporter.checkToken(SyntaxType.INTCON));

        addParseLog("Number");
        return numberNode;
    }

    private MainFuncDefNode parseMainFuncDef() throws PansyException
    {
        MainFuncDefNode mainFuncDefNode = new MainFuncDefNode();

        mainFuncDefNode.addChild(supporter.checkToken(SyntaxType.INTTK));
        mainFuncDefNode.addChild(supporter.checkToken(SyntaxType.MAINTK));
        mainFuncDefNode.addChild(supporter.checkToken(SyntaxType.LPARENT));
        mainFuncDefNode.addChild(assertToken(SyntaxType.RPARENT));
        mainFuncDefNode.addChild(parseBlock());

        addParseLog("MainFuncDef");
        return mainFuncDefNode;
    }
}
