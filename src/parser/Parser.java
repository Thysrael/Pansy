package parser;

import check.PansyException;
import lexer.token.Delimiter;
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

    /**
     * 这里的逻辑是，当发生一个出乎意料的意味（意料之中的是三种符号缺失错误）
     * 那么就会打印目前的解析情况（已经正常的解析的所有符号）
     * 然后打印栈，
     * 最后打印 PansyException
     * @return 正常情况是一棵语法树
     */
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

            return null;
        }
    }

    /**
     * 其本质是展示解析的成果，应该是语法树的后序遍历结果
     * @return 一个字符串
     */
    public String display()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (String log : supporter.getParseLog())
        {
            stringBuilder.append(log).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 这个用于包装 checkToken 被包装后，checkToken 异常后不会立即抛出异常终止程序
     * 而是根据 type 确定是否要抛出程序
     * @param type token 类型
     * @return 如果正常，会返回一个节点，即使缺少了一个终结符
     * @throws PansyException 解析异常
     */
    public CSTNode assertToken(SyntaxType type) throws PansyException
    {
        try
        {
            return supporter.checkToken(type);
        }
        catch (PansyException e)
        {
            if (type.equals(SyntaxType.SEMICN))
            {
                return new TokenNode(new Delimiter(e.getLine(), ";"), true);
            }
            else if (type.equals(SyntaxType.RBRACK))
            {
                return new TokenNode(new Delimiter(e.getLine(), "]"), true);
            }
            else if (type.equals(SyntaxType.RPARENT))
            {
                return new TokenNode(new Delimiter(e.getLine(), ")"), true);
            }
            else
            {
                throw e;
            }
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
           else
           {
               System.err.println("wrong CompUnit parse");
               System.exit(1);
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
     * 这里的 else 并不会有任何影响，因为进入这个分支的，一定只有 constDecl，varDecl 两种
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
     * 利用有没有逗号判断 constDef 的个数，是严谨的
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
     * 以左中括号为判断是否存在维度信息的标准，缺少右中括号并不会干扰判断
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
     * 利用的是，是否是左大括号，如果是，那么就走下面的分支，进行数组初始化，
     * 否则，就是单值常量，这没准会造成某种意义的不严谨
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
     * 利用有没有逗号判断 varDef 的个数，是严谨的
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
     * 根据有无左括号判断是否有维度信息
     * 根据有无等于号判断时是否有初始值，是严谨的
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
     * 利用的同样是左大括号是否存在来判断分支
     * 在判断的时候还考虑了 '{}' 这种情况的出现，其实是没有必要的，因为 '{}' 是 0 维的，语义约束要求了维度不能为 0
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
     * 根据是否是 INTTK 判断是否有形参表
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
     * 根据是否有逗号确定有几个形参
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
     * 利用有无左大括号判断是否传入指针
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
     * 利用是否是右大括号判断内容是否结束，按理说是没有啥问题的
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
     * 当是声明的时候，走声明分支，否则走语句分支，这是不严谨的
     * 这建立于语句分支的 FIRST 中没有 int 和 const。
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
     * 对于大部分的语句，都是有很明显的 FIRST 可以将其与其他语句分开
     * 但是对于 AssignStmt, InStmt, ExpStmt，他们的第一个元素都是 LVal，所以这里用到了回退
     * 先尝试解析一个 LVal，然后判断其后的元素是否是 = 号（其实类似于 LAST 的思想）
     * 如果是，那么就从 AssignStmt 和 InStmt 中选择
     * 否则从 ExpStmt 中选择，这里也造成了一定的不严谨性，所有的其他语句都会流入 ExpStmt
     * 没有办法通过前瞻解决
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
        // 首先尝试对于赋值语句的解析，不行就是对于表达式语句的解析
        else
        {
            ParseSupporter oldSupporter = this.supporter;
            this.supporter = new ParseSupporter(oldSupporter);
            try
            {
                parseLVal();
                supporter.checkToken(SyntaxType.ASSIGN);
                // 如果没有发生异常，那么也要回退，这是为了解析的方便，不进行树的嫁接
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
            // 尝试失败，进行 ExpStmt 的解析
            catch (PansyException e)
            {
                // 将 supporter 恢复成原来的情况
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
     * 就很显然
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
     * 通过有没有分号来确定是否到达结尾，缺少分号不会造成问题，因为如果是空语句缺分号，那么就是空行了
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
     * 	   : LVal ASSIGN GETINTTK L_PAREN R_PAREN SEMICOLON
     *     ;
     * 语句成分确定，所以很显然
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
     * 根据有无 else 判断是否有 else 分支
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
     * 语句成分确定，所以很显然
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
     * 语句成分确定，所以很显然
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
     * 语句成分确定，所以很显然
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
     * 依然是需要尝试解析，因为缺少分号会造成“连读”现象（虽然应该被语义约束了）
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
     * 利用逗号判断参数的个数
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
     * 利用中括号判断维数信息，没有歧义。
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
     *     ;
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
     *     ;
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
     *     ;
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
     * 利用的是 UnaryOp 和 Callee 的 FIRST 判断，如果都不是就是 UnaryExp，这应该也是不严谨的
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
     * 先判断 Exp 和 LVal 的 FIRST，否则是 LVal，这应该会造成一定程度的不严谨
     * 其实应该很好修，因为这仨的 FIRST 都是显然的
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
