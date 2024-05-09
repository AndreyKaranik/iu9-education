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