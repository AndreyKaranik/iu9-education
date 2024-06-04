import java.util.List;

public class AbstractTree {

    private Program program;
    public enum PrimType {
        INT, CHAR, BOOL
    }

    public interface PrettyPrintable {
        String toString(int indent);
    }

    public record Type(PrimType base, int arrayLevel) { }
    public record FormalParameter(Type type, String name) { }
    public record FunctionHeaderTypeAndName(Type type, String name) { }

    public record Program(List<FunctionDeclaration> functionDeclarations) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("Program", 0);
            builder.append("functionDeclarations", functionDeclarations);
            return builder.toString();
        }
    }
    public record FunctionDeclaration(FunctionHeader header, List<Statement> body) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("FunctionDeclaration", indent);
            builder.append("header", header).append("body", body);
            return builder.toString();
//            StringBuilder builder = new StringBuilder();
//            builder.append("FunctionDeclaration(");
//            int size = builder.length();
//            builder.append("header=");
//            builder.append(header).append(",\n").append(" ".repeat(Math.max(0, indent + size))).append("body=").append(body).append(")");
//            return builder.toString();
        }
    }
    public record FunctionHeader(Type type, String name, List<FormalParameter> formalParameters) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            return toString();
        }
    }

    public interface Expr { }
    public interface Statement extends PrettyPrintable { }

    public record ReturnStatement(Expr expr) implements Statement {
        @Override
        public String toString(int indent) {
            return toString();
        }
    }


    public AbstractTree(Program program) {
        this.program = program;
    }
    public Program getProgram() {
        return program;
    }

    @Override
    public String toString() {
        return program.toString();
    }

    public void print() {
        System.out.println(program.toString(0));
    }
}
