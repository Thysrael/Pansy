package back.component;

import java.util.ArrayList;

public class ObjGlobalVariable
{
    private final String name;
    private final boolean isInit;
    private final boolean isStr;
    private final int size;
    private final ArrayList<Integer> elements;
    private final String content;

    public ObjGlobalVariable(String name, ArrayList<Integer> elements)
    {
        this.name = name.substring(1);
        this.isInit = true;
        this.isStr = false;
        this.size = 4 * elements.size();
        this.elements = elements;
        this.content = null;
    }

    public ObjGlobalVariable(String name, int size)
    {
        this.name = name.substring(1);
        this.isInit = false;
        this.isStr = false;
        this.size = size;
        this.elements = null;
        this.content = null;
    }

    public ObjGlobalVariable(String name, String content)
    {
        this.name = name.substring(1);
        this.isInit = true;
        this.isStr = true;
        this.size = content.length() * 4;
        this.elements = null;
        this.content = content;
    }

    /**
     * 根据是否是 init 选择打印方式
     * @return 全局变量字符串
     */
    @Override
    public String toString()
    {
        StringBuilder globalSb = new StringBuilder("");
        globalSb.append(name).append(":\n");
        // 初始化了就用 .word 或者 .ascii
        if (isInit)
        {
            if (isStr)
            {
                globalSb.append(".asciiz\t\"").append(content).append("\"\n");
            }
            else
            {
                for (Integer element : elements)
                {
                    globalSb.append(".word\t").append(element).append("\n");
                }
            }
        }
        // 未初始化就用 .space
        else
        {
            globalSb.append(".space\t").append(size).append("\n");
        }
        return globalSb.toString();
    }
}
