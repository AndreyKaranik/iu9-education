% Лабораторная работа № 2.3 «Синтаксический анализатор на основе
  предсказывающего анализа»
% 3 июня 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является изучение алгоритма построения таблиц предсказывающего анализатора.

# Индивидуальный вариант
```
-- аксиома заключена
-- в фигурные скобки
T, T', { E }, E', F
[ E  : T E' ]
[ E' : "+" T E' : @ ]
[ T  : F T' ]
[ T' : "*" F T' : @ ]
[ F  : "n" : "(" E ")" ]
```

# Реализация

## Неформальное описание синтаксиса входного языка
Грамматика состоит из определений и правил в данном порядке объявления.
Определения состоят из нетерминалов, разделяемых запятыми.
Аксиома в определении дополнительно выделяется фигурными скобками.
Правила обосабливаются квадратными скобками, левая и правая части разделяются двоеточием.
Пустая строка (эпсилон) - это знак @.
Терминалы выделяются двойными кавычками.


## Лексическая структура
```
TERMINAL = "[^"]*"
NONTERMINAL = [^\s\[\]:,@"{}]+(?:[\s,]|$)
LEFT_CURLY_BRACE = (
RIGHT_CURLY_BRACE = )
COMMA = ,
LEFT_BRACKET = [
RIGHT_BRACKET = ]
COLON = :
EMPTY_STRING = @
END = [$]
```

## Грамматика языка
```
Grammar → Definitions Rules
Definitions → Axiom Definitions' | nonterminal , Definitions
Definitions' → , nonterminal Definitions' | eps
Rules → Rule Rules
Rule → [ nonterminal Rule' ]
Rule' → Variant Variants
Variants → Variant Variants | eps
Variant → : Sequence
Sequence → Symbol Sequence'
Sequence' → Symbol Sequence' | eps
Symbol → terminal | nonterminal | @
Axiom → { nonterminal }
```

## Программная реализация

