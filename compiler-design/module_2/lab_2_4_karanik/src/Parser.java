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

    public AbstractTree.Statement NStatement() {
        if (sym.getTag().equals(DomainTag.KW_RETURN)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        return new AbstractTree.ReturnStatement(null);
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
    public void NExpr() {
        NAndExpr();
        while (sym.getTag().equals(DomainTag.OR_XOR_OP)) {
            sym = scanner.nextToken();
            NAndExpr();
        }
    }

    // NAndExpr ::= NCmpExpr ('&' NCmpExpr)*
    public void NAndExpr() {
        NCmpExpr();
        while (sym.getTag().equals(DomainTag.AND_OP)) {
            sym = scanner.nextToken();
            NCmpExpr();
        }
    }

    // NCmpExpr ::= NFuncCallExpr (NCmpOp NFuncCallExpr)*
    public void NCmpExpr() {
        NFuncCallExpr();
        while (first.get("NCmpOp").contains(sym.getTag())) {
            NCmpOp();
            NFuncCallExpr();
        }
    }

    // NCmpOp ::= EQ_OP | ORD_OP
    public void NCmpOp() {
        if (sym.getTag().equals(DomainTag.EQ_OP)) {
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.ORD_OP)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }

    // NFuncCallExpr ::= NArithmExpr | (IDENTIFIER '<-' NArgs)
    public void NFuncCallExpr() {
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            sym = scanner.nextToken();
            if (sym.getTag().equals(DomainTag.LEFT_ARROW)) {
                sym = scanner.nextToken();
                NArgs();
            } else {
                reportError();
            }
        } else {
            NArithmExpr();
        }
    }

    // NArgs ::= NArithmExpr (',' NArithmExpr)*
    public void NArgs() {
        NArithmExpr();
        while (sym.getTag().equals(DomainTag.COMMA)) {
            sym = scanner.nextToken();
            NArithmExpr();
        }
    }

    // NArithmExpr ::= NTerm (('+' | '-') NTerm)*
    public void NArithmExpr() {
        NTerm();
        while (sym.getTag().equals(DomainTag.PLUS_MINUS_OP)) {
            sym = scanner.nextToken();
            NTerm();
        }
    }

    // NTerm ::= NFactor (('*' | '/' | '%') NFactor)*
    public void NTerm() {
        NFactor();
        while (sym.getTag().equals(DomainTag.MUL_DIV_REM_OP)) {
            sym = scanner.nextToken();
            NFactor();
        }
    }

    // NFactor ::= NPower ('^' NFactor)?
    public void NFactor() {
        NPower();
        if (sym.getTag().equals(DomainTag.POWER_OP)) {
            sym = scanner.nextToken();
            NFactor();
        }
    }

    // NPower ::= NArrExpr | (('!' | '-') NPower) | (NType NBottomExpr)
    public void NPower() {
        if (sym.getTag().equals(DomainTag.NOT_MINUS_OP)) {
            sym = scanner.nextToken();
        } else if (first.get("NType").contains(sym.getTag())) {
            NType();
            NBottomExpr();
        } else {
            NArrExpr();
        }
    }

    // NArrExpr ::= NBottomExpr | (NArrExpr NBottomExpr) | NStringConstant
    public void NArrExpr() {
        if (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            NStringConstant();
        } else if (first.get("NBottomExpr").contains(sym.getTag())) {
            NBottomExpr();
        } else {
            NArrExpr();
            NBottomExpr();
        }
    }

    // NBottomExpr ::= IDENTIFIER | NConst | ('(' NExpr ')')
    public void NBottomExpr() {
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.LEFT_PAR)) {
            sym = scanner.nextToken();
            NExpr();
            if (sym.getTag().equals(DomainTag.RIGHT_PAR)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
        } else {
            NConst();
        }
    }

    // NStringConstant ::= STRING_SECTION STRING_SECTION*
    public void NStringConstant() {
        if (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        while (sym.getTag().equals(DomainTag.STRING_SECTION)) {
            sym = scanner.nextToken();
        }
    }

    // NConst ::= DECIMAL_INTEGER_CONSTANT | NON_DECIMAL_INTEGER_CONSTANT | SYMBOLIC_CONSTANT | BOOLEAN_CONSTANT | KW_NULL
    public void NConst() {
        if (sym.getTag().equals(DomainTag.DECIMAL_INTEGER_CONSTANT)
                | sym.getTag().equals(DomainTag.NON_DECIMAL_INTEGER_CONSTANT)
                | sym.getTag().equals(DomainTag.SYMBOLIC_CONSTANT)
                | sym.getTag().equals(DomainTag.BOOLEAN_CONSTANT)
                | sym.getTag().equals(DomainTag.KW_NULL)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }
}
