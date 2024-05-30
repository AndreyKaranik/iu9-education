import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Scanner {
    private final String input;
    private int pos;

    private static final Pattern PATTERN = Pattern.compile(
            "(\"[^\"]*\")|([^\\s\\[\\]:,@\"{}]+)|(\\{[A-Z]+\\})|\\[|\\]|:|@|,|\\s+|."
    );

    public Scanner(String input) {
        this.input = input;
        this.pos = 0;
    }

    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token("$", DomainTag.END, "$");
        }

        Matcher matcher = PATTERN.matcher(input);
        if (matcher.find(pos)) {
            String token = matcher.group();
            pos = matcher.end();
            return determineToken(token);
        }

        return new Token("", DomainTag.NONE, "");
    }

    private Token determineToken(String token) {
        if (token.matches("\"[^\"]*\"")) {
            return new Token(token, DomainTag.TERMINAL, "terminal");
        } else if (token.matches("[^\\s\\[\\]:,@\"{}]+(?:[\\s,]|$)")) {
            return new Token(token, DomainTag.NONTERMINAL, "nonterminal");
        } else if (token.matches("\\{[A-Z]+\\}")) {
            return new Token(token, DomainTag.START_SYMBOL, "start");
        } else if (token.equals("[")) {
            return new Token(token, DomainTag.LEFT_BRACKET, "[");
        } else if (token.equals("]")) {
            return new Token(token, DomainTag.RIGHT_BRACKET, "]");
        } else if (token.equals(":")) {
            return new Token(token, DomainTag.COLON, ":");
        } else if (token.equals("@")) {
            return new Token(token, DomainTag.EMPTY_STRING, "@");
        } else if (token.equals(",")) {
            return new Token(token, DomainTag.COMMA, ",");
        } else if (token.trim().isEmpty()) {
            return nextToken();
        }
        return new Token(token, DomainTag.NONE, "");
    }
}
