package Absyn;

// BinOp(int pos, Exp left, String op, Exp right)
public class BinOp extends Exp {
    public Exp left;
    public String op;
    public Exp right;

    public BinOp(int pos, Exp left, String op, Exp right) {
        super(pos);
        this.left  = left;
        this.op    = op;
        this.right = right;
    }
}
