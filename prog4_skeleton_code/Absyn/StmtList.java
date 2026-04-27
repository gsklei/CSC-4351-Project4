package Absyn;

import java.util.ArrayList;

public class StmtList extends Absyn {
    public ArrayList<Stmt> list;

    public StmtList(int pos) {
        super(pos);
        this.list = new ArrayList<>();
    }
}
