package MulTutorial

import Chisel._
import util.Random

class Mulv3dpath(val n: Int) extends Module {

  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val rshift = Bool(INPUT)
    val write = Bool(INPUT)
  }
  val multiplicand = Reg(init = UInt(0, width = n));
  val product = Reg(init = UInt(0, width = (2*n)+1));

  when (io.load) {
    multiplicand := io.x
    product := Cat(UInt(0, width=n+1), io.y) 
  }
  when (io.write && (product(0) === UInt(1))) { 
    val left_side = product(2*n, n) + multiplicand
    val right_side = product(n-1, 0)
    product := Cat(left_side, right_side) >> UInt(1)
  } 
  when (io.rshift) { product := product >> UInt(1) }
  io.z := product
}

class Mulv3(val n: Int) extends Module {
  val io = new Bundle{
    val x = UInt(INPUT, n)
    val y = UInt(INPUT, n)
    val z = UInt(OUTPUT, 2*n)
    val finished = Bool(OUTPUT)
    val load = Bool(INPUT)
  }

  val state = Reg(init = UInt(0, width = log2Up(n)+1)) // keeps track of state
  val m = Module(new Mulv3dpath(n))

  // connect everything up
  m.io.x := io.x
  m.io.y := io.y

  m.io.load := io.load
  m.io.rshift := Bool(false)
  m.io.write := Bool(false)

  when (io.load) {
    state := UInt(0)
    m.io.load := io.load
  }.
  elsewhen (!io.finished) {
    when (m.io.z(0) === UInt(0)) { 
      m.io.rshift := Bool(true)
    }.
    elsewhen (m.io.z(0) === UInt(1)) { 
      m.io.write := Bool(true)
    }
    state := state + UInt(1)
  }

  io.z := m.io.z
  io.finished := state(log2Up(2*n) - 1)
}

class Mulv3Tester(c: Mulv3, n: Int) extends Tester(c) {
  poke(c.io.x, 8)
  poke(c.io.y, 13)
  poke(c.io.load, 1)
  step(1)
  expect(c.m.product, 13)
  poke(c.io.load, 0)
  step(n)
  expect(c.io.z, (8 * 13))

  for (i <- 0 until 10) {
    val x = BigInt(n, scala.util.Random)
    val y = BigInt(n, scala.util.Random)
    poke(c.io.x, x)
    poke(c.io.y, y)
    poke(c.io.load, 1)
    step(1)
    expect(c.m.product, y)
    poke(c.io.load, 0)
    step(n)
    expect(c.io.z, (x * y))
  }
}

