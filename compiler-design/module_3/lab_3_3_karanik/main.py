import abc
import enum
import parser_edsl as pe
import typing
import sys
from dataclasses import dataclass
from pprint import pprint


class SemanticError(pe.Error):
    pass

class RepeatedVariable(SemanticError):
    def __init__(self, pos, varname):
        self.pos = pos
        self.varname = varname

    @property
    def message(self):
        return f'Повторная переменная {self.varname}'


class RepeatedFunction(SemanticError):
    def __init__(self, pos, functionName):
        self.pos = pos
        self.functionName = functionName

    @property
    def message(self):
        return f'Повторная функция {self.functionName}'

class MainFunctionNotFound(SemanticError):
    def __init__(self, pos):
        self.pos = pos
    @property
    def message(self):
        return f'Функция Main не найдена'

class MainFunctionIncorrect(SemanticError):
    def __init__(self, pos):
        self.pos = pos

    @property
    def message(self):
        return 'Функция Main некорректна'




class UnknownVar(SemanticError):
    def __init__(self, pos, varname):
        self.pos = pos
        self.varname = varname

    @property
    def message(self):
        return f'Необъявленная переменная {self.varname}'


class BinBadType(SemanticError):
    def __init__(self, pos, left, op, right):
        self.pos = pos
        self.left = left
        self.op = op
        self.right = right

    @property
    def message(self):
        return f'Несовместимые типы: {self.left} {self.op} {self.right}'


class UnBadType(SemanticError):
    def __init__(self, pos, op, type_):
        self.pos = pos
        self.op = op
        self.type = type_

    @property
    def message(self):
        return f'Несовместимый тип: {self.op} {self.type}'

class NotBoolCond(SemanticError):
    def __init__(self, pos, type_):
        self.pos = pos
        self.type = type_

    @property
    def message(self):
        return f'Условие имеет тип {self.type} вместо логического'


class NotIntFor(SemanticError):
    def __init__(self, pos, type_):
        self.pos = pos
        self.type = type_

    @property
    def message(self):
        return f'Ожидался целый тип, получен {self.type}'

    @staticmethod
    def check(type_, pos):
        if type_ != Type.Integer:
            raise NotIntFor(pos, type_)


class PrimType(enum.Enum):
    Int = 'INT'
    Char = 'CHAR'
    Bool = 'BOOL'

@dataclass
class Type:
    base: PrimType
    array_level: int


class Expr(abc.ABC):
    @abc.abstractmethod
    def check(self, vars):
        pass

@dataclass
class Statement(abc.ABC):
    @abc.abstractmethod
    def check(self, vars):
        pass

@dataclass
class DeclarationStatement(Statement):
    type: Type
    variables: list[(str, pe.Position, Expr | None)]
    # @pe.ExAction
    # def create(attrs, coords, res_coord):
    #     var, expr = attrs
    #     cvar, cass, cexpr = coords
    #     return DeclarationStatement(var, cass.start, expr)
    def check(self, vars):
        for var, coord, expr in self.variables:
            if var in vars:
                raise RepeatedVariable(pe.Position(0, 1, 1), var[0])
            else:
                if expr is not None:
                    var[1].check(vars)
                    if self.type == var[1].type:
                        vars[var[0]] = var[1]
                    else:
                        raise BinBadType(pe.Position(0, 1, 1), self.type, ':=', var[1].type)
                else:
                    vars[var[0]] = var[1]



@dataclass
class AssignmentStatement(Statement):
    leftExpr: Expr
    var_coord: pe.Position
    rightExpr: Expr

    @pe.ExAction
    def create(attrs, coords, res_coord):
        var, expr = attrs
        cvar, cass, cexpr = coords
        return AssignmentStatement(var, cass.start, expr)

    def check(self, vars):
        if self.variable not in vars:
            raise UnknownVar(self.variable, self.var_coord)

        self.rightExpr.check(vars)
        if vars[self.variable] == self.expr.type:
            return
        if vars[self.variable] == Type.Real and self.expr.type == Type.Integer:
            return

        raise BinBadType(self.var_coord, vars[self.variable], ':=', self.expr.type)


@dataclass
class InvocationStatement(Statement):
    function: str
    actualParameters: list[Expr]

    def check(self, vars):
        pass


@dataclass
class IfStatement(Statement):
    condition: Expr
    then_branch: list[Statement]
    else_branch: list[Statement]
    def check(self, vars):
        pass


@dataclass
class PreWhileStatement(Statement):
    condition: Expr
    body: list[Statement]
    def check(self, vars):
        pass

