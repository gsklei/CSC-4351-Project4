package Absyn;

// VarDecl(int pos, Type type, String name, Exp init)
public class VarDecl extends Decl {
    public Type type;
    public String name;
    public Exp init;

    public VarDecl(int pos, Type type, String name, Exp init) {
        super(pos);
        this.type = type;
        this.name = name;
        this.init = init;
    }
}
