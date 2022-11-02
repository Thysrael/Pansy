package back.instruction;

import back.component.ObjFunction;

public class ObjCall extends ObjInstr
{
    // 如果从这个角度看，似乎操作数不只是立即数和寄存器，可能还有标签，这么写似乎是草率了
    private final ObjFunction targetFunction;

    public ObjCall(ObjFunction targetFunction)
    {
        this.targetFunction = targetFunction;
    }

    @Override
    public String toString()
    {
        return "jal\t" + targetFunction.getName() + "\n";
    }
}
