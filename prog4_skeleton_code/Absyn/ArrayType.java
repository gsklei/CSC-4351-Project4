package Absyn;

public class ArrayType extends Absyn {
    public Type elementType;
    public int dims;

    public ArrayType(int pos, Type elementType, int dims) {
        super(pos);
        this.elementType = elementType;
        this.dims = dims;
    }
}
