import java.util.List;

public class AbstractTree {

    private Program program;
    public enum PrimType {
        INT, CHAR, BOOL
    }

    public record Type(PrimType base, int arrayLevel) { }
    public record FormalParameter(Type type, String name) { }
    public record FunctionHeaderTypeAndName(Type type, String name) { }

    public record Program(List<FunctionDeclaration> functionDeclarations) { }
    public record FunctionDeclaration(FunctionHeader header, List<Statement> body) { }
    public record FunctionHeader(Type type, String name, List<FormalParameter> formalParameters) { }

    public interface Expr { }
    public interface Statement { }

    public record ReturnStatement(Expr expr) implements Statement { }


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
}
