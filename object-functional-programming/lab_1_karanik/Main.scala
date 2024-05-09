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