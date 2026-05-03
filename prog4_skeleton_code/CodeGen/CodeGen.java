package CodeGen;

import java.util.*;

public class CodeGen {

    private final Program prog = new Program();
    private Function currentFunc;

    private final Deque<HashMap<String, Var>> scopeStack = new ArrayDeque<>();

    private final Function globalInitFunc;

    private final Deque<String> breakTargets = new ArrayDeque<>();

    public CodeGen() {
        globalInitFunc = new Function("__geaux_globals__", "int");
        prog.funcs.add(globalInitFunc);
    }

    public Program generate(Absyn.Absyn ast) {
        Absyn.DeclList topLevel = (Absyn.DeclList) ast;

        currentFunc = globalInitFunc;
        pushScope();

        for (Object obj : topLevel.list) {
            genTopLevelDecl((Absyn.Decl) obj);
        }

        globalInitFunc.instr.add(new ReturnStmt(new Literal(0, Type.INT)));
        popScope();

        return prog;
    }

    private void genTopLevelDecl(Absyn.Decl d) {
        if (d instanceof Absyn.FunDecl) {
            genFunDecl((Absyn.FunDecl) d);
        } else if (d instanceof Absyn.VarDecl) {
            Function saved = currentFunc;
            currentFunc = globalInitFunc;
            genVarDecl((Absyn.VarDecl) d);
            currentFunc = saved;
        }
    }

    private void genFunDecl(Absyn.FunDecl fd) {
        String retStr = mapReturnType(fd.result);
        Function f = new Function(fd.name, retStr);
        prog.funcs.add(f);

        Function savedFunc = currentFunc;
        currentFunc = f;
        pushScope();

        if (fd.params != null) {
            for (Object obj : fd.params.list) {
                Absyn.Decl param = (Absyn.Decl) obj;
                if (param instanceof Absyn.VarDecl) {
                    Absyn.VarDecl vd = (Absyn.VarDecl) param;
                    registerVar(vd.name, mapType(vd.type));
                }
            }
        }

        if (fd.name.equals("main")) {
            Var tmp = freshGlobal(Type.INT);
            currentFunc.instr.add(new Assign(tmp, new Call("__geaux_globals__", Type.INT)));
        }

        genStmt(fd.body);

        popScope();
        currentFunc = savedFunc;
    }

    private Var genVarDecl(Absyn.VarDecl vd) {
        Type irType = mapType(vd.type);
        Var v = registerVar(vd.name, irType);

        if (irType == Type.INTARRAY) {
            currentFunc.instr.add(new ArrayAlloc(v, new Literal(0, Type.INT)));
            if (vd.init != null && !(vd.init instanceof Absyn.EmptyExp)) {
                genArrayInit(v, vd.init);
            }
        } else if (vd.init != null && !(vd.init instanceof Absyn.EmptyExp)) {
            IRExpr val = genExp(vd.init);
            currentFunc.instr.add(new Assign(v, val));
        }

        return v;
    }

