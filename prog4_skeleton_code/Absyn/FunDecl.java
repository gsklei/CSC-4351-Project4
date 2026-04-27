package Absyn;

// FunDecl(int pos, Type result, String name, DeclList params, Stmt body)
public class FunDecl extends Decl {
    public Type result;
    public String name;
    public DeclList params;
    public Stmt body;

    public FunDecl(int pos, Type result, String name, DeclList params, Stmt body) {
        super(pos);
        this.result = result;
        this.name   = name;
        this.params = params;
        this.body   = body;
    }
}
