package MulTutorial

import Chisel._
import util.Random

class Mulv4dpath(val n: Int) extends Module {

  val io = new Bundle{
    val x = SInt(INPUT, n)
    val y = SInt(INPUT, n)
    val z = SInt(OUTPUT, 2*n)
    val load = Bool(INPUT)
    val add = Bool(INPUT)
    val sub = Bool(INPUT)
  }
  val product = Reg(init = SInt(0, width = (2*n)+1));
  val add = Reg(init = SInt(0, width = (2*n)+1));
  val sub = Reg(init = SInt(0, width = (2*n)+1));

  when (io.load) {
    product := Cat(SInt(0, width=n+1), io.y, SInt(0)) 
    add := Cat(io.x, SInt(0, width=n+1))
    sub := Cat(~io.x + SInt(1), SInt(0, width=n+1))
  }.elsewhen (io.add) {
    product := (product + add) >> UInt(1)
  }.elsewhen (io.sub) {
    product := (product + sub) >> UInt(1)
  }.otherwise {
    product := product >> UInt(1)
  }
  io.z := product

}

class Mulv4(val n: Int) extends Module {
  val io = new Bundle{
    val x = SInt(INPUT, n)
    val y = SInt(INPUT, n)
    val z = SInt(OUTPUT, 2*n)
    val finished = Bool(OUTPUT)
    val load = Bool(INPUT)
  }

  val state = Reg(init = SInt(0, width = log2Up(2*n))) // keeps track of state
  val m = Module(new Mulv4dpath(n))

  // connect everything up
  m.io.x := io.x
  m.io.y := io.y

  m.io.load := io.load
  m.io.add := Bool(false)
  m.io.sub := Bool(false)

  when (io.load) {
    state := SInt(0)
    m.io.load := io.load
  }.elsewhen (m.io.z(1,0) === SInt(1)) { 
    m.io.add := Bool(true)
  }.elsewhen (m.io.z(1,0) === SInt(2)) { 
    m.io.sub := Bool(true)
  }
  state := state + SInt(1)

  io.z := m.io.z
  //io.finished := state(log2Up(2*n) - 1)
}

class Mulv4Tester(c: Mulv4, n: Int) extends Tester(c) {
    val x = BigInt(n, scala.util.Random)
    val y = BigInt(n, scala.util.Random)
    poke(c.io.x, x)
    poke(c.io.y, y)
    poke(c.io.load, 1)
    step(1)
    //expect(c.m.product, y)
    poke(c.io.load, 0)
    step(2*n)
    //expect(c.io.z, (x * y))
  //for (i <- 0 until 10) {
    //val x = BigInt(n, scala.util.Random)
    //val y = BigInt(n, scala.util.Random)
    //poke(c.io.x, x)
    //poke(c.io.y, y)
    //poke(c.io.load, 1)
    //step(1)
    ////expect(c.m.product, y)
    //poke(c.io.load, 0)
    //step(n)
    ////expect(c.io.z, (x * y))
  //}
}

