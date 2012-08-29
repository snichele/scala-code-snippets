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
      case Nil => sys.error("tail of empty list") // let's throw an error : tailing an empty list is often a logic error !
      case Cons(_, y) => y // let's use _ for part of a match we don't care (intention revealing)
    }
  }

  /*The usual default for `drop` is not to throw an exception, since it is typically used in cases where this is not 
    indicative of a programming error. 
    If you pay attention to how you use `drop`, it is often in cases where the length of the input list is unknown,
    and the number of elements to be dropped is being computed from something else. 
    If `drop` threw an exception, we'd have to first compute or check the length and only drop up to that many elements.  */
  def drop[A](l:List[A], n: Int) : List[A] = {
    if(n <= 0) l 
    else
      l match {
        case Nil => Nil
        case Cons(_, y) => drop(y,n-1)
      }
  }

  // currified args list for a better type inference in scala
  // good practice  : fName(list)(others args)
  def dropWhile[A](l: List[A])(f: A => Boolean): List[A] = {
    l match {
      case Nil => Nil
      case Cons(x, y) => if(f(x)) Cons(x, dropWhile(y)(f)) else dropWhile(y)(f)
    } 

    /* 
    Somewhat overkill, illustrate _pattern guard_, to only match a `Cons` whose head satisfies our predicate, `f`.
    The syntax is simply to add `if <cond>` after the pattern, before the `=>`, where `<cond>` can use any of the variables introduced by the pattern.
    If it does not match, then '_' case is used
    */
    l match {
      case Cons(h,t) if f(h) => dropWhile(t)(f) 
      case _ => l
    }
  }

  // Naive implementation : inneficient, and will stackoverflow due to recurstion with no tail recursive optimisation possible.
  def init[A](l: List[A]): List[A] = l match { 
      case Nil => sys.error("init of empty list")
      case Cons(_,Nil) => Nil
      case Cons(h,t) => Cons(h,init(t))
  }
  // Better, mutable state is hidden, so RT is preserved
  def init2[A](l: List[A]): List[A] = {
    import collection.mutable.ListBuffer
    val buf = new ListBuffer[A]
    def go(cur: List[A]): List[A] = cur match {
      case Nil => sys.error("init of empty list")
      case Cons(_,Nil) => List(buf.toList: _*)
      case Cons(h,t) => buf += h; go(t)
    }
    go(l)
  }  
}
import Ch3._
val x = List(1,2,3,4,5) match {
  case Cons(x, Cons(2, Cons(4, _))) => x
  case Nil => 42
  case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
  case Cons(h, t) => "Poxned"
  case _ => 101
}

