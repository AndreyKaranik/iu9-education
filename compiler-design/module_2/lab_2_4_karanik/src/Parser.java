import java.util.*;

public class Parser {

    private Scanner scanner;
    private Token sym;
    private Map<String, HashSet<DomainTag>> first;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        initializeMinFirstSet();
        sym = scanner.nextToken();
        AbstractTree tree = new AbstractTree(NProgram());
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
}
