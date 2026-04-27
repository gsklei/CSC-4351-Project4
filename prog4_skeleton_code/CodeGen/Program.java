package CodeGen;

import java.util.ArrayList;

public class Program {
    private int unique_name_counter;
    private int unique_label_counter;

    public ArrayList<Var> globals;
    public ArrayList<Function> funcs;

    public Program() {
        this.unique_name_counter = 0;
        this.unique_label_counter = 0;
        this.globals = new ArrayList<>();
        this.funcs = new ArrayList<>();
    }

    public String getUniqueVarName() {
        return "_x" + (++this.unique_name_counter);
    }

    public String getUniqueLabelName() {
        return "LABEL" + (++this.unique_label_counter);
    }
}
