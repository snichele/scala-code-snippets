
import org.specs2.mutable._

class XebiaTestSpec extends Specification {
       
  import XebiaTest.FunctionalSolution._
  
  "XebiaTest.FunctionalSolution  " should {

    " compute a mover starting from 1,2,N to  1,3,N and one from ( (3,3), E) to ((5,1),E)" in {
      
      runMowerFleet(
        (5,5),
        Seq(
          (( (1,2), N),"GAGAGAGAA"),
          (( (3,3), E),"AADAADADDA")
        )
      ) must containAllOf(
        List(((1,3),N), ((5,1),E))
      )
    }
    
    " not permit mower to get out of the field " in {
      runMowerFleet(
        (5,5),
        Seq(
          (( (1,2), N),"AAAAAAAAAAAAAAADAAAAAAA")
        )
      ) must containAllOf(
        List(((5,5),E))
      )
    }
    
    " should gobble invalid commands " in {
      runMowerFleet(
        (5,5),
        Seq(
          (( (1,2), N),"FOOBIR")
        )
      ) must containAllOf(
        List(((1,2),N))
      )
    }
  }

}