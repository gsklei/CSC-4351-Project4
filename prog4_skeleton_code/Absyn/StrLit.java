package Absyn;

// StrLit(int pos, String value)
public class StrLit extends Exp {
    public String value;

    public StrLit(int pos, String value) {
        super(pos);
        this.value = value;
    }
}
