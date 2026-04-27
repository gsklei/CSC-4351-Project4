package Absyn;

// ID(int pos, String name)
public class ID extends Exp {
    public String name;

    public ID(int pos, String name) {
        super(pos);
        this.name = name;
    }
}
