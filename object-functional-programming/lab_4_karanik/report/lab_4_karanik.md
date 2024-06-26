% Лабораторная работа № 4 «Case-классы и сопоставление с образцом в Scala»
% 22 апреля 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является приобретение навыков разработки case-классов на языке Scala для представления
абстрактных синтаксических деревьев.

# Индивидуальный вариант
Абстрактный синтаксис термов лямбда-исчисления:
```
Term → VARNAME | Term Term | λ VARNAME.Term
```
Соответственно, это имя переменной, аппликация и абстракция.

Требуется написать функцию optimize : Term => Term, выполняющую простые оптимизации в лямбда-терме:
β-редукцию, когда правый операнд аппликации — имя переменной, и η-редукцию — η-преобразование, устраняющее
абстракцию. Выполнять β-редукцию можно только если преобразование не приводит к конфликту имён.

# Реализация

```scala
sealed trait Term
case class VARNAME(name: String) extends Term
case class Application(func: Term, arg: Term) extends Term
case class Abstraction(param: VARNAME, body: Term) extends Term

object Main {
  def optimize(term: Term): Term = term match {
    case Application(Abstraction(param, body), VARNAME(name)) => substitute(body, param, VARNAME(name))
    case Abstraction(param, Application(a, b)) if !freeVariables(a).contains(param) => a
    case Application(f, a) => Application(optimize(f), optimize(a))
    case Abstraction(param, body) => Abstraction(param, optimize(body))
    case _ => term
  }

  def substitute(term: Term, param: VARNAME, value: Term): Term = term match {
    case VARNAME(name) if name == param.name => value
    case Application(func, arg) => Application(substitute(func, param, value), substitute(arg, param, value))
    case Abstraction(p, body) if p != param => Abstraction(p, substitute(body, param, value))
    case _ => term
  }


  def freeVariables(term: Term): Set[VARNAME] = term match {
    case VARNAME(name) => Set(VARNAME(name))
    case Application(func, arg) => freeVariables(func) ++ freeVariables(arg)
    case Abstraction(param, body) => freeVariables(body) - param
  }

  def main(args: Array[String]): Unit = {
    val term1 = Abstraction(VARNAME("x"), Application(Abstraction(VARNAME("a"), Application(VARNAME("a"),
    Application(VARNAME("b"), VARNAME("c")))), VARNAME("x")))
    val term2 = Application(Abstraction(VARNAME("x"), Abstraction(VARNAME("y"), Application(VARNAME("y"),
    VARNAME("x")))), VARNAME("z"))

    val optimizedTerm1 = optimize(term1)
    val optimizedTerm2 = optimize(term2)

    println(optimizedTerm1)
    println(optimizedTerm2)
  }
}

```

# Тестирование
Результат запуска программы:
```
Abstraction(VARNAME(a),Application(VARNAME(a),Application(VARNAME(b),VARNAME(c))))
Abstraction(VARNAME(y),Application(VARNAME(y),VARNAME(z)))
```

# Вывод
В ходе выполнения лабораторной работы были приобретены навыки разработки case-классов на языке Scala для
представления абстрактных синтаксических деревьев. Также было изучено более подробно о лямбда-исчислении.