import abc
import enum
import parser_edsl as pe
import sys
import re
import typing
from dataclasses import dataclass
from pprint import pprint


class Type(enum.Enum):
    Int = 'INT'
    Char = 'CHAR'
    Bool = 'BOOL'
    Array = 'ARRAY'


class Expr(abc.ABC):
    pass


# class EmptyExpr(Expr):
#     pass

@dataclass
class Statement(abc.ABC):
    pass


# @dataclass
# class EmptyStatement(Statement):
#     pass


@dataclass
class DeclarationStatement(Statement):
    type: Type
    variables: list[(str, Expr | None)]


@dataclass
class AssignmentStatement(Statement):
    leftExpr: Expr
    rightExpr: Expr


@dataclass
class InvocationStatement(Statement):
    function: str
    actualParameters: list[Expr]


@dataclass
class IfStatement(Statement):
    condition: Expr
    then_branch: list[Statement]
    else_branch: list[Statement]


@dataclass
class PreWhileStatement(Statement):
    condition: Expr
    body: list[Statement]


@dataclass
class PostWhileStatement(Statement):
    condition: Expr
    body: list[Statement]


@dataclass
class ReturnStatement(Statement):
    expr: Expr | None


@dataclass
class BinOpExpr(Expr):
    left: Expr
    op: str
    right: Expr


@dataclass
class UnOpExpr(Expr):
    op: str
    expr: Expr

@dataclass
class FunctionHeader:
    type: Type
    name: str
    formalParameters: list[(Type, str)]

@dataclass
class FunctionDeclaration:
    header: FunctionHeader
    body: list[Statement]

@dataclass
class Program:
    functionDeclarations: list[FunctionDeclaration]

ESCAPE_SEQUENCES_REGEX = '%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%'

# INVISIBLE_SYMBOLS = pe.Terminal('INVISIBLE_SYMBOLS', '\\s+', lambda name: None)
# COMMENTS = pe.Terminal('COMMENTS', '(##.*$)|(#.*#)', lambda name: None)
IDENTIFIER = pe.Terminal('IDENTIFIER', '{(\\w|[ ])+}', str)
DECIMAL_INTEGER_CONSTANT = pe.Terminal('DECIMAL_INTEGER_CONSTANT', '[0-9]+', str, priority=7)
NON_DECIMAL_INTEGER_CONSTANT = pe.Terminal('NON_DECIMAL_INTEGER_CONSTANT',
                                           '([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))?', str)

SYMBOLIC_CONSTANT = pe.Terminal('SYMBOLIC_CONSTANT',
                                '[$](\"(.|' + ESCAPE_SEQUENCES_REGEX + ')\")|[A-F0-9]+', str)

STRING_SECTION = pe.Terminal('STRING_SECTION',
                                '\"\"', str)

BOOLEAN_CONSTANT = pe.Terminal('BOOLEAN_CONSTANT', 'true|false', str)
REFERENCE_NULL_CONSTANT = pe.Terminal('REFERENCE_NULL_CONSTANT', 'null', str)


# Нетерминальные символы
def make_keyword(image):
    return pe.Terminal(image, image, lambda name: None)

NProgram, NFunctionDeclarations, NFunctionDeclaration, NFunctionHeader, NFunctionHeaderTypeName, NFormalParameters, NFormalParameter, NStatements, NStatement = \
    map(pe.NonTerminal, 'Program FunctionDeclarations FunctionDeclaration FunctionHeader FunctionHeaderTypeName FormalParameters FormalParameter Statements Statement'.split())

NType, NArrayType, NBrackets, NDeclarationAssignments, NDeclarationAssignment, NActualParameters, NActualParameter = \
    map(pe.NonTerminal, 'Type ArrayType Brackets DeclarationAssignments DeclarationAssignment ActualParameters ActualParameter'.split())

NExpr,  = \
    map(pe.NonTerminal, 'Expr'.split())


