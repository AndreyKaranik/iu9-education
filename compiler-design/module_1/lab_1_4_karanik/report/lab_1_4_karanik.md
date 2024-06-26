% Лабораторная работа № 1.4 «Лексический распознаватель»
% 25 марта 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является изучение использования детерминированных конечных автоматов с размеченными
заключительными состояниями (лексических распознавателей) для решения задачи лексического анализа.

# Индивидуальный вариант
signed, unsigned, ., .., строковые литералы ограничены запятыми, могут пересекать границы строк текста.

# Реализация

Лексическая структура языка — регулярные выражения для доменов по стандарту **POSIX**:
* SPACES = `[[:space:]]+`
 пробелы — непустые последовательности пробельных символов (пробел, горизонтальная табуляция, маркеры
конца строки);
* IDENTIFIER = `[a-zA-Z][a-zA-Z0-9]*`
 идентификаторы — непустые последовательности латинских букв и десятичных цифр, начинающиеся с буквы;
* NUMERIC_LITERAL = `[0-9]+`
 целочисленные литералы — непустые последовательности десятичных цифр;
* KEYWORD = `signed|unsigned`
 ключевые слова (см. индивидуальный вариант);
* OPERATION_SIGN = `[.]|[.][.]`
 знаки операций (см. индивидуальный вариант);
* STRING_LITERAL = `,[a-zA-Z0-9[:space:]]*,`
 комментарии или строковые литералы (см. индивидуальный вариант).

Граф детерминированного распознавателя:

```dot
digraph G {
    rankdir = LR;
    
    node [shape = doublecircle]; 
    6, 14, 15, 16, 18, 19, 20, 21, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13
    node [shape = circle];
    
    0 -> 1 [label = "s"];
    1 -> 2 [label = "i"];
    2 -> 3 [label = "g"];
    3 -> 4 [label = "n"];
    4 -> 5 [label = "e"];
    5 -> 6 [label = "d"];
    
    0 -> 7 [label = "u"];
    7 -> 8 [label = "n"];
    8 -> 9 [label = "s"];
    9 -> 10 [label = "i"];
    10 -> 11 [label = "g"];
    11 -> 12 [label = "n"];
    12 -> 13 [label = "e"];
    13 -> 14 [label = "d"];
    
    0 -> 15 [label = "[.]"];
    15 -> 16 [label = "[.]"];
    
    0 -> 17 [label = ","];
    17 -> 17 [label = "u|n|s|i|g|e|d|[.]|[[:space:]]|[^unsiged]|[0-9]"];
    17 -> 18 [label = ","];
    
    0 -> 19 [label = "[[:space:]]"]
    19 -> 19 [label = "[[:space:]]"]
    
    0 -> 20 [label = "n|i|g|e|d|[^unsiged]"]
    20 -> 20 [label = "u|n|s|i|g|e|d|[^unsiged]|[0-9]"]
    
    0 -> 21 [label = "[0-9]"]
    21 -> 21 [label = "[0-9]"]
    
    1 -> 20 [label = "u|n|s|g|e|d|[^unsiged]|[0-9]"]
    2 -> 20 [label = "u|n|s|i|e|d|[^unsiged]|[0-9]"]
    3 -> 20 [label = "u|s|i|g|e|d|[^unsiged]|[0-9]"]
    4 -> 20 [label = "u|n|s|i|g|d|[^unsiged]|[0-9]"]
    5 -> 20 [label = "u|n|s|i|g|e|[^unsiged]|[0-9]"]
    6 -> 20 [label = "u|n|s|i|g|e|d|[^unsiged]|[0-9]"]
    7 -> 20 [label = "u|s|i|g|e|d|[^unsiged]|[0-9]"]
    8 -> 20 [label = "u|n|i|g|e|d|[^unsiged]|[0-9]"]
    9 -> 20 [label = "u|n|s|g|e|d|[^unsiged]|[0-9]"]
    10 -> 20 [label = "u|n|s|i|e|d|[^unsiged]|[0-9]"]
    11 -> 20 [label = "u|s|i|g|e|d|[^unsiged]|[0-9]"]
    12 -> 20 [label = "u|n|s|i|g|d|[^unsiged]|[0-9]"]
    13 -> 20 [label = "u|n|s|i|g|e|[^unsiged]|[0-9]"]
    14 -> 20 [label = "u|n|s|i|g|e|d|[^unsiged]|[0-9]"]
}
```

