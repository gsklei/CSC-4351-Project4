package Absyn;

// ArrayExp(int pos, Exp var, ExpList dims)
public class ArrayExp extends Exp {
    public Exp var;
    public ExpList dims;

    public ArrayExp(int pos, Exp var, ExpList dims) {
        super(pos);
        this.var  = var;
        this.dims = dims;
    }
}
