package pass;

import ir.values.Function;
import ir.values.Module;
import util.MyList;


public class PassManager
{
    private final Module module = Module.getInstance();

    public void run()
    {
        for (MyList.MyNode<Function> functionNode : module.getFunctions())
        {
            Function function = functionNode.getVal();
            function.renumber();
        }
    }
}
