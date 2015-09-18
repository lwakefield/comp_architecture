package MIPSlite

import Chisel._
import scala.collection.mutable.ArrayBuffer

object C {
  // start execution at this address
  val STARTADDR = 0x00400000

  // memory and datapath sizes
  val WORDLENGTH = 32	// datapath size
  val IADDRZ = 10	// number of bits for instruction memory 
  val DADDRZ = 10	// number of bits for data memory 

  // instruction opcodes
  val OP_RTYPE = UInt(0x00)
  val OP_J = UInt(0x02)
  val OP_JAL = UInt(0x03)
  val OP_ADDI = UInt(0x08)
  val OP_ADDIU = UInt(0x09)
  val OP_ORI = UInt(0x0d)
  val OP_LUI = UInt(0x0f)
  val OP_LW = UInt(0x23)
  val OP_SW = UInt(0x2b)
  val OP_BEQ = UInt(0x04)
  val OP_BNE = UInt(0x05)
  // function codes for R-type instructions
  val FUNC_ADDU = UInt(0x21)
  val FUNC_SUBU = UInt(0x23)
  val FUNC_SLTU = UInt(0x2b)
  val FUNC_OR = UInt(0x25)
  val FUNC_SLL = UInt(0x00)
  val FUNC_JR = UInt(0x08)

  // ALU control
  val ALUCTLZ = 4		// number of ALU control bits
  val ALU_ADD = UInt(0x02)
  val ALU_SUB = UInt(0x06)
  val ALU_SLT = UInt(0x07)
  val ALU_SLL = UInt(0x08)
  val ALU_OR = UInt(0x01)
  val ALU_AND = UInt(0x00)
  val ALU_NOR = UInt(0x0C)
  val ALU_LUIB = UInt(0x0D)	// output is Cat(B(15, 0), UInt(0, width=16))

  // Registers
  val REG_LR = 31

  // instruction decoding helper functions
  def opi(inst: UInt, o: UInt) = (inst(31,26) === o)
  def fopi(inst: UInt, o: UInt, f: UInt) 
        = (inst(31,26) === o && inst(5, 0) === f)
}

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

  // pc next address
  val pcp4 = pc + UInt(4)

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

    io.v0 := regfile(UInt(2))
    io.v1 := regfile(UInt(3))
    io.a0 := regfile(UInt(4))
    io.t0 := regfile(UInt(8))
    io.t0 := regfile(UInt(9))
    io.s0 := regfile(UInt(16))
    io.s1 := regfile(UInt(17))
    io.t9 := regfile(UInt(25))
    io.sp := regfile(UInt(29))
    io.fp := regfile(UInt(30))
    io.ra := regfile(UInt(31))
    io.zero := zero

  // io for the processor
  io.out := regfile(UInt(25))
  io.pc := pc
  io.inst := inst
}

class Single extends Module {
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
                    StrUInt("8fa40000"), //  lw $4, 0($29)
                    StrUInt("27a50004"), //  addiu $5, $29, 4
                    StrUInt("24a60004"), //  addiu $6, $5, 4
                    StrUInt("00041080"), //  sll $2, $4, 2
                    StrUInt("00c23021"), //  addu $6, $6, $2
                    StrUInt("0c100009"), //  jal 0x00400024 [main]
                    StrUInt("00000000"), //  nop
                    StrUInt("3402000a"), //  ori $2, $0, 10
                    StrUInt("0000000c"), //  syscall
/*main@0x00400024*/ StrUInt("3c01ffff"), //  lui $1, -1
                    StrUInt("343d2000"), //  ori $29, $1, 8192
                    StrUInt("27bdffe0"), //  addiu $29, $29, -32
                    StrUInt("afbf0014"), //  sw $31, 20($29)
                    StrUInt("afbe0010"), //  sw $30, 16($29)
                    StrUInt("27be001c"), //  addiu $30, $29, 28
                    StrUInt("34100000"), //  ori $16, $0, 0
/*l1@0x00400040*/   StrUInt("34110014"), //  ori $17, $0, 20
                    StrUInt("00102021"), //  addu $4, $0, $16
/*@0x00400048*/     StrUInt("0c10001c"), //  jal 0x00400070 [fib]
                    StrUInt("0002c821"), //  addu $25, $0, $2
                    StrUInt("22100001"), //  addi $16, $16, 1
                    StrUInt("0211402b"), //  slt $8, $16, $17
/*@0x00400058*/     StrUInt("1500fffa"), //  bne $8, $0, -24 [l1-0x00400058]
                    StrUInt("00051021"), //  addu $2, $0, $5
                    StrUInt("8fbf0014"), //  lw $31, 20($29)
                    StrUInt("8fbe0010"), //  lw $30, 16($29)
                    StrUInt("27bd0020"), //  addiu $29, $29, 32
                    StrUInt("03e00008"), //  jr $31
/*fib@0x00400070*/  StrUInt("27bdffe0"), //  addiu $29, $29, -32
                    StrUInt("afbf0014"), //  sw $31, 20($29)
                    StrUInt("afbe0010"), //  sw $30, 16($29)
                    StrUInt("27be001c"), //  addiu $30, $29, 28
                    StrUInt("afc40000"), //  sw $4, 0($30)
                    StrUInt("8fc20000"), //  lw $2, 0($30)
/*@0x00400088*/     StrUInt("14400003"), //  bne $2, $0, 12 [$L2-0x00400088]
                    StrUInt("34020000"), //  ori $2, $0, 0
                    StrUInt("08100034"), //  j 0x004000d0 [$L1]
/*l2@0x00400094*/   StrUInt("34080001"), //  ori $8, $0, 1
                    StrUInt("0102482b"), //  slt $9, $8, $2
                    StrUInt("15200002"), //  bne $9, $0, 8 [$L3-0x0040009c]
                    StrUInt("08100034"), //  j 0x004000d0 [$L1]
                    StrUInt("8fc30000"), //  lw $3, 0($30)
                    StrUInt("2462ffff"), //  addiu $2, $3, -1
                    StrUInt("00022021"), //  addu $4, $0, $2
                    StrUInt("0c10001c"), //  jal 0x00400070 [fib]
                    StrUInt("afc20004"), //  sw $2, 4($30)
                    StrUInt("8fc30000"), //  lw $3, 0($30)
                    StrUInt("2462fffe"), //  addiu $2, $3, -2
                    StrUInt("00022021"), //  addu $4, $0, $2
                    StrUInt("0c10001c"), //  jal 0x00400070 [fib]
                    StrUInt("8fc30004"), //  lw $3, 4($30)
                    StrUInt("00431021"), //  addu $2, $2, $3
/*L1@0x00400d0*/    StrUInt("8fbf0014"), //  lw $31, 20($29)
                    StrUInt("8fbe0010"), //  lw $30, 16($29)
                    StrUInt("27bd0020"), //  addiu $29, $29, 32
/*loop 0x0040004c?*/StrUInt("03e00008")) //  jr $31
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) 
    wr(UInt(addr), app(addr))
  boot()
  var k = 0
  do {
    tick(); k += 1
  } while (k < 500000)
  expect(c.io.out, 0xa18)
}

object MIPSlite {
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

