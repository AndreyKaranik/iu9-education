% Лабораторная работа № 2.1. Синтаксические деревья
% 11 марта 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является изучение представления синтаксических деревьев в памяти компилятора и
приобретение навыков преобразования синтаксических деревьев.

# Индивидуальный вариант
Любая неанонимная функция, возвращающая ровно одно значение типа int, должна выводить своё имя и
возвращаемое значение.

# Реализация

Демонстрационная программа:

```go
package main

func test(a int) int {
	a = a + a
	return a + a
}

```

Программа, осуществляющая преобразование синтаксического дерева:

```go
package main

import (
	"fmt"
	"go/ast"
	"go/format"
	"go/parser"
	"go/token"
	"log"
	"os"
)

func find_addr(file *ast.File) {
	var funName string
	var funParam string
	ast.Inspect(file, func(node ast.Node) bool {

		if FuncDecl, ok := node.(*ast.FuncDecl); ok {
			if FuncDecl.Type.Results != nil {
				if len(FuncDecl.Type.Results.List) == 1 {
					if a, ok := FuncDecl.Type.Results.List[0].Type.(*ast.Ident); ok {
						if a.Name == "int" {
							if FuncDecl.Type.Results.List[0].Names == nil {
								FuncDecl.Type.Results.List[0].Names = append(FuncDecl.Type.Results.List[0].Names, &ast.Ident{
									Name: "r",
								})
							}
							funParam = FuncDecl.Type.Results.List[0].Names[0].Name
							funName = FuncDecl.Name.Name
							FuncDecl.Body.List = append(
								[]ast.Stmt{
									&ast.DeferStmt{
										Call: &ast.CallExpr{
											Fun: &ast.SelectorExpr{
												X:   ast.NewIdent("fmt"),
												Sel: ast.NewIdent("Println"),
											},
											Args: []ast.Expr{
												&ast.BasicLit{
													Kind:  token.INT,
													Value: "\"" + funName + "\"",
												},
												&ast.BasicLit{
													Kind:  token.INT,
													Value: funParam,
												},
											},
										},
									},
								},
								FuncDecl.Body.List...,
							)
						}
					}
				}
			}
		}
		return true
	})
}

func main() {
	if len(os.Args) != 2 {
		fmt.Printf("usage: astprint <filename.go>\n")
		return
	}

	// Создаём хранилище данных об исходных файлах
	fset := token.NewFileSet()

	// Вызываем парсер
	if file, err := parser.ParseFile(
		fset,                 // данные об исходниках
		os.Args[1],           // имя файла с исходником программы
		nil,                  // пусть парсер сам загрузит исходник
		parser.ParseComments, // приказываем сохранять комментарии
	); err != nil {
		log.Fatalln(err)
	} else {
		find_addr(file)
		if format.Node(os.Stdout, fset, file) != nil {
			fmt.Printf("Formatter error: %v\n", err)
		}
		ast.Fprint(os.Stdout, fset, file, nil)
	}
}

```

# Тестирование

Результат трансформации демонстрационной программы:

```go
package main

func test(a int) (r int) {
        defer fmt.Println("test", r)
        a = a + a
        return a + a
}

```

# Вывод
В ходе выполнения лабораторной работы я ознакомился с представлением синтаксических деревьев в памяти
компилятора и приобрел навыки их преобразования. Эта лабораторная работа позволила мне углубить знания
структуре синтаксических деревьев и научиться эффективно преобразовывать их для внесения изменений в
исходный код программы.