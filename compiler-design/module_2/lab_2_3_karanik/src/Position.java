public class Position implements Comparable<Position> {
    private int line;
    private int column;
    private int index;

    public Position(int line, int column, int index) {
        this.line = line;
        this.column = column;
        this.index = index;
    }

    @Override
    public int compareTo(Position other) {
        return Integer.compare(this.index, other.index);
    }

    @Override
    public String toString() {
        return "(" + line + ", " + column + ", " + index + ")";
    }

    public String getSimplifiedString() {
        return "(" + line + ", " + column + ")";
    }
}