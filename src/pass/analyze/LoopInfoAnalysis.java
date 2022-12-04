package pass.analyze;

import ir.values.Function;
import ir.values.Module;
import pass.Pass;
import util.MyList;

public class LoopInfoAnalysis implements Pass
{
    @Override
    public void run()
    {
        for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
        {
            funcNode.getVal().analyzeLoop();
        }
    }
}
