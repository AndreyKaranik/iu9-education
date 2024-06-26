% "Лабораторная работа 3.2 «Форматтер исходных текстов»"
% 20 мая 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является приобретение навыков использования генератора синтаксических анализаторов bison.

# Индивидуальный вариант
Язык L2.

# Реализация

lexer.h
```
#ifndef LEXER_H
#define LEXER_H

#include <stdbool.h>
#include <stdio.h>

#ifndef YY_TYPEDEF_YY_SCANNER_T
#define YY_TYPEDEF_YY_SCANNER_T
typedef void *yyscan_t;
#endif /* YY_TYPEDEF_YY_SCANNER_T */

struct Extra {
    bool continued;
    int cur_line;
    int cur_column;
};

void init_scanner(FILE *input, yyscan_t *scanner, struct Extra *extra);
void destroy_scanner(yyscan_t);
char *concat(const char *s1, const char *s2);


#endif /* LEXER_H */
```

lexer.l
```
%option reentrant noyywrap bison-bridge bison-locations
%option extra-type="struct Extra *"

/* Подавление предупреждений для -Wall */
%option noinput nounput

%{

#include <stdio.h>
#include <stdlib.h>
#include "lexer.h"
#include "y.tab.h"  /* файл генерируется Bison’ом */

//#define ESCAPE_SEQUENCE_REGEX "%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%"

#define YY_USER_ACTION \
  { \
    int i; \
    struct Extra *extra = yyextra; \
    if (! extra->continued ) { \
      yylloc->first_line = extra->cur_line; \
      yylloc->first_column = extra->cur_column; \
    } \
    extra->continued = false; \
    for (i = 0; i < yyleng; ++i) { \
      if (yytext[i] == '\n') { \
        extra->cur_line += 1; \
        extra->cur_column = 1; \
      } else { \
        extra->cur_column += 1; \
      } \
    } \
    yylloc->last_line = extra->cur_line; \
    yylloc->last_column = extra->cur_column; \
  }

void yyerror(YYLTYPE *loc, yyscan_t scanner, int indent, const char *message) {
    printf("Error (%d,%d): %s\n", loc->first_line, loc->first_column, message);
}

char *copystr(const char *str) {
  return concat("", str);
}

%}

%%

([\r\t\n ]+)|(((##.*$)|(#.*#))+)

\{([A-Za-z_]|[ ])+\} { 
  yylval->sval = copystr(yytext);
  return IDENTIFIER;
}

[0-9]+ { 
  yylval->sval = copystr(yytext);
  return DECIMAL_INTEGER_CONSTANT;
}

([A-Z0-9])+([$]([2-9]|[12][0-9]|3[0-6]))? { 
  yylval->sval = copystr(yytext);
  return NON_DECIMAL_INTEGER_CONSTANT;
}

[$](\"(.|(%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%))\")|[A-F0-9]+ { 
  yylval->sval = copystr(yytext);
  return SYMBOLIC_CONSTANT;
}

(\"([^"\n]|(%BEL%|%BS%|%TAB%|%LF%|%VT%|%FF%|%CR%|%\"%|%%))*\")|(%[A-F0-9]+) { 
  yylval->sval = copystr(yytext);
  return STRING_SECTION;
}

true|false {
  yylval->sval = copystr(yytext);
  return BOOLEAN_CONSTANT;
}

bool return KW_BOOL;
int return KW_INT;
return return KW_RETURN;
void return KW_VOID;
char return KW_CHAR;
loop return KW_LOOP;
then return KW_THEN;
else return KW_ELSE;
null return KW_NULL;
while return KW_WHILE;


\:\= return DECLARE;
\~ return TILDE;

\<\- return LEFT_ARROW;
\| return OR;
\@ return XOR;
\& return AND;
\> return GREATER;
\< return LESS;
\>\= return GREATER_OR_EQUAL;
\<\= return LESS_OR_EQUAL;
\=\= return EQUAL;
\!\= return NOT_EQUAL;
\=   return ASSIGN;
\+  return PLUS;
\-   return MINUS;
\*  return MUL;
\/  return DIV;
\% return PERCENT;
\^ return POWER;
\! return NOT;
\(  return LEFT_PAREN;
\)  return RIGHT_PAREN;
\,   return COMMA;
\;   return SEMICOLON;
\.  return DOT;
\[\] return BRACKETS;

%%

void init_scanner(FILE *input, yyscan_t *scanner, struct Extra *extra) {
    extra->continued = false;
    extra->cur_line = 1;
    extra->cur_column = 1;

    yylex_init(scanner);
    yylex_init_extra(extra, scanner);
    yyset_in(input, *scanner);
}

void destroy_scanner(yyscan_t scanner) {
    yylex_destroy(scanner);
}
```

