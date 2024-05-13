import abc
import enum
import parser_edsl as pe
import sys
import re
import typing
from dataclasses import dataclass
from pprint import pprint


class PrimType(enum.Enum):
    Int = 'INT'
    Char = 'CHAR'
    Bool = 'BOOL'

@dataclass
class Type:
    base: PrimType
    array_level: int


class Expr(abc.ABC):
    pass

@dataclass
class Statement(abc.ABC):
    pass

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
class ForStatement(Statement):
    start: Expr
    end: Expr
    variable: str
    body: list[Statement]


@dataclass
class PostWhileStatement(Statement):
    body: list[Statement]
    condition: Expr


@dataclass
class ReturnStatement(Statement):
    expr: Expr | None

@dataclass
class VariableExpr(Expr):
    varname: str

@dataclass
class ConstExpr(Expr):
    value: typing.Any
    type: Type

@dataclass
class FunctionInvocationExpr(Statement):
    function: str
    actualParameters: list[Expr]

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
                                '(\"([^"\n]|' + ESCAPE_SEQUENCES_REGEX + ')*\")|(%[A-F0-9]+)', str)

BOOLEAN_CONSTANT = pe.Terminal('BOOLEAN_CONSTANT', 'true|false', str)
REFERENCE_NULL_CONSTANT = pe.Terminal('REFERENCE_NULL_CONSTANT', 'null', str)


# Нетерминальные символы
def make_keyword(image):
    return pe.Terminal(image, image, lambda name: None)

NProgram, NFunctionDeclarations, NFunctionDeclaration, NFunctionHeader, NFunctionHeaderTypeName, NFormalParameters, NFormalParameter, NStatements, NStatement = \
    map(pe.NonTerminal, 'Program FunctionDeclarations FunctionDeclaration FunctionHeader FunctionHeaderTypeName FormalParameters FormalParameter Statements Statement'.split())

NType, NArrayType, NBrackets, NDeclarationAssignments, NDeclarationAssignment, NActualParameters, NActualParameter = \
    map(pe.NonTerminal, 'Type ArrayType Brackets DeclarationAssignments DeclarationAssignment ActualParameters ActualParameter'.split())

NExpr, NAndExpr, NCmpExpr, NStringConstant, NConst, NArithmExpr, NCmpOp, NTerm, NMulOp, NAddOp, NFactor, NPower, NArrExpr, NBottomExpr, NFuncCallExpr, NArgs = \
    map(pe.NonTerminal, 'Expr AndExpr CmpExpr StringConstant Const ArithmExpr CmpOp Term MulOp AddOp Factor Power ArrExpr BottomExpr FuncCallExpr Args'.split())

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

NExpr |= NAndExpr
NExpr |= NAndExpr, '|', NAndExpr, lambda x, y: BinOpExpr(x, '|', y)
NExpr |= NAndExpr, '@', NAndExpr, lambda x, y: BinOpExpr(x, '@', y)
NAndExpr |= NCmpExpr
NAndExpr |= NCmpExpr, '&', NCmpExpr, lambda x, y: BinOpExpr(x, '&', y)
NCmpExpr |= NFuncCallExpr
NCmpExpr |= NFuncCallExpr, NCmpOp, NFuncCallExpr, BinOpExpr
def make_op_lambda(op):
    return lambda: op

for op in ('>', '<', '>=', '<=', '==', '!='):
    NCmpOp |= op, make_op_lambda(op)

NFuncCallExpr |= NArithmExpr
NFuncCallExpr |= IDENTIFIER, '<-', NArgs, FunctionInvocationExpr
NArgs |= NArithmExpr, lambda ex: [ex]
NArgs |= NArgs, ',', NArithmExpr, lambda exs, ex: exs + [ex]

# {f} <- {x}+{y}, ({x}<{y})
# {f} <- {a} < {g} <- {b}, {c}
# ({f} <- {a}) + ({g} <- {b}, {c})

