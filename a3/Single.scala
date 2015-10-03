package MIPSlite

import Chisel._
import scala.collection.mutable.ArrayBuffer

class BaseTester(c: Core) extends Tester(c) {
  def wr(addr: UInt, data: UInt)  = {
    poke(c.io.isWr,   1)
    poke(c.io.wrAddr, addr.litValue())
    poke(c.io.wrData, data.litValue())
    step(1)
  }
  def boot()  = {
    poke(c.io.isWr, 0)
    poke(c.io.boot, 1)
    //peek(c.io.out)
    step(1)
    poke(c.io.isWr, 0)
    poke(c.io.boot, 0)
    //peek(c.io.out)
    step(1)
  }
  def tick()  = {
    peek(c.io.pc)
    peek(c.io.inst)

    peek(c.dp.memwb_reg_write)
    peek(c.dp.memwb_reg_dst)
    peek(c.dp.memwb_rti)
    peek(c.dp.memwb_rdi)
    peek(c.dp.memwb_mem_toreg)
    peek(c.dp.memwb_dmem_out)
    peek(c.dp.memwb_alu_out)
    peek(c.dp.exmem_alu_out)
    peek(c.dp.exmem_j_addr)
    peek(c.dp.exmem_branch_addr)
    peek(c.dp.exmem_pcp4)
    peek(c.dp.exmem_addr)
    peek(c.dp.exmem_sextimm)
    peek(c.dp.exmem_reg_write)
    peek(c.dp.exmem_reg_dst)
    peek(c.dp.exmem_mem_toreg)
    peek(c.dp.exmem_mem_write)
    peek(c.dp.exmem_rti)
    peek(c.dp.exmem_rdi)
    peek(c.dp.exmem_rt)
    peek(c.dp.idex_pcp4)
    peek(c.dp.idex_rs)
    peek(c.dp.idex_rt)
    //peek(c.dp.idex_rsi)
    peek(c.dp.idex_rti)
    peek(c.dp.idex_rdi)
    peek(c.dp.idex_sextimm)
    //peek(c.dp.idex_inst)
    peek(c.dp.idex_alu_src)
    peek(c.dp.idex_alu_op)
    peek(c.dp.idex_shamt)
    peek(c.dp.idex_reg_write)
    peek(c.dp.idex_reg_dst)
    peek(c.dp.idex_mem_toreg)
    peek(c.dp.idex_mem_write)
    peek(c.dp.ifid_pcp4)
    peek(c.dp.ifid_inst)
    peek(c.dp.ifid_rsi)
    peek(c.dp.ifid_rti)
    peek(c.dp.ifid_rdi)
    peek(c.dp.ifid_reg_write)
    peek(c.dp.ifid_reg_dst)
    peek(c.dp.ifid_mem_toreg)
    peek(c.dp.ifid_mem_write)
    step(1)
    peek(c.io.out)
  }
  def StrUInt(s: String) = UInt(BigInt(s, 16))
}

class SimplePipelineTest(c: Core) extends BaseTester(c) {  
  val app  = Array(
    /*0x00400000*/  StrUInt("2019001e"),  //addi $25,$0,0x0000001e1    addi $t9, $zero, 30
    /*0x00400004*/  StrUInt("20190019")  //addi $25,$0,0x000000192    addi $t9, $zero, 25
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  step(4)
  expect(c.io.out, 30)
  step(1)
  expect(c.io.out, 25)
}

/**
 * This tests that the processor breaks when we don't avoid hazards
 */
class BrokenHazardTest(c: Core) extends BaseTester(c) {  
  val app  = Array(
    /*0x00400000*/  StrUInt("2009001e"), //  addi $9,$0,0x0000001e 1    addi $t1, $zero, 30
    /*0x00400004*/  StrUInt("200a0019"), //  addi $10,$0,0x000000192    addi $t2, $zero, 25
    /*0x00400008*/  StrUInt("012ac821") //  addu $25,$9,$10        3    addu $t9, $t1, $t2
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  step(6)
  expect(c.io.out, 0)
}

class HandlesHazardTest(c: Core) extends BaseTester(c) {  
  val app  = Array(
    /*0x00400000*/  StrUInt("2009001e"), //  addi $9,$0,0x0000001e 1    addi $t1, $zero, 30
    /*0x00400004*/  StrUInt("200a0019"), //  addi $10,$0,0x000000192    addi $t2, $zero, 25
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    /*0x00400008*/  StrUInt("012ac821") //  addu $25,$9,$10        3    addu $t9, $t1, $t2
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  step(9)
  expect(c.io.out, 55)
}

class JumpTest(c: Core) extends BaseTester(c) {  
  val app  = Array(
    /*0x00400000*/  StrUInt("08100002"),  // j 0x00400008          1    j TEST_FN
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
    StrUInt("00000000"),
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
  step(3)
  expect(c.io.pc, 0x400008)
}

class BranchTest(c: Core) extends BaseTester(c) {
  val app = Array(
    /*0x00400000*/  StrUInt("112a0007"), //  beq $9,$10,0x00000003 1    beq $t1, $t2, L1
    /*0x00400004*/  StrUInt("00000000"), //  nop                   2    nop
    /*0x00400008*/  StrUInt("00000000"), //  nop                   3    nop
    /*0x0040000c*/  StrUInt("00000000"), //  nop                   4    nop
    /*0x00400010*/  StrUInt("00000000"), //  nop                   5    L1: nop
    StrUInt("00000000"),
    StrUInt("00000000"),
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
  step(3)
  expect(c.io.pc, 0x400020)
}

object MIPSlite {
  def main(args: Array[String]): Unit = {
    val tutArgs = args.slice(1, args.length)
    val res =
      args(0) match {
        case "Core" =>
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new SimplePipelineTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new BrokenHazardTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new HandlesHazardTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new JumpTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new BranchTest(c)}
      }
  }
}

