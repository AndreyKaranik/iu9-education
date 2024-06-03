import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Scanner {
    private final String input;
    private int pos;

    private static final Pattern PATTERN = Pattern.compile(
            "(\"[^\"]*\")|([^\\s\\[\\]:,@\"{}]+)|\\{|\\}|\\[|\\]|:|@|,|\\s+|."
    );

    public Scanner(String input) {
        this.input = input;
        this.pos = 0;
    }

    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token("$", DomainTag.END, "$",
                    new FragmentPosition(findPosition(input, input.length()), findPosition(input, input.length())));
        }

        Matcher matcher = PATTERN.matcher(input);
        if (matcher.find(pos)) {
            String token = matcher.group();
            int endPos = matcher.end();
            FragmentPosition fragmentPosition = new FragmentPosition(findPosition(input, pos), findPosition(input, endPos - 1));
            pos = endPos;
            return determineToken(token, fragmentPosition);
        }

        throw new RuntimeException("LEX_ERROR: " + new FragmentPosition(findPosition(input, pos), findPosition(input, pos)));
//        return new Token("", DomainTag.NONE, "",
//                new FragmentPosition(findPosition(input, pos), findPosition(input, pos)));
    }

    private Token determineToken(String token, FragmentPosition fragmentPosition) {
        if (token.matches("\"[^\"]*\"")) {
            return new Token(token, DomainTag.TERMINAL, "terminal", fragmentPosition);
        } else if (token.matches("[^\\s\\[\\]:,@\"{}]+(?:[\\s,]|$)")) {
            return new Token(token, DomainTag.NONTERMINAL, "nonterminal", fragmentPosition);
        } else if (token.equals("{")) {
            return new Token(token, DomainTag.LEFT_CURLY_BRACE, "{", fragmentPosition);
        } else if (token.equals("}")) {
            return new Token(token, DomainTag.RIGHT_CURLY_BRACE, "}", fragmentPosition);
        } else if (token.equals("[")) {
            return new Token(token, DomainTag.LEFT_BRACKET, "[", fragmentPosition);
        } else if (token.equals("]")) {
            return new Token(token, DomainTag.RIGHT_BRACKET, "]", fragmentPosition);
        } else if (token.equals(":")) {
            return new Token(token, DomainTag.COLON, ":", fragmentPosition);
        } else if (token.equals("@")) {
            return new Token(token, DomainTag.EMPTY_STRING, "@", fragmentPosition);
        } else if (token.equals(",")) {
            return new Token(token, DomainTag.COMMA, ",", fragmentPosition);
        } else if (token.trim().isEmpty()) {
            return nextToken();
        }
        throw new RuntimeException("LEX_ERROR: " + fragmentPosition);
        //return new Token(token, DomainTag.NONE, "", fragmentPosition);
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