Main.java
```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static List<Rule> parseGrammar(String grammar) {
        List<Rule> rules = new ArrayList<>();
        String[] lines = grammar.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("->");
            String left = parts[0].trim();
            String[] rightParts = parts[1].trim().split("\\|");

            for (String rightPart : rightParts) {
                String[] right = rightPart.trim().split("\\s+");
                Symbol lt = new Symbol(DomainTag.NONE, left,true);
                List<Symbol> symbols = new ArrayList<>();
                for (String rt : right) {
                    switch (rt) {
                        case "{" -> symbols.add(new Symbol(DomainTag.LEFT_CURLY_BRACE, rt, false));
                        case "}" -> symbols.add(new Symbol(DomainTag.RIGHT_CURLY_BRACE, rt, false));
                        case "[" -> symbols.add(new Symbol(DomainTag.LEFT_BRACKET, rt, false));
                        case "]" -> symbols.add(new Symbol(DomainTag.RIGHT_BRACKET, rt, false));
                        case ":" -> symbols.add(new Symbol(DomainTag.COLON, rt, false));
                        case "@" -> symbols.add(new Symbol(DomainTag.EMPTY_STRING, rt, false));
                        case "terminal" -> symbols.add(new Symbol(DomainTag.TERMINAL, rt, false));
                        case "nonterminal" -> symbols.add(new Symbol(DomainTag.NONTERMINAL, rt, false));
                        case "," -> symbols.add(new Symbol(DomainTag.COMMA, rt, false));
                        default -> symbols.add(new Symbol(DomainTag.NONE, rt, true));
                    }
                }
                rules.add(new Rule(lt, symbols));
            }
        }

        return rules;
    }

    public static void main(String[] args) throws IOException {
        String grammar = "Grammar -> Definitions Rules\n" +
                "Definitions -> Axiom Definitions' | nonterminal , Definitions\n" +
                "Definitions' -> , nonterminal Definitions' | eps\n" +
                "Rules -> Rule Rules\n" +
                "Rule -> [ nonterminal Rule' ]\n" +
                "Rule' -> Variant Variants\n" +
                "Variants -> Variant Variants | eps\n" +
                "Variant -> : Sequence\n" +
                "Sequence -> Symbol Sequence'\n" +
                "Sequence' -> Symbol Sequence' | eps\n" +
                "Symbol -> terminal | nonterminal | @\n" +
                "Axiom -> { nonterminal }";

        List<Rule> rules = parseGrammar(grammar);

        Map<String, String> map = new HashMap<>();
        map.put("S", "Grammar");
        map.put("C", "Definitions'");
        map.put("A", "Definitions");
        map.put("B", "Rules");
        map.put("E", "Rule'");
        map.put("D", "Rule");
        map.put("G", "Variants");
        map.put("F", "Variant");
        map.put("J", "Sequence'");
        map.put("H", "Sequence");
        map.put("I", "Symbol");
        map.put("X", "Axiom");
        map.put(",", ",");
        map.put(":", ":");
        map.put("{", "{");
        map.put("}", "}");
        map.put("[", "[");
        map.put("]", "]");
        map.put("@", "@");
        map.put("n", "nonterminal");
        map.put("t", "terminal");
        map.put("$", "$");

        Table<String, String, Rule> table = new Table<>();
        Table<String, String, Rule> table2 = new Table<>();

        for (char row : "SACBDEGFHJIX".toCharArray()) {
            table.addRow(map.get(String.valueOf(row)));
            table2.addRow(String.valueOf(row));
        }

        for (char column : "n,[]:t@{}$".toCharArray()) {
            table.addColumn(map.get(String.valueOf(column)));
            table2.addColumn(String.valueOf(column));
        }

        table.setValue(map.get("S"), map.get("n"), rules.get(0));
        table.setValue(map.get("S"), map.get("{"), rules.get(0));
        table.setValue(map.get("A"), map.get("n"), rules.get(2));
        table.setValue(map.get("A"), map.get("{"), rules.get(1));
        table.setValue(map.get("C"), map.get(","), rules.get(3));
        table.setValue(map.get("C"), map.get("["), rules.get(4));
        table.setValue(map.get("B"), map.get("["), rules.get(5));
        table.setValue(map.get("D"), map.get("["), rules.get(6));
        table.setValue(map.get("E"), map.get(":"), rules.get(7));
        table.setValue(map.get("G"), map.get("]"), rules.get(9));
        table.setValue(map.get("G"), map.get(":"), rules.get(8));
        table.setValue(map.get("F"), map.get(":"), rules.get(10));
        table.setValue(map.get("H"), map.get("n"), rules.get(11));
        table.setValue(map.get("H"), map.get("t"), rules.get(11));
        table.setValue(map.get("H"), map.get("@"), rules.get(11));
        table.setValue(map.get("J"), map.get("n"), rules.get(12));
        table.setValue(map.get("J"), map.get("]"), rules.get(13));
        table.setValue(map.get("J"), map.get(":"), rules.get(13));
        table.setValue(map.get("J"), map.get("t"), rules.get(12));
        table.setValue(map.get("J"), map.get("@"), rules.get(12));
        table.setValue(map.get("I"), map.get("n"), rules.get(15));
        table.setValue(map.get("I"), map.get("t"), rules.get(14));
        table.setValue(map.get("I"), map.get("@"), rules.get(16));
        table.setValue(map.get("X"), map.get("{"), rules.get(17));


        String input = new String(Files.readAllBytes(Paths.get("input.txt")));
        Scanner scanner = new Scanner(input);

        TopDownParse(scanner, table);

    }

    public static void TopDownParse(Scanner scanner, Table table) {
        List<Rule> result = new ArrayList<>();
        Stack<Symbol> stack = new Stack<>();
        stack.push(new Symbol(DomainTag.END, "$", false));
        stack.push(new Symbol(DomainTag.NONE, "Grammar", true));
        Token a = scanner.nextToken();
        do {
            Symbol x = stack.peek();
            if (!x.isNonterminal()) {
                if (x.getTag() == a.getTag()) {
                    stack.pop();
                    a = scanner.nextToken();
                } else {
                    throw new RuntimeException("ERROR1: " + a.getFragmentPosition());
                }
            } else if (table.get(x.getValue(), a.getTerminalTag()) != null) {
                stack.pop();
                Rule rule = (Rule) table.get(x.getValue(), a.getTerminalTag());
                for (int i = rule.getRight().size() - 1; i >= 0; i--) {
                    if (!rule.getRight().get(i).getValue().equals("eps")) {
                        stack.push(rule.getRight().get(i));
                    }
                }
                result.add(rule);
            } else {
                throw new RuntimeException("ERROR2: " + a.getFragmentPosition());
            }
        } while (a.getTag() != DomainTag.END);

        Queue<Rule> queue = new ArrayDeque<>(result);

        OutputTree tree = new OutputTree(new OutputTree.Node(result.get(0).getLeft().getValue()));
        buildTree(tree.getRoot(), queue);
        System.out.println(tree.toGraphviz());
    }

    public static void buildTree(OutputTree.Node node, Queue<Rule> queue) {
        Rule rule = queue.poll();
        if (rule == null) {
            return;
        }
        for (Symbol symbol : rule.getRight()) {
            if (symbol.isNonterminal()) {
                if (!symbol.getValue().equals("eps")) {
                    OutputTree.Node newNode = new OutputTree.Node(symbol.getValue());
                    node.add(newNode);
                    buildTree(newNode, queue);
                } else {
                    node.add(new OutputTree.Node(symbol.getValue()));
                }
            } else {
                node.add(new OutputTree.Node(symbol.getValue()));
            }
        }

    }
}
```

