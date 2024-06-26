% Лабораторная работа № 2.4 «Рекурсивный спуск»
% 5 июня 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является изучение алгоритмов построения парсеров методом рекурсивного спуска.

# Индивидуальный вариант
Язык L2.

# Реализация

## Лексическая структура
```
IDENTIFIER = {(\\w|[ ])+}
DECIMAL_INTEGER_CONSTANT = [0-9]+
NON_DECIMAL_INTEGER_CONSTANT = ([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))?
SYMBOLIC_CONSTANT = [$]("(.|%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%"%|%%)")|[A-F0-9]+
STRING_SECTION = ("([^"\n]|%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%"%|%%)*")|(%[A-F0-9]+)
BOOLEAN_CONSTANT = true|false
KW_BOOL = bool
KW_INT = int
KW_RETURN = return
KW_VOID = void
KW_CHAR = char
KW_LOOP = loop
KW_THEN = then
KW_ELSE = else
KW_NULL = null
KW_WHILE = while

DomainTag {
    IDENTIFIER, DECIMAL_INTEGER_CONSTANT, NON_DECIMAL_INTEGER_CONSTANT, SYMBOLIC_CONSTANT,
    STRING_SECTION, BOOLEAN_CONSTANT,
    OR_XOR_OP, AND_OP, EQ_OP, ORD_OP, PLUS_OP, MINUS_OP, MUL_DIV_REM_OP, POWER_OP, NOT_OP,
    EQUAL, ASSIGN, DOT, LEFT_ARROW, COMMA, TILDE, BRACKETS, LEFT_PAR, RIGHT_PAR, SEMICOLON,
    KW_INT, KW_BOOL, KW_RETURN, KW_VOID, KW_CHAR, KW_LOOP, KW_THEN, KW_ELSE, KW_NULL, KW_WHILE, END, NONE
}
```

## Грамматика языка
Грамматика в РБНФ:
```
NProgram ::= NFunctionDeclarations
NFunctionDeclarations ::= NFunctionDeclaration+
NFunctionDeclaration ::= NFunctionHeader '=' NStatements '.'
NFunctionHeader ::= NFunctionHeaderTypeName ('<-' NFormalParameters)?
NFunctionHeaderTypeName ::= (NType | KW_VOID) IDENTIFIER
NFormalParameters ::= NFormalParameter (',' NFormalParameter)*
NFormalParameter ::= NType IDENTIFIER

NStatements ::= NStatement (';' NStatement)*

NStatement ::= NType NDeclarationAssignments
             | NExpr
               (
                 ':=' NExpr
               | KW_THEN NStatements (KW_ELSE NStatements)? '.'
               | KW_LOOP NStatements '.'
               | '~' NExpr KW_LOOP IDENTIFIER NStatements '.'
               )?
             | KW_LOOP NStatements KW_WHILE NExpr '.'
             | KW_RETURN (NExpr)?


NDeclarationAssignments ::= NDeclarationAssignment (',' NDeclarationAssignment)*
NDeclarationAssignment ::= IDENTIFIER (':=' NArithmExpr)?

NType ::= (KW_INT | KW_CHAR | KW_BOOL) '[]'*

NStringConstant ::= STRING_SECTION STRING_SECTION*

NConst ::= DECIMAL_INTEGER_CONSTANT | NON_DECIMAL_INTEGER_CONSTANT | SYMBOLIC_CONSTANT |
        | BOOLEAN_CONSTANT | KW_NULL

NExpr ::= NAndExpr ('|' NAndExpr | '@' NAndExpr)*
NAndExpr ::= NCmpExpr ('&' NCmpExpr)*
NCmpExpr ::= NFuncCallExpr (NCmpOp NFuncCallExpr)*
NCmpOp ::= EQ_OP | ORD_OP
NFuncCallExpr ::= NArithmExpr ('<-' NArgs)?
NArgs ::= NArithmExpr (',' NArithmExpr)*
NArithmExpr ::= NTerm (('+' | '-') NTerm)*
NTerm ::= NFactor (('*' | '/' | '%') NFactor)*
NFactor ::= NPower ('^' NFactor)?
NPower ::= NArrExpr | (('!' | '-') NPower) | (NType NBottomExpr)
NArrExpr ::= NBottomExpr | (NArrExpr NBottomExpr) | NStringConstant
NBottomExpr ::= IDENTIFIER | NConst | ('(' NExpr ')')
```

## Программная реализация

Main.java
```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("input.txt")));
        Scanner scanner = new Scanner(input);

        Parser parser = new Parser(scanner);
        parser.printTree();
    }
}
```

