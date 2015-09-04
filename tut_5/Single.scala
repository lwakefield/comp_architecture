package TutorialExamples

import Chisel._
import scala.collection.mutable.ArrayBuffer

object Proc {
  val WORDLENGTH = 32	// datapath size
  val IADDRZ = 10	// number of bits for instruction memory 
}

class Single extends Module {

  val io = new Bundle {
    val isWr   = Bool(INPUT)
    val wrAddr = UInt(INPUT, Proc.IADDRZ)
    val wrData = Bits(INPUT, Proc.WORDLENGTH)
    val boot   = Bool(INPUT)
    val valid  = Bool(OUTPUT)
    val out    = Bits(OUTPUT, Proc.WORDLENGTH)
    val inst   = Bits(OUTPUT, Proc.WORDLENGTH)
    val pc     = Bits(OUTPUT, Proc.IADDRZ)
    //val op     = Bits(OUTPUT, 6)
  }
  val regfile = Mem(Bits(width = Proc.WORDLENGTH), Proc.WORDLENGTH)
  val imem = Mem(Bits(width = Proc.WORDLENGTH), 1 << Proc.IADDRZ)
  val pc   = Reg(init=UInt(0, Proc.IADDRZ))

  val addiu_op :: rtype_op :: Nil = Enum(Bits(), 2)
  val addu_fn :: subu_fn :: or_fn :: sltu_fn  :: Nil = Enum(Bits(), 4)
  
  val inst = imem(pc(Proc.IADDRZ-1, 2))
  val op   = inst(31,26)
  val rsi  = inst(25,21)
  val rti  = inst(20,16)
  val rdi  = inst(15,11)
  val shamt  = inst(10,6)
  val funct  = inst(5,0)

  val ra = Mux(rsi === Bits(0), Bits(0), regfile(rsi))	// regfile(0) = 0
  val rb = Mux(rti === Bits(0), Bits(0), regfile(rti))
  val rc = Bits(width = Proc.WORDLENGTH)

  io.valid := Bool(false)
  io.out   := Bits(0)
  rc       := Bits(0)

  when (io.isWr) {
    imem(io.wrAddr) := io.wrData
  } .elsewhen (io.boot) {
    pc := UInt(0)
  } .otherwise {
    when (op != rtype_op) {
      switch(op) {
        is(addiu_op) { rc := ra + rdi }  // $t = $s + C
      }
      regfile(rti) := rc
    }.
    elsewhen (op === rtype_op) {
      switch(funct) {
        is(addu_fn) { rc := ra + rb } // $d = $s + $t
        is(subu_fn) { rc := ra + rb } // $d = $s - $t
        is(or_fn) { rc := ra | rb }   // $d = $s | $t 
        is(sltu_fn) {                 // $d = $s < $t ? 1 : 0
          rc := Mux(ra > rb, UInt(1), UInt(0))
        }
      }
      regfile(rdi) := rc
    }
    pc := pc + UInt(4)
  }
  io.out := regfile(UInt(3))
  io.inst := inst
  io.pc := pc
}

class SingleTests(c: Single) extends Tester(c) {  
  def wr(addr: UInt, data: UInt)  = {
    poke(c.io.isWr,   1)
    poke(c.io.wrAddr, addr.litValue())
    poke(c.io.wrData, data.litValue())
    step(1)
  }
  def boot()  = {
    poke(c.io.isWr, 0)
    poke(c.io.boot, 1)
    step(1)
    poke(c.io.isWr, 0)
    poke(c.io.boot, 0)
  }
  def tick()  = {
    peek(c.io.inst)
    peek(c.io.pc)
    step(1)
    peek(c.io.out)
  }
  def I (op: UInt, rsi: Int, rti: Int, rdi: Int, shamt: Int, funct: Int) = 
    Cat(op, UInt(rsi, 5), UInt(rti, 5), 
        UInt(rdi, 5), UInt(shamt, 5), UInt(funct, 6))
  val app  = Array(I(c.addiu_op, 0, 3, 15, 0, 0), //$t = $s + C
                   I(c.addiu_op, 3, 3, 15, 0, 0),
                   I(c.addiu_op, 3, 3, 15, 0, 0),
                   I(c.addiu_op, 3, 3, 15, 0, 0),
                   I(c.addiu_op, 3, 3, 6, 0, 0),
                   I(c.rtype_op, 0, 0, 0, 0, 0),
                   I(c.rtype_op, 0, 0, 0, 0, 0),
                   I(c.rtype_op, 0, 0, 0, 0, 0),
                   I(c.rtype_op, 0, 0, 0, 0, 0))
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) 
    wr(UInt(addr), app(addr))
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < 5)
  expect(c.io.out, 66)
}

object TutorialExamples {
  def main(args: Array[String]): Unit = {
    val tutArgs = args.slice(1, args.length)
    val res =
    args(0) match {
      case "Single" =>
        chiselMainTest(tutArgs, () => Module(new Single())){
          c => new SingleTests(c)}
    }
  }
}