OutputTree.java
```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputTree {

    private Node root;
    public static class Node {
        private String name;
        private List<Node> nodes;

        public Node(String name) {
            this.name = name;
            nodes = new ArrayList<>();
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public String getName() {
            return name;
        }

        public void add(Node node) {
            nodes.add(node);
        }
    }

    public OutputTree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public String toGraphviz() {
        StringBuilder sb = new StringBuilder();
        Map<Node, String> nodeIds = new HashMap<>();
        sb.append("digraph {\n");
        generateNodeIds(root, nodeIds, 1);
        for (Map.Entry<Node, String> entry : nodeIds.entrySet()) {
            sb.append("  ").append(entry.getValue())
                    .append(" [label=\"").append(entry.getKey().getName()).append("\"];\n");
        }
        generateEdges(root, nodeIds, sb);
        sb.append("}\n");
        return sb.toString();
    }

    private int generateNodeIds(Node node, Map<Node, String> nodeIds, int id) {
        String nodeId = "node" + id++;
        nodeIds.put(node, nodeId);
        for (Node child : node.getNodes()) {
            id = generateNodeIds(child, nodeIds, id);
        }
        return id;
    }

    private void generateEdges(Node node, Map<Node, String> nodeIds, StringBuilder sb) {
        List<String> childrenIds = new ArrayList<>();
        for (Node child : node.getNodes()) {
            sb.append("  ").append(nodeIds.get(node)).append(" -> ").append(nodeIds.get(child)).append(";\n");
            childrenIds.add(nodeIds.get(child));
            generateEdges(child, nodeIds, sb);
        }
        if (childrenIds.size() > 1) {
            sb.append("  { rank=same; ");
            for (int i = 0; i < childrenIds.size(); i++) {
                sb.append(childrenIds.get(i));
                if (i < childrenIds.size() - 1) {
                    sb.append(" -> ");
                }
            }
            sb.append(" [style=invis]; }\n");
        }
    }
}

```

Position.java
```java
public class Position implements Comparable<Position> {
    private int line;
    private int column;
    private int index;

    public Position(int line, int column, int index) {
        this.line = line;
        this.column = column;
        this.index = index;
    }

    @Override
    public int compareTo(Position other) {
        return Integer.compare(this.index, other.index);
    }

    @Override
    public String toString() {
        return "(" + line + ", " + column + ", " + index + ")";
    }

    public String getSimplifiedString() {
        return "(" + line + ", " + column + ")";
    }
}
```

Rule.java
```java
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

```

Scanner.java
```java
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
                    new FragmentPosition(
                                        findPosition(input, input.length()),
                                        findPosition(input, input.length())));
        }

        Matcher matcher = PATTERN.matcher(input);
        if (matcher.find(pos)) {
            String token = matcher.group();
            int endPos = matcher.end();
            FragmentPosition fragmentPosition = new FragmentPosition(
                                                    findPosition(input, pos),
                                                    findPosition(input, endPos - 1));
            pos = endPos;
            return determineToken(token, fragmentPosition);
        }

        throw new RuntimeException("LEX_ERROR: " + new FragmentPosition(
                                                    findPosition(input, pos),
                                                    findPosition(input, pos)));
    }

    private Token determineToken(String token, FragmentPosition fragmentPosition) {
        if (token.matches("\"[^\"]*\"")) {
            return new Token(token, DomainTag.TERMINAL, "terminal", fragmentPosition);
        } else if (token.matches("[^\\s\\[\\]:,@\"{}\\-]+(?:[\\s,]|$)")) {
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

```

