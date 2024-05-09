import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static class Token {
        private int line;
        private int column;
        private int type;
        private String value;

        public final static int IF = 0x0001;
        public final static int M1 = 0x0002;
        public final static int FOR = 0x0004;
        public final static int IDENT = 0x0008;
        public final static int COMMENTS = 0x0010;

        public Token(int line, int column, int type, String value) {
            this.line = line;
            this.column = column;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            switch (type) {
                case FOR -> builder.append("FOR");
                case IF -> builder.append("IF");
                case M1 -> builder.append("M1");
                case IDENT -> builder.append("IDENT");
                case COMMENTS -> builder.append("COMMENTS");
            }
            builder.append(" (").append(line).append(", ").append(column).append("):");
            if (type == COMMENTS) {
                builder.append('\n');
            } else {
                builder.append(' ');
            }
            builder.append(value);
            return builder.toString();
        }
    }

    public static void main(String[] args) {
        String ident = "(((^(([A-Z]|[a-z])(\\d))+)([A-Z]|[a-z])?)|((^((\\d)([A-Z]|[a-z]))+)(\\d)?))";
        String comments = "/\\*([^*]|\\*+[^*/])*\\*/";
        String _if = "if";
        String m1 = "m1";
        String _for = "for";
        String pattern = "(?<for>^" + _for + ")|" + "(?<if>^" + _if + ")|" + "(?<m1>^" + m1 + ")|" +
                "(?<ident>^" + ident + ")|" + "(?<comments>^" + comments + ")";
        Pattern p = Pattern.compile(pattern);

        String filename = "input.txt";
        String input = "";

        try {
            input = Files.readString(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int line = 1;
        int column = 1;
        boolean found = false;

        String text = input;

        int i = 0;

        ArrayList<Token> tokens = new ArrayList<>();

        while (i < input.length()) {
            text = input.substring(i);
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                String str;
                if ((str = matcher.group("for")) != null) {
                    tokens.add(new Token(line, column, Token.FOR, str));
                } else if ((str = matcher.group("if")) != null) {
                    tokens.add(new Token(line, column, Token.IF, str));
                } else if ((str = matcher.group("m1")) != null) {
                    tokens.add(new Token(line, column, Token.M1, str));
                } else if ((str = matcher.group("ident")) != null) {
                    tokens.add(new Token(line, column, Token.IDENT, str));
                } else if ((str = matcher.group("comments")) != null) {
                    tokens.add(new Token(line, column, Token.COMMENTS, str));
                }
                i += matcher.end();
                int count = 0;
                for (int j = 0; j < matcher.end(); j++) {
                    if (text.charAt(j) == '\n') {
                        count++;
                        column = 1;
                        continue;
                    }
                    column++;
                }
                line += count;
                found = true;
                continue;
            } else if (!Character.isWhitespace(input.charAt(i))) {
                if (found) {
                    System.out.println("ERROR (" + line + ", " + column + ")");
                }
                found = false;
            }
            if (input.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            i++;
        }

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}