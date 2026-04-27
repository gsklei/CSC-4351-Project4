package Absyn;

public class StructMember extends Decl {
    public Type type;
    public String name;

    public StructMember(int pos, Type type, String name) {
        super(pos);
        this.type = type;
        this.name = name;
    }
}
