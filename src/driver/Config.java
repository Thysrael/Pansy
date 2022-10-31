package driver;

public class Config
{
    public static String sourceFilePath = "testfile.txt";
    public static String targetFilePath = "output.txt";
    public static String errorFilePath = "error.txt";
    public static String irFilePath = "llvm_ir.txt";
    public static final boolean lexOutputToCmd = false;
    public static final boolean lexOutputToFile = false;
    public static final boolean parseOutputToCmd = false;
    public static final boolean parseOutputToFile = true;
    public static final boolean checkOutputToCmd = true;
    public static final boolean checkOutputToFile = true;

    public static final boolean irBuildOutputToCmd = true;
    public static final boolean irBuildOutputToFile = true;
}
