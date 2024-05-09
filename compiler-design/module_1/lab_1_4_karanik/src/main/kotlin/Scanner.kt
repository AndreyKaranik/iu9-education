class Scanner(private val compiler: Compiler, private val program: String) {
    private val cursor = Position(1, 1, 0)
    private val automaton = Automaton()
    private var error = false

    init {
        automaton.transitionMatrix = getTransitionMatrixFromGraphviz("transitions.txt", 22, 13)
        automaton.finalStates = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 16, 18, 19, 20, 21)
    }

    fun nextToken() : Token? {
        while (cursor.index < program.length) {
            var currentState = 0
            var final = false
            val startPosition = cursor.copy()
            var endPosition = cursor.copy()

            while (cursor.index < program.length) {
                val symbolIndex = getSymbolIndex(program[cursor.index])

                val state = automaton.transitionMatrix[currentState][symbolIndex]

                if (state == -1) {
                    break
                }

                currentState = state

                final = automaton.finalStates.contains(currentState)
                endPosition = cursor.copy()

                if (program[cursor.index] == '\n') {
                    cursor.line++
                    cursor.column = 1
                } else {
                    cursor.column++
                }
                cursor.index++
            }

            if (final) {
                error = false
                when (currentState) {
                    in 1..5, in 7..13, 20 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.Identifier(FragmentPosition(startPosition, endPosition), name, compiler.addName(name)!!)
                    }
                    6 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.Keyword(FragmentPosition(startPosition, endPosition), name)
                    }

                    14 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.Keyword(FragmentPosition(startPosition, endPosition), name)
                    }

                    15 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.OperationSign(FragmentPosition(startPosition, endPosition), name)
                    }

                    16 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.OperationSign(FragmentPosition(startPosition, endPosition), name)
                    }

                    18 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.StringLiteral(FragmentPosition(startPosition, endPosition), name)
                    }

                    21 -> {
                        val name = program.substring(startPosition.index, endPosition.index + 1)
                        return Token.NumericLiteral(FragmentPosition(startPosition, endPosition), name.toLong())
                    }
                }
            } else if (cursor.index >= program.length) {
                compiler.addMessage(Message.Type.ERROR, startPosition, "bad token")
            } else {
                if (!error) {
                    compiler.addMessage(Message.Type.ERROR, startPosition, "bad token")
                    error = true
                }
            }
        }
        return null
    }

    private fun getSymbolIndex(symbol: Char): Int {
        if (symbol == 'u') {
            return 0
        }
        else if (symbol == 'n') {
            return 1
        }
        else if (symbol == 's') {
            return 2
        }
        else if (symbol == 'i') {
            return 3
        }
        else if (symbol == 'g') {
            return 4
        }
        else if (symbol == 'e') {
            return 5
        }
        else if (symbol == 'd') {
            return 6
        }
        else if (symbol == '.') {
            return 7
        }
        else if (symbol == ',') {
            return 8
        }
        else if (symbol.isWhitespace()) {
            return 9
        }
        else if (symbol.isLetter()) {
            return 10
        }
        else if (symbol.isDigit()) {
            return 11
        }
        else {
            return 12
        }
    }
}