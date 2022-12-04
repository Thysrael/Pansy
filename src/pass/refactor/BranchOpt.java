package pass.refactor;

import driver.Config;
import ir.values.Function;
import ir.values.Module;
import pass.Pass;
import util.MyList;

public class BranchOpt implements Pass
{
    @Override
    public void run()
    {
        if (Config.openBranchOpt)
        {
            for (MyList.MyNode<Function> funcNode : Module.getInstance().getFunctions())
            {
                funcNode.getVal().reducePhi(true);
            }
        }
    }
}