@dataclass
class ForStatement(Statement):
    start: Expr
    end: Expr
    variable: str
    body: list[Statement]
    def check(self, vars):
        pass


@dataclass
class PostWhileStatement(Statement):
    body: list[Statement]
    condition: Expr
    def check(self, vars):
        pass


@dataclass
class ReturnStatement(Statement):
    expr: Expr | None
    def check(self, vars):
        pass

@dataclass
class VariableExpr(Expr):
    varname: str
    var_coord: pe.Position
    @pe.ExAction
    def create(attrs, coords, res_coord):
        varname, = attrs
        cvarname, = coords
        return VariableExpr(varname, cvarname)

    def check(self, vars):
        try:
            self.type = vars[self.varname]
        except KeyError:
            raise UnknownVar(self.var_coord, self.varname)

@dataclass
class ConstExpr(Expr):
    value: typing.Any
    type: Type
    def check(self, vars):
        pass

@dataclass
class FunctionInvocationExpr(Expr):
    function: str
    actualParameters: list[Expr]

@dataclass
class BinOpExpr(Expr):
    left: Expr
    op: str
    op_coord: pe.Position
    right: Expr
    @pe.ExAction
    def create(attrs, coords, res_coord):
        left, op, right = attrs
        cleft, cop, cright = coords
        return BinOpExpr(left, op, cop.start, right)
    def check(self, vars):
        self.left.check(vars)
        self.right.check(vars)

        common_type = None
        is_numeric = lambda t: (t == Type(PrimType.Int, 0))

        if self.left.type == self.right.type:
            common_type = self.left.type

        self.type = None
        if self.op in ('<', '>', '<=', '>=', '=', '<>'):
            if common_type != None:
                self.type = Type(PrimType.Bool, 0)
        elif self.op in ('$', '|'):
            if common_type == Type(PrimType.Bool, 0):
                self.type = Type(PrimType.Bool, 0)
        elif self.op in ('/', '%'):
            if common_type == Type(PrimType.Int, 0):
                self.type = Type(PrimType.Int, 0)
        elif self.op in ('+', '-', '*', '**'):
            if is_numeric(common_type):
                self.type = common_type

        if self.type == None:
            raise BinBadType(self.op_coord, self.left.type,
                             self.op, self.right.type)


@dataclass
class UnOpExpr(Expr):
    op: str
    op_coord: pe.Position
    expr: Expr
    @staticmethod
    def create(op):
        @pe.ExAction
        def action(attrs, coords, res_coords):
            expr, = attrs
            cop, cexpr = coords
            return UnOpExpr(op, cop.start, expr)
        return action
    def check(self, vars):
        self.expr.check(vars)
        if self.op == '-' and self.expr.type != Type(PrimType.Int, 0):
            raise UnBadType(self.op_coord, self.op, self.expr.type)
        if self.op == '!' and self.expr.type != Type(PrimType.Bool, 0):
            raise UnBadType(self.op_coord, self.op, self.expr.type)
        self.type = self.expr.type

@dataclass
class FunctionHeader:
    type: Type
    type_coord: pe.Position
    name: str
    name_coord: pe.Position
    formalParameters: list[(Type, str)]

    @pe.ExAction
    def create(attrs, coords, res_coord):
        typename, params = attrs
        ctypename, carrow, cparams = coords
        return FunctionHeader(typename[0], ctypename.start, typename[1], ctypename.start, params)

@dataclass
class FunctionDeclaration:
    header: FunctionHeader
    body: list[Statement]

    def check(self):
        vars = {}
        for p in self.header.formalParameters:
            vars[p[1]] = p[0]

        for statement in self.body:
            statement.check(vars)

        print(vars)

