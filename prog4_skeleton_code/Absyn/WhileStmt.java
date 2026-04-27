package Absyn;

// WhileStmt(int pos, Exp test, Stmt body)
public class WhileStmt extends Stmt {
    public Exp test;
    public Stmt body;

    public WhileStmt(int pos, Exp test, Stmt body) {
        super(pos);
        this.test = test;
        this.body = body;
    }
}
