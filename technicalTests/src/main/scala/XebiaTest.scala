
object XebiaTest {
    
  sealed abstract trait Direction
  case object N extends Direction
  case object S extends Direction
  case object E extends Direction
  case object W extends Direction
  
  case class Position2D(x: Int, y: Int)
  case class MowerState(pos: Position2D, direction: Direction)
  
 
  val acceptedCommands = List('A, 'G, 'D)
  def changeDirectionFor(mowerState: MowerState)(withRule: Direction => Direction) : MowerState = MowerState(mowerState.pos,withRule(mowerState.direction))
  val ┌ : MowerState => MowerState = changeDirectionFor(_)(_ match {case N => E case E => S case S => W case W => N})
  val ┐ : MowerState => MowerState = changeDirectionFor(_)(_ match {case N => W case E => N case S => E case W => S})
  
  /*
   Usage :
   launch sbt in the directory of the project
   > test
   Enjoy the specs2 report in the console.
   
   todo : Enhance the Tuple2 semantics and review some val naming.
   */
  object FunctionalSolution {
    // todo add implicit conversion String => Seq[Symbol]
 
    /** Main function to execute. */
    def runMowerFleet(terrainSize: Position2D, fleet: Seq[Tuple2[MowerState,String]]) = {
      val garden = for (x <- (0 to terrainSize.x ); y <- (0 to terrainSize.y )) yield Position2D(x, y) // cartesian product, pairs a wrapped in a Position2D

      // todo parametrize garden !
      def maybeMoveXOf(mowerState: MowerState)(by: Int) = MowerState( garden.find(_.equals(Position2D(mowerState.pos.x + by, mowerState.pos.y))).getOrElse(mowerState.pos) ,mowerState.direction)
      def maybeMoveYOf(mowerState: MowerState)(by: Int) = MowerState( garden.find(_.equals(Position2D(mowerState.pos.x, mowerState.pos.y + by))).getOrElse(mowerState.pos) ,mowerState.direction)
      val ► = (mowerState: MowerState) => maybeMoveXOf(mowerState)(1)
      val ◄ = (mowerState: MowerState) => maybeMoveXOf(mowerState)(-1)
      val ▲ = (mowerState: MowerState) => maybeMoveYOf(mowerState)(1)
      val ▼ = (mowerState: MowerState) => maybeMoveYOf(mowerState)(-1)
    
    
      def runCommands(startMowerState: MowerState)(commands: Seq[Symbol]) = {
        def nextState(fromState: MowerState, withCommand: Symbol):MowerState = {
          val newState : MowerState = withCommand match {
            case 'G => ┐(fromState)
            case 'D => ┌(fromState)
            case 'A => {
                fromState.direction match {
                  case N => ▲(fromState)
                  case E => ►(fromState)
                  case S => ▼(fromState)
                  case W => ◄(fromState)
                }
              }
            case x: Symbol => {
                println(" Unknown command ["+x+"], skipping it.")
                identity(fromState)
              }
          }
          newState
        }
        val allStates = commands.foldLeft(List[MowerState](startMowerState)){ (statesHistory, command) =>
          nextState(statesHistory.head, command) :: statesHistory
        }
        allStates.head
      }
              
      val allPositions = for {
        nextMower <- fleet
        if garden.find(_.equals(nextMower._1.pos)).isDefined
      } yield runCommands(nextMower._1)(nextMower._2.map(c => Symbol(c.toString)))
      allPositions
    }    
  }
  
  object ObjectSolution {
    case class Garden(width: Int, height: Int)
    case class Mower(x: Int, y: Int, direction: Direction, garden: Garden) {
      def moveByCommand(command: Symbol) {
//        command match {
//          case 'G => ┐(fromState)
//          case 'D => ┌(fromState)
//          case 'A => {
//              fromState._2 match {
//                case N => ▲(fromState)
//                case E => ►(fromState)
//                case S => ▼(fromState)
//                case W => ◄(fromState)
//              }
//            }
//          case x: Symbol => {
//              println(" Unknown command ["+x+"], skipping it.")
//              identity(fromState)
//            }        
//        }
      }
    }
  }
}
