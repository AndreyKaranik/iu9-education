import java.util.ArrayList;
import java.util.List;

public class Rule {
    private Symbol left;
    private List<Symbol> right;

    public Rule(Symbol left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }

    public Symbol getLeft() {
        return left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left.getValue() + " -> " + right.toString();
    }

    public String toSpecificString() {
        String str = GrammarConverter.convert(left.getValue());

        List<String> list = new ArrayList<>();
        for (Symbol symbol : right) {
            list.add(GrammarConverter.convert(symbol.getValue()));
        }
        return str + " -> " + list;
    }
}