parser.y
```
%{
#include <stdio.h>
#include "lexer.h"
#include <stdlib.h>
#include <string.h>

struct Chunk {
    struct Chunk *next;
    char data[];
};

struct Chunk *chunks = NULL;

char *stralloc(size_t len) {
    struct Chunk *chunk = malloc(sizeof(struct Chunk) + len);
    chunk->next = chunks;
    chunks = chunk;
    return chunk->data;
}

char *concat(const char *s1, const char *s2) {
    size_t len1 = strlen(s1);
    size_t len2 = strlen(s2);
    char *result = stralloc(len1 + len2 + 1);
    strcpy(result, s1);
    strcat(result, s2);
    return result;
}

char *make_indent(int level) {
    char *spaces = stralloc(2 * level + 1);
    for (int i = 0; i < level; ++i) {
        spaces[2*i] = ' ';
        spaces[2*i + 1] = ' ';
    }
    spaces[2 * level + 2] = '\0';
    return spaces;
}

%}

%define api.pure
%locations
%lex-param {yyscan_t scanner}  /* параметр для yylex() */
/* параметры для yyparse() */
%parse-param {yyscan_t scanner}
%parse-param {int indent}

%union {
    char *sval;
}


%token <sval> IDENTIFIER DECIMAL_INTEGER_CONSTANT NON_DECIMAL_INTEGER_CONSTANT SYMBOLIC_CONSTANT
%token <sval> STRING_SECTION BOOLEAN_CONSTANT
%token KW_BOOL KW_INT KW_RETURN KW_VOID KW_CHAR KW_LOOP KW_THEN KW_ELSE KW_NULL KW_WHILE
%token LEFT_ARROW PLUS MINUS MUL DIV PERCENT POWER NOT ASSIGN LEFT_PAREN RIGHT_PAREN COMMA SEMICOLON DOT
%token BRACKETS
%token OR XOR AND GREATER LESS GREATER_OR_EQUAL LESS_OR_EQUAL EQUAL NOT_EQUAL
%token DECLARE TILDE

%type <sval> function_declarations function_declaration function_header function_header_type_name
%type <sval> formal_parameters formal_parameter type brackets
%type <sval> declaration_assignments declaration_assignment
%type <sval> statements statement expr and_expr cmp_expr cmp_op func_call_expr arithm_expr args term
%type <sval> factor power arr_expr bottom_expr string_constant constant

%{
int yylex(YYSTYPE *yylval_param, YYLTYPE *yylloc_param, yyscan_t scanner);
void yyerror(YYLTYPE *loc, yyscan_t scanner, int indent, const char *message);
%}

%%

program:
        function_declarations
        {
            printf("%s\n", $1);
        }
    ;

function_declarations:
        function_declaration function_declarations
        {
            $$ = concat($1, "\n\n");    
            $$ = concat($$, $2);
        }
    |   function_declaration
        {
            $$ = $1;
        }
    ;

function_declaration:
        function_header ASSIGN statements DOT
        {
            $$ = concat($1, " =\n");
            $$ = concat($$, $3);
            $$ = concat($$, ".");
        }
    ;

function_header:
        function_header_type_name
        {
            $$ = $1;
        }
    |   function_header_type_name LEFT_ARROW formal_parameters
        {
            $$ = concat($1, " <- ");
            $$ = concat($$, $3);
        }
    ;

function_header_type_name:
        type IDENTIFIER
        {
            $$ = concat($1, " ");
            $$ = concat($$, $2);
        }
    |
        KW_VOID IDENTIFIER
        {
            $$ = concat("void ", $2);
        }
    ;

formal_parameters:
        formal_parameter
        {
            $$ = $1;
        }
    |
        formal_parameter COMMA formal_parameters
        {
            $$ = concat($1, ", ");
            $$ = concat($$, $3);
        }
    ;

formal_parameter:
        type IDENTIFIER
        {
            $$ = concat($1, " ");
            $$ = concat($$, $2);
        }
    ;

type:
        KW_INT
        {
            $$ = "int";
        }
    |   KW_INT brackets
        {
            $$ = concat("int", $2);
        }
    |   KW_CHAR
        {
            $$ = "char";
        }
    |   KW_CHAR brackets
        {
            $$ = concat("char", $2);
        }
    |   KW_BOOL
        {
            $$ = "bool";
        }
    |   KW_BOOL brackets
        {
            $$ = concat("bool", $2);
        }

brackets:
        BRACKETS
        {
            $$ = "[]";
        }
    |
        BRACKETS brackets
        {
            $$ = concat("[]", $2);
        }


statements:
        statement
        {
            $$ = concat(make_indent(indent), $1);
        }
    |
        statement SEMICOLON statements
        {
            $$ = concat(make_indent(indent), $1);
            $$ = concat($$, ";\n");
            $$ = concat($$, $3);
            // $$ = concat($1, ";\n");
            // $$ = concat($$, $3);
        }
    ;

inc: { ++indent; }
dec: { --indent; }

statement:
        type declaration_assignments
        {
            $$ = concat($1, " ");
            $$ = concat($$, $2);
        }
    |   arr_expr DECLARE expr
        {
            $$ = concat($1, " := ");
            $$ = concat($$, $3);
        }
    |   expr KW_THEN inc statements dec DOT
        {
            $$ = concat($1, " then\n");
            $$ = concat($$, $4);
            $$ = concat($$, ".");
        }
    |   expr KW_THEN inc statements dec KW_ELSE inc statements dec DOT
        {
            $$ = concat($1, " then\n");
            $$ = concat($$, $4);
            $$ = concat($$, "\nelse\n");
            $$ = concat($$, $8);
            $$ = concat($$, ".");
        }
    |   expr KW_LOOP statements DOT
        {
            $$ = concat($1, " then\n");
            $$ = concat($$, $3);
            $$ = concat($$, ".");
        }
    |   expr TILDE expr KW_LOOP IDENTIFIER inc statements dec DOT
        {
            $$ = concat($1, " ~ ");
            $$ = concat($$, $3);
            $$ = concat($$, " loop ");
            $$ = concat($$, $5);
            $$ = concat($$, "\n");
            $$ = concat($$, $7);
            $$ = concat($$, ".");
        }
    |   KW_LOOP inc statements dec KW_WHILE expr DOT
        {
            $$ = concat("loop\n", $3);
            $$ = concat($$, "\nwhile ");
            $$ = concat($$, $6);
            $$ = concat($$, ".");
        }
    |   KW_RETURN
        {
            $$ = "return";
        }
    |   KW_RETURN expr
        {
            $$ = concat("return ", $2);
        }
    ;

declaration_assignments:
        declaration_assignment
        {
            $$ = $1;
        }
    |   declaration_assignments COMMA declaration_assignment
        {
            $$ = concat($1, ", ");
            $$ = concat($$, $3);
        }
    ;

declaration_assignment:
        IDENTIFIER
        {
            $$ = $1;
        }
    |   IDENTIFIER DECLARE arithm_expr
        {
            $$ = concat($1, " := ");
            $$ = concat($$, $3);
        }
    ;

expr:
        and_expr
        {
            $$ = $1;
        }
    |   and_expr OR and_expr
        {
            $$ = concat($1, " | ");
            $$ = concat($$, $3);
        }
    |   and_expr XOR and_expr
    ;

and_expr:
        cmp_expr
        {
            $$ = $1;
        }
    |   cmp_expr AND cmp_expr
        {
            $$ = concat($1, " & ");
            $$ = concat($$, $3);
        }
    ;

cmp_expr:
        func_call_expr
        {
            $$ = $1;
        }
    |   func_call_expr cmp_op func_call_expr
        {
            $$ = concat($1, " ");
            $$ = concat($$, $2);
            $$ = concat($$, " ");
            $$ = concat($$, $3);
        }
    ;

cmp_op:
        GREATER
        {
            $$ = ">";
        }
    |   LESS
        {
            $$ = "<";
        }
    |   GREATER_OR_EQUAL
        {
            $$ = ">=";
        }
    |   LESS_OR_EQUAL
        {
            $$ = "<=";
        }
    |   EQUAL
        {
            $$ = "==";
        }
    |   NOT_EQUAL
        {
            $$ = "!=";
        }
    ;

func_call_expr:
        arithm_expr
        {
            $$ = $1;
        }
    |   IDENTIFIER LEFT_ARROW args
        {
            $$ = concat($1, " <- ");
            $$ = concat($$, $3);
        }
    ;

args:
        arithm_expr
        {
            $$ = $1;
        }
    |   args COMMA arithm_expr
        {
            $$ = concat($1, ", ");
            $$ = concat($$, $3);
        }
    ;

arithm_expr:
        term
        {
            $$ = $1;
        }
    |   arithm_expr PLUS term
        {
            $$ = concat($1, " + ");
            $$ = concat($$, $3);
        }
    |   arithm_expr MINUS term
        {
            $$ = concat($1, " - ");
            $$ = concat($$, $3);
        }
    ;

term:
        factor
        {
            $$ = $1;
        }
    |   term MUL factor
        {
            $$ = concat($1, " * ");
            $$ = concat($$, $3);
        }
    |   term DIV factor
        {
            $$ = concat($1, " / ");
            $$ = concat($$, $3);
        }
    |   term PERCENT factor
        {
            $$ = concat($1, " %% ");
            $$ = concat($$, $3);
        }
    ;

factor:
        power
        {
            $$ = $1;
        }
    |   power POWER factor
        {
            $$ = concat($1, " ^ ");
            $$ = concat($$, $3);
        }
    ;

power:
        arr_expr
        {
            $$ = $1;
        }
    |   NOT power
        {
            $$ = concat("!", $2);
        }
    |   MINUS power
        {
            $$ = concat("-", $2);
        }
    |   type bottom_expr
        {
            $$ = concat($1, " ");
            $$ = concat($$, $2);
        }
    ;

arr_expr:
        bottom_expr
        {
            $$ = $1;
        }
    |   arr_expr bottom_expr
        {
            $$ = concat($1, $2);
        }
    |   string_constant
        {
            $$ = $1;
        }
    ;

bottom_expr:
        IDENTIFIER
        {
            $$ = $1;
        }
    |   constant
        {
            $$ = $1;
        }
    |   LEFT_PAREN expr RIGHT_PAREN
        {
            $$ = concat("(", $2);
            $$ = concat($$, ")");
        }
    ;

string_constant:
        string_constant STRING_SECTION
        {
            $$ = concat($1, $2);
        }
    |   STRING_SECTION
        {
            $$ = $1;
        }
    ;

constant:
        DECIMAL_INTEGER_CONSTANT
        {
            $$ = $1;
        }
    |   NON_DECIMAL_INTEGER_CONSTANT
        {
            $$ = $1;
        }
    |   SYMBOLIC_CONSTANT
        {
            $$ = $1;
        }
    |   BOOLEAN_CONSTANT
        {
            $$ = $1;
        }
    |   KW_NULL
        {
            $$ = "null";
        }
    ;

%%

int main(int argc, char *argv[]) {
    FILE *input = 0;
    yyscan_t scanner;
    struct Extra extra;

    if (argc > 1) {
        printf("Read file %s\n", argv[1]);
        input = fopen(argv[1], "r");
    } else {
        printf("No file in command line, use stdin\n");
        input = stdin;
    }

    init_scanner(input, &scanner, &extra);
    yyparse(scanner, 0);
    destroy_scanner(scanner);

    if (input != stdin) {
        fclose(input);
    }

    return 0;
}
```

# Тестирование

Входные данные

```
int {foo} <- int{a},bool[] {b} = return -({a})*2+ 3.



void {test} =
int {a} := ({foo} <- 10, null);
bool {b} := true;
{b} := false;
{a} > 0 then
    {b} := false.;

int {i};

0 ~ 10 loop {i}
    {i} == 5 then
        {b} == true then
            {b} := false..;
    {a} := {a} + {i}.;

return.
```

Вывод на `stdout`

```
Read file input.txt
int {foo} <- int {a}, bool[] {b} =
return -({a}) * 2 + 3.

void {test} =
int {a} := ({foo} <- 10, null);
bool {b} := true;
{b} := false;
{a} > 0 then
  {b} := false.;
int {i};
0 ~ 10 loop {i}
  {i} == 5 then
    {b} == true then
      {b} := false..;
  {a} := {a} + {i}.;
return.
```

# Вывод
В ходе выполнения лабораторной работы приобрел навыки использования генератора синтаксических
анализаторов bison.