@dataclass
class Program:
    functionDeclarations: list[FunctionDeclaration]
    def check(self):
        functionDeclarationNames = {}
        mainFunction = None

        for functionDeclaration in self.functionDeclarations:
            if functionDeclaration.header.name == '{Main}':
                mainFunction = functionDeclaration
            if functionDeclaration.header.name in functionDeclarationNames:
                raise RepeatedFunction(functionDeclaration.header.name_coord, functionDeclaration.header.name)
            else:
                functionDeclarationNames[functionDeclaration.header.name] = functionDeclaration.header.type

        if mainFunction:
            if mainFunction.header.type.base != Type(PrimType.Int, 0):
                raise MainFunctionIncorrect(mainFunction.header.type_coord)
            else:
                if len(mainFunction.header.formalParameters) == 1:
                    if mainFunction.header.formalParameters[0][0].base != PrimType.Char or mainFunction.header.formalParameters[0][0].array_level != 2:
                        raise MainFunctionIncorrect(mainFunction.header.type_coord)
                else:
                    raise MainFunctionIncorrect(mainFunction.header.type_coord)
        else:
            raise MainFunctionNotFound(pe.Position(0, 1, 1))

        for functionDeclaration in self.functionDeclarations:
            functionDeclaration.check()


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
NProgram |= NFunctionDeclarations, Program
NFunctionDeclarations |= NFunctionDeclaration, lambda fd: [fd]
NFunctionDeclarations |= NFunctionDeclarations, NFunctionDeclaration, lambda fds, fd: fds + [fd]
NFunctionDeclaration |= NFunctionHeader, '=', NStatements, '.', FunctionDeclaration
NFunctionHeader |= NFunctionHeaderTypeName, lambda typename: FunctionHeader(typename[0], typename[1], [])
NFunctionHeader |= NFunctionHeaderTypeName, '<-', NFormalParameters, FunctionHeader.create
NFunctionHeaderTypeName |= NType, IDENTIFIER, lambda t, n: (t, n)
NFunctionHeaderTypeName |= KW_VOID, IDENTIFIER, lambda n: (None, n)
NFormalParameters |= NFormalParameters, ',', NFormalParameter, lambda fps, fp: fps + [fp]
NFormalParameters |= NFormalParameter, lambda fp: [fp]
NFormalParameter |= NType, IDENTIFIER, lambda t, n: (t, n)

NExpr |= NAndExpr
NExpr |= NAndExpr, '|', NAndExpr, BinOpExpr.create  # lambda x, y: BinOpExpr(x, '|', y)
NExpr |= NAndExpr, '@', NAndExpr, BinOpExpr.create  # lambda x, y: BinOpExpr(x, '@', y)
NAndExpr |= NCmpExpr
NAndExpr |= NCmpExpr, '&', NCmpExpr, BinOpExpr.create   # lambda x, y: BinOpExpr(x, '&', y)
NCmpExpr |= NFuncCallExpr
NCmpExpr |= NFuncCallExpr, NCmpOp, NFuncCallExpr, BinOpExpr.create  # BinOpExpr
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
NArithmExpr |= NArithmExpr, NAddOp, NTerm, BinOpExpr.create  # BinOpExpr
NAddOp |= '+', lambda: '+'
NAddOp |= '-', lambda: '-'
NTerm |= NFactor
NTerm |= NTerm, NMulOp, NFactor, BinOpExpr.create  # BinOpExpr
NMulOp |= '*', lambda: '*'
NMulOp |= '/', lambda: '/'
NMulOp |= '%', lambda: '%'
NFactor |= NPower
NFactor |= NPower, '^', NFactor, BinOpExpr.create  # lambda p, f: BinOpExpr(p, '^', f)
NPower |= NArrExpr
NPower |= '!', NPower, UnOpExpr.create('!')  # lambda p: UnOpExpr('!', p)
NPower |= '-', NPower, UnOpExpr.create('-')  # lambda p: UnOpExpr('-', p)
NArrExpr |= NBottomExpr
NArrExpr |= NArrExpr, NBottomExpr, BinOpExpr.create  # lambda x, y: BinOpExpr(x, 'at', y)
NArrExpr |= NStringConstant, lambda v: ConstExpr(v, Type(PrimType.Char, 1))
NPower |= NType, NBottomExpr,  BinOpExpr.create  # lambda x, y: BinOpExpr(x, 'alloc', y)
NBottomExpr |= IDENTIFIER, VariableExpr.create  # VariableExpr
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
NDeclarationAssignment |= IDENTIFIER, pe.ExAction(lambda attrs, coords, res_coord: (attrs[0], coords[0], None))
NDeclarationAssignment |= IDENTIFIER, ':=', NArithmExpr, lambda name, ex: (name, ex)

# int {a} := ({f} <- {x}, {y})

# int {a} 5 := 7

#Оператор присваивания
NStatement |= NArrExpr, ':=', NExpr, AssignmentStatement.create  # AssignmentStatement

#Оператор вызова функции
NStatement |= IDENTIFIER, '<-', NArgs, InvocationStatement
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
            tree.check()
            pprint(tree)
    except pe.Error as e:
        print(f'Ошибка {e.pos}: {e.message}')
    except Exception as e:
        print('Исключение', e)
