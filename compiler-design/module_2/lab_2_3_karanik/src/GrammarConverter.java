public class GrammarConverter {


    /*
    T, T', { E }, E', F
    [ E  : T E' ]
    [ E' : "+" T E' : @]
    [ T  : F T' ]
    [ T' : "*" F T' : @ ]
    [ F  : "n" : "(" E ")" ]
     */

    /*
    Grammar -> Definitions Rules
    Definitions -> Axiom Definitions' | nonterminal , Definitions
    Definitions' -> , nonterminal Definitions' | eps
    Rules -> Rule Rules
    Rule -> [ nonterminal Rule' ]
    Rule' -> Variant Variants
    Variants -> Variant Variants | eps
    Variant -> : Sequence
    Sequence -> Symbol Sequence'
    Sequence' -> Symbol Sequence' | eps
    Symbol -> terminal | nonterminal | @
    Axiom -> { nonterminal }
     */

    /*

    S = Grammar
    A = Definitions
    B = Rules
    C = Definitions'
    D = Rule
    E = Rule'
    F = Variant
    G = Variants
    H = Sequence
    I = Symbol
    J = Sequence'
    X = Axiom

     */

    public static String convert(String s) {
        String result = s
                .replace("Grammar", "S")
                .replace("Definitions'", "C")
                .replace("Definitions", "A")
                .replace("Rules", "B")
                .replace("Rule'", "E")
                .replace("Rule", "D")
                .replace("Variants", "G")
                .replace("Variant", "F")
                .replace("Sequence'", "J")
                .replace("Sequence", "H")
                .replace("Symbol", "I")
                .replace("nonterminal", "n")
                .replace("terminal", "t")
                .replace("Axiom", "X");
        return result;
    }


    public static String convertSpecificGrammar(String grammar) {
        String result = grammar
                .replace("Grammar", "S")
                .replace("Definitions'", "C")
                .replace("Definitions", "A")
                .replace("Rules", "B")
                .replace("Rule'", "E")
                .replace("Rule", "D")
                .replace("Variants", "G")
                .replace("Variant", "F")
                .replace("Sequence'", "J")
                .replace("Sequence", "H")
                .replace("Symbol", "I")
                .replace(",", "','")
                .replace(":", "':'")
                .replace("@", "'@'")
                .replace("eps", "")
                .replace("{", "'{'")
                .replace("}", "'}'")
                .replace("[", "'['")
                .replace("]", "']'")
                .replace("nonterminal", "n")
                .replace("terminal", "t")
                .replace("Axiom", "X");

        result = result.replaceAll("(?m)$", ".");

        return result;
    }
}
