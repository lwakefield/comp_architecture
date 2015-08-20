package MulTutorial

import Chisel._
import util.Random

class Mulv4(val n: Int) extends Module {
  val io = new Bundle{
    val x = SInt(INPUT, n)
    val y = SInt(INPUT, n)
    val z = SInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val add = Bool(OUTPUT)
    val sub = Bool(OUTPUT)
    val state = UInt(OUTPUT, log2Up(2*n))
  }

  val state = Reg(init = UInt(0, width = log2Up(2*n)))
  val product = Reg(init = SInt(0, width = (2*n)+1));
  val add = Reg(init = SInt(0, width = (2*n)+1));
  val sub = Reg(init = SInt(0, width = (2*n)+1));

  io.add := Bool(false)
  io.sub := Bool(false)
  io.state := state

  when (io.load) {
    state := SInt(0)
    product := Cat(SInt(0, width=n), io.y, SInt(0)) 
    add := Cat(io.x, SInt(0, width=n+1))
    sub := Cat(~io.x + SInt(1), SInt(0, width=n+1))
  }.elsewhen (product(1,0) === SInt(1)) { 
    io.add := Bool(true)
    product := (product + add) >> UInt(1)
  }.elsewhen (product(1,0) === SInt(2)) { 
    io.sub := Bool(true)
    product := (product + sub) >> UInt(1)
  }.otherwise {
    product := product >> UInt(1)
  }
  io.z := product(2*n, 1)
  state := state + UInt(1)
}

class Mulv4Tester(c: Mulv4, n: Int) extends Tester(c) {
  val ran = scala.util.Random
  for (i <- 0 until 10) {
    val inX = ran.nextInt((1 << (n-1)) - 1) * -1
    val x = BigInt(inX)
    val y = BigInt(n-1, scala.util.Random)
    poke(c.io.x, x)
    poke(c.io.y, y)
    poke(c.io.load, 1)
    step(1)
    poke(c.io.load, 0)
    step(n)
    expect(c.io.z, (x * y))
  }
}

