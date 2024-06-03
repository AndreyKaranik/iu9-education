public class Token {

    private String value;
    private DomainTag tag;
    private FragmentPosition fragmentPosition;

    public Token(String value, DomainTag tag, FragmentPosition fragmentPosition) {
        this.value = value;
        this.tag = tag;
        this.fragmentPosition = fragmentPosition;
    }

    public String getValue() {
        return value;
    }

    public DomainTag getTag() {
        return tag;
    }


    public FragmentPosition getFragmentPosition() {
        return fragmentPosition;
    }
}