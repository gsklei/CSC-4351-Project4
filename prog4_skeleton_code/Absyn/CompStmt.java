package Absyn;

// CompStmt(int pos, DeclList decls, StmtList stmts)
public class CompStmt extends Stmt {
    public DeclList decls;
    public StmtList stmts;

    public CompStmt(int pos, DeclList decls, StmtList stmts) {
        super(pos);
        this.decls = decls;
        this.stmts = stmts;
    }
}
