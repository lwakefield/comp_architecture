package MIPSlite

import Chisel._
import scala.collection.mutable.ArrayBuffer

class Core extends Module {
  val io = new Bundle {
    val isWr   = Bool(INPUT)
    val wrAddr = UInt(INPUT, C.IADDRZ)
    val wrData = Bits(INPUT, C.WORDLENGTH)
    val boot   = Bool(INPUT)
    val out    = Bits(OUTPUT, C.WORDLENGTH)
    val pc    = UInt(OUTPUT, C.WORDLENGTH)
    val inst    = UInt(OUTPUT, C.WORDLENGTH)

    val j_en = Bool(OUTPUT)
    val j_src = Bool(OUTPUT)
    val jal = Bool(OUTPUT)

    val b_en = Bool(OUTPUT)
    val is_branch_on_eq = Bool(OUTPUT)

    val j_addr = UInt(OUTPUT, C.WORDLENGTH - 6)
  }
  def op(o: UInt) = C.opi(io.inst, o)
  def fop(o: UInt, f: UInt) = C.opi(io.inst, o) && C.fopi(io.inst, o, f)

  // instantiate datapath and do instruction decoding 
  val dp = Module(new Dpath)
  dp.io.inst <> io.inst
  val zero = dp.io.zero

  // control logic
  val alu_src = op(C.OP_RTYPE) || op(C.OP_BNE) || op(C.OP_BEQ)
  val alu_op = MuxCase(UInt(0), Array(
                (fop(C.OP_RTYPE, C.FUNC_ADDU) || 
                  op(C.OP_ADDI) || op(C.OP_ADDIU) || op(C.OP_SW)
                  || op(C.OP_LW)) -> (C.ALU_ADD),
                fop(C.OP_RTYPE, C.FUNC_SUBU) -> (C.ALU_SUB),
                fop(C.OP_RTYPE, C.FUNC_SLTU) -> (C.ALU_SLT),
                fop(C.OP_RTYPE, C.FUNC_SLL) -> (C.ALU_SLL),
                (op(C.OP_BNE) || op(C.OP_BNE)) -> (C.ALU_SUB),
                (fop(C.OP_RTYPE, C.FUNC_OR) || op(C.OP_ORI)) -> (C.ALU_OR),
                op(C.OP_LUI) -> (C.ALU_LUIB)))
  val reg_write = (op(C.OP_RTYPE) || op(C.OP_ADDI) || op(C.OP_ADDIU)
                || op(C.OP_LW) || op(C.OP_ORI) || op(C.OP_LUI))
  val reg_dst = op(C.OP_RTYPE)
  val mem_toreg = op(C.OP_LW)
  val mem_write = op(C.OP_SW)

  val j_en = op(C.OP_J) || op(C.OP_JAL) || fop(C.OP_RTYPE, C.FUNC_JR)
  val j_src = Mux(op(C.OP_RTYPE), UInt(1), UInt(0))
  val jal = op(C.OP_JAL)

  val b_en = op(C.OP_BNE) || op(C.OP_BEQ)
  val is_branch_on_eq = op(C.OP_BEQ)

  // wire up the datapath 
  dp.io.isWr <> io.isWr
  dp.io.wrAddr <> io.wrAddr
  dp.io.wrData <> io.wrData
  dp.io.boot <> io.boot
  dp.io.out <> io.out
  dp.io.pc <> io.pc
  dp.io.alu_src := alu_src
  dp.io.alu_op := alu_op
  dp.io.reg_write := reg_write
  dp.io.reg_dst := reg_dst
  dp.io.mem_toreg := mem_toreg
  dp.io.mem_write := mem_write

  dp.io.j_en := j_en
  dp.io.j_src := j_src
  dp.io.jal := jal

  dp.io.b_en := b_en
  dp.io.is_branch_on_eq := is_branch_on_eq

  io.j_en := j_en
  io.j_src := j_src
  io.jal := jal
  io.j_addr := io.inst(25,0)

  io.b_en := b_en
  io.is_branch_on_eq := is_branch_on_eq
}
