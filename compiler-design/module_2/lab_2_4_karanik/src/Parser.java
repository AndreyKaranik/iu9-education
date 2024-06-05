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

    // NFuncCallExpr ::= NArithmExpr | (IDENTIFIER '<-' NArgs)
    public AbstractTree.Expr NFuncCallExpr() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            String name = sym.getValue();
            sym = scanner.nextToken();
            if (sym.getTag().equals(DomainTag.LEFT_ARROW)) {
                sym = scanner.nextToken();
                List<AbstractTree.Expr> args = NArgs();
                expr = new AbstractTree.FunctionInvocationExpr(name, args);
            } else {
                reportError();
            }
        } else {
            expr = NArithmExpr();
        }
        return expr;
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
        return new AbstractTree.StringConstExpr(new AbstractTree.Type(AbstractTree.PrimType.CHAR, 1), sections);
    }

    // NConst ::= DECIMAL_INTEGER_CONSTANT | NON_DECIMAL_INTEGER_CONSTANT | SYMBOLIC_CONSTANT | BOOLEAN_CONSTANT | KW_NULL
    public AbstractTree.Expr NConst() {
        AbstractTree.Expr expr = null;
        if (sym.getTag().equals(DomainTag.DECIMAL_INTEGER_CONSTANT)
                || sym.getTag().equals(DomainTag.NON_DECIMAL_INTEGER_CONSTANT)
                || sym.getTag().equals(DomainTag.SYMBOLIC_CONSTANT)
                || sym.getTag().equals(DomainTag.BOOLEAN_CONSTANT)
                || sym.getTag().equals(DomainTag.KW_NULL)) {
            String value = sym.getValue();
            if (sym.getTag().equals(DomainTag.DECIMAL_INTEGER_CONSTANT) || sym.getTag().equals(DomainTag.NON_DECIMAL_INTEGER_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.INT, 0), value);
            } else if (sym.getTag().equals(DomainTag.SYMBOLIC_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.CHAR, 0), value);
            } else if (sym.getTag().equals(DomainTag.BOOLEAN_CONSTANT)) {
                expr = new AbstractTree.ConstExpr(new AbstractTree.Type(AbstractTree.PrimType.BOOL, 0), value);
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
