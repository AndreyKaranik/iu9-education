import java.util.*;

public class Main {

    enum DomainTag {
        TERMINAL, NONTERMINAL, START_SYMBOL, COMMA, LEFT_BRACKET, RIGHT_BRACKET, COLON, EMPTY_STRING, NONE
    }

    class Token {
        private String value;
        private DomainTag tag;

        public Token(String value, DomainTag tag) {
            this.value = value;
            this.tag = tag;
        }

        public String getValue() {
            return value;
        }

        public DomainTag getTag() {
            return tag;
        }
    }
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
                List<String> right = Arrays.asList(rightPart.trim().split("\\s+"));
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
        TwoKeyMap<String, String, Rule> table = new TwoKeyMap<>();
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

        List<Rule> rules = parseGrammar(grammar);
        for (int i = 0; i < rules.size(); i++) {
            //System.out.println(i + ": " + rules.get(i));
            System.out.println(i + ": " + rules.get(i).toSpecificString());
        }

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
        map.put("','", ",");
        map.put("':'", ":");
        map.put("'@'", "@");
        map.put("n", "nonterminal");
        map.put("t", "terminal");
        map.put("s", "start");


        table.put(map.get("S"), map.get("s"), rules.get(0));
        table.put(map.get("S"), map.get("n"), rules.get(0));
        table.put(map.get("A"), map.get("s"), rules.get(1));
        table.put(map.get("A"), map.get("n"), rules.get(2));
        table.put(map.get("C"), map.get(","), rules.get(3));
        table.put(map.get("C"), map.get("["), rules.get(4));
        table.put(map.get("B"), map.get("["), rules.get(5));
        table.put(map.get("D"), map.get("["), rules.get(6));

        table.put(map.get("E"), map.get(":"), rules.get(7));
        table.put(map.get("G"), map.get("]"), rules.get(9));
        table.put(map.get("G"), map.get(":"), rules.get(8));
        table.put(map.get("F"), map.get(":"), rules.get(10));

        table.put(map.get("H"), map.get("s"), rules.get(11));
        table.put(map.get("H"), map.get("n"), rules.get(11));
        table.put(map.get("H"), map.get("t"), rules.get(11));
        table.put(map.get("H"), map.get("@"), rules.get(11));

        table.put(map.get("J"), map.get("s"), rules.get(12));
        table.put(map.get("J"), map.get("n"), rules.get(12));
        table.put(map.get("J"), map.get("]"), rules.get(13));
        table.put(map.get("J"), map.get(":"), rules.get(13));
        table.put(map.get("J"), map.get("t"), rules.get(12));
        table.put(map.get("J"), map.get("@"), rules.get(12));


        table.put(map.get("I"), map.get("s"), rules.get(15));
        table.put(map.get("I"), map.get("n"), rules.get(15));
        table.put(map.get("I"), map.get("t"), rules.get(14));
        table.put(map.get("I"), map.get("@"), rules.get(16));

        table.put(map.get("N"), map.get("s"), rules.get(18));
        table.put(map.get("N"), map.get("n"), rules.get(17));


    }
}