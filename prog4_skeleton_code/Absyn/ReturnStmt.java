package Absyn;

// ReturnStmt(int pos, Exp exp)
public class ReturnStmt extends Stmt {
    public Exp exp;

    public ReturnStmt(int pos, Exp exp) {
        super(pos);
        this.exp = exp;
    }
}
