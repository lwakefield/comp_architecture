package MulTutorial

import Chisel._
import util.Random

class Divdpath(val n: Int) extends Module {

  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val left_shift = Bool(INPUT)
    val write = Bool(INPUT)
    val subtract = Bool(INPUT)
  }
}

class Div(val n: Int) extends Module {
  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
  }

  val state = Reg(init = UInt(0, width = log2Up(2*n))) // keeps track of state
  val m = Module(new Divdpath(n))

  // connect everything up
  m.io.x := io.x
  m.io.y := io.y
  m.io.load := io.load
  m.io.left_shift := Bool(false)
  m.io.write := Bool(false)
  m.io.subtract := Bool(false)

}

class DivTester(c: Div, n: Int) extends Tester(c) {
  for (i <- 0 until 1000) {
    val x = BigInt(n, scala.util.Random)
    var y = BigInt(n, scala.util.Random)
    
    if (y == BigInt(0)) {       // never divide by zero
      y = 1
    }
    poke(c.io.x, x)
    poke(c.io.y, y)
    poke(c.io.load, 1)
    step(1)
    poke(c.io.load, 0)
    step(2 * n + 1)
    expect(c.io.z, (x % y) << n | (x / y))
  }
}

