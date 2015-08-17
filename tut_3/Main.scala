
package MulTutorial

import Chisel._

object UnitTests {
  def main(args: Array[String]): Unit = {
    val n: Int = 4

    val tutArgs = args.slice(1, args.length) 
    args(0) match {
      case "Mul" =>
        chiselMainTest(tutArgs, () => Module(new Mul(n))){ c => new MulTester(c, n) }
      case "Mulv3" =>
        chiselMainTest(tutArgs, () => Module(new Mulv3(n))){ c => new Mulv3Tester(c, n) }
    }
  }
}

