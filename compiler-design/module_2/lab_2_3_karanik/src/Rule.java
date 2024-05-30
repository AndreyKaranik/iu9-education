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
        StringBuilder builder = new StringBuilder();
        for (Symbol symbol : right) {
            builder.append(symbol.toString()).append(' ');
        }
        return left.getValue() + " -> " + builder.toString();
    }
}
