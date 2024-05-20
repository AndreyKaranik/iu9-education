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