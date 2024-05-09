class Tree[K : Ordering, V](private val root: Option[Node[K, V]], flip: (V => V) = ((v : V) => v)) {

  def this() = this(None)

  def add(key: K, value: V): Tree[K, V] = {
    root match {
      case None => new Tree(Some(Node(key, value)), flip)
      case Some(r) => new Tree(root.map(_.add(key, value)), flip)
    }
  }

  def find(key: K): Option[V] =
    root.flatMap(_.find(key)).map(flip)
   
  def replaceBooleans()(implicit flipper: AbstractFlipper[V]) = {
    new Tree(root, flipper.flipper(flip))
  }
}

abstract class AbstractFlipper[T] {
  def flipper(f: (T => T)): (T => T)
}

object AbstractFlipper {
  implicit object bool_flipper extends AbstractFlipper[Boolean] {
    def flipper(f: (Boolean => Boolean)) = v => !f(v)
  }
}

case class Node[K, V](key: K, value: V, left: Option[Node[K, V]] = None, right: Option[Node[K, V]] = None) {

  def add(key: K, value: V)(implicit ev: Ordering[K]): Node[K, V] = {
    val cmp = ev.compare(key, this.key)
    if (cmp < 0)
      Node(this.key, this.value, left.map(_.add(key, value)).orElse(Some(Node(key, value))), right)
    else if (cmp > 0)
      Node(this.key, this.value, left, right.map(_.add(key, value)).orElse(Some(Node(key, value))))
    else
      Node(key, value, left, right)
  }

  def find(key: K)(implicit ev: Ordering[K]): Option[V] = {
    val cmp = ev.compare(key, this.key)
    if (cmp < 0)
      left.flatMap(_.find(key))
    else if (cmp > 0)
      right.flatMap(_.find(key))
    else
      Some(value)
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    var tree = new Tree[Int, Boolean]()
    tree = tree.add(3, true)
    tree = tree.add(4, false)
    tree = tree.add(2, true)
    tree = tree.replaceBooleans()
    val value = tree.find(4)
    println(s"Value at key 4: $value")

    var tree2 = new Tree[Int, Int]()
    /* следующая строка не компилируется */
    // tree2.replaceBooleans()
  }
}