/** A case class that will be used hereafter. */
case class Box(height: Double, width: Double)
def abs(n: Int): Int = if (n < 0) -n else n

// Some basic approach to comparing Box
/*def wider(x: Box, y: Box): Box = {
  if (x.width > y.width) x else y
}

def taller(x: Box, y: Box): Box = {
  if (x.height > y.height) x else y
}
*/
// But there is copy paste here ! Let's improve this

// First improvement
// Higher order function based solution to parametrize the comparison
// Code block as a method body : remember, the last expression is the value of the block
/*
def greaterBy(x: Box, y: Box, f: Box => Double): Box = {
  if (f(x) > f(y)) x else y
}

def wider(x: Box, y: Box): Box = {
  greaterBy(x, y, p => p.width)
}

def taller(x: Box, y: Box) = {
  greaterBy(x, y, p => p.height)
}
*/

// Second improvement, no code block
def greaterBy(x: Box, y: Box, f: Box => Double): Box =  if (f(x) > f(y)) x else y

def wider(x: Box, y: Box): Box = greaterBy(x, y, p => p.width)

def taller(x: Box, y: Box) = greaterBy(x, y, p => p.height)

// EXERCISE 1 SOLUTION
// Second improvment refined with scala syntaxic sugar for function litteral
def widerSS(x: Box, y: Box): Box = greaterBy(x, y, _.width)

def tallerSS(x: Box, y: Box) = greaterBy(x, y, _.height)
// EOF EXERCISE 1 SOLUTION

// But there is still some improvement to do on the comparison !

// Let's define a max comparison
// really short version for integer comparison, syntaxic sugar
val max: (Int, Int) => Int = (a, b) => if (a < b) b else a

// Let's decompose it
/* A val, which contains an anonymous fonction
val max : (Int, Int) => Int = {
	// anonymous function litteral
	(a, b) => if (a < b) b else a
	
	// equivalent to
	//new Function2 [Int, Int, Int] {
  	//	def apply(a: Int, b: Int): Int = if (a < b) b else a
	//}

	// Quick reminder for no arg anonymous fonction litteral
	// () => { System.getProperty("user.dir") }
}
*/

val lessThan: (Int, Int) => Boolean = _ < _

def isEven(n: Int): Boolean = n % 2 == 0
def isNegative(n: Int): Boolean = n < 0
def odd(n: Int): Boolean =  !(isEven(n))
def positive(n: Int): Boolean = !(isNegative(n))

// See the negative usage ? Improve it with a negative function factory !
def not(p: Int => Boolean): Int => Boolean =  n => !(p(n))
// Using this, we can redefine the previous functions as this, storing them in val
// NOTE we are not passing functions around, it's just a Scala syntaxic sugar
// not(x => even(x))
val odd = not(isEven)
val positive = not(isNegative)

// EXERCISE 2 SOLUTION
def absolute(f: Int => Int): Int => Int = n => abs(f(n))
val add10AndAbstoluteIt = absolute(_ + 10)
val result = add10AndAbstoluteIt(-50)
println(result)
// EOF EXERCISE 2 SOLUTION

// Reminder : those functions where monomorphic : operating on a specific type

// Let's write a polymorphic not fonction !
// Notice that the method body is not relying on any types of the signature, so there will be easy
def notPoly[A](p: A => Boolean): A => Boolean =  n => !(p(n))

// EXERCISE 3
def absolutePoly[A](f: A => Int): A => Int = a => abs(f(a))

// Hum...lots of A => ... Let's declare type aliases !
// Let's call them predicate (since boolean is involved)
type Pred[A] = A => Boolean

// EXERCISE 4
def divisibleBy(k: Int): Pred[Int] = _ % k == 0
divisibleBy(5)(10)
// EOF EXERCISE 4

// EXERCISE 5
def isEven2(n: Int): Boolean = divisibleBy(2)(n)
// works too !
// val even = divisibleBy(2)
// EOF EXERCISE 5

// EXERCISE 6
def predBy3and5 = (num : Int) => divisibleBy(3)(num) && divisibleBy(5)(num)
def predBy3or5 = (num : Int) => divisibleBy(3)(num) || divisibleBy(5)(num)

def lift[A](f: (Boolean, Boolean) => Boolean, g: Pred[A], h: Pred[A]): Pred[A] = (num : A) => f(g(num),h(num))
def predBy3and5L = lift[Int] (_ && _, divisibleBy(3),divisibleBy(5))
def predBy3or5L = lift[Int] (_ || _, divisibleBy(3),divisibleBy(5))
// EOF EXERCISE 6

// EXERCISE 7
def curry[A,B,C](f: (A, B) => C): A => B => C = p1 => f(p1,_)
// EOF EXERCISE 7

// EXERCISE 8
def uncurry[A,B,C](f: A => B => C): (A, B) => C = f(_)(_)
// EOF EXERCISE 8

// EXERCISE 9
def compose[A,B,C](f: B => C, g: A => B): A => C = (x: A) => f(g(x))
// EOF EXERCISE 9

// From the book
// def liftPoly[A,B,C,D](f: (B, C) => D)(g: A => B, h: A => C): A => D = (p : A) => f(g(p),h(p))

def liftPoly[A,B,C,D](f: (B, C) => D)( g: A => B, h: A => C): A => D = (p : A) => f(g(p),h(p))

val divBox = liftPoly[Box, Double, Double, Double](_ / _) _
val aspectRatio = divBox(_.height,_.width)
val ar = aspectRatio(Box(320, 256))

// EXERCISE 10
def lift3[A,B,C,D,E](f: (B, C, D) => E)(g: A => B, h: A => C, i: A => D): A => E = (p : A) => f(g(p),h(p),i(p))

def lift3Hard[A,B,C,D,E](f: (B, C, D) => E)(g: A => B, h: A => C, i: A => D): A => E =
      a => 
      	liftPoly[A,B,C,E](f(_, _, i(a)))(g, h)(a)
// EOF EXERCISE 10
