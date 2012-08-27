object Ch3 {
  sealed trait List[+A]

  case object Nil extends List[Nothing]
  case class Cons[+A](head: A, tail: List[A]) extends List[A]

  object List {
    def sum(ints: List[Int]): Int = ints match {
      case Nil => 0
      case Cons(x,xs) => x + sum(xs)
    }
   
    def product(ds: List[Double]): Double = ds match {
      case Nil => 1.0
      case Cons(0.0, _) => 0.0
      case Cons(x,xs) => x * product(xs)
    }
   
    def apply[A](as: A*): List[A] =
      if (as.isEmpty) Nil
      else Cons(as.head, apply(as.tail: _*))
   
    val example = Cons(1, Cons(2, Cons(3, Nil)))
    val example2 = List(1,2,3)
    val total = sum(example)
  }

  def tail[A](l:List[A]) : List[A] = {
    l match {
      case Cons(x, y) => y
      case Nil => Nil
    }
  }

  def drop[A](l:List[A], n: Int) : List[A] = {
    if(n == 0) l 
    else
      l match {
        case Cons(x, y) => drop(y,n-1)
        case Nil => Nil
      }
  }

  // currified args list for a better type inference in scala
  // good practice  : fName(list)(others args)
  def dropWhile[A](l: List[A])(f: A => Boolean): List[A] = {
    l match {
      case Cons(x, y) => if(f(x)) Cons(x, dropWhile(y)(f)) else dropWhile(y)(f)
      case Nil => Nil
    } 
  }
}

val x = List(1,2,3,4,5) match {
  case Cons(x, Cons(2, Cons(4, _))) => x
  case Nil => 42
  case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
  case Cons(h, t) => "Poxned"
  case _ => 101
}

