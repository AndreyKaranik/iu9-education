public class FragmentPosition {
    private Position startPosition;
    private Position endPosition;

    public FragmentPosition(Position startPosition, Position endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return startPosition.toString() + "-" + endPosition.toString();
    }

    public String getSimplifiedString() {
        return startPosition.getSimplifiedString() + "-" + endPosition.getSimplifiedString();
    }
}