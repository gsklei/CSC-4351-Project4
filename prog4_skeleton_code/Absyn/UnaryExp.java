package Absyn;

// UnaryExp(int pos, String op, Exp exp)
public class UnaryExp extends Exp {
    public String op;
    public Exp exp;

    public UnaryExp(int pos, String op, Exp exp) {
        super(pos);
        this.op  = op;
        this.exp = exp;
    }
}
