Определения функций:
```
NProgram → NFunctionDeclarations
NFunctionDeclarations → NFunctionDeclaration
                      | NFunctionDeclarations NFunctionDeclaration
NFunctionDeclaration → NFunctionHeader '=' NStatements '.'
NFunctionHeader → NFunctionHeaderTypeName
                | NFunctionHeaderTypeName '<-' NFormalParameters
NFunctionHeaderTypeName → NType IDENTIFIER
                        | KW_VOID IDENTIFIER
NFormalParameters → NFormalParameters ',' NFormalParameter
                  |NFormalParameter
NFormalParameter → NType IDENTIFIER
```

Выражения:
```
NExpr → NAndExpr
      | NExpr '|' NAndExpr
      | NExpr '@' NAndExpr
NAndExpr → NCmpExpr
         | NAndExpr '&' NCmpExpr
         | NFuncCallExpr
         | NCmpExpr NCmpOp NFuncCallExpr
NFuncCallExpr → NArithmExpr
              | IDENTIFIER '<-' NArgs
NArgs → NArithmExpr
      | NArgs ',' NArithmExpr
NArithmExpr → NTerm
            | NArithmExpr NAddOp NTerm
NAddOp → '+'
       | '-'
NTerm → NFactor
      | NTerm NMulOp NFactor
NMulOp → '*'
       | '/'
       | '%'
NFactor → NPower
        | NPower '^' NFactor
NPower → NArrExpr
       | '!' NPower
       | '-' NPower
NArrExpr → NBottomExpr
         | NArrExpr NBottomExpr
         | NStringConstant
NPower → NType NBottomExpr
NBottomExpr → IDENTIFIER VariableExpr
            | NConst
            | '(' NExpr ')'
```

Строковая константа:
```
NStringConstant → NStringConstant STRING_SECTION
                | STRING_SECTION
```

Остальные константы:
```
NConst → DECIMAL_INTEGER_CONSTANT
       | NON_DECIMAL_INTEGER_CONSTANT
       | SYMBOLIC_CONSTANT
       | BOOLEAN_CONSTANT
       | KW_NULL
```

Операторы:
```
NStatements → NStatements ';' NStatement
            | NStatement

NStatement → NType NDeclarationAssignments
           | NArrExpr ':=' NExpr
           | IDENTIFIER '<-' NArgs
           | NExpr KW_THEN NStatements '.'
           | NExpr KW_THEN NStatements KW_ELSE NStatements '.'
           | NExpr KW_LOOP NStatements '.'
           | NExpr '~' NExpr KW_LOOP IDENTIFIER NStatements '.'
           | KW_LOOP NStatements KW_WHILE NExpr '.'
           | KW_RETURN NExpr
           | KW_RETURN

NDeclarationAssignments → NDeclarationAssignment
                        | NDeclarationAssignments ',' NDeclarationAssignment
NDeclarationAssignment → IDENTIFIER
                        | IDENTIFIER ':=' NArithmExpr
```

Типы:
```
NType → KW_INT
      | KW_CHAR
      | KW_BOOL
      | NArrayType
NArrayType → KW_INT NBrackets
           | KW_CHAR NBrackets
           | KW_BOOL NBrackets
NBrackets → NBrackets '[]'
          | '[]'
```