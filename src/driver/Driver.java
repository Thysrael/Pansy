package driver;

import lexer.Lexer;
import util.MyIO;

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
                argIndex++;
            }
            else
            {
                argIndex++;
            }
        }
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

    public void run(String[] args)
    {
        parseArgs(args);
        String sourceCode = MyIO.readFile(Config.sourceFilePath);

        Lexer lexer = new Lexer(sourceCode);
        lexer.run();
        lexDisplay(lexer);
    }
}