Parser.java
```java
import java.util.*;

public class Parser {

    private Scanner scanner;
    private Token sym;
    private Map<String, HashSet<DomainTag>> first;

    private AbstractTree tree;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        initializeMinFirstSet();
        sym = scanner.nextToken();
        tree = new AbstractTree(NProgram());
    }

    public void printTree() {
        tree.print();
    }

    public void initializeMinFirstSet() {
        first = new HashMap<>();
        HashSet<DomainTag> set1 = new HashSet<>();
        set1.add(DomainTag.KW_INT);
        set1.add(DomainTag.KW_CHAR);
        set1.add(DomainTag.KW_BOOL);
        set1.add(DomainTag.KW_VOID);
        first.put("NFunctionDeclaration", set1);

        HashSet<DomainTag> set2 = new HashSet<>();
        set2.add(DomainTag.KW_INT);
        set2.add(DomainTag.KW_CHAR);
        set2.add(DomainTag.KW_BOOL);
        first.put("NType", set2);

        HashSet<DomainTag> set3 = new HashSet<>();
        set3.add(DomainTag.IDENTIFIER);
        set3.add(DomainTag.LEFT_PAR);
        set3.add(DomainTag.DECIMAL_INTEGER_CONSTANT);
        set3.add(DomainTag.NON_DECIMAL_INTEGER_CONSTANT);
        set3.add(DomainTag.SYMBOLIC_CONSTANT);
        set3.add(DomainTag.BOOLEAN_CONSTANT);
        set3.add(DomainTag.KW_NULL);
        first.put("NBottomExpr", set3);

        HashSet<DomainTag> set4 = new HashSet<>();
        set4.add(DomainTag.EQ_OP);
        set4.add(DomainTag.ORD_OP);
        first.put("NCmpOp", set4);

        HashSet<DomainTag> set5 = new HashSet<>();
        set5.add(DomainTag.IDENTIFIER);
        set5.add(DomainTag.LEFT_PAR);
        set5.add(DomainTag.DECIMAL_INTEGER_CONSTANT);
        set5.add(DomainTag.NON_DECIMAL_INTEGER_CONSTANT);
        set5.add(DomainTag.SYMBOLIC_CONSTANT);
        set5.add(DomainTag.BOOLEAN_CONSTANT);
        set5.add(DomainTag.KW_NULL);
        set5.add(DomainTag.STRING_SECTION);
        set5.add(DomainTag.MINUS_OP);
        set5.add(DomainTag.NOT_OP);
        set5.add(DomainTag.KW_INT);
        set5.add(DomainTag.KW_CHAR);
        set5.add(DomainTag.KW_BOOL);
        first.put("NExpr", set5);
    }

    public void reportError() {
        throw new RuntimeException(sym.getFragmentPosition().toString());
    }

    // NProgram ::= NFunctionDeclarations
    public AbstractTree.Program NProgram() {
        List<AbstractTree.FunctionDeclaration> functionDeclarations = NFunctionDeclarations();
        return new AbstractTree.Program(functionDeclarations);
    }

    // NFunctionDeclarations ::= NFunctionDeclaration+
    public List<AbstractTree.FunctionDeclaration> NFunctionDeclarations() {
        List<AbstractTree.FunctionDeclaration> functionDeclarations = new ArrayList<>();
        do {
            functionDeclarations.add(NFunctionDeclaration());
        } while (first.get("NFunctionDeclaration").contains(sym.getTag()));

        return functionDeclarations;
    }

    // NFunctionDeclaration ::= NFunctionHeader '=' NStatements '.'
    public AbstractTree.FunctionDeclaration NFunctionDeclaration() {
        AbstractTree.FunctionHeader header = NFunctionHeader();
        if (sym.getTag().equals(DomainTag.EQUAL)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        List<AbstractTree.Statement> statements = NStatements();
        if (sym.getTag().equals(DomainTag.DOT)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }

        return new AbstractTree.FunctionDeclaration(header, statements);
    }

    // NFunctionHeader ::= NFunctionHeaderTypeName ('<-' NFormalParameters)?
    public AbstractTree.FunctionHeader NFunctionHeader() {
        AbstractTree.FunctionHeaderTypeAndName typeAndName = NFunctionHeaderTypeName();
        List<AbstractTree.FormalParameter> formalParameters = new ArrayList<>();
        if (sym.getTag().equals(DomainTag.LEFT_ARROW)) {
            sym = scanner.nextToken();
            formalParameters = NFormalParameters();
        }
        return new AbstractTree.FunctionHeader(typeAndName.type(), typeAndName.name(), formalParameters);
    }

    // NStatements ::= NStatement (';' NStatement)*
    public List<AbstractTree.Statement> NStatements() {
        List<AbstractTree.Statement> statements = new ArrayList<>();
        statements.add(NStatement());
        while (sym.getTag().equals(DomainTag.SEMICOLON)) {
            sym = scanner.nextToken();
            statements.add(NStatement());
        }
        return statements;
    }

    // (NType | KW_VOID) IDENTIFIER
    public AbstractTree.FunctionHeaderTypeAndName NFunctionHeaderTypeName() {
        AbstractTree.Type type = null;
        String name = null;
        if (first.get("NType").contains(sym.getTag())) {
            type = NType();
        } else {
            if (sym.getTag().equals(DomainTag.KW_VOID)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
        }

        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            name = sym.getValue();
            sym = scanner.nextToken();
        } else {
            reportError();
        }

        return new AbstractTree.FunctionHeaderTypeAndName(type, name);

    }

    // NFormalParameters ::= NFormalParameter (',' NFormalParameter)*
    public List<AbstractTree.FormalParameter> NFormalParameters() {
        List<AbstractTree.FormalParameter> formalParameters = new ArrayList<>();
        formalParameters.add(NFormalParameter());
        while (sym.getTag().equals(DomainTag.COMMA)) {
            sym = scanner.nextToken();
            formalParameters.add(NFormalParameter());
        }
        return formalParameters;
    }

    // NFormalParameter ::= NType IDENTIFIER
    public AbstractTree.FormalParameter NFormalParameter() {
        AbstractTree.Type type = NType();
        String name = null;
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            name = sym.getValue();
            sym = scanner.nextToken();
        } else {
            reportError();
        }

        return new AbstractTree.FormalParameter(type, name);
    }

    // NStatement ::= NType NDeclarationAssignments
    //             | NExpr
    //               (
    //                 ':=' NExpr
    //               | KW_THEN NStatements (KW_ELSE NStatements)? '.'
    //               | KW_LOOP NStatements '.'
    //               | '~' NExpr KW_LOOP IDENTIFIER NStatements '.'
    //               )?
    //             | KW_LOOP NStatements KW_WHILE NExpr '.'
    //             | KW_RETURN (NExpr)?
    public AbstractTree.Statement NStatement() {

        AbstractTree.Statement statement = null;

        if (first.get("NType").contains(sym.getTag())) {
            AbstractTree.Type type = NType();
            List<AbstractTree.DeclarationAssignment> declarationAssignments = NDeclarationAssignments();
            statement = new AbstractTree.DeclarationStatement(type, declarationAssignments);
        } else if (sym.getTag().equals(DomainTag.KW_LOOP)) {
            sym = scanner.nextToken();
            List<AbstractTree.Statement> body = NStatements();
            if (sym.getTag().equals(DomainTag.DOT)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
            AbstractTree.Expr condition = NExpr();
            if (sym.getTag().equals(DomainTag.DOT)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
            statement = new AbstractTree.PostWhileStatement(condition, body);
        } else if (sym.getTag().equals(DomainTag.KW_RETURN)) {
            sym = scanner.nextToken();
            AbstractTree.Expr expr = null;
            if (first.get("NExpr").contains(sym.getTag())) {
                expr = NExpr();
            }
            statement = new AbstractTree.ReturnStatement(expr);
        } else {
            AbstractTree.Expr expr = NExpr();
            if (sym.getTag().equals(DomainTag.ASSIGN)) {
                sym = scanner.nextToken();
                AbstractTree.Expr expr2 = NExpr();
                statement = new AbstractTree.AssignmentStatement(expr, expr2);
            } else if (sym.getTag().equals(DomainTag.KW_THEN)) {
                sym = scanner.nextToken();
                List<AbstractTree.Statement> thenBranch = NStatements();
                List<AbstractTree.Statement> elseBranch = new ArrayList<>();
                if (sym.getTag().equals(DomainTag.KW_ELSE)) {
                    sym = scanner.nextToken();
                    elseBranch = NStatements();
                }
                if (sym.getTag().equals(DomainTag.DOT)) {
                    sym = scanner.nextToken();
                } else {
                    reportError();
                }
                statement = new AbstractTree.IfStatement(expr, thenBranch, elseBranch);
            } else if (sym.getTag().equals(DomainTag.KW_LOOP)) {
                sym = scanner.nextToken();
                List<AbstractTree.Statement> body = NStatements();
                if (sym.getTag().equals(DomainTag.DOT)) {
                    sym = scanner.nextToken();
                } else {
                    reportError();
                }
                statement = new AbstractTree.PreWhileStatement(expr, body);
            } else if (sym.getTag().equals(DomainTag.TILDE)) {
                sym = scanner.nextToken();
                AbstractTree.Expr expr2 = NExpr();
                if (sym.getTag().equals(DomainTag.KW_LOOP)) {
                    sym = scanner.nextToken();
                } else {
                    reportError();
                }
                String variable = null;
                if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
                    variable = sym.getValue();
                    sym = scanner.nextToken();
                } else {
                    reportError();
                }
                List<AbstractTree.Statement> body = NStatements();
                if (sym.getTag().equals(DomainTag.DOT)) {
                    sym = scanner.nextToken();
                } else {
                    reportError();
                }
                statement = new AbstractTree.ForStatement(expr, expr2, variable, body);
            }
        }
        return statement;
    }

    // NDeclarationAssignments ::= NDeclarationAssignment (',' NDeclarationAssignment)*
    public List<AbstractTree.DeclarationAssignment> NDeclarationAssignments() {
        List<AbstractTree.DeclarationAssignment> declarationAssignments = new ArrayList<>();
        declarationAssignments.add(NDeclarationAssignment());
        while (sym.getTag().equals(DomainTag.COMMA)) {
            sym = scanner.nextToken();
            declarationAssignments.add(NDeclarationAssignment());
        }
        return declarationAssignments;
    }

    // NDeclarationAssignment ::= IDENTIFIER (':=' NArithmExpr)?
    public AbstractTree.DeclarationAssignment NDeclarationAssignment() {
        String name = null;
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            name = sym.getValue();
            sym = scanner.nextToken();
        } else {
            reportError();
        }

        if (sym.getTag().equals(DomainTag.ASSIGN)) {
            sym = scanner.nextToken();
            expr = NArithmExpr();
        }

        return new AbstractTree.DeclarationAssignment(name, expr);
    }

    // NType ::= (KW_INT | KW_CHAR | KW_BOOL) '[]'*
    public AbstractTree.Type NType() {
        AbstractTree.PrimType prim = null;
        if (sym.getTag().equals(DomainTag.KW_INT)) {
            prim = AbstractTree.PrimType.INT;
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.KW_CHAR)) {
            prim = AbstractTree.PrimType.CHAR;
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.KW_BOOL)){
            prim = AbstractTree.PrimType.BOOL;
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        int n = 0;
        while (sym.getTag().equals(DomainTag.BRACKETS)) {
            n++;
            sym = scanner.nextToken();
        }

        return new AbstractTree.Type(prim, n);
    }

    // NExpr ::= NAndExpr ('|' NAndExpr | '@' NAndExpr)*
    public AbstractTree.Expr NExpr() {
        AbstractTree.Expr expr = NAndExpr();
        AbstractTree.BinOpExpr binOpExpr = null;
        while (sym.getTag().equals(DomainTag.OR_XOR_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            AbstractTree.Expr expr2 = NAndExpr();
            if (binOpExpr != null) {
                binOpExpr = new AbstractTree.BinOpExpr(binOpExpr, op, expr2);
            } else {
                binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
            }
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NAndExpr ::= NCmpExpr ('&' NCmpExpr)*
    public AbstractTree.Expr NAndExpr() {
        AbstractTree.Expr expr = NCmpExpr();
        AbstractTree.BinOpExpr binOpExpr = null;
        while (sym.getTag().equals(DomainTag.AND_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            AbstractTree.Expr expr2 = NCmpExpr();
            if (binOpExpr != null) {
                binOpExpr = new AbstractTree.BinOpExpr(binOpExpr, op, expr2);
            } else {
                binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
            }
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NCmpExpr ::= NFuncCallExpr (NCmpOp NFuncCallExpr)*
    public AbstractTree.Expr NCmpExpr() {
        AbstractTree.Expr expr = NFuncCallExpr();
        AbstractTree.BinOpExpr binOpExpr = null;
        while (first.get("NCmpOp").contains(sym.getTag())) {
            String op = NCmpOp();
            AbstractTree.Expr expr2 = NFuncCallExpr();
            if (binOpExpr != null) {
                binOpExpr = new AbstractTree.BinOpExpr(binOpExpr, op, expr2);
            } else {
                binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
            }
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NCmpOp ::= EQ_OP | ORD_OP
    public String NCmpOp() {
        String op = "";
        if (sym.getTag().equals(DomainTag.EQ_OP)) {
            op = sym.getValue();
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.ORD_OP)) {
            op = sym.getValue();
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        return op;
    }

    // NFuncCallExpr ::= NArithmExpr ('<-' NArgs)?
    public AbstractTree.Expr NFuncCallExpr() {
        AbstractTree.Expr expr = null;
        expr = NArithmExpr();
        List<AbstractTree.Expr> args = new ArrayList<>();
        if (sym.getTag().equals(DomainTag.LEFT_ARROW)) {
            sym = scanner.nextToken();
            args = NArgs();
        }
        if (args.isEmpty()) {
            return expr;
        }
        return new AbstractTree.FunctionInvocationExpr(expr, args);
    }

    // NArgs ::= NArithmExpr (',' NArithmExpr)*
    public List<AbstractTree.Expr> NArgs() {
        List<AbstractTree.Expr> args = new ArrayList<>();
        args.add(NArithmExpr());
        while (sym.getTag().equals(DomainTag.COMMA)) {
            sym = scanner.nextToken();
            args.add(NArithmExpr());
        }
        return args;
    }

    // NArithmExpr ::= NTerm (('+' | '-') NTerm)*
    public AbstractTree.Expr NArithmExpr() {
        AbstractTree.Expr expr = NTerm();
        AbstractTree.BinOpExpr binOpExpr = null;
        while (sym.getTag().equals(DomainTag.PLUS_OP) || sym.getTag().equals(DomainTag.MINUS_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            AbstractTree.Expr expr2 = NTerm();
            if (binOpExpr != null) {
                binOpExpr = new AbstractTree.BinOpExpr(binOpExpr, op, expr2);
            } else {
                binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
            }
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NTerm ::= NFactor (('*' | '/' | '%') NFactor)*
    public AbstractTree.Expr NTerm() {
        AbstractTree.Expr expr = NFactor();
        AbstractTree.BinOpExpr binOpExpr = null;
        while (sym.getTag().equals(DomainTag.MUL_DIV_REM_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            AbstractTree.Expr expr2 = NFactor();
            if (binOpExpr != null) {
                binOpExpr = new AbstractTree.BinOpExpr(binOpExpr, op, expr2);
            } else {
                binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
            }
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NFactor ::= NPower ('^' NFactor)?
    public AbstractTree.Expr NFactor() {
        AbstractTree.Expr expr = NPower();
        AbstractTree.BinOpExpr binOpExpr = null;
        if (sym.getTag().equals(DomainTag.POWER_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            AbstractTree.Expr expr2 = NFactor();
            binOpExpr = new AbstractTree.BinOpExpr(expr, op, expr2);
        }
        if (binOpExpr == null) {
            return expr;
        }
        return binOpExpr;
    }

    // NPower ::= NArrExpr | (('!' | '-') NPower) | (NType NBottomExpr)
    public AbstractTree.Expr NPower() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.NOT_OP) || sym.getTag().equals(DomainTag.MINUS_OP)) {
            String op = sym.getValue();
            sym = scanner.nextToken();
            expr = new AbstractTree.UnOpExpr(op, NPower());
        } else if (first.get("NType").contains(sym.getTag())) {
            AbstractTree.Type type = NType();
            AbstractTree.Expr expr2 = NBottomExpr();
            expr = new AbstractTree.AllocExpr(type, expr2);
        } else {
            expr = NArrExpr();
        }

        return expr;
    }

    // NArrExpr ::= NBottomExpr | (NArrExpr NBottomExpr) | NStringConstant
    public AbstractTree.Expr NArrExpr() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            expr = NStringConstant();
        } else if (first.get("NBottomExpr").contains(sym.getTag())) {
            expr = NBottomExpr();
        } else {
            AbstractTree.Expr expr1 = NArrExpr();
            AbstractTree.Expr expr2 = NBottomExpr();
            expr = new AbstractTree.BinOpExpr(expr1, "at", expr2);
        }
        return expr;
    }

    // NBottomExpr ::= IDENTIFIER | NConst | ('(' NExpr ')')
    public AbstractTree.Expr NBottomExpr() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            String name = sym.getValue();
            sym = scanner.nextToken();
            expr = new AbstractTree.VariableExpr(name);
        } else if (sym.getTag().equals(DomainTag.LEFT_PAR)) {
            sym = scanner.nextToken();
            expr = NExpr();
            if (sym.getTag().equals(DomainTag.RIGHT_PAR)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
        } else {
            expr = NConst();
        }
        return expr;
    }

    // NStringConstant ::= STRING_SECTION STRING_SECTION*
    public AbstractTree.Expr NStringConstant() {
        List<String> sections = new ArrayList<>();
        if (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            sections.add(sym.getValue());
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        while (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            sections.add(sym.getValue());
            sym = scanner.nextToken();
        }
        return new AbstractTree.StringConstExpr(new AbstractTree.Type(AbstractTree.PrimType.CHAR, 1),
        sections);
    }

    // NConst ::= DECIMAL_INTEGER_CONSTANT | NON_DECIMAL_INTEGER_CONSTANT | SYMBOLIC_CONSTANT |
    BOOLEAN_CONSTANT | KW_NULL
    public AbstractTree.Expr NConst() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.DECIMAL_INTEGER_CONSTANT)
                || sym.getTag().equals(DomainTag.NON_DECIMAL_INTEGER_CONSTANT)
                || sym.getTag().equals(DomainTag.SYMBOLIC_CONSTANT)
                || sym.getTag().equals(DomainTag.BOOLEAN_CONSTANT)
                || sym.getTag().equals(DomainTag.KW_NULL)) {
            String value = sym.getValue();
            if (sym.getTag().equals(DomainTag.DECIMAL_INTEGER_CONSTANT) ||
            sym.getTag().equals(DomainTag.NON_DECIMAL_INTEGER_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.INT, 0),
                value);
            } else if (sym.getTag().equals(DomainTag.SYMBOLIC_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.CHAR, 0),
                value);
            } else if (sym.getTag().equals(DomainTag.BOOLEAN_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.BOOL, 0),
                value);
            } else if (sym.getTag().equals(DomainTag.KW_NULL)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(null, 0), value);
            }
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        return expr;
    }
}

```

