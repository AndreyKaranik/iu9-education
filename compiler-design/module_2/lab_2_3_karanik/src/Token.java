public class Token {

    private String value;
    private DomainTag tag;
    private String terminalTag;

    public Token(String value, DomainTag tag, String terminalTag) {
        this.value = value;
        this.tag = tag;
        this.terminalTag = terminalTag;
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
}