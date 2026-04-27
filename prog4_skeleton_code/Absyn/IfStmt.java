package Absyn;

// IfStmt(int pos, Exp test, Stmt thenStmt, Stmt elseStmt)
public class IfStmt extends Stmt {
    public Exp test;
    public Stmt thenStmt;
    public Stmt elseStmt;

    public IfStmt(int pos, Exp test, Stmt thenStmt, Stmt elseStmt) {
        super(pos);
        this.test     = test;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
}
