% Лабораторная работа № 1 «Введение в функциональное
  программирование на языке Scala»
% 11 марта 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является ознакомление с программированием на языке Scala на основе чистых функций.

# Индивидуальный вариант
Закаренная функция frames: Int => (List[Int] => List[List[Int]]), формирующая список, состоящий из всех
подсписков списка целых чисел указанной в качестве параметра функции длины. Подсписком будем считать
список, который можно получить удалением произвольного количества элементов от начала и от конца списка.

# Реализация и тестирование

Исходный код программы:
```scala
def frames(length: Int): List[Int] => List[List[Int]] = {
  list => {
    def getFrames(list: List[Int], acc: List[List[Int]]): List[List[Int]] = {
      def getLength[A](list: List[A]): Int = {
        def recLength(list: List[A], count: Int): Int = list match {
          case Nil => count
          case head :: tail => recLength(tail, count + 1)
        }
        recLength(list, 0)
      }

      def takeList[A](n: Int, list: List[A]): List[A] = {
        if (n <= 0 || getLength(list) == 0) {
          Nil
        } else if (n >= list.length) {
          list
        } else {
          list.head :: takeList(n - 1, list.tail)
        }
      }

      if (getLength(list) < length) acc
      else {
        val frame = takeList(length, list)
        getFrames(list.tail, acc :+ frame)
      }
    }
    getFrames(list, List.empty)
  }
}

object Main {
    def main(args: Array[String]) = {
        val frameFunction = frames(3)
        val inputList = List(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val result = frameFunction(inputList)
        println(result)
    }
}
```

Вывод программы:
```
List(List(1, 2, 3), List(2, 3, 4), List(3, 4, 5), List(4, 5, 6), List(5, 6, 7), List(6, 7, 8), 
List(7, 8, 9))
```

# Вывод
Лабораторная работа позволила мне ознакомиться с принципами функционального программирования и применением
функций высшего порядка. В процессе выполнения работы я составил и отладил функцию frames, которая
формирует список всех подсписков списка целых чисел указанной длины. Эта задача позволила мне применить
концепцию чистых функций и работать с функциями высшего порядка, не прибегая к использованию стандартных
функций библиотеки Scala.