    private void genStmt(Absyn.Stmt s) {
        if      (s instanceof Absyn.CompStmt)   genCompStmt((Absyn.CompStmt) s);
        else if (s instanceof Absyn.IfStmt)     genIfStmt((Absyn.IfStmt) s);
        else if (s instanceof Absyn.WhileStmt)  genWhileStmt((Absyn.WhileStmt) s);
        else if (s instanceof Absyn.ReturnStmt) genReturnStmt((Absyn.ReturnStmt) s);
        else if (s instanceof Absyn.ExprStmt)   genExprStmt((Absyn.ExprStmt) s);
        else if (s instanceof Absyn.EmptyStmt)  {}
        else if (s instanceof Absyn.BreakStmt)  genBreakStmt();
        else throw new RuntimeException("Unknown stmt: " + s.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private void genCompStmt(Absyn.CompStmt cs) {
        pushScope();

        if (cs.decls != null) {
            for (Object obj : cs.decls.list) {
                Absyn.Decl d = (Absyn.Decl) obj;
                if      (d instanceof Absyn.VarDecl) genVarDecl((Absyn.VarDecl) d);
                else if (d instanceof Absyn.FunDecl) genFunDecl((Absyn.FunDecl) d);
            }
        }

        if (cs.stmts != null) {
            for (Object obj : cs.stmts.list) {
                genStmt((Absyn.Stmt) obj);
            }
        }

        popScope();
    }

    private void genIfStmt(Absyn.IfStmt is) {
        String trueLabel  = prog.getUniqueLabelName();
        String falseLabel = prog.getUniqueLabelName();
        String doneLabel  = prog.getUniqueLabelName();

        IRExpr cond = genExp(is.test);
        currentFunc.instr.add(new IfStmt(cond, trueLabel, falseLabel));

        currentFunc.instr.add(new Label(trueLabel));
        genStmt(is.thenStmt);
        currentFunc.instr.add(new Goto(doneLabel));

        currentFunc.instr.add(new Label(falseLabel));
        boolean hasElse = is.elseStmt != null && !(is.elseStmt instanceof Absyn.EmptyStmt);
        if (hasElse) genStmt(is.elseStmt);

        currentFunc.instr.add(new Label(doneLabel));
    }

    private void genWhileStmt(Absyn.WhileStmt ws) {
        String topLabel  = prog.getUniqueLabelName();
        String bodyLabel = prog.getUniqueLabelName();
        String exitLabel = prog.getUniqueLabelName();

        breakTargets.push(exitLabel);

        currentFunc.instr.add(new Label(topLabel));
        IRExpr cond = genExp(ws.test);
        currentFunc.instr.add(new IfStmt(cond, bodyLabel, exitLabel));

        currentFunc.instr.add(new Label(bodyLabel));
        genStmt(ws.body);
        currentFunc.instr.add(new Goto(topLabel));

        currentFunc.instr.add(new Label(exitLabel));
        breakTargets.pop();
    }

    private void genBreakStmt() {
        if (breakTargets.isEmpty())
            throw new RuntimeException("break outside of loop");
        currentFunc.instr.add(new Goto(breakTargets.peek()));
    }

    private void genReturnStmt(Absyn.ReturnStmt rs) {
        IRExpr val = (rs.exp != null && !(rs.exp instanceof Absyn.EmptyExp))
            ? genExp(rs.exp)
            : new Literal(0, Type.INT);
        currentFunc.instr.add(new ReturnStmt(val));
    }

    @SuppressWarnings("unchecked")
    private void genExprStmt(Absyn.ExprStmt es) {
        if (es.exp == null || es.exp instanceof Absyn.EmptyExp) return;

        if (es.exp instanceof Absyn.FunExp) {
            Absyn.FunExp fe = (Absyn.FunExp) es.exp;
            if (fe.func instanceof Absyn.ID) {
                String fname = ((Absyn.ID) fe.func).name;
                if (tryGenBuiltin(fname, fe)) return;
            }
        }

        if (es.exp instanceof Absyn.AssignExp) {
            genAssignExp((Absyn.AssignExp) es.exp);
            return;
        }

        IRExpr val = genExp(es.exp);
        if (!(val instanceof Literal)) {
            Var tmp = freshGlobal(val.type);
            currentFunc.instr.add(new Assign(tmp, val));
        }
    }

    private IRExpr genExp(Absyn.Exp e) {
        if (e instanceof Absyn.BinOp)     return genBinOp((Absyn.BinOp) e);
        if (e instanceof Absyn.UnaryExp)  return genUnaryExp((Absyn.UnaryExp) e);
        if (e instanceof Absyn.AssignExp) return genAssignExp((Absyn.AssignExp) e);
        if (e instanceof Absyn.FunExp)    return genFunExpExpr((Absyn.FunExp) e);
        if (e instanceof Absyn.ArrayExp)  return genArrayLoad((Absyn.ArrayExp) e);
        if (e instanceof Absyn.ID)        return lookupVar(((Absyn.ID) e).name);
        if (e instanceof Absyn.DecLit)    return new Literal(((Absyn.DecLit) e).value, Type.INT);
        if (e instanceof Absyn.StrLit)    return new Literal(((Absyn.StrLit) e).value, Type.STRING);
        if (e instanceof Absyn.EmptyExp)  return new Literal(0, Type.INT);
        throw new RuntimeException("Unknown expr: " + e.getClass().getSimpleName());
    }

    private IRExpr genBinOp(Absyn.BinOp b) {
        IRExpr left  = genExp(b.left);
        IRExpr right = genExp(b.right);
        Type t = isRelational(b.op) ? Type.INT : left.type;
        return new BinOp(b.op, left, right, t);
    }

    private IRExpr genUnaryExp(Absyn.UnaryExp u) {
        IRExpr inner = genExp(u.exp);
        return new UnaryOp(u.op, inner, inner.type);
    }

    @SuppressWarnings("unchecked")
    private IRExpr genAssignExp(Absyn.AssignExp ae) {
        IRExpr rval = genExp(ae.rhs);

        if (ae.lhs instanceof Absyn.ArrayExp) {
            Absyn.ArrayExp arrExp = (Absyn.ArrayExp) ae.lhs;
            IRExpr baseExpr = genExp(arrExp.var);
            if (!(baseExpr instanceof Var))
                throw new RuntimeException("Array base must resolve to a Var");
            Var arrVar = (Var) baseExpr;
            IRExpr idx = genExp((Absyn.Exp) arrExp.dims.list.get(0));

            Var newSize = freshGlobal(Type.INT);
            currentFunc.instr.add(new Assign(newSize,
                new BinOp("+", idx, new Literal(1, Type.INT), Type.INT)));
            currentFunc.instr.add(new ArrayAlloc(arrVar, newSize));
            currentFunc.instr.add(new ArrayStore(arrVar, idx, rval));
        } else if (ae.lhs instanceof Absyn.ID) {
            Var target = lookupVar(((Absyn.ID) ae.lhs).name);
            currentFunc.instr.add(new Assign(target, rval));
        } else {
            throw new RuntimeException("Unsupported assignment target: "
                + ae.lhs.getClass().getSimpleName());
        }

        return rval;
    }

    @SuppressWarnings("unchecked")
    private IRExpr genFunExpExpr(Absyn.FunExp fe) {
        if (!(fe.func instanceof Absyn.ID))
            throw new RuntimeException("Indirect calls not supported");

        String fname = ((Absyn.ID) fe.func).name;

        switch (fname) {
            case "input": {
                Var result = freshGlobal(Type.INT);
                currentFunc.instr.add(new Input(result));
                return result;
            }
            case "readfromfile": {
                IRExpr fnameArg = fe.args.list.isEmpty()
                    ? new Literal("in.txt", Type.STRING)
                    : genExp((Absyn.Exp) fe.args.list.get(0));
                Var result = freshGlobal(Type.STRING);
                currentFunc.instr.add(new ReadFromFile(fnameArg, result));
                return result;
            }
            case "printf":
            case "writefile":
                tryGenBuiltin(fname, fe);
                return new Literal(0, Type.INT);

            default:
                return new Call(fname, Type.INT);
        }
    }

    @SuppressWarnings("unchecked")
    private IRExpr genArrayLoad(Absyn.ArrayExp ae) {
        IRExpr base = genExp(ae.var);
        if (!(base instanceof Var))
            throw new RuntimeException("Array base must be a Var");
        Var arrVar = (Var) base;

        IRExpr idx = genExp((Absyn.Exp) ae.dims.list.get(0));
        for (int i = 1; i < ae.dims.list.size(); i++) {
            IRExpr next = genExp((Absyn.Exp) ae.dims.list.get(i));
            idx = new BinOp("+", idx, next, Type.INT);
        }

        return new ArrayLoad(arrVar, idx, Type.INT);
    }

    @SuppressWarnings("unchecked")
    private boolean tryGenBuiltin(String fname, Absyn.FunExp fe) {
        switch (fname) {
            case "printf": {
                List<IRExpr> irArgs = new ArrayList<>();
                String fmt;
                int startIdx = 0;

                if (!fe.args.list.isEmpty() && fe.args.list.get(0) instanceof Absyn.StrLit) {
                    fmt = ((Absyn.StrLit) fe.args.list.get(0)).value;
                    startIdx = 1;
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Object obj : fe.args.list) {
                        IRExpr arg = genExp((Absyn.Exp) obj);
                        irArgs.add(arg);
                        sb.append(arg.type == Type.STRING ? "%s" : "%d").append("\\n");
                    }
                    currentFunc.instr.add(new Printf(sb.toString(), irArgs));
                    return true;
                }

                for (int i = startIdx; i < fe.args.list.size(); i++) {
                    irArgs.add(genExp((Absyn.Exp) fe.args.list.get(i)));
                }
                currentFunc.instr.add(new Printf(fmt, irArgs));
                return true;
            }

            case "writefile": {
                IRExpr file = fe.args.list.size() > 0
                    ? genExp((Absyn.Exp) fe.args.list.get(0)) : new Literal("out.txt", Type.STRING);
                IRExpr content = fe.args.list.size() > 1
                    ? genExp((Absyn.Exp) fe.args.list.get(1)) : new Literal("", Type.STRING);
                currentFunc.instr.add(new WriteToFile(file, content));
                return true;
            }

            case "readfromfile": {
                IRExpr file = fe.args.list.size() > 0
                    ? genExp((Absyn.Exp) fe.args.list.get(0)) : new Literal("in.txt", Type.STRING);
                Var result = freshGlobal(Type.STRING);
                currentFunc.instr.add(new ReadFromFile(file, result));
                return true;
            }

            case "input": {
                Var result = freshGlobal(Type.INT);
                currentFunc.instr.add(new Input(result));
                return true;
            }

            default:
                return false;
        }
    }

    private void genArrayInit(Var arrVar, Absyn.Exp initExp) {
        IRExpr val = genExp(initExp);
        currentFunc.instr.add(new Assign(arrVar, val));
    }

    private Type mapType(Absyn.Type t) {
        if (t == null) return Type.INT;
        if (t.dims > 0) return Type.INTARRAY;
        switch (t.name) {
            case "string": return Type.STRING;
            default:       return Type.INT;
        }
    }

    private String mapReturnType(Absyn.Type t) {
        if (t == null) return "int";
        if (t.dims > 0) return "int*";
        switch (t.name) {
            case "string": return "char*";
            case "void":   return "void";
            default:       return "int";
        }
    }

    private void pushScope() { scopeStack.push(new HashMap<>()); }
    private void popScope()  { scopeStack.pop(); }

    private Var registerVar(String name, Type t) {
        String uname = prog.getUniqueVarName();
        Var v = new Var(uname, t);
        prog.globals.add(v);
        scopeStack.peek().put(name, v);
        return v;
    }

    private Var lookupVar(String name) {
        for (HashMap<String, Var> scope : scopeStack) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    private Var freshGlobal(Type t) {
        Var v = new Var(prog.getUniqueVarName(), t);
        prog.globals.add(v);
        return v;
    }

    private static boolean isRelational(String op) {
        return op.equals("<") || op.equals(">") || op.equals("<=")
            || op.equals(">=") || op.equals("==") || op.equals("!=");
    }
}
