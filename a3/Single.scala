package MIPSlite

import Chisel._
import scala.collection.mutable.ArrayBuffer

class SingleTests(c: Core) extends Tester(c) {  
  def wr(addr: UInt, data: UInt)  = {
    poke(c.io.isWr,   1)
    poke(c.io.wrAddr, addr.litValue())
    poke(c.io.wrData, data.litValue())
    step(1)
  }
  def boot()  = {
    poke(c.io.isWr, 0)
    poke(c.io.boot, 1)
    peek(c.io.out)
    step(1)
    poke(c.io.isWr, 0)
    poke(c.io.boot, 0)
    peek(c.io.out)
    step(1)
  }
  def tick()  = {
    step(1)
    peek(c.io.out)
  }
  def IR (op: Int, rsi: Int, rti: Int, rdi: Int, shamt: Int, func: Int) = 
    Cat(UInt(op, 6), UInt(rsi, 5), UInt(rti, 5), 
      UInt(rdi, 5), UInt(shamt, 5), UInt(func, 5))
  def II (op: Int, rsi: Int, rti: Int, imm: Int) = 
    Cat(UInt(op, 6), UInt(rsi, 5), UInt(rti, 5), UInt(imm, 16))
  def StrUInt(s: String) = UInt(BigInt(s, 16))
  val app  = Array(
    /*0x00400000*/  StrUInt("2009000e"),  // addi $9,$0,0x0000000e 1    addi $t1, $zero, 14
    /*0x00400004*/  StrUInt("200a0007"),  // addi $10,$0,0x000000072    addi $t2, $zero, 7
    /*0x00400008*/  StrUInt("200b0006"),  // addi $11,$0,0x000000063    addi $t3, $zero, 6
    /*0x0040000c*/  StrUInt("200d0009"),  // addi $13,$0,0x000000094    addi $t5, $zero, 9
    /*0x00400010*/  StrUInt("200e002a"),  // addi $14,$0,0x0000002a5    addi $t6, $zero, 42
    /*0x00400014*/  StrUInt("012b5023"),  // subu $10,$9,$11       6    subu $t2, $t1, $t3
    /*0x00400018*/  StrUInt("014d6021"),  // addu $12,$10,$13      7    addu $t4, $t2, $t5
    /*0x0040001c*/  StrUInt("01ca7823"),  // subu $15,$14,$10      8    subu $t7, $t6, $t2
    /*0x00400020*/  StrUInt("014ac021")   // addu $24,$10,$10      9    addu $t8, $t2, $t2
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < app.length)
}

object MIPSlite {
  def main(args: Array[String]): Unit = {
    val tutArgs = args.slice(1, args.length)
    val res =
      args(0) match {
        case "Core" =>
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new SingleTests(c)}
      }
  }
}

