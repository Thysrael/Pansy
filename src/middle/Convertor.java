package middle;

import check.PansyException;
import middle.symbol.SymbolTable;
import parser.cst.*;

import java.util.ArrayList;

public class Convertor
{
    /**
     * 为 CST 的根节点
     */
    private final CSTNode root;
    /**
     * 用来存储语义分析中产生的错误
     */
    private final ArrayList<PansyException> errors;
    /**
     * 用来存储转换日志
     */
    private final ArrayList<String> transLog;

    private final SymbolTable symbolTable;

    public Convertor(CSTNode root)
    {
        this.root = root;
        this.errors = new ArrayList<>();
        this.transLog = new ArrayList<>();
        this.symbolTable = new SymbolTable();
    }

//    public void run()
//    {
//        try
//        {
//            transCompileUnit((RootNode) root);
//        }
//        catch (Exception e)
//        {
//            System.err.println("Convert exception happened.");
//            for (String log : transLog)
//            {
//                System.err.println(log);
//            }
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
//
//    private void addLog(Class<? extends CSTNode> procedure)
//    {
//        this.transLog.add("[" + procedure + "]");
//    }
//
//    /**
//     * CompUnit
//     *     : (FuncDef | Decl)+
//     *     ;
//     */
//    private void transCompileUnit(RootNode ctx)
//    {
//        addLog(ctx.getClass());
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof DeclNode)
//            {
//                transDecl((DeclNode) child);
//            }
//            else if (child instanceof FuncDefNode)
//            {
//                transFuncDef((FuncDefNode) child);
//            }
//            else if (child instanceof MainFuncDefNode)
//            {
//                transMainFuncDef((MainFuncDefNode) child);
//            }
//        }
//    }
//
//    /**
//     * Decl
//     *     : ConstDecl
//     *     | VarDecl
//     *     ;
//     */
//    private void transDecl(DeclNode ctx)
//    {
//        addLog(ctx.getClass());
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof ConstDeclNode)
//            {
//                transConstDecl((ConstDeclNode) child);
//            }
//            else if (child instanceof VarDeclNode)
//            {
//                transVarDecl((VarDeclNode) child);
//            }
//        }
//    }
//
//    /**
//     * ConstDecl
//     *     : CONST_KW BType ConstDef (COMMA ConstDef)* SEMICOLON
//     *     ;
//     */
//    private void transConstDecl(ConstDeclNode ctx)
//    {
//        addLog(ctx.getClass());
//        errors.addAll(ctx.check());
//
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof ConstDefNode)
//            {
//                transConstDef((ConstDefNode) child);
//            }
//        }
//    }
//
//    /**
//     * ConstDef
//     *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* ASSIGN ConstInitVal
//     *     ;
//     * 这里需要增加对于常量的定义
//     */
//    private void transConstDef(ConstDefNode ctx)
//    {
//        addLog(ctx.getClass());
//        errors.addAll(ctx.check(symbolTable));
//        symbolTable.addConst(ctx);
//
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof ConstExpNode)
//            {
//                transConstExp((ConstExpNode) child);
//            }
//            else if (child instanceof ConstInitValNode)
//            {
//                transConstInitVal((ConstInitValNode) child);
//            }
//        }
//    }
//
//    /**
//     * ConstInitVal
//     *     : ConstExp
//     *     | L_BRACE (ConstInitVal (COMMA ConstInitVal)*)? R_BRACE
//     *     ;
//     */
//    private void transConstInitVal(ConstInitValNode ctx)
//    {
//        addLog(ctx.getClass());
//
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof ConstExpNode)
//            {
//                transConstExp((ConstExpNode) child);
//            }
//            else if (child instanceof ConstInitValNode)
//            {
//                transConstInitVal((ConstInitValNode) child);
//            }
//        }
//    }
//
//    /**
//     * VarDecl
//     *     : BType VarDef (COMMA VarDef)* SEMICOLON
//     *     ;
//     */
//    private void transVarDecl(VarDeclNode ctx)
//    {
//        addLog(ctx.getClass());
//        errors.addAll(ctx.check());
//
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof VarDefNode)
//            {
//                transVarDef((VarDefNode) child);
//            }
//        }
//    }
//
//    /**
//     * VarDef
//     *     : IDENFR (L_BRACKT ConstExp R_BRACKT)* (ASSIGN InitVal)?
//     *     ;
//     */
//    private void transVarDef(VarDefNode ctx)
//    {
//        addLog(ctx.getClass());
//        errors.addAll(ctx.check(symbolTable));
//        symbolTable.addVar(ctx);
//
//        for (CSTNode child : ctx.getChildren())
//        {
//            if (child instanceof ConstExpNode)
//            {
//                transConstExp((ConstExpNode) child);
//            }
//            else if (child instanceof InitValNode)
//            {
//                transInitVal((InitValNode) child);
//            }
//        }
//    }
}
