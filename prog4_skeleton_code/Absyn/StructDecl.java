package Absyn;

// StructDecl(int pos, String name, DeclList members)
public class StructDecl extends Decl {
    public String name;
    public DeclList members;

    public StructDecl(int pos, String name, DeclList members) {
        super(pos);
        this.name    = name;
        this.members = members;
    }
}
