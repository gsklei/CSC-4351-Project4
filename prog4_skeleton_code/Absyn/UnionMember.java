package Absyn;

public class UnionMember extends Decl {
    public Type type;
    public String name;

    public UnionMember(int pos, Type type, String name) {
        super(pos);
        this.type = type;
        this.name = name;
    }
}
