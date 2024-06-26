% Лабораторная работа № 2 «Введение в объектно-ориентированное программирование на языке Scala»
% 11 марта 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является изучение базовых объектно-ориентированных возможностей языка Scala.

# Индивидуальный вариант
Последовательность степеней простых делителей, на которые раскладывается некоторое натуральное число.
Операции: «\*» — умножение на число, представленное другой последовательностью; «\*\*» — наибольший общий
делитель числа, представленного текущей последовательностью, и числа, представленного другой
последовательностью; «==», «!=» — сравнения; «!» — преобразование к Int.

# Реализация и тестирование

Исходный код программы:
```scala
class PrimeFactorsSequence(val factors: Map[Int, Int]) {
    def * (other: PrimeFactorsSequence): PrimeFactorsSequence = {
        new PrimeFactorsSequence(mergeFactors(this.factors, other.factors))
    }

    def ** (other: PrimeFactorsSequence): PrimeFactorsSequence = {
        new PrimeFactorsSequence(gcdFactors(this.factors, other.factors))
    }

    def == (other: PrimeFactorsSequence): Boolean = this.factors == other.factors

    def != (other: PrimeFactorsSequence): Boolean = !(this == other)

    def unary_! : Int = factors.foldLeft(1) {
        case (acc, (prime, power)) => acc * math.pow(prime, power).toInt
    }

    private def mergeFactors(map1: Map[Int, Int], map2: Map[Int, Int]): Map[Int, Int] =
        (map1.keySet.union(map2.keySet)).map { prime =>
            prime -> (map1.getOrElse(prime, 0) + map2.getOrElse(prime, 0))
        }.toMap

    private def gcdFactors(map1: Map[Int, Int], map2: Map[Int, Int]): Map[Int, Int] = {
        map1.keySet.intersect(map2.keySet).map { key =>
            key -> math.min(map1(key), map2(key))
        }.toMap
    }
}

object Main {
    def main(args: Array[String]) = {
        val sequence1 = PrimeFactorsSequence(Map(2 -> 2, 3 -> 1))
        val sequence2 = PrimeFactorsSequence(Map(2 -> 1, 3 -> 2))

        val product = sequence1 * sequence2
        val gcd = sequence1 ** sequence2

        println(product.factors)
        println(gcd.factors)

        println(!product)
        println(!gcd)

        println(sequence1 == sequence2)
        println(sequence1 != sequence2)
    }
}
```

Вывод программы:
```
Map(2 -> 3, 3 -> 3)
Map(2 -> 1, 3 -> 1)
216
6
false
true
```

# Вывод
Лабораторная работа по изучению базовых объектно-ориентированных возможностей языка Scala позволила мне
разработать класс, представляющий последовательность степеней простых делителей натурального числа. В
процессе выполнения работы я ознакомился с принципами работы с применением
контейнерных классов и избеганием использования рекурсии для замены циклов.