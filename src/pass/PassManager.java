package pass;

import ir.values.Module;
import pass.analyze.BuildCFG;
import pass.analyze.DomInfo;
import pass.refactor.Mem2reg;

import java.util.ArrayList;


public class PassManager
{
    private final Module module = Module.getInstance();
    private final ArrayList<Pass> passes = new ArrayList<>();
    public void run()
    {
        passes.add(new BuildCFG());
        passes.add(new DomInfo());
        passes.add(new Mem2reg());
        for (Pass pass : passes)
        {
            pass.run();
        }
    }
}