Position.java
```java
public class Position implements Comparable<Position> {
    private int line;
    private int column;
    private int index;

    public Position(int line, int column, int index) {
        this.line = line;
        this.column = column;
        this.index = index;
    }

    @Override
    public int compareTo(Position other) {
        return Integer.compare(this.index, other.index);
    }

    @Override
    public String toString() {
        return "(" + line + ", " + column + ", " + index + ")";
    }

    public String getSimplifiedString() {
        return "(" + line + ", " + column + ")";
    }
}
```

PrettyBuilder.java
```java
import java.util.List;

public class PrettyBuilder {
    private StringBuilder stringBuilder;
    private int indent;
    private int count;


    public PrettyBuilder(String name, int indent) {
        stringBuilder = new StringBuilder();
        stringBuilder.append(name).append("(");
        this.indent = indent + stringBuilder.length();
        count = 0;
    }
    public PrettyBuilder append(String name, List<? extends AbstractTree.PrettyPrintable> list) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=[");
        int size = indent + name.length() + 2;
        if (!list.isEmpty()) {
            builder.append(list.get(0).toString(size));
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) != null) {
                    builder.append(",\n").append(" ".repeat(Math.max(0,
                    size))).append(list.get(i).toString(size));
                } else {
                    builder.append(",\n").append(" ".repeat(Math.max(0, size))).append("null");
                }
            }
        }
        builder.append("]");
        stringBuilder.append(builder);
        count++;
        return this;
    }

    public PrettyBuilder append(String name, AbstractTree.PrettyPrintable prettyPrintable) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=");
        int size = indent + name.length() + 1;
        builder.append(prettyPrintable.toString(size));
        stringBuilder.append(builder);
        count++;
        return this;
    }

    public PrettyBuilder append(String name, String value) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=");
        builder.append(value);
        stringBuilder.append(builder);
        count++;
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString() + ")";
    }
}

```

