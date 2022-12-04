package pass;

import ir.values.Module;
import pass.analyze.BuildCFG;
import pass.analyze.DomInfo;
import pass.analyze.LoopInfoAnalysis;
import pass.analyze.SideEffectAnalysis;
import pass.refactor.*;

import java.util.ArrayList;


public class PassManager
{
    private final Module module = Module.getInstance();
    private final ArrayList<Pass> passes = new ArrayList<>();
    public void run()
    {
        passes.add(new BuildCFG());
        passes.add(new DomInfo());
        passes.add(new LoopInfoAnalysis());
        passes.add(new Mem2reg());
        // 因为 mem2reg 会减少内存访存，所以此时才进行副作用分析
        passes.add(new SideEffectAnalysis());
        // 这些 pass 都不会改变分支结构
        passes.add(new UselessRetEmit());
        passes.add(new GVN());
        passes.add(new GCM());
        passes.add(new BranchOpt());

        for (Pass pass : passes)
        {
            pass.run();
        }
    }
}
