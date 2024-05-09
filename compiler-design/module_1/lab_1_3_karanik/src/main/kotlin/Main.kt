import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

sealed class Message(val type: Type, val text: String) {
    enum class Type {
        ERROR, WARNING, INFO
    }

    class Error(text: String) : Message(Type.ERROR, text)
    class Warning(text: String) : Message(Type.WARNING, text)
    class Info(text: String) : Message(Type.INFO, text)
}

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

enum class DomainTag {
    IDENTIFIER, NUMERIC_LITERAL
}

sealed class Token(val domainTag: DomainTag, val fragmentPosition: FragmentPosition) {
    class Identifier(fragmentPosition: FragmentPosition, val name: String, val code: Int)
        : Token(DomainTag.IDENTIFIER, fragmentPosition)
    class NumericLiteral(fragmentPosition: FragmentPosition, val name: String, val value: Long)
        : Token(DomainTag.NUMERIC_LITERAL, fragmentPosition)
}

class Compiler {
    private val _messages: MutableList<Message> = mutableListOf()
    val messages: List<Message> = _messages

    private val _nameCodes: MutableMap<String, Int> = mutableMapOf()
    val nameCodes: Map<String, Int> = _nameCodes

    private val _names: MutableList<String> = mutableListOf()
    val names: List<String> = _names

    private val _tokens: MutableList<Token> = mutableListOf()
    val tokens: List<Token> = _tokens

    fun addMessage(message: Message) {
        _messages.add(message)
    }

    fun addMessage(messageType: Message.Type, position: Position, description: String) {
        val text = "${messageType.name} ${position.getSimplifiedString()}: $description"

        when (messageType) {
            Message.Type.ERROR -> _messages.add(Message.Error(text))
            Message.Type.WARNING -> _messages.add(Message.Warning(text))
            Message.Type.INFO -> _messages.add(Message.Info(text))
        }
    }

    fun printTokens() {
        for (token in tokens) {
            val attributes = when (token.domainTag) {
                DomainTag.IDENTIFIER -> "${(token as Token.Identifier).code}"
                DomainTag.NUMERIC_LITERAL -> "${(token as Token.NumericLiteral).value}"
                else -> ""
            }
            println("${token.domainTag} ${token.fragmentPosition.getSimplifiedString()}: $attributes")
        }
    }

    fun printMessages() {
        for (message in messages) {
            println(message.text)
        }
    }

    fun addName(name: String) : Int? {
        if (_names.contains(name)) {
            return _nameCodes[name]
        } else {
            val code = _names.count()
            _names.add(name)
            _nameCodes[name] = code
            return code
        }
    }

    fun getName(code: Int): String {
        return _names[code]
    }

    fun applyLexicalAnalysis(program: String) {
        _tokens.clear()
        _messages.clear()
        _nameCodes.clear()
        _names.clear()

        val scanner = Scanner(this, program)

        var token = scanner.nextToken()
        while (token != null) {
            _tokens.add(token)
            token = scanner.nextToken()
        }
    }
}

fun romanToLong(value: String): Long =
    value.mapIndexed { index, c ->
        when (c) {
            'I' -> if ("VX".contains(value.getOrNull(index + 1) ?: ' ')) -1L else 1L
            'V' -> 5L
            'X' -> if ("LC".contains(value.getOrNull(index + 1) ?: ' ')) -10L else 10L
            'L' -> 50L
            'C' -> if ("DM".contains(value.getOrNull(index + 1) ?: ' ')) -100L else 100L
            'D' -> 500L
            'M' -> 1000L
            else -> throw IllegalArgumentException("$c is not roman char")
        }
    }.sum()

fun Char.isRomanChar(): Boolean {
    return this.equals('I', true) ||
            this.equals('V', true) ||
            this.equals('X', true) ||
            this.equals('L', true) ||
            this.equals('C', true) ||
            this.equals('D', true) ||
            this.equals('M', true)
}

class Scanner(private val compiler: Compiler, private val program: String) {
    private val cursor = Position(1, 1, 0)
    private var error = false

    fun nextToken() : Token? {
        while (cursor.index < program.length) {
            if (program[cursor.index] == '\n') {
                error = false
                cursor.line++
                cursor.column = 1
                cursor.index++
            } else if (program[cursor.index].isWhitespace()) {
                error = false
                cursor.column++
                cursor.index++
            } else if (program[cursor.index].isDigit()) {
                error = false
                val startPosition = cursor.copy()
                var endPosition = cursor.copy()
                while (cursor.index < program.length && program[cursor.index].isDigit()) {
                    endPosition = cursor.copy()
                    cursor.column++
                    cursor.index++
                }
                if (cursor.index == program.length || program[cursor.index].isWhitespace()) {
                    val name = program.substring(startPosition.index, endPosition.index + 1)
                    return Token.Identifier(FragmentPosition(startPosition, endPosition), name,
                        compiler.addName(name)!!)
                } else {
                    compiler.addMessage(Message.Type.ERROR, startPosition, "Bad identifier")
                    error = true
                }
            } else if (program[cursor.index].equals('N', true) || program[cursor.index].isRomanChar()) {
                error = false
                val startPosition = cursor.copy()
                var endPosition = cursor.copy()
                var nihilIndex = 0
                var nihil = false
                if (program[cursor.index].equals('N',true)) {
                    while (!nihil && !error && cursor.index < program.length) {
                        when(nihilIndex) {
                            0 -> if (!program[cursor.index].equals('N', true)) error = true
                            1 -> if (!program[cursor.index].equals('I', true)) error = true
                            2 -> if (!program[cursor.index].equals('H', true)) error = true
                            3 -> if (!program[cursor.index].equals('I', true)) error = true
                            4 -> if (!program[cursor.index].equals('L', true)) error = true else nihil = true
                        }
                        nihilIndex++
                        endPosition = cursor.copy()
                        cursor.column++
                        cursor.index++
                    }
                    error = nihilIndex == 5
                } else if (program[cursor.index].isRomanChar()) {
                    while (cursor.index < program.length && program[cursor.index].isRomanChar()) {
                        endPosition = cursor.copy()
                        cursor.column++
                        cursor.index++
                    }
                } else {
                    error = true
                    cursor.column++
                    cursor.index++
                }
                if (nihil) {
                    val name = program.substring(startPosition.index, endPosition.index + 1)
                    return Token.NumericLiteral(FragmentPosition(startPosition, endPosition), name, 0)
                } else if (!error && (cursor.index == program.length ||
                            program[cursor.index].isWhitespace())) {
                    val name = program.substring(startPosition.index, endPosition.index + 1)
                    return Token.NumericLiteral(FragmentPosition(startPosition, endPosition), name,
                        romanToLong(name.uppercase(Locale.getDefault())))
                } else {
                    compiler.addMessage(Message.Type.ERROR, startPosition, "Bad numerical literal")
                    error = true
                }
            } else if (!error) {
                compiler.addMessage(Message.Type.ERROR, cursor.copy(), "Bad code point")
                cursor.column++
                cursor.index++
                error = true
            } else {
                cursor.column++
                cursor.index++
            }
        }

        return null
    }
}

fun main(args: Array<String>) {
    val compiler = Compiler()

    val filePath = "input.txt"
    var content = Files.readString(Paths.get(filePath), Charsets.UTF_8)

    compiler.applyLexicalAnalysis(content)

    println("PROGRAM:")
    println(content)
    println()
    println("TOKENS:")
    compiler.printTokens()
    println()
    println("MESSAGES:")
    compiler.printMessages()
}