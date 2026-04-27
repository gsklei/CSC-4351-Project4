package Absyn;

// ExprStmt(int pos, Exp exp)
public class ExprStmt extends Stmt {
    public Exp exp;

    public ExprStmt(int pos, Exp exp) {
        super(pos);
        this.exp = exp;
    }
}
