
// That is not really functionnal...
val random = new java.util.Random
random.nextDouble
random.nextDouble

// comme expliqué, il faut renvoyer un nouvel objet en même temps 
// que la valeur générée.
// Définissons un contrat pour la génération d'un nombre aléatoire
trait RandomPure {
	def nextInt: (Int, RandomPure)
}

// Objet compagnon utile à la génération d'un générateur de nombre aléatoire
object RandomPure {
	def apply(seed: Long): RandomPure = new RandomPure {
		def nextInt = {
			val nextSeed = (seed*0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)
			((nextSeed >>> 16).asInstanceOf[Int], apply(nextSeed))
		}
	}
}

// Quelques fonctions utilitaires visant à utiliser ce générateur de nombre
val generator = RandomPure(4807) // Aurait pu être n'importe quel autre chiffre

def randomPositiveInt(using: RandomPure): (Int, RandomPure) = {
	val (a,b) = using.nextInt
	(a.abs,b)
}
val posInt = randomPositiveInt(generator)

def nextDouble(using: RandomPure): (Double,RandomPure) = {
	val (a,b) = using.nextInt
	(a.toDouble / Integer.MAX_VALUE,b)	
}
val double = nextDouble(generator)

def intDouble(using: RandomPure): ((Int,Double),RandomPure) = {
	val (_int, rnd) = using.nextInt
	val (_double, rnd2) = nextDouble(rnd)
	((_int, _double), rnd2)
}
def doubleInt(using: RandomPure): ((Double, Int),RandomPure) = {
	val (_int, rnd) = using.nextInt
	val (_double, rnd2) = nextDouble(rnd)
	((_double, _int), rnd2)
}
def double3(using: RandomPure): ((Double, Double, Double),RandomPure) = {
	val (_double1, rnd) = nextDouble(using)
	val (_double2, rnd2) = nextDouble(rnd)
	val (_double3, rnd3) = nextDouble(rnd2)
	((_double1, _double2, _double3), rnd3)
}
val ((_i, _d), _gen) = intDouble(generator)
val ((_d, _i), _gen) = doubleInt(generator)
val ((_d1, _d2, _d3), _gen) = double3(generator)

def ints(count: Int)(rnd: RandomPure) : (List[Int], RandomPure) = {
	def sub(xs: List[Int], count: Int, rnd: RandomPure): (List[Int], RandomPure) = {
		(rnd.nextInt._1 :: xs, rnd.nextInt._2)
	}
	val l = Nil
	if(count > 0) 
	else { 
		List( ints(count-1)())
	}
}