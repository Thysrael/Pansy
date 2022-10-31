package driver;

import check.Checker;
import ir.IrBuilder;
import ir.values.Module;
import lexer.Lexer;
import lexer.token.Token;
import parser.Parser;
import parser.cst.CSTNode;
import pass.PassManager;
import util.MyIO;

import java.util.ArrayList;

public class Driver
{
    /**
     * 用于解析命令行参数
     * @param args 命令行参数
     */
    private void parseArgs(String[] args)
    {
        int argIndex = 0;
        while (argIndex < args.length)
        {
            if (argIndex == 0)
            {
                Config.sourceFilePath = args[argIndex];
            }
            argIndex++;
        }
    }

    public void run(String[] args)
    {
        parseArgs(args);
        String sourceCode = MyIO.readFile(Config.sourceFilePath);

        Lexer lexer = new Lexer(sourceCode);
        ArrayList<Token> tokens = lexer.run();
        lexDisplay(lexer);

        Parser parser = new Parser(tokens);
        CSTNode cstRoot = parser.run();
        parseDisplay(parser);

        Checker checker = new Checker(cstRoot);
        checker.run();
        checkDisPlay(checker);

        IrBuilder.getInstance().buildModule(cstRoot);
        PassManager passManager = new PassManager();
        passManager.run();
        IrBuildDisPlay();
    }

    private void lexDisplay(Lexer lexer)
    {
        if (Config.lexOutputToCmd)
        {
            System.out.println(lexer.display());
        }
        if (Config.lexOutputToFile)
        {
            MyIO.output(Config.targetFilePath, lexer.display());
        }
    }

    private void parseDisplay(Parser parser)
    {
        if (Config.parseOutputToCmd)
        {
            System.out.println(parser.display());
        }
        if (Config.parseOutputToFile)
        {
            MyIO.output(Config.targetFilePath, parser.display());
        }
    }

    private void checkDisPlay(Checker checker)
    {
        if (Config.checkOutputToCmd)
        {
            System.out.println(checker.display());
        }
        if (Config.checkOutputToFile)
        {
            MyIO.output(Config.errorFilePath, checker.display());
        }
    }

    private void tmpDisPlay(Parser parser, Checker checker)
    {
        System.out.println(parser.display() + checker.display());
        MyIO.output(Config.targetFilePath, parser.display() + checker.display());
    }

    private void IrBuildDisPlay()
    {
        if (Config.irBuildOutputToCmd)
        {
            System.out.println(Module.getInstance());
        }
        if (Config.irBuildOutputToFile)
        {
            MyIO.output(Config.irFilePath, Module.getInstance().toString());
        }
    }
}
