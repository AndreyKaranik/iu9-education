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