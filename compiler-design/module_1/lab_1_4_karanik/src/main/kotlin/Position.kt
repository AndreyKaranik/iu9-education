data class Position(var line: Int, var column: Int, var index: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int {
        return index.compareTo(other.index)
    }

    override fun toString(): String {
        return "($line, $column, $index)"
    }

    fun getSimplifiedString(): String {
        return "($line, $column)"
    }
}

data class FragmentPosition(var startPosition: Position, var endPosition: Position) {
    override fun toString(): String {
        return "$startPosition-$endPosition"
    }

    fun getSimplifiedString(): String {
        return "${startPosition.getSimplifiedString()}-${endPosition.getSimplifiedString()}"
    }
}