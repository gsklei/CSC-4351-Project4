package Absyn;

// DecLit(int pos, int value)
public class DecLit extends Exp {
    public int value;

    public DecLit(int pos, int value) {
        super(pos);
        this.value = value;
    }
}
