import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Scanner {
    private final String input;
    private int pos;

    private final static String ESCAPE_SEQUENCES_REGEX = "%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%";

    private final static String IDENTIFIER_REGEX = "(\\{(\\w|[ ])+\\})";
    private final static String DECIMAL_INTEGER_CONSTANT_REGEX = "([0-9]+)";
    private final static String NON_DECIMAL_INTEGER_CONSTANT_REGEX = "([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))?";

    private final static String SYMBOLIC_CONSTANT_REGEX = "([$](\"(.|" + ESCAPE_SEQUENCES_REGEX + ")\")|[A-F0-9]+)";

    private final static String STRING_SECTION_REGEX = "(\"([^\"\n]|" + ESCAPE_SEQUENCES_REGEX + ")*\")|(%[A-F0-9]+)";

    private final static String BOOLEAN_CONSTANT_REGEX = "(true|false)";

    private final static String REFERENCE_NULL_CONSTANT = "(null)";

    private final static String OR_XOR_OP_REGEX = "[|@]";
    private final static String AND_OP_REGEX = "[&]";
    private final static String EQ_OP_REGEX = "([!=]=)";
    private final static String ORD_OP_REGEX = "([<>]=?)";
    private final static String PLUS_MINUS_OP_REGEX = "[+-]";
    private final static String MUL_DIV_REM_OP_REGEX = "[*/%]";
    private final static String POWER_OP_REGEX = "[^]";
    private final static String NOT_MINUS_OP_REGEX = "[!-]";
    private final static String EQUAL_REGEX = "[=]";
    private final static String ASSIGN_REGEX = "(:=)";
    private final static String DOT_REGEX = "[.]";
    private final static String LEFT_ARROW_REGEX = "(<-)";
    private final static String COMMA_REGEX = ",";
    private final static String TILDE_REGEX = "~";
    private final static String BRACKETS_REGEX = "(\\[\\])";
    private final static String LEFT_PAR_REGEX = "[(]";
    private final static String RIGHT_PAR_REGEX = "[)]";

    private static final Pattern PATTERN = Pattern.compile(
            IDENTIFIER_REGEX + "|" + DECIMAL_INTEGER_CONSTANT_REGEX + "|" + NON_DECIMAL_INTEGER_CONSTANT_REGEX + "|" +
                    SYMBOLIC_CONSTANT_REGEX + "|" + STRING_SECTION_REGEX + "|" + BOOLEAN_CONSTANT_REGEX + "|" +
                    REFERENCE_NULL_CONSTANT + "|" +
                    OR_XOR_OP_REGEX + "|" + AND_OP_REGEX + "|" + EQ_OP_REGEX + "|" + ORD_OP_REGEX + "|" + PLUS_MINUS_OP_REGEX + "|" +
                    MUL_DIV_REM_OP_REGEX + "|" + POWER_OP_REGEX + "|" + NOT_MINUS_OP_REGEX + "|" + EQUAL_REGEX + "|" + ASSIGN_REGEX + "|" + DOT_REGEX + "|" +
                    LEFT_ARROW_REGEX + "|" + COMMA_REGEX + "|" + TILDE_REGEX + "|" + BRACKETS_REGEX + "|" + LEFT_PAR_REGEX + "|" + RIGHT_PAR_REGEX
    );

    public Scanner(String input) {
        this.input = input;
        this.pos = 0;
    }

    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token("$", DomainTag.END,
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
        } else if (token.matches(SYMBOLIC_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.SYMBOLIC_CONSTANT, fragmentPosition);
        } else if (token.matches(STRING_SECTION_REGEX)) {
            return new Token(token, DomainTag.STRING_SECTION, fragmentPosition);
        } else if (token.matches(BOOLEAN_CONSTANT_REGEX)) {
            return new Token(token, DomainTag.BOOLEAN_CONSTANT, fragmentPosition);
        } else if (token.matches(REFERENCE_NULL_CONSTANT)) {
            return new Token(token, DomainTag.REFERENCE_NULL_CONSTANT, fragmentPosition);
        } else if (token.matches(OR_XOR_OP_REGEX)) {
            return new Token(token, DomainTag.OR_XOR_OP, fragmentPosition);
        } else if (token.matches(AND_OP_REGEX)) {
            return new Token(token, DomainTag.AND_OP, fragmentPosition);
        } else if (token.matches(EQ_OP_REGEX)) {
            return new Token(token, DomainTag.EQ_OP, fragmentPosition);
        } else if (token.matches(ORD_OP_REGEX)) {
            return new Token(token, DomainTag.ORD_OP, fragmentPosition);
        } else if (token.matches(PLUS_MINUS_OP_REGEX)) {
            return new Token(token, DomainTag.PLUS_MINUS_OP, fragmentPosition);
        } else if (token.matches(MUL_DIV_REM_OP_REGEX)) {
            return new Token(token, DomainTag.MUL_DIV_REM_OP, fragmentPosition);
        } else if (token.matches(POWER_OP_REGEX)) {
            return new Token(token, DomainTag.POWER_OP, fragmentPosition);
        } else if (token.matches(NOT_MINUS_OP_REGEX)) {
            return new Token(token, DomainTag.NOT_MINUS_OP, fragmentPosition);
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
