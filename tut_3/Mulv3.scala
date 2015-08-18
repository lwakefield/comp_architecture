package MulTutorial

import Chisel._
import util.Random

class Mulv3dpath(val n: Int) extends Module {

  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val lshift = Bool(INPUT)
    val rshift = Bool(INPUT)
    val write = Bool(INPUT)
  }
  val multiplier = Reg(init = UInt(0, width = n));
  val multiplicand = Reg(init = UInt(0, width = 2 * n));
  val product = Reg(init = UInt(0, width = 2 * n));

  when (io.load) {
    multiplicand := io.x
    multiplier := io.y
    product := UInt(0)
  }
  when (io.lshift) { multiplicand := multiplicand << UInt(1) }
  when (io.rshift) { multiplier := multiplier >> UInt(1) }
  when (io.write && (multiplier(0) === UInt(1))) { 
    product := product + multiplicand 
  } 
  io.z := product
}

class Mulv3(val n: Int) extends Module {
  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
  }

  val state = Reg(init = UInt(0, width = log2Up(2*n))) // keeps track of state
  val m = Module(new Mulv3dpath(n))

  // connect everything up
  m.io.x := io.x
  m.io.y := io.y

  m.io.load := Bool(false)
  m.io.rshift := Bool(false)
  m.io.lshift := Bool(false)
  m.io.write := Bool(false)

  when (state === UInt(0)) { 
    m.io.load := io.load
  } .elsewhen (state(0) === UInt(0)) { 
    m.io.rshift := Bool(true)
    m.io.lshift := Bool(true)
  } .elsewhen (state(0) === UInt(1)) { 
    m.io.write := Bool(true)
  }
  state := state + UInt(1)
  io.z := m.io.z
}

class Mulv3Tester(c: Mulv3, n: Int) extends Tester(c) {
  for (i <- 0 until 10) {
    val x = BigInt(n, scala.util.Random)
    val y = BigInt(n, scala.util.Random)
    poke(c.io.x, x)
    poke(c.io.y, y)
    poke(c.io.load, 1)
    step(1)
    poke(c.io.load, 0)
    step(n)
    expect(c.io.z, (x * y))
  }
}

