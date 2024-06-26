% Лабораторная работа № 1.3 «Объектно-ориентированный
  лексический анализатор»
% 18 марта 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является приобретение навыка реализации лексического анализатора на
объектно-ориентированном языке без применения каких-либо средств автоматизации решения задачи
лексического анализа.

# Индивидуальный вариант
- Идентификаторы: последовательности десятичных цифр.
- Числовые литералы: римские цифры или ключевое слово «NIHIL» (представляет значение 0), не чувствительны
к регистру.

# Реализация

Исходный код программы
```kotlin
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
```

## Особенности программы

Преобразование римских чисел в целочисленные значения в Kotlin представлен следующим фрагментом кода:
```kotlin
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
```
Данный фрагмент кода реализует функцию для преобразования римских чисел в их целочисленные эквиваленты в
языке программирования Kotlin. Код использует функцию mapIndexed для обработки каждого символа входной
строки и конструкцию when для определения значения каждого символа в римском числе. Дополнительно
реализована логика вычитания в соответствии с правилами римской нумерации.
Преобразование римских чисел в целочисленные значения происходит корректно и эффективно благодаря
компактности и ясности представленного кода.

Также был применен подход, демонстрирующий следующий фрагмент кода программы:
```kotlin
private val _messages: MutableList<Message> = mutableListOf()
val messages: List<Message> = _messages

private val _nameCodes: MutableMap<String, Int> = mutableMapOf()
val nameCodes: Map<String, Int> = _nameCodes

private val _names: MutableList<String> = mutableListOf()
val names: List<String> = _names

private val _tokens: MutableList<Token> = mutableListOf()
val tokens: List<Token> = _tokens
```
Этот подход использует приватные свойства с префиксом _, которые представляют собой изменяемые коллекции
(списки и словари), и публичные свойства без префикса, которые представляют эти коллекции в неизменяемом
виде. 

Преимущества такого подхода:

1. Изоляция данных: Публичные свойства `messages`, `nameCodes`, `names` и `tokens` предоставляют доступ к
данным в неизменяемом виде, что обеспечивает защиту от несанкционированных изменений. Только владелец
класса имеет доступ к изменяемым версиям данных, что обеспечивает контроль над их состоянием.

2. Безопасность типов: Из-за использования неизменяемых коллекций через публичные свойства, внешние
компоненты не могут изменять данные напрямую. Это помогает избежать ошибок типов данных и обеспечивает
безопасность данных.

3. Повышение уровня абстракции: Публичные свойства предоставляют доступ к данным через более абстрактный
интерфейс, что может облегчить использование класса и сократить объем кода внешних компонентов за счет
отсутствия необходимости вручную преобразовывать изменяемые коллекции в неизменяемые.

4. Удобство использования: Поскольку публичные свойства представляют коллекции в неизменяемом виде, их
можно использовать для чтения данных без опасности случайного изменения.

Такой подход способствует созданию более безопасного и модульного кода, облегчает отладку и управление
состоянием данных внутри класса.

Кроме того, в программе используются sealed классы для представления различных типов сообщений и токенов
в данной программе, что обеспечивает безопасность типов и упрощает обработку данных. Вот какие
преимущества это приносит:

1. Безопасность типов: Sealed классы позволяют определить ограниченный набор подтипов. Это означает, что
компилятор может гарантировать, что все возможные варианты типа обрабатываются. Например, в программе
используются классы `Error`, `Warning` и `Info` внутри sealed класса `Message`. Это обеспечивает
компилятору возможность проверить, что все возможные типы сообщений учитываются при обработке.

2. Ясность кода: Использование sealed классов делает код более понятным и выразительным. Поскольку все
типы сообщений и токенов определены в рамках одного sealed класса, это делает структуру программы более
ясной и упрощает понимание ее логики.

3. Удобство обработки данных: Поскольку все типы сообщений и токенов определены внутри одного sealed
класса, их можно обрабатывать единообразно. Например, можно использовать сопоставление с образцом
(pattern matching) для обработки различных типов сообщений или токенов, что делает код более лаконичным и
выразительным.

4. Расширяемость: Sealed классы позволяют легко добавлять новые типы сообщений или токенов, не затрагивая
существующий код. Просто добавьте новый подкласс к sealed классу, и компилятор убедится, что все места,
где используются типы сообщений или токенов, обрабатывают новый подтип.

Таким образом, использование sealed классов в данной программе обеспечивает безопасность типов, упрощает
код и делает его более гибким для расширения.

Также были использованы Data классы в программе для представления различных структур данных. Например,
классы `Position`, `FragmentPosition` были объявлены как data классы. Использование data классов в этом
контексте позволило автоматически сгенерировать реализацию стандартных методов, таких как `toString()`,
`equals()`, `hashCode()`, что сделало код более компактным и читаемым. Кроме того, data классы обеспечили
возможность использования деструктуризации для удобного доступа к полям объектов.

# Тестирование

Входные данные

```
237 niHIL 666aa     23
2332a a888 XVII xxx2
777 iv xx  237
```

Вывод на `stdout`

```
PROGRAM:
237 niHIL 666aa     23
2332a a888 XVII xxx2
777 iv xx  237

TOKENS:
IDENTIFIER (1, 1)-(1, 3): 0
NUMERIC_LITERAL (1, 5)-(1, 9): 0
IDENTIFIER (1, 21)-(1, 22): 1
IDENTIFIER (2, 8)-(2, 10): 2
NUMERIC_LITERAL (2, 12)-(2, 15): 17
IDENTIFIER (2, 20)-(2, 20): 3
IDENTIFIER (3, 1)-(3, 3): 4
NUMERIC_LITERAL (3, 5)-(3, 6): 4
NUMERIC_LITERAL (3, 8)-(3, 9): 20
IDENTIFIER (3, 12)-(3, 14): 0

MESSAGES:
ERROR (1, 11): Bad identifier
ERROR (2, 1): Bad identifier
ERROR (2, 7): Bad code point
ERROR (2, 17): Bad numerical literal

```

# Вывод
В результате выполнения лабораторной работы был успешно реализован лексический анализатор на языке
программирования Kotlin. Программа способна корректно обрабатывать входной текст, выделяя из него токены
предоставляя информацию о них в заданном формате. Кроме того, были применены разнообразные подходы в
написании этой программы, которые делают код компактным и эффективным. Реализация анализатора позволила
углубить понимание принципов лексического анализа и его реализации на практике.