Scanner.java
```java
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Scanner {
    private final String input;
    private int pos;

    private final static String ESCAPE_SEQUENCES_REGEX = "%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%";

    private final static String IDENTIFIER_REGEX = "(\\{(\\w|[ ])+\\})";
    private final static String DECIMAL_INTEGER_CONSTANT_REGEX = "([0-9]+)";
    private final static String NON_DECIMAL_INTEGER_CONSTANT_REGEX =
    "(([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))?)";

    private final static String SYMBOLIC_CONSTANT_REGEX = "([$](\"(.|" + ESCAPE_SEQUENCES_REGEX +
    ")\")|[A-F0-9]+)";

    private final static String STRING_SECTION_REGEX = "((\"([^\"\n]|" + ESCAPE_SEQUENCES_REGEX +
    ")*\")|(%[A-F0-9]+))";

    private final static String BOOLEAN_CONSTANT_REGEX = "(true|false)";

    private final static String OR_XOR_OP_REGEX = "[|@]";
    private final static String AND_OP_REGEX = "[&]";
    private final static String EQ_OP_REGEX = "([!=]=)";
    private final static String ORD_OP_REGEX = "([<>]=?)";
    private final static String PLUS_OP_REGEX = "[+]";
    private final static String MINUS_OP_REGEX = "-";

    private final static String MUL_DIV_REM_OP_REGEX = "[*/%]";
    private final static String POWER_OP_REGEX = "\\^";
    private final static String NOT_OP_REGEX = "!";
    private final static String EQUAL_REGEX = "[=]";
    private final static String ASSIGN_REGEX = "(:=)";
    private final static String DOT_REGEX = "[.]";
    private final static String LEFT_ARROW_REGEX = "(<-)";
    private final static String COMMA_REGEX = ",";
    private final static String TILDE_REGEX = "~";
    private final static String BRACKETS_REGEX = "(\\[])";
    private final static String LEFT_PAR_REGEX = "[(]";
    private final static String RIGHT_PAR_REGEX = "[)]";
    private final static String SEMICOLON_REGEX = ";";
    private final static String KW_INT_REGEX = "(int)";
    private final static String KW_BOOL_REGEX = "(bool)";
    private final static String KW_RETURN_REGEX = "(return)";
    private final static String KW_VOID_REGEX = "(void)";
    private final static String KW_CHAR_REGEX = "(char)";
    private final static String KW_LOOP_REGEX = "(loop)";
    private final static String KW_THEN_REGEX = "(then)";
    private final static String KW_ELSE_REGEX = "(else)";
    private final static String KW_NULL_REGEX = "(null)";
    private final static String KW_WHILE_REGEX = "(while)";
    private final static String SPACES_REGEX = "(\\s+)";
    private final static String COMMENTS_REGEX = "((##.*$)|(#.*#))";


    private static final Pattern PATTERN = Pattern.compile(
              IDENTIFIER_REGEX + "|" + DECIMAL_INTEGER_CONSTANT_REGEX + "|" +
              NON_DECIMAL_INTEGER_CONSTANT_REGEX + "|" +
                    SYMBOLIC_CONSTANT_REGEX + "|" + STRING_SECTION_REGEX + "|" + BOOLEAN_CONSTANT_REGEX
                    + "|" +
                    OR_XOR_OP_REGEX + "|" + AND_OP_REGEX + "|" + EQ_OP_REGEX + "|" + LEFT_ARROW_REGEX +
                    "|" + PLUS_OP_REGEX + "|" + MINUS_OP_REGEX + "|" +
                    MUL_DIV_REM_OP_REGEX + "|" + POWER_OP_REGEX + "|" + NOT_OP_REGEX + "|" + EQUAL_REGEX
                    + "|" + ASSIGN_REGEX + "|" + DOT_REGEX + "|" +
                    ORD_OP_REGEX + "|" + COMMA_REGEX + "|" + TILDE_REGEX + "|" + BRACKETS_REGEX + "|" +
                    LEFT_PAR_REGEX + "|" + RIGHT_PAR_REGEX + "|" + SEMICOLON_REGEX + "|" +
                    KW_INT_REGEX + "|" + KW_BOOL_REGEX + "|" + KW_RETURN_REGEX + "|" +
                    KW_VOID_REGEX + "|" + KW_CHAR_REGEX + "|" + KW_LOOP_REGEX + "|" +
                    KW_THEN_REGEX + "|" + KW_ELSE_REGEX + "|" + KW_NULL_REGEX + "|" + KW_WHILE_REGEX + "|" +
                    SPACES_REGEX + "|" + COMMENTS_REGEX + "|."
    );

    public Scanner(String input) {
        this.input = input;
        this.pos = 0;
    }

    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token("$", DomainTag.END,
                    new FragmentPosition(findPosition(input, input.length()), findPosition(input,
                    input.length())));
        }

        Matcher matcher = PATTERN.matcher(input);
        if (matcher.find(pos)) {
            String token = matcher.group();
            int endPos = matcher.end();
            FragmentPosition fragmentPosition = new FragmentPosition(findPosition(input, pos),
            findPosition(input, endPos - 1));
            pos = endPos;
            return determineToken(token, fragmentPosition);
        }

        throw new RuntimeException("LEX_ERROR: " + new FragmentPosition(findPosition(input, pos),
        findPosition(input, pos)));
    }

    private Token determineToken(String token, FragmentPosition fragmentPosition) {
        if (token.matches(IDENTIFIER_REGEX)) {
            return new Token(token, DomainTag.IDENTIFIER, fragmentPosition);
        } else if (token.matches(DECIMAL_INTEGER_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.DECIMAL_INTEGER_CONSTANT, fragmentPosition);
        } else if (token.matches(NON_DECIMAL_INTEGER_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.NON_DECIMAL_INTEGER_CONSTANT, fragmentPosition);
        } else if (token.matches(SYMBOLIC_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.SYMBOLIC_CONSTANT, fragmentPosition);
        } else if (token.matches(STRING_SECTION_REGEX)) {
            return new Token(token, DomainTag.STRING_SECTION, fragmentPosition);
        } else if (token.matches(BOOLEAN_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.BOOLEAN_CONSTANT, fragmentPosition);
        } else if (token.matches(OR_XOR_OP_REGEX)) {
            return new Token(token, DomainTag.OR_XOR_OP, fragmentPosition);
        } else if (token.matches(AND_OP_REGEX)) {
            return new Token(token, DomainTag.AND_OP, fragmentPosition);
        } else if (token.matches(EQ_OP_REGEX)) {
            return new Token(token, DomainTag.EQ_OP, fragmentPosition);
        } else if (token.matches(ORD_OP_REGEX)) {
            return new Token(token, DomainTag.ORD_OP, fragmentPosition);
        } else if (token.matches(PLUS_OP_REGEX)) {
            return new Token(token, DomainTag.PLUS_OP, fragmentPosition);
        } else if (token.matches(MINUS_OP_REGEX)) {
            return new Token(token, DomainTag.MINUS_OP, fragmentPosition);
        } else if (token.matches(MUL_DIV_REM_OP_REGEX)) {
            return new Token(token, DomainTag.MUL_DIV_REM_OP, fragmentPosition);
        } else if (token.matches(POWER_OP_REGEX)) {
            return new Token(token, DomainTag.POWER_OP, fragmentPosition);
        } else if (token.matches(NOT_OP_REGEX)) {
            return new Token(token, DomainTag.NOT_OP, fragmentPosition);
        } else if (token.matches(EQUAL_REGEX)) {
            return new Token(token, DomainTag.EQUAL, fragmentPosition);
        } else if (token.matches(ASSIGN_REGEX)) {
            return new Token(token, DomainTag.ASSIGN, fragmentPosition);
        } else if (token.matches(DOT_REGEX)) {
            return new Token(token, DomainTag.DOT, fragmentPosition);
        } else if (token.matches(LEFT_ARROW_REGEX)) {
            return new Token(token, DomainTag.LEFT_ARROW, fragmentPosition);
        } else if (token.matches(COMMA_REGEX)) {
            return new Token(token, DomainTag.COMMA, fragmentPosition);
        } else if (token.matches(TILDE_REGEX)) {
            return new Token(token, DomainTag.TILDE, fragmentPosition);
        } else if (token.matches(BRACKETS_REGEX)) {
            return new Token(token, DomainTag.BRACKETS, fragmentPosition);
        } else if (token.matches(LEFT_PAR_REGEX)) {
            return new Token(token, DomainTag.LEFT_PAR, fragmentPosition);
        } else if (token.matches(RIGHT_PAR_REGEX)) {
            return new Token(token, DomainTag.RIGHT_PAR, fragmentPosition);
        } else if (token.matches(SEMICOLON_REGEX)) {
            return new Token(token, DomainTag.SEMICOLON, fragmentPosition);
        } else if (token.matches(KW_INT_REGEX)) {
            return new Token(token, DomainTag.KW_INT, fragmentPosition);
        } else if (token.matches(KW_BOOL_REGEX)) {
            return new Token(token, DomainTag.KW_BOOL, fragmentPosition);
        } else if (token.matches(KW_RETURN_REGEX)) {
            return new Token(token, DomainTag.KW_RETURN, fragmentPosition);
        } else if (token.matches(KW_VOID_REGEX)) {
            return new Token(token, DomainTag.KW_VOID, fragmentPosition);
        } else if (token.matches(KW_CHAR_REGEX)) {
            return new Token(token, DomainTag.KW_CHAR, fragmentPosition);
        } else if (token.matches(KW_LOOP_REGEX)) {
            return new Token(token, DomainTag.KW_LOOP, fragmentPosition);
        } else if (token.matches(KW_THEN_REGEX)) {
            return new Token(token, DomainTag.KW_THEN, fragmentPosition);
        } else if (token.matches(KW_ELSE_REGEX)) {
            return new Token(token, DomainTag.KW_ELSE, fragmentPosition);
        } else if (token.matches(KW_NULL_REGEX)) {
            return new Token(token, DomainTag.KW_NULL, fragmentPosition);
        } else if (token.matches(KW_WHILE_REGEX)) {
            return new Token(token, DomainTag.KW_WHILE, fragmentPosition);
        } else if (token.matches(COMMENTS_REGEX)) {
            return nextToken();
        } else if (token.matches(SPACES_REGEX)) {
            return nextToken();
        }
        System.out.println(token);
        throw new RuntimeException("LEX_ERROR: " + fragmentPosition);
    }

    public static Position findPosition(String text, int index) {
        int line = 1;
        int col = 1;
        for (int i = 0; i < index; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        return new Position(line, col, index);
    }
}

```