Здесь финальные состояния:
* **1-5, 7-13, 20** - IDENTIFIER
* **6** - KEYWORD (signed)
* **14** - KEYWORD (unsigned)
* **15** - OPERATION_SIGN (.)
* **16** - OPERATION_SIGN (.\.)
* **18** - STRING_LITERAL
* **19** - SPACES
* **21** - NUMERIC_LITERAL

По графу видно, что
* s (0 -> 1) - IDENTIFIER
* si (0 -> 1 -> 2) - IDENTIFIER
* sig (0 -> 1 -> 2 -> 3) - IDENTIFIER
* sign (0 -> 1 -> 2 -> 3 -> 4) - IDENTIFIER
* signe (0 -> 1 -> 2 -> 3 -> 4 -> 5) - IDENTIFIER
* signed (0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 6) - KEYWORD
* u (0 -> 7) - IDENTIFIER
* un (0 -> 7 -> 8) - IDENTIFIER
* uns (0 -> 7 -> 8 -> 9) - IDENTIFIER
* unsi (0 -> 7 -> 8 -> 9 -> 10) - IDENTIFIER
* unsig (0 -> 7 -> 8 -> 9 -> 10 -> 11) - IDENTIFIER
* unsign (0 -> 7 -> 8 -> 9 -> 10 -> 11 -> 12) - IDENTIFIER
* unsigne (0 -> 7 -> 8 -> 9 -> 10 -> 11 -> 12 -> 13) - IDENTIFIER
* unsigned (0 -> 7 -> 8 -> 9 -> 10 -> 11 -> 12 -> 13 -> 14) - KEYWORD

Заметим, что состояние 17 не является финальным. Это означает, что строковой литерал обязан заканчиваться
на ','. Стоит отметить, что состояние не может являться финальным состоянием и относиться к IDENTIFIER,
как в приведенном выше примере, поскольку идентификатор принадлежит регулярному выражению
`[a-zA-Z][a-zA-Z0-9]*` и не может к тому же начиться с ','.

Реализация распознавателя:

Детерменированный автомат был представлен следующим классом:
```kotlin
class Automaton {
    var transitionMatrix: Array<Array<Int>> = arrayOf()
    val statesNumber
        get() = transitionMatrix.size
    var finalStates = mutableSetOf<Int>()
}
```

Он описывается матрицей переходов, числом всех состояний и множеством финальных состояний.

Файл `Position.kt`:
```kotlin
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
```

Файл `Token.kt`:
```kotlin
sealed class Token(val domainTag: DomainTag, val fragmentPosition: FragmentPosition) {
    class Identifier(fragmentPosition: FragmentPosition, val name: String, val code: Int)
        : Token(DomainTag.IDENTIFIER, fragmentPosition)
    class NumericLiteral(fragmentPosition: FragmentPosition, val value: Long)
        : Token(DomainTag.NUMERIC_LITERAL, fragmentPosition)
    class Keyword(fragmentPosition: FragmentPosition, val name: String)
        : Token(DomainTag.KEYWORD, fragmentPosition)
    class OperationSign(fragmentPosition: FragmentPosition, val name: String)
        : Token(DomainTag.OPERATION_SIGN, fragmentPosition)
    class StringLiteral(fragmentPosition: FragmentPosition, val name: String)
        : Token(DomainTag.STRING_LITERAL, fragmentPosition)
}
```

Файл `Scanner.kt`:
```kotlin
class Scanner(private val compiler: Compiler, private val program: String) {
    private val cursor = Position(1, 1, 0)
    private val automaton = Automaton()
    private var error = false

    init {
        automaton.transitionMatrix = getTransitionMatrixFromGraphviz("transitions.txt", 22, 13)
        automaton.finalStates = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18,
        19, 20, 21)
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
                        return Token.Identifier(FragmentPosition(startPosition, endPosition), name,
                        compiler.addName(name)!!)
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
                        return Token.NumericLiteral(FragmentPosition(startPosition, endPosition),
                        name.toLong())
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
```

