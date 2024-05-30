public class GrammarConverter {


    /*
    T, T', { E }, E', F
    [ E  : T E' ]
    [ E' : "+" T E' : @]
    [ T  : F T' ]
    [ T' : "*" F T' : @ ]
    [ F  : "n" : "(" E ")" ]
     */

    /* кол-во {E} не отслеживается
    Grammar -> Definitions Rules
    Definitions -> nonterminal Definitions'
    Definitions' -> , nonterminal Definitions' | eps
    Rules -> Rule Rules
    Rule -> [ nonterminal Rule' ]
    Rule' -> Variant Variants
    Variants -> Variant Variants | eps
    Variant -> : Sequence
    Sequence -> Symbol Sequence'
    Sequence' -> Symbol Sequence' | eps
    Symbol -> terminal | nonterminal | @
     */

    /* {E} ровно 1
    Grammar -> Definitions Rules
    Definitions -> start Definitions' | nonterminal , Definitions
    Definitions' -> , nonterminal Definitions' | eps
    Rules -> Rule Rules
    Rule -> [ Nonterminal Rule' ]
    Rule' -> Variant Variants
    Variants -> Variant Variants | eps
    Variant -> : Sequence
    Sequence -> Symbol Sequence'
    Sequence' -> Symbol Sequence' | eps
    Symbol -> terminal | Nonterminal | @
    Nonterminal -> nonterminal | start
     */

    /*

    S = Grammar
    A = Definitions
    B = Rules
    C = Definitions'
    D = Rule
    N = Nonterminal
    E = Rule'
    F = Variant
    G = Variants
    H = Sequence
    I = Symbol
    J = Sequence'

     */

    public static String convert(String symbol) {
        String result = symbol
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
                .replace("Nonterminal", "N")
                .replace(",", "','")
                .replace(":", "':'")
                .replace("@", "'@'")
                .replace("nonterminal", "n")
                .replace("terminal", "t")
                .replace("start", "s");
        return result;
    }


    public static String convertGrammar(String grammar) {
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
                .replace("Nonterminal", "N")
                .replace(",", "','")
                .replace(":", "':'")
                .replace("@", "'@'")
                .replace("eps", "")
                .replace("[", "'['")
                .replace("]", "']'")
                .replace("nonterminal", "n")
                .replace("terminal", "t")
                .replace("start", "s");

        result = result.replaceAll("(?m)$", ".");

        return result;
    }
}