Token.java
```java
public class Token {

    private String value;
    private DomainTag tag;
    private FragmentPosition fragmentPosition;

    public Token(String value, DomainTag tag, FragmentPosition fragmentPosition) {
        this.value = value;
        this.tag = tag;
        this.fragmentPosition = fragmentPosition;
    }

    public String getValue() {
        return value;
    }

    public DomainTag getTag() {
        return tag;
    }


    public FragmentPosition getFragmentPosition() {
        return fragmentPosition;
    }
}
```

FragmentPosition.java
```java
public class FragmentPosition {
    private Position startPosition;
    private Position endPosition;

    public FragmentPosition(Position startPosition, Position endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return startPosition.toString() + "-" + endPosition.toString();
    }

    public String getSimplifiedString() {
        return startPosition.getSimplifiedString() + "-" + endPosition.getSimplifiedString();
    }
}
```

DomainTag.java
```java
public enum DomainTag {
    IDENTIFIER, DECIMAL_INTEGER_CONSTANT, NON_DECIMAL_INTEGER_CONSTANT, SYMBOLIC_CONSTANT,
    STRING_SECTION, BOOLEAN_CONSTANT,
    OR_XOR_OP, AND_OP, EQ_OP, ORD_OP, PLUS_OP, MINUS_OP, MUL_DIV_REM_OP, POWER_OP, NOT_OP,
    EQUAL, ASSIGN, DOT, LEFT_ARROW, COMMA, TILDE, BRACKETS, LEFT_PAR, RIGHT_PAR, SEMICOLON,
    KW_INT, KW_BOOL, KW_RETURN, KW_VOID, KW_CHAR, KW_LOOP, KW_THEN, KW_ELSE, KW_NULL, KW_WHILE, END, NONE
}
```

