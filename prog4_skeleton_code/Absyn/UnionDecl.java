package Absyn;

// UnionDecl(int pos, String name, DeclList members)
public class UnionDecl extends Decl {
    public String name;
    public DeclList members;

    public UnionDecl(int pos, String name, DeclList members) {
        super(pos);
        this.name    = name;
        this.members = members;
    }
}
