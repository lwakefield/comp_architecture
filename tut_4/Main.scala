
package MulTutorial

import Chisel._

object UnitTests {
  def main(args: Array[String]): Unit = {
    val n: Int = 4

    val tutArgs = args.slice(1, args.length) 
    args(0) match {
      case "Div" =>
        chiselMainTest(tutArgs, () => Module(new Div(n))){ c => new DivTester(c, n) }
    }
  }
}

