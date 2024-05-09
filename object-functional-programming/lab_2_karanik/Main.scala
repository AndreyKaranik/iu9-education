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