Файл `Main.kt`:
```kotlin
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

sealed class Message(val type: Type, val text: String) {
    enum class Type {
        ERROR, WARNING, INFO
    }

    class Error(text: String) : Message(Type.ERROR, text)
    class Warning(text: String) : Message(Type.WARNING, text)
    class Info(text: String) : Message(Type.INFO, text)
}

class Automaton {
    var transitionMatrix: Array<Array<Int>> = arrayOf()
    val statesNumber
        get() = transitionMatrix.size
    var finalStates = mutableSetOf<Int>()
}

enum class DomainTag {
    SPACES, IDENTIFIER, NUMERIC_LITERAL, KEYWORD, OPERATION_SIGN, STRING_LITERAL
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
                DomainTag.IDENTIFIER -> (token as Token.Identifier).name + " " + token.code
                DomainTag.NUMERIC_LITERAL -> (token as Token.NumericLiteral).value
                DomainTag.KEYWORD -> (token as Token.Keyword).name
                DomainTag.OPERATION_SIGN -> (token as Token.OperationSign).name
                DomainTag.STRING_LITERAL -> (token as Token.StringLiteral).name
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

fun getTransitionMatrixFromGraphviz(fileName: String, statesNumber: Int, symbolsNumber: Int) :
        Array<Array<Int>> {
    val result: Array<Array<Int>> = Array(statesNumber) { Array(symbolsNumber) { -1 } }
    for (i in 0..<statesNumber) {
        result
    }
    try {
        val lines = Files.readAllLines(Paths.get(fileName))
        for (line in lines) {
            if (line == "") {
                continue
            }
            val str = line.replace(" ", "")
            val split1 = str.split('l')
            val split2 = split1.first.split('-')
            val stateFrom = split2.first.toInt()
            val stateTo = split2[1].substring(1, split2[1].length - 1).toInt()
            val split3 = split1[2].split('"')
            val split4 = split3[1].split('|')
            for (s in split4) {
                result[stateFrom][getSymbolIndexFromGraphviz(s)] = stateTo
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return result
}

fun getSymbolIndexFromGraphviz(symbol: String): Int {
    if (symbol == "u") {
        return 0
    }
    else if (symbol == "n") {
        return 1
    }
    else if (symbol == "s") {
        return 2
    }
    else if (symbol == "i") {
        return 3
    }
    else if (symbol == "g") {
        return 4
    }
    else if (symbol == "e") {
        return 5
    }
    else if (symbol == "d") {
        return 6
    }
    else if (symbol == "[.]") {
        return 7
    }
    else if (symbol == ",") {
        return 8
    }
    else if (symbol == "[[:space:]]") {
        return 9
    }
    else if (symbol == "[^unsiged]") {
        return 10
    }
    else if (symbol == "[0-9]") {
        return 11
    }
    else {
        return 12
    }
}

fun main(args: Array<String>) {
    val compiler = Compiler()

    getTransitionMatrixFromGraphviz("transitions.txt", 22, 13)

    val filePath = "input.txt"
    var content = Files.readString(Paths.get(filePath), Charsets.US_ASCII)

    compiler.applyLexicalAnalysis(content)

    println("PROGRAM:")
    println(content)
    println()
    println("TOKENS:")
    compiler.printTokens()
    println()
    println("MESSAGES:")
    compiler.printMessages()
    println()
}
```

В реализации также были применены возможности языка Kotlin: использование sealed классов, data классов и т.д.

# Тестирование

Входные данные

```
signed
,compiler,
abc cde
.. . unsigned
1234
```

Вывод на `stdout`

```
PROGRAM:
signed
,compiler,
abc cde
.. . unsigned
1234

TOKENS:
KEYWORD (1, 1)-(1, 6): signed
STRING_LITERAL (2, 1)-(2, 10): ,compiler,
IDENTIFIER (3, 1)-(3, 3): abc 0
IDENTIFIER (3, 5)-(3, 7): cde 1
OPERATION_SIGN (4, 1)-(4, 2): ..
OPERATION_SIGN (4, 4)-(4, 4): .
KEYWORD (4, 6)-(4, 13): unsigned
NUMERIC_LITERAL (5, 1)-(5, 4): 1234

MESSAGES:

```

Лексемы, принадлежащие домену пробелов, отбрасываются. Это видно по результатам работы программы.

Ключевое слово signed идет в самом начале в первой строчке и это дейсвительно так. Затем на следующей
строчке идет строковой литерал, соответствующий своему определению. Далее идут 2 идентификатора, 2 знака
операций, ключевое слово unsigned и числовой литерал, что совпадает с ожидаемым.

# Вывод
В ходе выполнения лабораторной работы было изучено использование детерминированных конечных автоматов с
размеченными заключительными состояниями лексических распознавателей для решения задачи лексического
анализа. Была разработана программа на языке программирования Kotlin с использованием его особенностей
для которой были описаны лексические домены модельного языка в виде регулярных выражений, построен
недетерминированный лексический распознаватель для модельного языка, который затем был детерминирован с
факторизацией его алфавита, построен массив обобщённых символов, матрицы переходов и множества
заключительных состояний для полученного детерминированного лексического распознавателя с факторизованным
алфавитом.