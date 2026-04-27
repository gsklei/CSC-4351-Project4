package Absyn;

// Type(int pos, boolean isConst, String name, int dims, DeclList structMembers)
public class Type extends Absyn {
    public boolean isConst;
    public String name;
    public int dims;
    public DeclList structMembers;

    public Type(int pos, boolean isConst, String name, int dims, DeclList structMembers) {
        super(pos);
        this.isConst = isConst;
        this.name = name;
        this.dims = dims;
        this.structMembers = structMembers;
    }
}
