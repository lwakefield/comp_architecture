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
    StrUInt("2009000e"),  // 0x00400000 addi $9,$0,0x0000000e  1    addi $t1, $zero, 14
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("200a0007"),  // 0x00400004 addi $10,$0,0x00000007 2    addi $t2, $zero, 7
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("200b0006"),  // 0x00400008 addi $11,$0,0x00000006 3    addi $t3, $zero, 6
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("012b5022"),  // 0x0040000c sub $10,$9,$11         4    sub  $t2, $t1, $t3
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("014d6024"),  // 0x00400010 and $12,$10,$13        5    and  $t4, $t2, $t5
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("01ca7825"),  // 0x00400014 or $15,$14,$10         6    or   $t7, $t6, $t2
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("014ac020"),  // 0x00400018 add $24,$10,$10        7    add  $t8, $t2, $t2
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000")
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < 7*5)
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

