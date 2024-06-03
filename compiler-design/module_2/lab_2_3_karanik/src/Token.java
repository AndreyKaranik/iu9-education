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