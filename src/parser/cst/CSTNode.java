package parser.cst;

import java.util.ArrayList;

public abstract class CSTNode
{
    protected CSTNode parent;
    private final ArrayList<CSTNode> children = new ArrayList<>();

    public void addChild(CSTNode child)
    {
        children.add(child);
    }
}