AbstractTree.java
```java
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
    public record FunctionDeclaration(FunctionHeader header, List<Statement> body) implements
    PrettyPrintable {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("FunctionDeclaration", indent);
            builder.append("header", header).append("body", body);
            return builder.toString();
        }
    }
    public record FunctionHeader(Type type, String name, List<FormalParameter> formalParameters)
    implements PrettyPrintable {
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
    public record FunctionInvocationExpr(Expr expr, List<Expr> actualParameters) implements Expr {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("FunctionInvocationExpr", indent);
            builder.append("Expr", expr).append("actualParameters", actualParameters);
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

    public record DeclarationStatement(Type type, List<DeclarationAssignment> declarationAssignments)
    implements Statement {
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

    public record IfStatement(Expr condition, List<Statement> thenBranch, List<Statement> elseBranch)
    implements Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("IfStatement", indent);
            builder.append("condition", condition).append("thenBranch", thenBranch).append("elseBranch",
            elseBranch);
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

    public record ForStatement(Expr start, Expr end, String variable, List<Statement> body) implements
    Statement {
        @Override
        public String toString(int indent) {
            PrettyBuilder builder = new PrettyBuilder("ForStatement", indent);
            builder.append("start", start).append("end", end).append("variable",
            variable).append("body", body);
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

```

