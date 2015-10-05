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

  // pc next address
  val pcp4 = pc + UInt(4)
  
  /**
   * Define pipeline registers
   */
  val ifid_pcp4            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_inst            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_rsi             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_rti             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_rdi             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_j_addr          = Reg(UInt(x=0, width=C.WORDLENGTH))
  val ifid_reg_write       = Reg(Bool(false))
  val ifid_reg_dst         = Reg(Bool(false))
  val ifid_mem_toreg       = Reg(Bool(false))
  val ifid_mem_write       = Reg(Bool(false))
  val ifid_j_en            = Reg(Bool(false))
  val ifid_j_src           = Reg(Bool(false))
  val ifid_b_en            = Reg(Bool(false))

  val idex_pcp4            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_rs              = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_rt              = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_rsi             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_rti             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_rdi             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_sextimm         = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_inst            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_alu_src         = Reg(Bool(false))
  val idex_alu_op          = Reg(UInt(x=0, width=C.ALUCTLZ))
  val idex_shamt           = Reg(UInt(x=0, width=C.WORDLENGTH))
  val idex_reg_write       = Reg(Bool(false))
  val idex_reg_dst         = Reg(Bool(false))
  val idex_mem_toreg       = Reg(Bool(false))
  val idex_mem_write       = Reg(Bool(false))
  val idex_j_en            = Reg(Bool(false))
  val idex_j_src           = Reg(Bool(false))
  val idex_b_en            = Reg(Bool(false))

  val exmem_alu_out        = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_branch_addr    = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_pcp4           = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_addr           = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_sextimm        = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_inst           = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_reg_write      = Reg(Bool(false))
  val exmem_reg_dst        = Reg(Bool(false))
  val exmem_mem_toreg      = Reg(Bool(false))
  val exmem_mem_write      = Reg(Bool(false))
  val exmem_rt             = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_rti            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_rdi            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val exmem_b_en           = Reg(Bool(false))

  val memwb_reg_write      = Reg(Bool(false))
  val memwb_reg_dst        = Reg(Bool(false))
  val memwb_rti            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val memwb_rdi            = Reg(UInt(x=0, width=C.WORDLENGTH))
  val memwb_mem_toreg      = Reg(Bool(false))
  val memwb_dmem_out       = Reg(UInt(x=0, width=C.WORDLENGTH))
  val memwb_alu_out        = Reg(UInt(x=0, width=C.WORDLENGTH))
  val memwb_inst           = Reg(UInt(x=0, width=C.WORDLENGTH))

  /**
   * MEM/WB Stage
   */
  val dmem_out = dmem(exmem_alu_out)
  when (!io.isWr && !io.boot) {
    memwb_reg_write := exmem_reg_write
    memwb_reg_dst := exmem_reg_dst
    memwb_mem_toreg := exmem_mem_toreg
    memwb_dmem_out := dmem_out
    memwb_alu_out := exmem_alu_out
    memwb_rti := exmem_rti
    memwb_rdi := exmem_rdi
    memwb_inst := exmem_inst
    when (memwb_reg_write) {
      val writedata = Mux(memwb_mem_toreg, memwb_dmem_out, memwb_alu_out)
      val dstreg = Mux(memwb_reg_dst, memwb_rdi, memwb_rti)
      regfile(dstreg) := writedata
    }
  }

  /**
   * EX/MEM Stage
   */
  def exmem_op(o: UInt) = C.opi(idex_inst, o)
  def exmem_fop(o: UInt, f: UInt) = C.opi(idex_inst, o) && C.fopi(idex_inst, o, f)
  def aop(o: UInt) = idex_alu_op === o	// match alu op
  //val alu_in1 = idex_rs
  val memwb_rtype_forwardable = C.opi(memwb_inst, C.OP_RTYPE)
  val memwb_itype_forwardable = C.opi(memwb_inst, C.OP_ADDI) ||
    C.opi(memwb_inst, C.OP_ADDIU) ||
    C.opi(memwb_inst, C.OP_ORI) ||
    C.opi(memwb_inst, C.OP_LUI)
  val exmem_rtype_forwardable = C.opi(exmem_inst, C.OP_RTYPE)
  val exmem_itype_forwardable = C.opi(exmem_inst, C.OP_ADDI) ||
    C.opi(exmem_inst, C.OP_ADDIU) ||
    C.opi(exmem_inst, C.OP_ORI) ||
    C.opi(exmem_inst, C.OP_LUI)

  val alu_in1 = MuxCase(idex_rs, Array(
    (memwb_rtype_forwardable && idex_rsi === memwb_rdi) -> memwb_alu_out,
    (memwb_itype_forwardable && idex_rsi === memwb_rti) -> memwb_alu_out,
    (exmem_rtype_forwardable && idex_rsi === exmem_rdi) -> exmem_alu_out,
    (exmem_itype_forwardable && idex_rsi === exmem_rti) -> exmem_alu_out
    ))

  val alu_in2 = Mux(idex_alu_src, MuxCase(idex_rt, Array(
      (memwb_rtype_forwardable && idex_rti === memwb_rdi) -> memwb_alu_out,
      (memwb_itype_forwardable && idex_rti === memwb_rti) -> memwb_alu_out,
      (exmem_rtype_forwardable && idex_rti === exmem_rdi) -> exmem_alu_out,
      (exmem_itype_forwardable && idex_rti === exmem_rti) -> exmem_alu_out
      )), idex_sextimm)
  val alu_out = MuxCase(UInt(0), Array(
              aop(C.ALU_ADD) -> (alu_in1 + alu_in2).toUInt,
              aop(C.ALU_SUB) -> (alu_in1 - alu_in2).toUInt,
              aop(C.ALU_LUIB) -> (Cat(alu_in2(15, 0), UInt(0, width=16))).toUInt,
              aop(C.ALU_SLT) -> (Mux(idex_rs < idex_rt, UInt(1), UInt(0))).toUInt,
              aop(C.ALU_OR) -> (alu_in1 | alu_in2).toUInt,
              aop(C.ALU_SLL) -> (alu_in1 << idex_shamt).toUInt
              ))

  val zero = (alu_out === UInt(0))
  val is_branch_on_eq = exmem_op(C.OP_BEQ) 
  val b_en = exmem_op(C.OP_BNE) || exmem_op(C.OP_BEQ)
  val branch = b_en && ((~is_branch_on_eq && ~zero) || (is_branch_on_eq && zero))
  when (!io.isWr && !io.boot) {
    exmem_alu_out := alu_out
    exmem_pcp4 := idex_pcp4
    exmem_inst := idex_inst
    exmem_branch_addr := Mux(branch, (idex_pcp4 - UInt(4)) + (UInt(4) * Cat(Fill(16, idex_inst(15)), idex_inst(15,0))), idex_pcp4)
    exmem_reg_write := idex_reg_write
    exmem_reg_dst   := idex_reg_dst
    exmem_mem_toreg := idex_mem_toreg
    exmem_mem_write := idex_mem_write
    exmem_rt        := idex_rt
    exmem_rti       := idex_rti
    exmem_rdi       := idex_rdi
    exmem_b_en      := b_en
    when (exmem_mem_write) {
      dmem(exmem_alu_out) := exmem_rt
    }
  }

  /**
   * ID/EX Stage
   */
  def idex_op(o: UInt) = C.opi(ifid_inst, o)
  def idex_fop(o: UInt, f: UInt) = C.opi(ifid_inst, o) && C.fopi(ifid_inst, o, f)
  val alu_src = idex_op(C.OP_RTYPE) || idex_op(C.OP_BNE) || idex_op(C.OP_BEQ)
  val alu_op = MuxCase(UInt(0), Array(
                (idex_fop(C.OP_RTYPE, C.FUNC_ADDU) || 
                  idex_op(C.OP_ADDI) || idex_op(C.OP_ADDIU) || idex_op(C.OP_SW)
                  || idex_op(C.OP_LW)) -> (C.ALU_ADD),
                idex_fop(C.OP_RTYPE, C.FUNC_SUBU) -> (C.ALU_SUB),
                idex_fop(C.OP_RTYPE, C.FUNC_SLTU) -> (C.ALU_SLT),
                idex_fop(C.OP_RTYPE, C.FUNC_SLL) -> (C.ALU_SLL),
                (idex_op(C.OP_BNE) || idex_op(C.OP_BNE)) -> (C.ALU_SUB),
                (idex_fop(C.OP_RTYPE, C.FUNC_OR) || idex_op(C.OP_ORI)) -> (C.ALU_OR),
                idex_op(C.OP_LUI) -> (C.ALU_LUIB)))
  when (!io.isWr && !io.boot) {
    idex_sextimm := Cat(Fill(16, ifid_inst(15)), ifid_inst(15,0))
    idex_inst := ifid_inst
    idex_pcp4 := ifid_pcp4
    idex_rsi := ifid_rsi
    idex_rti := ifid_rti
    idex_rdi := ifid_rdi
    idex_rs := Mux(ifid_rsi === Bits(0), Bits(0), regfile(ifid_rsi))  // regfile(0) = 0
    idex_rt := Mux(ifid_rti === Bits(0), Bits(0), regfile(ifid_rti))
    idex_alu_src := alu_src
    idex_alu_op := alu_op
    idex_reg_write := ifid_reg_write
    idex_reg_dst   := ifid_reg_dst
    idex_mem_toreg := ifid_mem_toreg
    idex_mem_write := ifid_mem_write
    idex_j_en      := ifid_j_en
    idex_j_src     := ifid_j_src
    idex_b_en      := ifid_b_en
  }


  /**
   * IF/ID Stage
   */
  val inst = imem(pc(C.IADDRZ-1, 2))
  def ifid_op(o: UInt) = C.opi(inst, o)
  def ifid_fop(o: UInt, f: UInt) = C.opi(inst, o) && C.fopi(inst, o, f)
  val reg_write = (ifid_op(C.OP_RTYPE) || ifid_op(C.OP_ADDI) || ifid_op(C.OP_ADDIU)
                || ifid_op(C.OP_LW) || ifid_op(C.OP_ORI) || ifid_op(C.OP_LUI))
  val reg_dst = ifid_op(C.OP_RTYPE)
  val mem_toreg = ifid_op(C.OP_LW)
  val mem_write = ifid_op(C.OP_SW)

  val j_en = ifid_op(C.OP_J) || ifid_op(C.OP_JAL) || ifid_fop(C.OP_RTYPE, C.FUNC_JR)
  val j_src = Mux(ifid_op(C.OP_RTYPE), Bool(true), Bool(false))
  val ifid_rs = Mux(inst(25,21) === Bits(0), Bits(0), regfile(inst(25,21)))  // regfile(0) = 0
  when (!io.isWr && !io.boot) {
    ifid_pcp4      := pc + UInt(4)
    ifid_inst      := inst
    ifid_rsi       := inst(25,21)
    ifid_rti       := inst(20,16)
    ifid_rdi       := inst(15,11)
    ifid_reg_write := reg_write
    ifid_reg_dst   := reg_dst
    ifid_mem_toreg := mem_toreg
    ifid_mem_write := mem_write
    ifid_j_en      := j_en
    ifid_j_src     := j_src
    ifid_j_addr    := Mux(j_src, ifid_rs, UInt(4) * inst(25,0))
    ifid_b_en      := io.b_en
    when (ifid_op(C.OP_JAL)) {
      regfile(UInt(31)) := pc + UInt(4)
    }
  }


  // changes of state
  when (io.isWr) {
    imem(io.wrAddr) := io.wrData
  } .elsewhen (io.boot) {
    pc := UInt(C.STARTADDR)
  } .otherwise {
    when (ifid_j_en) {
      pc := ifid_j_addr
    }.elsewhen (exmem_b_en) {
      pc := exmem_branch_addr
    }.otherwise {
      pc := pcp4
    }
  }

  // io for the processor
  io.out := regfile(UInt(25))
  io.pc := pc
  io.inst := ifid_inst

  io.ra := regfile(UInt(31))
  io.sp := regfile(UInt(29))
  io.s0 := regfile(UInt(16))
  io.s1 := regfile(UInt(17))
  io.fp := regfile(UInt(30))
  io.t0 := regfile(UInt(8))
  io.t1 := regfile(UInt(9))
  io.t9 := regfile(UInt(25))
  io.v0 := regfile(UInt(2))
  io.v1 := regfile(UInt(3))
  io.a0 := regfile(UInt(4))
}
