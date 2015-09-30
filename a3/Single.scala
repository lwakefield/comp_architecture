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
  def IR (op: Int, rsi: Int, rti: Int, rdi: Int, shamt: Int, func: Int) = 
    Cat(UInt(op, 6), UInt(rsi, 5), UInt(rti, 5), 
      UInt(rdi, 5), UInt(shamt, 5), UInt(func, 5))
  def II (op: Int, rsi: Int, rti: Int, imm: Int) = 
    Cat(UInt(op, 6), UInt(rsi, 5), UInt(rti, 5), UInt(imm, 16))
  def StrUInt(s: String) = UInt(BigInt(s, 16))
  val app  = Array(
    /*0x00400000*/  StrUInt("2019001e"),  //addi $25,$0,0x0000001e1    addi $t9, $zero, 30
    /*0x00400004*/  StrUInt("20190019")  //addi $25,$0,0x000000192    addi $t9, $zero, 25
  )
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < (app.length) * 5)
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

