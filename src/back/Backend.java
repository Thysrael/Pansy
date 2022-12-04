package back;

import back.component.ObjModule;
import back.process.IrParser;
import back.process.Peephole;
import back.process.RegAllocator;
import driver.Config;

public class Backend
{
    private final ObjModule objModule;
    private final String rawMips;

    public Backend()
    {
        IrParser irParser = new IrParser();
        this.objModule = irParser.parseModule();
        if (Config.rawMipsOutputToCmd)
        {
            rawMips = objModule.toString();
        }
        else
        {
            rawMips = null;
        }
        RegAllocator regAllocator = new RegAllocator(objModule);
        regAllocator.process();
        Peephole peephole = new Peephole(objModule);
        peephole.process();
    }

    public String display()
    {
        if (Config.rawMipsOutputToCmd)
        {
            System.out.println(rawMips);
        }

        return objModule.toString();

    }
}
