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