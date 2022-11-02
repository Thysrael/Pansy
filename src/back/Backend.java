package back;

import back.component.ObjModule;
import back.process.IrParser;
import back.process.RegAllocator;

public class Backend
{
    private final ObjModule objModule;

    public Backend()
    {
        IrParser irParser = new IrParser();
        this.objModule = irParser.parseModule();
        RegAllocator regAllocator = new RegAllocator(objModule);
        regAllocator.process();
    }

    public String display()
    {
        return objModule.toString();
    }
}
