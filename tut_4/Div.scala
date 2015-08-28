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

  val remainder = Reg(init = UInt(0, width=2*n+1))
  val divisor = Reg(init = UInt(0, width=n))
  val end = (2*n)-1

  when (io.load) {
    remainder := io.x << UInt(1)
    divisor := io.y
  }.
  elsewhen (io.subtract) {
    val left_half = remainder(end, n) - divisor
    val right_half = remainder(n-1,0)
    remainder := Cat(left_half(n-2, 0), right_half, UInt(1))
  }.
  elsewhen (io.left_shift) {
    remainder := remainder << UInt(1)
  }.
  elsewhen (io.write) {
    val left_half = remainder(end+1, n+1)
    val right_half = remainder(n-1, 0)
    remainder := Cat(left_half, right_half)
  }

  io.z := remainder
}

class Div(val n: Int) extends Module {
  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val finished = Bool(OUTPUT)
  }

  val state = Reg(init = UInt(1, width = log2Up(2*n))) // keeps track of state
  val m = Module(new Divdpath(n))
  val finished = Reg(init = Bool(false))

  // connect everything up
  m.io.x := io.x
  m.io.y := io.y
  io.z := m.io.z

  m.io.load := io.load
  m.io.left_shift := Bool(false)
  m.io.write := Bool(false)
  m.io.subtract := Bool(false)

  val left_half = m.io.z((2*n)-1, n)
  val right_half = m.io.z(n-1,0)

  when (io.load) {
    finished := Bool(false)
    state := UInt(1)
  }.
  elsewhen (!finished) {
    when (state > UInt(n)) {
      m.io.write := Bool(true)
      finished := Bool(true)
    }.
    elsewhen (left_half >= io.y) {
      m.io.subtract := Bool(true)
    }.
    elsewhen (left_half < io.y) {
      m.io.left_shift := Bool(true)
    }
    state := state + UInt(1)
  }

  io.finished := finished

}

class DivTester(c: Div, n: Int) extends Tester(c) {
  //val x = 11
  //val y = 4
  //poke(c.io.x, x)
  //poke(c.io.y, y)
  //poke(c.io.load, 1)
  //step(1)
  //poke(c.io.load, 0)
  //step(n + 1)
  //expect(c.io.z, (x % y) << n | (x / y))
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
    step(n + 1)
    expect(c.io.z, (x % y) << n | (x / y))
  }
}

