package Absyn;

import java.util.ArrayList;

public class ExpList extends Absyn {
    public ArrayList<Exp> list;

    public ExpList(int pos) {
        super(pos);
        this.list = new ArrayList<>();
    }
}
