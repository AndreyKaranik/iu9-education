import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Scanner {
    private final String input;
    private int pos;

    private final static String ESCAPE_SEQUENCES_REGEX = "%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%";

    private final static String IDENTIFIER_REGEX = "(\\{(\\w|[ ])+\\})";
    private final static String DECIMAL_INTEGER_CONSTANT_REGEX = "[0-9]+";
    private final static String NON_DECIMAL_INTEGER_CONSTANT_REGEX = "([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))?";

    private final static String SYMBOLIC_CONSTANT_REGEX = "([$](\"(.|" + ESCAPE_SEQUENCES_REGEX + ")\")|[A-F0-9]+)";

    private final static String STRING_SECTION_REGEX = "(\"([^\"\n]|" + ESCAPE_SEQUENCES_REGEX + ")*\")|(%[A-F0-9]+)";

    private final static String BOOLEAN_CONSTANT_REGEX = "true|false";

    private final static String REFERENCE_NULL_CONSTANT = "null";

    private static final Pattern PATTERN = Pattern.compile(
            IDENTIFIER_REGEX + "|" + DECIMAL_INTEGER_CONSTANT_REGEX + "|" + NON_DECIMAL_INTEGER_CONSTANT_REGEX + "|" +
                    SYMBOLIC_CONSTANT_REGEX + "|" + STRING_SECTION_REGEX + "|" + BOOLEAN_CONSTANT_REGEX + "|" +
                    REFERENCE_NULL_CONSTANT + "|\\s+|(##.*$)|(#.*#)|."
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
    }

    private Token determineToken(String token, FragmentPosition fragmentPosition) {
        if (token.matches(IDENTIFIER_REGEX)) {
            return new Token(token, DomainTag.IDENTIFIER, fragmentPosition);
        } else if (token.matches(DECIMAL_INTEGER_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.DECIMAL_INTEGER_CONSTANT, fragmentPosition);
        } else if (token.matches(NON_DECIMAL_INTEGER_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.NON_DECIMAL_INTEGER_CONSTANT, fragmentPosition);
        } else if (token.trim().isEmpty()) {
            return nextToken();
        }
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
