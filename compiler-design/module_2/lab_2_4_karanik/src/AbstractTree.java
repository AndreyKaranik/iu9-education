import java.util.List;

public class AbstractTree {

    private Program program;
    public enum PrimType {
        INT, CHAR, BOOL
    }

    public interface PrettyPrintable {
        String toString(int indent);
    }

    public record Type(PrimType base, int arrayLevel) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            return toString();
        }
    }
    public record FormalParameter(Type type, String name) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            return toString();
        }
    }
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
        }
    }
    public record FunctionHeader(Type type, String name, List<FormalParameter> formalParameters) implements PrettyPrintable {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("FunctionHeader", indent);
            builder.append("type", type).append("name", name).append("formalParameters", formalParameters);
            return builder.toString();
        }
    }

    public interface Expr extends PrettyPrintable { }

    public record VariableExpr(String name) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("VariableExpr", indent);
            builder.append("name", name);
            return builder.toString();
        }
    }
    public record ConstExpr(Type type, String value) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("ConstExpr", indent);
            builder.append("type", type).append("value", value);
            return builder.toString();
        }
    }
    public record StringConstExpr(Type type, List<String> value) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("StringConstExpr", indent);
            builder.append("type", type).append("value", value.toString());
            return builder.toString();
        }
    }
    public record FunctionInvocationExpr(String name, List<Expr> actualParameters) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("FunctionInvocationExpr", indent);
            builder.append("name", name).append("actualParameters", actualParameters);
            return builder.toString();
        }
    }
    public record BinOpExpr(Expr left, String op, Expr right) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("BinOpExpr", indent);
            builder.append("left", left).append("op", op).append("right", right);
            return builder.toString();
        }
    }
    public record AllocExpr(Type type, Expr expr) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("AllocExpr", indent);
            builder.append("type", type).append("expr", expr);
            return builder.toString();
        }
    }
    public record UnOpExpr(String op, Expr expr) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("UnOpExpr", indent);
            builder.append("op", op).append("expr", expr);
            return builder.toString();
        }
    }


    public interface Statement extends PrettyPrintable { }

    public record DeclarationStatement(Type type, List<DeclarationAssignment> declarationAssignments) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("DeclarationStatement", indent);
            builder.append("type", type).append("declarationAssignments", declarationAssignments);
            return builder.toString();
        }
    }

    public record AssignmentStatement(Expr left, Expr right) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("AssignmentStatement", indent);
            builder.append("left", left).append("right", right);
            return builder.toString();
        }
    }

    public record InvocationStatement(String name, List<Expr> actualParameters) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("InvocationStatement", indent);
            builder.append("name", name).append("actualParameters", actualParameters);
            return builder.toString();
        }
    }

    public record IfStatement(Expr condition, List<Statement> thenBranch, List<Statement> elseBranch) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("IfStatement", indent);
            builder.append("condition", condition).append("thenBranch", thenBranch).append("elseBranch", elseBranch);
            return builder.toString();
        }
    }

    public record PreWhileStatement(Expr condition, List<Statement> body) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("PreWhileStatement", indent);
            builder.append("condition", condition).append("body", body);
            return builder.toString();
        }
    }

    public record PostWhileStatement(Expr condition, List<Statement> body) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("PostWhileStatement", indent);
            builder.append("condition", condition).append("body", body);
            return builder.toString();
        }
    }

    public record ForStatement(Expr start, Expr end, String variable, List<Statement> body) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("ForStatement", indent);
            builder.append("start", start).append("end", end).append("variable", variable).append("body", body);
            return builder.toString();
        }
    }

    public record ReturnStatement(Expr expr) implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("ReturnStatement", indent);
            builder.append("expr", expr);
            return builder.toString();
        }
    }

    public record DeclarationAssignment(String name, Expr expr) implements PrettyPrintable {
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
