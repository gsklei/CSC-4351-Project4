package Absyn;

// Typedef(int pos, Type type, String name)
public class Typedef extends Decl {
    public Type type;
    public String name;

    public Typedef(int pos, Type type, String name) {
        super(pos);
        this.type = type;
        this.name = name;
    }
}
