import org.antlr.v4.runtime.*;
import Parse.antlr_build.Parse.gLexer;
import Parse.antlr_build.Parse.gParser;
import Parse.ASTBuilder;
import Absyn.DeclList;
import CodeGen.CodeGen;
import CodeGen.Emitter;

import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Main <source.gx>");
            System.exit(1);
        }

        // ── 1. Parse ──────────────────────────────────────────────────────────
        CharStream input = CharStreams.fromFileName(args[0]);
        gLexer lexer     = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        gParser parser   = new gParser(tokens);
        gParser.ProgramContext tree = parser.program();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Parse errors — aborting.");
            System.exit(1);
        }

        ASTBuilder builder = new ASTBuilder();
        DeclList ast = (DeclList) builder.visit(tree);

        // ── 2. Typecheck ──────────────────────────────────────────────────────
        // Uncomment once your Typecheck class is available:
        // new Typecheck.TypeChecker().check(ast);

        // ── 3. Code generation ───────────────────────────────────────────────
        var prog = new CodeGen().generate(ast);

        // ── 4. Emit C ─────────────────────────────────────────────────────────
        String cCode = new Emitter.ProgramEmitter(prog.globals, prog.funcs).emitProgram();

        // ── 5. Write output.c ────────────────────────────────────────────────
        Files.writeString(Path.of("output.c"), cCode);
        System.out.println("Wrote output.c");
        System.out.println(cCode);

        // ── 6. Compile with gcc and run ──────────────────────────────────────
        Process gcc = new ProcessBuilder("gcc", "-o", "a.out", "output.c")
            .inheritIO()
            .start();
        int gccExit = gcc.waitFor();
        if (gccExit != 0) {
            System.err.println("gcc failed (exit " + gccExit + ")");
            System.exit(gccExit);
        }

        System.out.println("\n── Running a.out ──────────────────────────────");
        Process run = new ProcessBuilder("./a.out")
            .inheritIO()
            .start();
        System.exit(run.waitFor());
    }
}
