import java.util.HashSet;
import java.util.Map;

public class Parser {

    private Scanner scanner;
    private Token sym;
    private Map<String, HashSet<DomainTag>> first;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        sym = scanner.nextToken();
        NProgram();
    }

    public void reportError() {
        throw new RuntimeException(sym.getFragmentPosition().toString());
    }

    // NProgram ::= NFunctionDeclarations
    public void NProgram() {
        NFunctionDeclarations();
    }

    // NFunctionDeclarations ::= NFunctionDeclaration+
    public void NFunctionDeclarations() {
        do {
            NFunctionDeclaration();
        } while (first.get("NFunctionDeclaration").contains(sym.getTag()));
    }

    // NFunctionDeclaration ::= NFunctionHeader '=' NStatements '.'
    public void NFunctionDeclaration() {
        NFunctionHeader();
        if (sym.getTag().equals(DomainTag.EQUAL)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        NStatements();
        if (sym.getTag().equals(DomainTag.DOT)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }

    // NFunctionHeader ::= NFunctionHeaderTypeName ('<-' NFormalParameters)?
    public void NFunctionHeader() {
        NFunctionHeaderTypeName();
        if (sym.getTag().equals(DomainTag.LEFT_ARROW)) {
            sym = scanner.nextToken();
            NFormalParameters();
        }
    }

    // NStatements ::= NStatement (';' NStatement)*
    public void NStatements() {
        NStatement();
        while (sym.getTag().equals(DomainTag.SEMICOLON)) {
            sym = scanner.nextToken();
            NStatement();
        }
    }

    // (NType | KW_VOID) IDENTIFIER
    public void NFunctionHeaderTypeName() {
        if (first.get("NType").contains(sym.getTag())) {
            NType();
        } else {
            if (sym.getTag().equals(DomainTag.KW_VOID)) {
                sym = scanner.nextToken();
            } else {
                reportError();
            }
        }

        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }

    // NFormalParameters ::= NFormalParameter (',' NFormalParameter)*
    public void NFormalParameters() {
        NFormalParameter();
        while (sym.getTag().equals(DomainTag.COMMA)) {
            sym = scanner.nextToken();
            NFormalParameter();
        }
    }

    // NFormalParameter ::= NType IDENTIFIER
    public void NFormalParameter() {
        NType();
        if (sym.getTag().equals(DomainTag.IDENTIFIER)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }

    public void NStatement() {
        if (sym.getTag().equals(DomainTag.KW_RETURN)) {
            sym = scanner.nextToken();
        } else {
            reportError();
        }
    }

    // NType ::= (KW_INT | KW_CHAR | KW_BOOL) '[]'*
    public void NType() {
        if (sym.getTag().equals(DomainTag.KW_INT)) {
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.KW_CHAR)) {
            sym = scanner.nextToken();
        } else if (sym.getTag().equals(DomainTag.KW_BOOL)){
            sym = scanner.nextToken();
        } else {
            reportError();
        }
        while (sym.getTag().equals(DomainTag.BRACKETS)) {
            sym = scanner.nextToken();
        }
    }
}
