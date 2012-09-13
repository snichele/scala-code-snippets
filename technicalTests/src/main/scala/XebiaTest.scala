
object XebiaTest {
  /*
   Usage :
   launch sbt in the directory of the project
   > test
   Enjoy the specs2 report in the console.
   
   todo : Enhance the Tuple2 semantics and review some val naming.
   */
  object FunctionalSolution {
    // todo add implicit conversion String => Seq[Symbol]
 
    type Pos2D = Tuple2[Int,Int]
    type MowerState = Tuple2[Pos2D,Direction]
    
    sealed abstract trait Direction
    case object N extends Direction
    case object S extends Direction
    case object E extends Direction
    case object W extends Direction
    
    val acceptedCommands = List('A, 'G, 'D)
    
    /** Main function to execute. */
    def runMowerFleet(terrainSize: Pos2D, fleet: Seq[Tuple2[MowerState,String]]) = {
      val possibleTerrainPositions = for (x <- (0 to terrainSize._1 ); y <- (0 to terrainSize._2 )) yield (x, y) // cartesian product

      
      def maybeMoveXOf(mowerState: MowerState)(by: Int) = ( possibleTerrainPositions.find(_.equals((mowerState._1._1 + by, mowerState._1._2))).getOrElse(mowerState._1) ,mowerState._2)
      def maybeMoveYOf(mowerState: MowerState)(by: Int) = ( possibleTerrainPositions.find(_.equals((mowerState._1._1, mowerState._1._2 + by))).getOrElse(mowerState._1) ,mowerState._2)
      val ► = (mowerState: MowerState) => maybeMoveXOf(mowerState)(1)
      val ◄ = (mowerState: MowerState) => maybeMoveXOf(mowerState)(-1)
      val ▲ = (mowerState: MowerState) => maybeMoveYOf(mowerState)(1)
      val ▼ = (mowerState: MowerState) => maybeMoveYOf(mowerState)(-1)
    
      def changeDirectionFor(mowerState: MowerState)(withRule: Direction => Direction) = (mowerState._1,withRule(mowerState._2))
      val ┌ = (mowerState: MowerState) => changeDirectionFor(mowerState)((fromDirection: Direction) => fromDirection match {case N => E case E => S case S => W case W => N})
      val ┐ = (mowerState: MowerState) => changeDirectionFor(mowerState)((fromDirection: Direction) =>  fromDirection match {case N => W case E => N case S => E case W => S})
    
      def runCommands(startMowerState: MowerState)(commands: Seq[Symbol]) = {
        def nextState(fromState: MowerState, withCommand: Symbol) = {
          val newState = withCommand match {
            case 'G => ┐(fromState)
            case 'D => ┌(fromState)
            case 'A => {
                fromState._2 match {
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
        if possibleTerrainPositions.find(_.equals(nextMower._1._1)).isDefined
      } yield runCommands(nextMower._1)(nextMower._2.map(c => Symbol(c.toString)))
      allPositions
    }    
  }
}
