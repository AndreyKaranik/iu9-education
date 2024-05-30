class Symbol {
    private Main.DomainTag tag;

    private String value;
    private boolean isNonterminal;
    public Symbol(Main.DomainTag tag, String value, boolean isNonterminal) {
        this.tag = tag;
        this.value = value;
        this.isNonterminal = isNonterminal;
    }

    public Main.DomainTag getTag() {
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