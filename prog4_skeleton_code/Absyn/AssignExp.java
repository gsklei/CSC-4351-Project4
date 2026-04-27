package Absyn;

// AssignExp(int pos, Exp lhs, Exp rhs)
public class AssignExp extends Exp {
    public Exp lhs;
    public Exp rhs;

    public AssignExp(int pos, Exp lhs, Exp rhs) {
        super(pos);
        this.lhs = lhs;
        this.rhs = rhs;
    }
}