Symbol.java
```java
class Symbol {
    private DomainTag tag;

    private String value;
    private boolean isNonterminal;
    public Symbol(DomainTag tag, String value, boolean isNonterminal) {
        this.tag = tag;
        this.value = value;
        this.isNonterminal = isNonterminal;
    }

    public DomainTag getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public boolean isNonterminal() {
        return isNonterminal;
    }

    @Override
    public String toString() {
        return value;
    }
}
```

Table.java
```java
import java.util.ArrayList;
import java.util.List;

public class Table<R, C, V> {
    private List<C> columnHeaders = new ArrayList<>();
    private List<R> rowHeaders = new ArrayList<>();
    private List<List<V>> data = new ArrayList<>();

    public void addColumn(C header) {
        columnHeaders.add(header);
        for (List<V> row : data) {
            row.add(null);
        }
    }

    public void addRow(R header) {
        rowHeaders.add(header);
        List<V> newRow = new ArrayList<>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            newRow.add(null);
        }
        data.add(newRow);
    }

    public V get(R rowHeader, C colHeader) {
        int rowIndex = rowHeaders.indexOf(rowHeader);
        int colIndex = columnHeaders.indexOf(colHeader);

        if (rowIndex == -1 || colIndex == -1) {
            throw new IllegalArgumentException("Invalid row or column header.");
        }

        return data.get(rowIndex).get(colIndex);
    }

    public void setValue(R rowHeader, C colHeader, V value) {
        int rowIndex = rowHeaders.indexOf(rowHeader);
        int colIndex = columnHeaders.indexOf(colHeader);

        if (rowIndex == -1 || colIndex == -1) {
            throw new IllegalArgumentException("Invalid row or column header.");
        }

        data.get(rowIndex).set(colIndex, value);
    }

    public void print() {
        int[] colWidths = new int[columnHeaders.size()];

        for (int i = 0; i < columnHeaders.size(); i++) {
            colWidths[i] = columnHeaders.get(i).toString().length();
        }
        for (List<V> row : data) {
            for (int i = 0; i < row.size(); i++) {
                V value = row.get(i);
                if (value != null) {
                    colWidths[i] = Math.max(colWidths[i], value.toString().length());
                }
            }
        }

        int rowHeaderWidth = rowHeaders.stream().mapToInt(header ->
        header.toString().length()).max().orElse(0);

        System.out.print(" ".repeat(rowHeaderWidth + 2));
        for (int i = 0; i < columnHeaders.size(); i++) {
            System.out.print(String.format("%-" + (colWidths[i] + 2) + "s", columnHeaders.get(i)));
        }
        System.out.println();

        for (int row = 0; row < data.size(); row++) {
            System.out.print(String.format("%-" + (rowHeaderWidth + 2) + "s", rowHeaders.get(row)));
            for (int col = 0; col < columnHeaders.size(); col++) {
                V value = data.get(row).get(col);
                System.out.print(String.format("%-" + (colWidths[col] + 2) + "s", value != null ?
                value.toString() : "null"));
            }
            System.out.println();
        }
    }
}
```

Token.java
```java
public class Token {

    private String value;
    private DomainTag tag;
    private String terminalTag;

    private FragmentPosition fragmentPosition;

    public Token(String value, DomainTag tag, String terminalTag, FragmentPosition fragmentPosition) {
        this.value = value;
        this.tag = tag;
        this.terminalTag = terminalTag;
        this.fragmentPosition = fragmentPosition;
    }

    public String getValue() {
        return value;
    }

    public DomainTag getTag() {
        return tag;
    }

    public String getTerminalTag() {
        return terminalTag;
    }

    public FragmentPosition getFragmentPosition() {
        return fragmentPosition;
    }
}
```

