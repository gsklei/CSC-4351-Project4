package Absyn;

import java.util.ArrayList;

public class DeclList extends Absyn {
    public ArrayList<Decl> list;

    public DeclList(int pos) {
        super(pos);
        this.list = new ArrayList<>();
    }
}
