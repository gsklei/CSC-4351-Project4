package Absyn;

// FunExp(int pos, Exp func, ExpList args)
public class FunExp extends Exp {
    public Exp func;
    public ExpList args;

    public FunExp(int pos, Exp func, ExpList args) {
        super(pos);
        this.func = func;
        this.args = args;
    }
}