KW_BOOL, KW_INT, KW_RETURN, KW_VOID, KW_CHAR, KW_LOOP, KW_THEN, KW_ELSE, KW_NULL, KW_WHILE = \
    map(make_keyword, 'bool int return void char loop then else null while'.split())

# Правила грамматики
NProgram |= NFunctionDeclarations
NFunctionDeclarations |= NFunctionDeclaration, lambda fd: [fd]
NFunctionDeclarations |= NFunctionDeclarations, NFunctionDeclaration, lambda fds, fd: fds + [fd]
NFunctionDeclaration |= NFunctionHeader, '=', NStatements, '.', FunctionDeclaration
NFunctionHeader |= NFunctionHeaderTypeName, lambda typename: FunctionHeader(typename[0], typename[1], [])
NFunctionHeader |= NFunctionHeaderTypeName, '<-', NFormalParameters, lambda typename, params: FunctionHeader(typename[0], typename[1], params)
NFunctionHeaderTypeName |= NType, IDENTIFIER, lambda t, n: (t, n)
NFunctionHeaderTypeName |= KW_VOID, IDENTIFIER, lambda n: (None, n)
NFormalParameters |= NFormalParameters, ',', NFormalParameter, lambda fps, fp: fps + [fp]
NFormalParameters |= NFormalParameter, lambda fp: [fp]
NFormalParameter |= NType, IDENTIFIER, lambda t, n: (t, n)


NStatements |= NStatements, ';', NStatement, lambda sts, st: sts + [st]
NStatements |= NStatement, lambda st: [st]
#
# #Оператор объявления
# NStatement |= NType, NDeclarationAssignment, ';'
# NDeclarationAssignments |= NDeclarationAssignments, ',', NDeclarationAssignment, ';'
# NDeclarationAssignment |= IDENTIFIER
# NDeclarationAssignment |= IDENTIFIER, ':=', NExpr
#
# #Оператор присваивания
# NStatement |= NExpr, ':=', NExpr, ';'
#
# #Оператор вызова функции
# NStatement |= IDENTIFIER, '<-', NActualParameters
# NActualParameters |= NActualParameters, ',', NActualParameter
# NActualParameters |= NActualParameter
# NActualParameter |= NExpr
#
# #Оператор выбора
# NStatement |= NExpr, KW_THEN, NStatements, '.'
# NStatement |= NExpr, KW_THEN, NStatements, KW_ELSE, NStatements, '.'
#
# #Оператор цикла с предусловием
# NStatement |= NExpr, KW_LOOP, NStatements, '.'
# #Второй вариант
# NStatement |= NExpr, '~', NExpr, KW_LOOP, IDENTIFIER, '.'
#
# #Оператор цикла с постусловием
# NStatement |= KW_LOOP, NStatements, KW_WHILE, NExpr, '.'
#
# #Оператор завершения функции
# NStatement |= 'return', NExpr, ReturnStatement
NStatement |= KW_RETURN, lambda: ReturnStatement(None)

#Тип
NType |= KW_INT, lambda: Type.Int
NType |= KW_CHAR, lambda: Type.Char
NType |= KW_BOOL, lambda: Type.Bool
NType |= NArrayType, lambda: Type.Array
#Ссылочный тип
NArrayType |= KW_INT, NBrackets
NArrayType |= KW_CHAR, NBrackets
NArrayType |= KW_BOOL, NBrackets
NBrackets |= NBrackets, '[]'
NBrackets |= '[]'


# Парсер
parser = pe.Parser(NProgram)
assert parser.is_lalr_one()

parser.add_skipped_domain('\\s')
parser.add_skipped_domain('(##.*$)|(#.*#)')
for filename in sys.argv[1:]:
    try:
        with open(filename) as f:
            tree = parser.parse(f.read())
            pprint(tree)
    except pe.Error as e:
        print(f'Ошибка {e.pos}: {e.message}')
    except Exception as e:
        print('Исключение', e)