DomainTag.java
```java
public enum DomainTag {
    TERMINAL, NONTERMINAL, LEFT_CURLY_BRACE, RIGHT_CURLY_BRACE, COMMA, LEFT_BRACKET, RIGHT_BRACKET,
    COLON, EMPTY_STRING, END, NONE
}
```

FragmentPosition.java
```java
public class FragmentPosition {
    private Position startPosition;
    private Position endPosition;

    public FragmentPosition(Position startPosition, Position endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return startPosition.toString() + "-" + endPosition.toString();
    }

    public String getSimplifiedString() {
        return startPosition.getSimplifiedString() + "-" + endPosition.getSimplifiedString();
    }
}
```

# Тестирование

Входные данные

```
T, T', { E }, E', F
[ E  : T E' ]
[ E' : "+" T E' : @ ]
[ T  : F T' ]
[ T' : "*" F T' : @ ]
[ F  : "n" : "(" E ")" ]
```

Вывод на `stdout`

```
digraph {
  node104 [label="Variants"];
  node115 [label="Rules"];
  node33 [label="nonterminal"];
  node18 [label="nonterminal"];
  node40 [label="Rule"];
  node78 [label="Sequence'"];
  node114 [label="]"];
  node126 [label="eps"];
  node54 [label="nonterminal"];
  node80 [label="nonterminal"];
  node11 [label="nonterminal"];
  node142 [label="eps"];
  node21 [label="Rules"];
  node112 [label="Variants"];
  node116 [label="Rule"];
  node117 [label="["];
  node35 [label="eps"];
  node77 [label="nonterminal"];
  node132 [label="terminal"];
  node8 [label="Definitions"];
  node42 [label="nonterminal"];
  node61 [label="Symbol"];
  node31 [label="Sequence'"];
  node56 [label="eps"];
  node98 [label="nonterminal"];
  node100 [label="Symbol"];
  node67 [label="]"];
  node140 [label="eps"];
  node10 [label="{"];
  node51 [label="nonterminal"];
  node143 [label="]"];
  node72 [label="Rule'"];
  node87 [label="Rule"];
  node58 [label="Variant"];
  node111 [label="eps"];
  node136 [label="Sequence'"];
  node120 [label="Variant"];
  node75 [label="Sequence"];
  node24 [label="nonterminal"];
  node12 [label="}"];
  node30 [label="nonterminal"];
  node105 [label="Variant"];
  node64 [label="eps"];
  node127 [label="Variants"];
  node94 [label="Symbol"];
  node69 [label="Rule"];
  node92 [label=":"];
  node91 [label="Variant"];
  node125 [label="Sequence'"];
  node16 [label="Definitions'"];
  node44 [label="Variant"];
  node95 [label="terminal"];
  node102 [label="Sequence'"];
  node83 [label="Variants"];
  node62 [label="@"];
  node71 [label="nonterminal"];
  node4 [label=","];
  node81 [label="Sequence'"];
  node6 [label="nonterminal"];
  node89 [label="nonterminal"];
  node41 [label="["];
  node15 [label="nonterminal"];
  node107 [label="Sequence"];
  node141 [label="Variants"];
  node144 [label="Rules"];
  node43 [label="Rule'"];
  node128 [label="Variant"];
  node20 [label="eps"];
  node119 [label="Rule'"];
  node76 [label="Symbol"];
  node137 [label="Symbol"];
  node130 [label="Sequence"];
  node121 [label=":"];
  node26 [label="Variant"];
  node123 [label="Symbol"];
  node113 [label="eps"];
  node70 [label="["];
  node97 [label="Symbol"];
  node79 [label="Symbol"];
  node133 [label="Sequence'"];
  node106 [label=":"];
  node139 [label="Sequence'"];
  node38 [label="]"];
  node65 [label="Variants"];
  node5 [label="Definitions"];
  node37 [label="eps"];
  node59 [label=":"];
  node9 [label="Axiom"];
  node138 [label="terminal"];
  node99 [label="Sequence'"];
  node49 [label="Sequence'"];
  node85 [label="]"];
  node50 [label="Symbol"];
  node86 [label="Rules"];
  node34 [label="Sequence'"];
  node39 [label="Rules"];
  node109 [label="@"];
  node32 [label="Symbol"];
  node93 [label="Sequence"];
  node66 [label="eps"];
  node28 [label="Sequence"];
  node7 [label=","];
  node88 [label="["];
  node3 [label="nonterminal"];
  node48 [label="terminal"];
  node2 [label="Definitions"];
  node90 [label="Rule'"];
  node108 [label="Symbol"];
  node63 [label="Sequence'"];
  node1 [label="Grammar"];
  node129 [label=":"];
  node19 [label="Definitions'"];
  node53 [label="Symbol"];
  node22 [label="Rule"];
  node110 [label="Sequence'"];
  node23 [label="["];
  node101 [label="nonterminal"];
  node36 [label="Variants"];
  node17 [label=","];
  node84 [label="eps"];
  node118 [label="nonterminal"];
  node68 [label="Rules"];
  node96 [label="Sequence'"];
  node55 [label="Sequence'"];
  node122 [label="Sequence"];
  node134 [label="Symbol"];
  node135 [label="nonterminal"];
  node13 [label="Definitions'"];
  node25 [label="Rule'"];
  node124 [label="terminal"];
  node74 [label=":"];
  node73 [label="Variant"];
  node14 [label=","];
  node60 [label="Sequence"];
  node131 [label="Symbol"];
  node103 [label="eps"];
  node82 [label="eps"];
  node46 [label="Sequence"];
  node45 [label=":"];
  node52 [label="Sequence'"];
  node29 [label="Symbol"];
  node27 [label=":"];
  node47 [label="Symbol"];
  node57 [label="Variants"];
  node1 -> node2;
  node2 -> node3;
  node2 -> node4;
  node2 -> node5;
  node5 -> node6;
  node5 -> node7;
  node5 -> node8;
  node8 -> node9;
  node9 -> node10;
  node9 -> node11;
  node9 -> node12;
  { rank=same; node10 -> node11 -> node12 [style=invis]; }
  node8 -> node13;
  node13 -> node14;
  node13 -> node15;
  node13 -> node16;
  node16 -> node17;
  node16 -> node18;
  node16 -> node19;
  node19 -> node20;
  { rank=same; node17 -> node18 -> node19 [style=invis]; }
  { rank=same; node14 -> node15 -> node16 [style=invis]; }
  { rank=same; node9 -> node13 [style=invis]; }
  { rank=same; node6 -> node7 -> node8 [style=invis]; }
  { rank=same; node3 -> node4 -> node5 [style=invis]; }
  node1 -> node21;
  node21 -> node22;
  node22 -> node23;
  node22 -> node24;
  node22 -> node25;
  node25 -> node26;
  node26 -> node27;
  node26 -> node28;
  node28 -> node29;
  node29 -> node30;
  node28 -> node31;
  node31 -> node32;
  node32 -> node33;
  node31 -> node34;
  node34 -> node35;
  { rank=same; node32 -> node34 [style=invis]; }
  { rank=same; node29 -> node31 [style=invis]; }
  { rank=same; node27 -> node28 [style=invis]; }
  node25 -> node36;
  node36 -> node37;
  { rank=same; node26 -> node36 [style=invis]; }
  node22 -> node38;
  { rank=same; node23 -> node24 -> node25 -> node38 [style=invis]; }
  node21 -> node39;
  node39 -> node40;
  node40 -> node41;
  node40 -> node42;
  node40 -> node43;
  node43 -> node44;
  node44 -> node45;
  node44 -> node46;
  node46 -> node47;
  node47 -> node48;
  node46 -> node49;
  node49 -> node50;
  node50 -> node51;
  node49 -> node52;
  node52 -> node53;
  node53 -> node54;
  node52 -> node55;
  node55 -> node56;
  { rank=same; node53 -> node55 [style=invis]; }
  { rank=same; node50 -> node52 [style=invis]; }
  { rank=same; node47 -> node49 [style=invis]; }
  { rank=same; node45 -> node46 [style=invis]; }
  node43 -> node57;
  node57 -> node58;
  node58 -> node59;
  node58 -> node60;
  node60 -> node61;
  node61 -> node62;
  node60 -> node63;
  node63 -> node64;
  { rank=same; node61 -> node63 [style=invis]; }
  { rank=same; node59 -> node60 [style=invis]; }
  node57 -> node65;
  node65 -> node66;
  { rank=same; node58 -> node65 [style=invis]; }
  { rank=same; node44 -> node57 [style=invis]; }
  node40 -> node67;
  { rank=same; node41 -> node42 -> node43 -> node67 [style=invis]; }
  node39 -> node68;
  node68 -> node69;
  node69 -> node70;
  node69 -> node71;
  node69 -> node72;
  node72 -> node73;
  node73 -> node74;
  node73 -> node75;
  node75 -> node76;
  node76 -> node77;
  node75 -> node78;
  node78 -> node79;
  node79 -> node80;
  node78 -> node81;
  node81 -> node82;
  { rank=same; node79 -> node81 [style=invis]; }
  { rank=same; node76 -> node78 [style=invis]; }
  { rank=same; node74 -> node75 [style=invis]; }
  node72 -> node83;
  node83 -> node84;
  { rank=same; node73 -> node83 [style=invis]; }
  node69 -> node85;
  { rank=same; node70 -> node71 -> node72 -> node85 [style=invis]; }
  node68 -> node86;
  node86 -> node87;
  node87 -> node88;
  node87 -> node89;
  node87 -> node90;
  node90 -> node91;
  node91 -> node92;
  node91 -> node93;
  node93 -> node94;
  node94 -> node95;
  node93 -> node96;
  node96 -> node97;
  node97 -> node98;
  node96 -> node99;
  node99 -> node100;
  node100 -> node101;
  node99 -> node102;
  node102 -> node103;
  { rank=same; node100 -> node102 [style=invis]; }
  { rank=same; node97 -> node99 [style=invis]; }
  { rank=same; node94 -> node96 [style=invis]; }
  { rank=same; node92 -> node93 [style=invis]; }
  node90 -> node104;
  node104 -> node105;
  node105 -> node106;
  node105 -> node107;
  node107 -> node108;
  node108 -> node109;
  node107 -> node110;
  node110 -> node111;
  { rank=same; node108 -> node110 [style=invis]; }
  { rank=same; node106 -> node107 [style=invis]; }
  node104 -> node112;
  node112 -> node113;
  { rank=same; node105 -> node112 [style=invis]; }
  { rank=same; node91 -> node104 [style=invis]; }
  node87 -> node114;
  { rank=same; node88 -> node89 -> node90 -> node114 [style=invis]; }
  node86 -> node115;
  node115 -> node116;
  node116 -> node117;
  node116 -> node118;
  node116 -> node119;
  node119 -> node120;
  node120 -> node121;
  node120 -> node122;
  node122 -> node123;
  node123 -> node124;
  node122 -> node125;
  node125 -> node126;
  { rank=same; node123 -> node125 [style=invis]; }
  { rank=same; node121 -> node122 [style=invis]; }
  node119 -> node127;
  node127 -> node128;
  node128 -> node129;
  node128 -> node130;
  node130 -> node131;
  node131 -> node132;
  node130 -> node133;
  node133 -> node134;
  node134 -> node135;
  node133 -> node136;
  node136 -> node137;
  node137 -> node138;
  node136 -> node139;
  node139 -> node140;
  { rank=same; node137 -> node139 [style=invis]; }
  { rank=same; node134 -> node136 [style=invis]; }
  { rank=same; node131 -> node133 [style=invis]; }
  { rank=same; node129 -> node130 [style=invis]; }
  node127 -> node141;
  node141 -> node142;
  { rank=same; node128 -> node141 [style=invis]; }
  { rank=same; node120 -> node127 [style=invis]; }
  node116 -> node143;
  { rank=same; node117 -> node118 -> node119 -> node143 [style=invis]; }
  node115 -> node144;
  { rank=same; node116 -> node144 [style=invis]; }
  { rank=same; node87 -> node115 [style=invis]; }
  { rank=same; node69 -> node86 [style=invis]; }
  { rank=same; node40 -> node68 [style=invis]; }
  { rank=same; node22 -> node39 [style=invis]; }
  { rank=same; node2 -> node21 [style=invis]; }
}

```

# Вывод
В ходе выполнения лабораторной работы изучил изучение алгоритма построения таблиц
предсказывающего анализатора.