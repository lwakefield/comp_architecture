package MIPSlite

import Chisel._
import scala.collection.mutable.ArrayBuffer

class Dpath extends Module {
  val io = new Bundle {
    val isWr   = Bool(INPUT)
    val wrAddr = UInt(INPUT, C.IADDRZ)
    val wrData = Bits(INPUT, C.WORDLENGTH)
    val boot   = Bool(INPUT)
    val out    = Bits(OUTPUT, C.WORDLENGTH)
    val pc    = UInt(OUTPUT, C.WORDLENGTH)
    val inst    = UInt(OUTPUT, C.WORDLENGTH)
    val alu_src = Bool(INPUT)
    val alu_op = UInt(INPUT, C.ALUCTLZ)
    val reg_write = Bool(INPUT)
    val reg_dst = Bool(INPUT)
    val mem_toreg = Bool(INPUT)
    val mem_write = Bool(INPUT)
    val zero = Bool(OUTPUT)

    val j_en = Bool(INPUT)
    val j_src = Bool(INPUT)
    val jal = Bool(INPUT)

    val b_en = Bool(INPUT)
    val is_branch_on_eq = Bool(INPUT)

    val ra = UInt(OUTPUT, C.WORDLENGTH)
    val sp = UInt(OUTPUT, C.WORDLENGTH)
    val s0 = UInt(OUTPUT, C.WORDLENGTH)
    val s1 = UInt(OUTPUT, C.WORDLENGTH)
    val fp = UInt(OUTPUT, C.WORDLENGTH)
    val t0 = UInt(OUTPUT, C.WORDLENGTH)
    val t1 = UInt(OUTPUT, C.WORDLENGTH)
    val t9 = UInt(OUTPUT, C.WORDLENGTH)
    val v0 = UInt(OUTPUT, C.WORDLENGTH)
    val v1 = UInt(OUTPUT, C.WORDLENGTH)
    val a0 = UInt(OUTPUT, C.WORDLENGTH)

  }
  val regfile = Mem(Bits(width = C.WORDLENGTH), C.WORDLENGTH)
  val imem = Mem(Bits(width = C.WORDLENGTH), 1 << C.IADDRZ)
  val dmem = Mem(Bits(width = C.WORDLENGTH), 1 << C.DADDRZ)
  val pc   = Reg(init=UInt(C.STARTADDR, C.WORDLENGTH))
  def mop(o: UInt) = C.opi(io.inst, o)	// match op
  def aop(o: UInt) = io.alu_op === o	// match alu op

  // pc next address
  val pcp4 = pc + UInt(4)

  // instruction decoding
  val inst = imem(pc(C.IADDRZ-1, 2))
  val rsi  = inst(25,21)
  val rti  = inst(20,16)
  val rdi  = inst(15,11)
  val shamt  = inst(10,6)
  val func  = inst(5,0)
  val imm  = inst(15,0)
  val addr = inst(25, 0)
  val rs = Mux(rsi === Bits(0), Bits(0), regfile(rsi))	// regfile(0) = 0
  val rt = Mux(rti === Bits(0), Bits(0), regfile(rti))

  // IF/ID Stage
  val ifid_pcp4 = Reg(UInt(width=C.WORDLENGTH))
  val ifid_inst = Reg(UInt(width=C.WORDLENGTH))
  ifid_pcp4 := pcp4
  ifid_inst := inst

  // immediate values extracted from instruction
  val sextimm = Cat(Fill(16, imm(15)), imm)

  // alu
  val alu_in1 = rs
  val alu_in2 = Mux(io.alu_src, rt, sextimm)
  val alu_out = MuxCase(UInt(0), Array(
              aop(C.ALU_ADD) -> (alu_in1 + alu_in2).toUInt,
              aop(C.ALU_SUB) -> (alu_in1 - alu_in2).toUInt,
              aop(C.ALU_LUIB) -> (Cat(alu_in2(15, 0), UInt(0, width=16))).toUInt,
              aop(C.ALU_SLT) -> (Mux(rs < rt, UInt(1), UInt(0))).toUInt,
              aop(C.ALU_OR) -> (alu_in1 | alu_in2).toUInt,
              aop(C.ALU_SLL) -> (alu_in1 << shamt).toUInt
              ))
  val zero = (alu_out === UInt(0))

  // data memory
  val dmem_out = dmem(alu_out)


  val j_addr = Mux(io.j_src, rs, UInt(4) * addr)

  val branch = io.b_en && ((~io.is_branch_on_eq && ~io.zero) || (io.is_branch_on_eq && ~io.zero))
  val branch_addr = Mux(branch, pc + (UInt(4) * sextimm), pcp4)

  // changes of state
  when (io.isWr) {
    imem(io.wrAddr) := io.wrData
  } .elsewhen (io.boot) {
    pc := UInt(C.STARTADDR)
  } .otherwise {
    when (io.reg_write) {
      val writedata = Mux(io.mem_toreg, dmem_out, alu_out)
      val dstreg = Mux(io.reg_dst, rdi, rti)
      regfile(dstreg) := writedata
    }
    when (io.mem_write) {
      dmem(alu_out) := rt
    }
    when (io.jal) {
      regfile(UInt(31)) := pcp4
    }
    when (io.j_en) {
      pc := j_addr
    }.elsewhen (io.b_en) {
      pc := branch_addr
    }.otherwise {
      pc := pcp4
    }
  }

  // io for the processor
  io.out := regfile(UInt(24))
  io.pc := pc
  io.inst := inst
}