# Тестирование

Входные данные

```
#comment#
int {f} <- int {a} =
int {b}, {c} := 10;
{c} > 5 then
    {c} := 0
else
    int {d} := ({Main} <- {a});
    {d} := 5.;

int {i} := 0;
1 ~ 10 loop {i}
    {a} := 10.;

return {param}.

int {Main} <- char[][] {args} =
return 2 + 2.
```

Вывод на `stdout`

<!-- ENABLE LONG LINES -->

```
Program(functionDeclarations=[FunctionDeclaration(header=FunctionHeader(type=Type[base=INT, arrayLevel=0],
                                                                        name={f},
                                                                        formalParameters=[FormalParameter[type=Type[base=INT, arrayLevel=0], name={a}]]),
                                                  body=[DeclarationStatement(type=Type[base=INT, arrayLevel=0],
                                                                             declarationAssignments=[DeclarationAssignment[name={b}, expr=null],
                                                                                                     DeclarationAssignment[name={c}, expr=ConstExpr[type=Type[base=INT, arrayLevel=0], value=10]]]),
                                                        IfStatement(condition=BinOpExpr(left=VariableExpr(name={c}),
                                                                                        op=>,
                                                                                        right=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                        value=5)),
                                                                    thenBranch=[AssignmentStatement(left=VariableExpr(name={c}),
                                                                                                    right=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                                    value=0))],
                                                                    elseBranch=[DeclarationStatement(type=Type[base=INT, arrayLevel=0],
                                                                                                     declarationAssignments=[DeclarationAssignment[name={d}, expr=FunctionInvocationExpr[expr=VariableExpr[name={Main}], actualParameters=[VariableExpr[name={a}]]]]]),
                                                                                AssignmentStatement(left=VariableExpr(name={d}),
                                                                                                    right=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                                    value=5))]),
                                                        DeclarationStatement(type=Type[base=INT, arrayLevel=0],
                                                                             declarationAssignments=[DeclarationAssignment[name={i}, expr=ConstExpr[type=Type[base=INT, arrayLevel=0], value=0]]]),
                                                        ForStatement(start=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                     value=1),
                                                                     end=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                   value=10),
                                                                     variable={i},
                                                                     body=[AssignmentStatement(left=VariableExpr(name={a}),
                                                                                               right=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                               value=10))]),
                                                        ReturnStatement(expr=VariableExpr(name={param}))]),
                              FunctionDeclaration(header=FunctionHeader(type=Type[base=INT, arrayLevel=0],
                                                                        name={Main},
                                                                        formalParameters=[FormalParameter[type=Type[base=CHAR, arrayLevel=2], name={args}]]),
                                                  body=[ReturnStatement(expr=BinOpExpr(left=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                      value=2),
                                                                                       op=+,
                                                                                       right=ConstExpr(type=Type[base=INT, arrayLevel=0],
                                                                                                       value=2)))])])

```

# Вывод
В ходе выполнения лабораторной работы изучил алгоритмы построения парсеров методом рекурсивного спуска.