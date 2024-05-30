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
                        case "[" -> symbols.add(new Symbol(DomainTag.LEFT_BRACKET, rt, false));
                        case "]" -> symbols.add(new Symbol(DomainTag.RIGHT_BRACKET, rt, false));
                        case ":" -> symbols.add(new Symbol(DomainTag.COLON, rt, false));
                        case "@" -> symbols.add(new Symbol(DomainTag.EMPTY_STRING, rt, false));
                        case "start" -> symbols.add(new Symbol(DomainTag.START_SYMBOL, rt, false));
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

    public static void main(String[] args) {
        String grammar = "Grammar -> Definitions Rules\n" +
                "Definitions -> start Definitions' | nonterminal , Definitions\n" +
                "Definitions' -> , nonterminal Definitions' | eps\n" +
                "Rules -> Rule Rules\n" +
                "Rule -> [ Nonterminal Rule' ]\n" +
                "Rule' -> Variant Variants\n" +
                "Variants -> Variant Variants | eps\n" +
                "Variant -> : Sequence\n" +
                "Sequence -> Symbol Sequence'\n" +
                "Sequence' -> Symbol Sequence' | eps\n" +
                "Symbol -> terminal | Nonterminal | @\n" +
                "Nonterminal -> nonterminal | start";

        String grammar2 = GrammarConverter.convert(grammar);

        List<Rule> rules = parseGrammar(grammar);
        List<Rule> rules2 = parseGrammar(grammar2);

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
        map.put("N", "Nonterminal");
        map.put(",", ",");
        map.put(":", ":");
        map.put("[", "[");
        map.put("]", "]");
        map.put("@", "@");
        map.put("n", "nonterminal");
        map.put("t", "terminal");
        map.put("s", "start");
        map.put("$", "$");

        Table<String, String, Rule> table = new Table<>();
        Table<String, String, Rule> table2 = new Table<>();

        for (char row : "SACBDEGFHJIN".toCharArray()) {
            table.addRow(map.get(String.valueOf(row)));
            table2.addRow(String.valueOf(row));
        }

        for (char column : "sn,[]:t@$".toCharArray()) {
            table.addColumn(map.get(String.valueOf(column)));
            table2.addColumn(String.valueOf(column));
        }

        table.setValue(map.get("S"), map.get("s"), rules.get(0));
        table.setValue(map.get("S"), map.get("n"), rules.get(0));
        table.setValue(map.get("A"), map.get("s"), rules.get(1));
        table.setValue(map.get("A"), map.get("n"), rules.get(2));
        table.setValue(map.get("C"), map.get(","), rules.get(3));
        table.setValue(map.get("C"), map.get("["), rules.get(4));
        table.setValue(map.get("B"), map.get("["), rules.get(5));
        table.setValue(map.get("D"), map.get("["), rules.get(6));
        table.setValue(map.get("E"), map.get(":"), rules.get(7));
        table.setValue(map.get("G"), map.get("]"), rules.get(9));
        table.setValue(map.get("G"), map.get(":"), rules.get(8));
        table.setValue(map.get("F"), map.get(":"), rules.get(10));
        table.setValue(map.get("H"), map.get("s"), rules.get(11));
        table.setValue(map.get("H"), map.get("n"), rules.get(11));
        table.setValue(map.get("H"), map.get("t"), rules.get(11));
        table.setValue(map.get("H"), map.get("@"), rules.get(11));
        table.setValue(map.get("J"), map.get("s"), rules.get(12));
        table.setValue(map.get("J"), map.get("n"), rules.get(12));
        table.setValue(map.get("J"), map.get("]"), rules.get(13));
        table.setValue(map.get("J"), map.get(":"), rules.get(13));
        table.setValue(map.get("J"), map.get("t"), rules.get(12));
        table.setValue(map.get("J"), map.get("@"), rules.get(12));
        table.setValue(map.get("I"), map.get("s"), rules.get(15));
        table.setValue(map.get("I"), map.get("n"), rules.get(15));
        table.setValue(map.get("I"), map.get("t"), rules.get(14));
        table.setValue(map.get("I"), map.get("@"), rules.get(16));
        table.setValue(map.get("N"), map.get("s"), rules.get(18));
        table.setValue(map.get("N"), map.get("n"), rules.get(17));

        table2.setValue("S", "s", rules2.get(0));
        table2.setValue("S", "n", rules2.get(0));
        table2.setValue("A", "s", rules2.get(1));
        table2.setValue("A", "n", rules2.get(2));
        table2.setValue("C", ",", rules2.get(3));
        table2.setValue("C", "[", rules2.get(4));
        table2.setValue("B", "[", rules2.get(5));
        table2.setValue("D", "[", rules2.get(6));
        table2.setValue("E", ":", rules2.get(7));
        table2.setValue("G", "]", rules2.get(9));
        table2.setValue("G", ":", rules2.get(8));
        table2.setValue("F", ":", rules2.get(10));
        table2.setValue("H", "s", rules2.get(11));
        table2.setValue("H", "n", rules2.get(11));
        table2.setValue("H", "t", rules2.get(11));
        table2.setValue("H", "@", rules2.get(11));
        table2.setValue("J", "s", rules2.get(12));
        table2.setValue("J", "n", rules2.get(12));
        table2.setValue("J", "]", rules2.get(13));
        table2.setValue("J", ":", rules2.get(13));
        table2.setValue("J", "t", rules2.get(12));
        table2.setValue("J", "@", rules2.get(12));
        table2.setValue("I", "s", rules2.get(15));
        table2.setValue("I", "n", rules2.get(15));
        table2.setValue("I", "t", rules2.get(14));
        table2.setValue("I", "@", rules2.get(16));
        table2.setValue("N", "s", rules2.get(18));
        table2.setValue("N", "n", rules2.get(17));



//        String input = "T, T', {E}, E', F\n" +
//                "    [ E  : T E' ]\n" +
//                "    [ E' : \"+\" T E' : @ ]\n" +
//                "    [ T  : F T' ]\n" +
//                "    [ T' : \"*\" F T' : @ ]\n" +
//                "    [ F  : \"n\" : \"(\" E \")\" ]";

        String input = "{S}, A" +
                "[ S : \"-\" A ]" +
                "[ A : \"n\" ]";

        Scanner scanner = new Scanner(input);

        TopDownParse(scanner, table);

    }

    static void TopDownParse(Scanner scanner, Table table) {
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
                    throw new RuntimeException("ERROR1");
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
                throw new RuntimeException("ERROR2");
            }
        } while (a.getTag() != DomainTag.END);

//        for (Rule rule : result) {
//            System.out.println(rule);
//        }

        Queue<Rule> queue = new ArrayDeque<>(result);

        OutputTree tree = new OutputTree(new OutputTree.Node(result.get(0).getLeft().getValue()));
        foo(tree.getRoot(), queue);
        System.out.println(tree.toGraphviz());
    }

    public static void foo(OutputTree.Node node, Queue<Rule> queue) {
        Rule rule = queue.poll();
        if (rule == null) {
            return;
        }
        for (Symbol symbol : rule.getRight()) {
            if (symbol.isNonterminal()) {
                if (!symbol.getValue().equals("eps")) {
                    OutputTree.Node newNode = new OutputTree.Node(symbol.getValue());
                    node.add(newNode);
                    foo(newNode, queue);
                } else {
                    node.add(new OutputTree.Node(symbol.getValue()));
                }
            } else {
                node.add(new OutputTree.Node(symbol.getValue()));
            }
        }

    }
}