package MIPSlite

import Chisel._

object C 
{
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
