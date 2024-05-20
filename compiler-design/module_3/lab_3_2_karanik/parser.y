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


%token <sval> IDENTIFIER DECIMAL_INTEGER_CONSTANT NON_DECIMAL_INTEGER_CONSTANT SYMBOLIC_CONSTANT STRING_SECTION BOOLEAN_CONSTANT
%token KW_BOOL KW_INT KW_RETURN KW_VOID KW_CHAR KW_LOOP KW_THEN KW_ELSE KW_NULL KW_WHILE
%token LEFT_ARROW PLUS MINUS MUL DIV PERCENT POWER NOT ASSIGN LEFT_PAREN RIGHT_PAREN COMMA SEMICOLON DOT BRACKETS
%token OR XOR AND GREATER LESS GREATER_OR_EQUAL LESS_OR_EQUAL EQUAL NOT_EQUAL
%token DECLARE TILDE

%type <sval> function_declarations function_declaration function_header function_header_type_name formal_parameters formal_parameter type brackets
%type <sval> declaration_assignments declaration_assignment
%type <sval> statements statement expr and_expr cmp_expr cmp_op func_call_expr arithm_expr args term factor power arr_expr bottom_expr string_constant constant

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