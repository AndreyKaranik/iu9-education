public class Token {

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