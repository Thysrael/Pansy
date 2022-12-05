package driver;

public class Config
{
    public static String sourceFilePath = "testfile.txt";
    public static String targetFilePath = "output.txt";
    public static String errorFilePath = "error.txt";
    public static String irFilePath = "llvm_ir.txt";
    public static String mipsFilePath = "mips.txt";
    public static final boolean lexOutputToCmd = false;
    public static final boolean lexOutputToFile = false;
    public static final boolean parseOutputToCmd = false;
    public static final boolean parseOutputToFile = false;
    public static final boolean openCheck = false;
    public static final boolean checkOutputToCmd = true;
    public static final boolean checkOutputToFile = true;
    public static final boolean irBuildOutputToCmd = false;
    public static final boolean irBuildOutputToFile = true;
    public static final boolean mipsOutputToCmd = true;
    public static final boolean mipsOutputToFile = true;
    public static final boolean rawMipsOutputToCmd = false;
    public static final boolean openMem2reg = true;
    public static final boolean openURE = true;
    /**
     * 因为 GVN 会进行激进的代码移动，所以需要 GCM 进行调整
     * 所以开启 GVN 的时候一定要开启 GCM
     */
    public static final boolean openGVN = true;
    public static final boolean openGCM = true;
    public static final boolean openInlineFunction = true;
    public static final boolean openBranchOpt = true;
    public static final boolean isO1 = false;
    public static final boolean openDataPeepHole = false;
}
