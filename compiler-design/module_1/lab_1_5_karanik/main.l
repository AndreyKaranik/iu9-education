%option noyywrap bison-bridge bison-locations

%{

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <algorithm>
#include <map>

#define TAG_KEYWORD   1
#define TAG_IDENTIFIER      2
#define TAG_ERROR       3

char *tag_names[] = {
    "END_OF_PROGRAM", "KEYWORD", "IDENTIFIER", "ERROR"
};


struct Position {
    int line, pos, index;
};

void print_pos(struct Position *p) {
    printf("(%d,%d)", p->line, p->pos);
}

struct Fragment {
    struct Position starting, following;
};

typedef struct Fragment YYLTYPE;

void print_frag(struct Fragment *f) {
    print_pos(&(f->starting));
    printf("-");
    print_pos(&(f->following));
}

union Token {
    char *tokenName;
    int code;
};

typedef union Token YYSTYPE;

int continued;
struct Position cur;

#define YY_USER_ACTION                       \
{                                            \
    int i;                                   \
    if (!continued)                          \
        yylloc->starting = cur;              \
    continued = 0;                           \
                                             \
    for (i = 0; i < yyleng; i++) {           \
        if (yytext [i] == '\n') {            \
            cur.line++;                      \
            cur.pos = 1;                     \
        } else {                             \
            cur.pos++;                       \
        }                                    \
        cur.index++;                         \
    }                                        \
    yylloc->following = cur;                 \
}

typedef struct {
    YYLTYPE* array;
    size_t used;
    size_t size;
} Array;

void initArray(Array* a, int initial_size) {
    a->array = new YYLTYPE[initial_size];
    a->used = 0;
    a->size = initial_size;
}

void insertArray(Array* a, YYLTYPE element) {
    if (a->used == a->size) {
        a->size *= 2;
        a->array = new YYLTYPE[a->size];
    }
    a->array[a->used++] = element;
}

void freeArray(Array* a) {
    free(a->array);
    a->array = NULL;
    a->used = a->size = 0;
}

void printArray(Array* a) {
    printf("Comments:");
    for(int i = 0; i < a->used; i++) {
        print_frag(&a->array[i]);
    } 
}

Array comments;

void init_scanner(const char *path) {
    continued = 0;
    cur.line = 1;
    cur.pos = 1;
    cur.index = 0;
    initArray(&comments, 2);
    yyin = fopen(path, "r");
}

void err(const char *msg) {
    printf("Error");
    print_pos(&cur);
    printf(": %s\n", msg);
}

std::vector<std::string> names;
std::map<std::string, int> nameCodes;


int addName(std::string name) {
    if (std::find(names.begin(), names.end(), name) != names.end()) {
        return nameCodes[name];
    } else {
        int code = names.size();
        names.push_back(name);
        nameCodes.insert(std::pair<std::string, int>(name, code));
        return code;
    }
}

%}

IDENTIFIER  [\*\*\*][\*]*|[A-Za-z]([A-Za-z][A-Za-z])*
COMMENT     ^[\*][^\*]([^\n])*
KEYWORD     with|end|\*\*

%x SKIP

%%

[\n\t ]+

{COMMENT}   {
                insertArray(&comments, *yylloc);
            }

{KEYWORD} {
                yylval->tokenName = yytext;
                return TAG_KEYWORD;
            }

{IDENTIFIER}    {
                yylval->code = addName(yytext);
                return TAG_IDENTIFIER;
            }

. {
    err("unexpected symbol");
    BEGIN(SKIP);
}

<SKIP>[^\n\t ]+

<SKIP>[\n\t ]+ BEGIN(0);

<<EOF>>     return 0;

%%

int main(int argc, const char **argv) {
    int tag;
    YYSTYPE value;
    YYLTYPE coords;
    init_scanner("text.txt");

    do {
        tag = yylex(&value, &coords);
        if (0 != tag && TAG_ERROR != tag) {
		    printf("%s ", tag_names[tag]);
            print_frag(&coords);
            if (tag == TAG_IDENTIFIER) {
                printf(": %d\n", value.code);
            } else {
                printf(": %s\n", value.tokenName);
            }
        }
    } while (0 != tag);

    printArray(&comments);


    return 0;
}
