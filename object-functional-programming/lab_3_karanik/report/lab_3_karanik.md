% Лабораторная работа № 3 «Обобщённые классы в Scala»
% 1 апреля 2024 г.
% Андрей Караник, ИУ9-62Б

# Цель работы
Целью данной работы является приобретение навыков разработки обобщённых классов на языке Scala с 
использованием неявных преобразований типов.

# Индивидуальный вариант
Класс Tree[K : Ordering,V], представляющий неизменяемое дерево поиска с ключами типа K и значениями типа V. 
Дерево должно поддерживать операции добавления словарной пары и поиска значения по ключу. Кроме того,
в случае, еслиа V — булевский тип, у дерева должна дополнительно присутствовать операция замены всех true
в вершинах на false, и наоборот, работающая за константное время.

# Реализация

```scala
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
```

# Тестирование

Результат запуска программы:

```
Value at key 4: Some(true)
```

# Вывод
В ходе выполнения лабораторной работы были приобретены навыки разработки обобщённых классов на языке Scala
с использованием неявных преобразований типов. Разработанный класс Tree[K : Ordering,V] представляет собой
неизменяемое дерево поиска с ключами типа K и значениями типа V, поддерживая операции добавления словарной
пары и поиска значения по ключу. Дополнительно, при наличии булевского типа V, класс предоставляет
операцию замены всех true на false и наоборот за константное время.