NArithmExpr |= NTerm
NArithmExpr |= NArithmExpr, NAddOp, NTerm, BinOpExpr
NAddOp |= '+', lambda: '+'
NAddOp |= '-', lambda: '-'
NTerm |= NFactor
NTerm |= NTerm, NMulOp, NFactor, BinOpExpr
NMulOp |= '*', lambda: '*'
NMulOp |= '/', lambda: '/'
NMulOp |= '%', lambda: '%'
NFactor |= NPower
NFactor |= NPower, '^', NFactor, lambda p, f: BinOpExpr(p, '^', f)
NPower |= NArrExpr
NPower |= '!', NPower, lambda p: UnOpExpr('!', p)
NPower |= '-', NPower, lambda p: UnOpExpr('-', p)
NArrExpr |= NBottomExpr
NArrExpr |= NArrExpr, NBottomExpr, lambda x, y: BinOpExpr(x, 'at', y)
NArrExpr |= NStringConstant, lambda v: ConstExpr(v, Type(PrimType.Char, 1))
NPower |= NType, NBottomExpr, lambda x, y: BinOpExpr(x, 'alloc', y)
NBottomExpr |= IDENTIFIER, VariableExpr
NBottomExpr |= NConst
NBottomExpr |= '(', NExpr, ')'

# {arr} 2 ({ix} 3)  ~~ arr[2][ix[3]]

NStringConstant |= NStringConstant, STRING_SECTION, lambda s1, s2: s1 + s2
NStringConstant |= STRING_SECTION

NConst |= DECIMAL_INTEGER_CONSTANT, lambda v: ConstExpr(v, Type(PrimType.Int, 0))
NConst |= NON_DECIMAL_INTEGER_CONSTANT, lambda v: ConstExpr(v, Type(PrimType.Int, 0))
NConst |= SYMBOLIC_CONSTANT, lambda v: ConstExpr(v, Type(PrimType.Char, 0))
NConst |= BOOLEAN_CONSTANT, lambda v: ConstExpr(v, Type(PrimType.Bool, 0))

NStatements |= NStatements, ';', NStatement, lambda sts, st: sts + [st]
NStatements |= NStatement, lambda st: [st]

#Оператор объявления
NStatement |= NType, NDeclarationAssignments, DeclarationStatement
NDeclarationAssignments |= NDeclarationAssignment, lambda vr: [vr]
NDeclarationAssignments |= NDeclarationAssignments, ',', NDeclarationAssignment, lambda vrs, vr: vrs + [vr]
NDeclarationAssignment |= IDENTIFIER, lambda name: (name, None)
NDeclarationAssignment |= IDENTIFIER, ':=', NArithmExpr, lambda name, ex: (name, ex)

# int {a} := ({f} <- {x}, {y})

# int {a} 5 := 7

#Оператор присваивания
NStatement |= NArrExpr, ':=', NExpr, AssignmentStatement

#Оператор вызова функции
NStatement |= IDENTIFIER, '<-', NArgs, InvocationStatement
NActualParameters |= NActualParameter, lambda ap: [ap]
NActualParameters |= NActualParameters, ',', NActualParameter, lambda aps, ap: aps + [ap]
NActualParameter |= NArithmExpr

#Оператор выбора
NStatement |= NExpr, KW_THEN, NStatements, '.', lambda cond, sts: IfStatement(cond, [sts], [])
NStatement |= NExpr, KW_THEN, NStatements, KW_ELSE, NStatements, '.', IfStatement

#Оператор цикла с предусловием
NStatement |= NExpr, KW_LOOP, NStatements, '.', PreWhileStatement
#Второй вариант
NStatement |= NExpr, '~', NExpr, KW_LOOP, IDENTIFIER, NStatements, '.', ForStatement

#Оператор цикла с постусловием
NStatement |= KW_LOOP, NStatements, KW_WHILE, NExpr, '.', PostWhileStatement

# #Оператор завершения функции
NStatement |= KW_RETURN, NExpr, ReturnStatement
NStatement |= KW_RETURN, lambda: ReturnStatement(None)

#Тип
NType |= KW_INT, lambda: Type(PrimType.Int, 0)
NType |= KW_CHAR, lambda: Type(PrimType.Char, 0)
NType |= KW_BOOL, lambda: Type(PrimType.Bool, 0)
NType |= NArrayType
#Ссылочный тип
NArrayType |= KW_INT, NBrackets, lambda n: Type(PrimType.Int, n)
NArrayType |= KW_CHAR, NBrackets, lambda n: Type(PrimType.Char, n)
NArrayType |= KW_BOOL, NBrackets, lambda n: Type(PrimType.Bool, n)
NBrackets |= NBrackets, '[]', lambda n: n + 1
NBrackets |= '[]', lambda: 1


# Парсер
parser = pe.Parser(NProgram)
# parser.print_table()
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
