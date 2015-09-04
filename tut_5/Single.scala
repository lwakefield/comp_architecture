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
    val funct  = Bits(OUTPUT, 6)
    val op     = Bits(OUTPUT, 6)
    val lo = Bits(OUTPUT, Proc.WORDLENGTH)
    val hi = Bits(OUTPUT, Proc.WORDLENGTH)
  }
  val regfile = Mem(Bits(width = Proc.WORDLENGTH), Proc.WORDLENGTH)
  val imem = Mem(Bits(width = Proc.WORDLENGTH), 1 << Proc.IADDRZ)
  val pc   = Reg(init=UInt(0, Proc.IADDRZ))
  val lo = Reg(init=UInt(0, Proc.WORDLENGTH))
  val hi = Reg(init=UInt(0, Proc.WORDLENGTH))

  val rtype_op = UInt(0, width=6)
  val addiu_op = UInt(9, width=6)

  val addu_fn  = UInt(33, width=6)
  val subu_fn  = UInt(35, width=6)
  val or_fn    = UInt(37, width=6)
  val sltu_fn  = UInt(42, width=6)
  val multu_fn = UInt(25, width=6)
  val mflo_fn  = UInt(18, width=6)
  val mfhi_fn  = UInt(16, width=6)
  
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
    when (op === rtype_op){
      switch(funct) {
        is(addu_fn) { rc := ra + rb } // $d = $s + $t
        is(subu_fn) { rc := ra + rb } // $d = $s - $t
        is(or_fn) { rc := ra | rb }   // $d = $s | $t 
        is(sltu_fn) {                 // $d = $s < $t ? 1 : 0
          rc := Mux(ra > rb, UInt(1), UInt(0))
        }
        is(multu_fn) {
          val res = UInt(ra, width=2*Proc.WORDLENGTH) * UInt(rb, width=2*Proc.WORDLENGTH)
          hi := res(63,32)
          lo := res(31,0)
        }
        is(mflo_fn) { rc := lo }
        is(mfhi_fn) { rc := hi }
      }
      regfile(rdi) := rc
    }.
    otherwise {
      switch(op) {
        is(addiu_op) { rc := Cat(rdi, shamt, funct) }  // $t = $s + C
      }
      regfile(rti) := rc
    }
    pc := pc + UInt(4)
  }
  io.out := regfile(UInt(3))
  io.inst := inst
  io.pc := pc
  io.hi := hi
  io.lo := lo
  io.funct := funct
  io.op := op
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
    peek(c.io.funct)
    peek(c.io.op)
    peek(c.io.pc)
    step(1)
    peek(c.io.out)
    peek(c.io.hi)
    peek(c.io.lo)
  }
  def R (op: UInt, rsi: Int, rti: Int, rdi: Int, shamt: Int, funct: UInt) = 
    Cat(op, UInt(rsi, 5), UInt(rti, 5), 
        UInt(rdi, 5), UInt(shamt, 5), funct)
  def I (op: UInt, rsi: Int, rti: Int, imm: Int) = 
    Cat(op, UInt(rsi, 5), UInt(rti, 5), 
        UInt(imm, 16))
  val app  = Array(I(c.addiu_op, 0, 3, 66), //$t = $s + C
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0)))
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) 
    wr(UInt(addr), app(addr))
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < 5)
  expect(c.io.out, 66)

  val app2  = Array(I(c.addiu_op, 0, 3, 50123),
                   I(c.addiu_op, 0, 1, 1357),
                   R(c.rtype_op, 3, 1, 0, 0, c.multu_fn),
                   R(c.rtype_op, 0, 0, 3, 0, c.mflo_fn),
                   R(c.rtype_op, 3, 1, 0, 0, c.multu_fn),
                   R(c.rtype_op, 0, 0, 3, 0, c.mflo_fn),
                   R(c.rtype_op, 0, 0, 3, 0, c.mfhi_fn),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0, width=6)),
                   R(c.rtype_op, 0, 0, 0, 0, UInt(0, width=6)))
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app2.length) 
    wr(UInt(addr), app2(addr))
  boot()
  step(6)
  expect(c.io.out, 2104635011)
  step(1)
  expect(c.io.out, 21)

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

