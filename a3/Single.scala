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

class FibTest(c: Core) extends BaseTester(c) {
  val app = Array(
    /*[00400000]*/ StrUInt("8fa40000"), //  lw $4, 0($29)            ; 183: lw $a0 0($sp) # argc 
/*[00400004]*/ StrUInt("27a50004"), //  addiu $5, $29, 4         ; 184: addiu $a1 $sp 4 # argv 
/*[00400008]*/ StrUInt("24a60004"), //  addiu $6, $5, 4          ; 185: addiu $a2 $a1 4 # envp 
/*[0040000c]*/ StrUInt("00041080"), //  sll $2, $4, 2            ; 186: sll $v0 $a0 2 
/*[00400010]*/ StrUInt("00c23021"), //  addu $6, $6, $2          ; 187: addu $a2 $a2 $v0 
/*[00400014]*/ StrUInt("0c100009"), //  jal 0x00400024 [main]    ; 188: jal main 
/*[00400018]*/ StrUInt("00000000"), //  nop                      ; 189: nop 
/*[0040001c]*/ StrUInt("3402000a"), //  ori $2, $0, 10           ; 191: li $v0 10 
/*[00400020]*/ StrUInt("0000000c"), //  syscall                  ; 192: syscall # syscall 10 (exit) 
/*[00400024]*/ StrUInt("00000000"), //  nop                      ; 5: nop # subu $sp, $zero, 0xE000 # hack to initialise SP 
/*[00400028]*/ StrUInt("00000000"), //  nop                      ; 6: nop 
/*[0040002c]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 7: subu $sp, $sp, 32 # pseudo: actual: addi 
/*[00400030]*/ StrUInt("00000000"), //  nop                      ; 8: nop 
/*[00400034]*/ StrUInt("00000000"), //  nop                      ; 9: nop 
/*[00400038]*/ StrUInt("00000000"), //  nop                      ; 10: nop 
/*[0040003c]*/ StrUInt("00000000"), //  nop                      ; 11: nop 
/*[00400040]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 13: sw $ra, 20($sp) # save return address 
/*[00400044]*/ StrUInt("00000000"), //  nop                      ; 14: nop 
/*[00400048]*/ StrUInt("00000000"), //  nop                      ; 15: nop 
/*[0040004c]*/ StrUInt("00000000"), //  nop                      ; 16: nop 
/*[00400050]*/ StrUInt("00000000"), //  nop                      ; 17: nop 
/*[00400054]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 18: sw $fp, 16($sp) # save old frame pointer 
/*[00400058]*/ StrUInt("00000000"), //  nop                      ; 19: nop 
/*[0040005c]*/ StrUInt("00000000"), //  nop                      ; 20: nop 
/*[00400060]*/ StrUInt("00000000"), //  nop                      ; 21: nop 
/*[00400064]*/ StrUInt("00000000"), //  nop                      ; 22: nop 
/*[00400068]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 23: addu $fp, $sp, 28 # set up new frame pointer 
/*[0040006c]*/ StrUInt("00000000"), //  nop                      ; 24: nop 
/*[00400070]*/ StrUInt("00000000"), //  nop                      ; 25: nop 
/*[00400074]*/ StrUInt("00000000"), //  nop                      ; 26: nop 
/*[00400078]*/ StrUInt("00000000"), //  nop                      ; 27: nop 
/*[0040007c]*/ StrUInt("34100000"), //  ori $16, $0, 0           ; 28: li $s0, 0 # i is counter 
/*[00400080]*/ StrUInt("00000000"), //  nop                      ; 29: nop 
/*[00400084]*/ StrUInt("00000000"), //  nop                      ; 30: nop 
/*[00400088]*/ StrUInt("00000000"), //  nop                      ; 31: nop 
/*[0040008c]*/ StrUInt("00000000"), //  nop                      ; 32: nop 
/*[00400090]*/ StrUInt("34110014"), //  ori $17, $0, 20          ; 33: li $s1, 20 # up to this number 
/*[00400094]*/ StrUInt("00000000"), //  nop                      ; 34: nop 
/*[00400098]*/ StrUInt("00000000"), //  nop                      ; 35: nop 
/*[0040009c]*/ StrUInt("00000000"), //  nop                      ; 36: nop 
/*[004000a0]*/ StrUInt("00000000"), //  nop                      ; 37: nop 
/*[004000a4]*/ StrUInt("00102021"), //  addu $4, $0, $16         ; 38: move $a0, $s0 # counter 
/*[004000a8]*/ StrUInt("00000000"), //  nop                      ; 39: nop 
/*[004000ac]*/ StrUInt("00000000"), //  nop                      ; 40: nop 
/*[004000b0]*/ StrUInt("00000000"), //  nop                      ; 41: nop 
/*[004000b4]*/ StrUInt("00000000"), //  nop                      ; 42: nop 
/*[004000b8]*/ StrUInt("0c100060"), //  jal 0x00400180 [fib]     ; 43: jal fib # call fib function 
/*[004000bc]*/ StrUInt("00000000"), //  nop                      ; 44: nop 
/*[004000c0]*/ StrUInt("00000000"), //  nop                      ; 45: nop 
/*[004000c4]*/ StrUInt("00000000"), //  nop                      ; 46: nop 
/*[004000c8]*/ StrUInt("00000000"), //  nop                      ; 47: nop 
/*[004000cc]*/ StrUInt("0002c821"), //  addu $25, $0, $2         ; 48: move $t9, $v0 # move result to $t9 
/*[004000d0]*/ StrUInt("00000000"), //  nop                      ; 49: nop 
/*[004000d4]*/ StrUInt("00000000"), //  nop                      ; 50: nop 
/*[004000d8]*/ StrUInt("00000000"), //  nop                      ; 51: nop 
/*[004000dc]*/ StrUInt("00000000"), //  nop                      ; 52: nop 
/*[004000e0]*/ StrUInt("22100001"), //  addi $16, $16, 1         ; 53: addi $s0, $s0, 1 # increment s0 
/*[004000e4]*/ StrUInt("00000000"), //  nop                      ; 54: nop 
/*[004000e8]*/ StrUInt("00000000"), //  nop                      ; 55: nop 
/*[004000ec]*/ StrUInt("00000000"), //  nop                      ; 56: nop 
/*[004000f0]*/ StrUInt("00000000"), //  nop                      ; 57: nop 
/*[004000f4]*/ StrUInt("0211402b"), //  sltu $8, $16, $17        ; 58: sltu $t0, $s0, $s1 
/*[004000f8]*/ StrUInt("00000000"), //  nop                      ; 59: nop 
/*[004000fc]*/ StrUInt("00000000"), //  nop                      ; 60: nop 
/*[00400100]*/ StrUInt("00000000"), //  nop                      ; 61: nop 
/*[00400104]*/ StrUInt("00000000"), //  nop                      ; 62: nop 
/*[00400108]*/ StrUInt("1500ffe2"), //  bne $8, $0, -120 [l1-0x00400108]
/*[0040010c]*/ StrUInt("00000000"), //  nop                      ; 64: nop 
/*[00400110]*/ StrUInt("00000000"), //  nop                      ; 65: nop 
/*[00400114]*/ StrUInt("00000000"), //  nop                      ; 66: nop 
/*[00400118]*/ StrUInt("00000000"), //  nop                      ; 67: nop 
/*[0040011c]*/ StrUInt("00051021"), //  addu $2, $0, $5          ; 68: move $v0, $a1 # restore result to result register (SPIM) 
/*[00400120]*/ StrUInt("00000000"), //  nop                      ; 69: nop 
/*[00400124]*/ StrUInt("00000000"), //  nop                      ; 70: nop 
/*[00400128]*/ StrUInt("00000000"), //  nop                      ; 71: nop 
/*[0040012c]*/ StrUInt("00000000"), //  nop                      ; 72: nop 
/*[00400130]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 73: lw $ra, 20($sp) # restore return address 
/*[00400134]*/ StrUInt("00000000"), //  nop                      ; 74: nop 
/*[00400138]*/ StrUInt("00000000"), //  nop                      ; 75: nop 
/*[0040013c]*/ StrUInt("00000000"), //  nop                      ; 76: nop 
/*[00400140]*/ StrUInt("00000000"), //  nop                      ; 77: nop 
/*[00400144]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 78: lw $fp, 16($sp) # restore frame pointer 
/*[00400148]*/ StrUInt("00000000"), //  nop                      ; 79: nop 
/*[0040014c]*/ StrUInt("00000000"), //  nop                      ; 80: nop 
/*[00400150]*/ StrUInt("00000000"), //  nop                      ; 81: nop 
/*[00400154]*/ StrUInt("00000000"), //  nop                      ; 82: nop 
/*[00400158]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 83: addu $sp, $sp, 32 # pop stack frame 
/*[0040015c]*/ StrUInt("00000000"), //  nop                      ; 84: nop 
/*[00400160]*/ StrUInt("00000000"), //  nop                      ; 85: nop 
/*[00400164]*/ StrUInt("00000000"), //  nop                      ; 86: nop 
/*[00400168]*/ StrUInt("00000000"), //  nop                      ; 87: nop 
/*[0040016c]*/ StrUInt("03e00008"), //  jr $31                   ; 88: jr $ra # return to caller 
/*[00400170]*/ StrUInt("00000000"), //  nop                      ; 89: nop 
/*[00400174]*/ StrUInt("00000000"), //  nop                      ; 90: nop 
/*[00400178]*/ StrUInt("00000000"), //  nop                      ; 91: nop 
/*[0040017c]*/ StrUInt("00000000"), //  nop                      ; 92: nop 
/*[00400180]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 95: subu $sp, $sp, 32 # stack frame is 32 bytes 
/*[00400184]*/ StrUInt("00000000"), //  nop                      ; 96: nop 
/*[00400188]*/ StrUInt("00000000"), //  nop                      ; 97: nop 
/*[0040018c]*/ StrUInt("00000000"), //  nop                      ; 98: nop 
/*[00400190]*/ StrUInt("00000000"), //  nop                      ; 99: nop 
/*[00400194]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 100: sw $ra, 20($sp) # save return address 
/*[00400198]*/ StrUInt("00000000"), //  nop                      ; 101: nop 
/*[0040019c]*/ StrUInt("00000000"), //  nop                      ; 102: nop 
/*[004001a0]*/ StrUInt("00000000"), //  nop                      ; 103: nop 
/*[004001a4]*/ StrUInt("00000000"), //  nop                      ; 104: nop 
/*[004001a8]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 105: sw $fp, 16($sp) # save frame pointer 
/*[004001ac]*/ StrUInt("00000000"), //  nop                      ; 106: nop 
/*[004001b0]*/ StrUInt("00000000"), //  nop                      ; 107: nop 
/*[004001b4]*/ StrUInt("00000000"), //  nop                      ; 108: nop 
/*[004001b8]*/ StrUInt("00000000"), //  nop                      ; 109: nop 
/*[004001bc]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 110: addu $fp, $sp, 28 # set up frame pointer 
/*[004001c0]*/ StrUInt("00000000"), //  nop                      ; 111: nop 
/*[004001c4]*/ StrUInt("00000000"), //  nop                      ; 112: nop 
/*[004001c8]*/ StrUInt("00000000"), //  nop                      ; 113: nop 
/*[004001cc]*/ StrUInt("00000000"), //  nop                      ; 114: nop 
/*[004001d0]*/ StrUInt("afc40000"), //  sw $4, 0($30)            ; 115: sw $a0, 0($fp) # save argument (n) 
/*[004001d4]*/ StrUInt("00000000"), //  nop                      ; 116: nop 
/*[004001d8]*/ StrUInt("00000000"), //  nop                      ; 117: nop 
/*[004001dc]*/ StrUInt("00000000"), //  nop                      ; 118: nop 
/*[004001e0]*/ StrUInt("00000000"), //  nop                      ; 119: nop 
/*[004001e4]*/ StrUInt("8fc20000"), //  lw $2, 0($30)            ; 120: lw $v0, 0($fp) # load n 
/*[004001e8]*/ StrUInt("00000000"), //  nop                      ; 121: nop 
/*[004001ec]*/ StrUInt("00000000"), //  nop                      ; 122: nop 
/*[004001f0]*/ StrUInt("00000000"), //  nop                      ; 123: nop 
/*[004001f4]*/ StrUInt("00000000"), //  nop                      ; 124: nop 
/*[004001f8]*/ StrUInt("1440000f"), //  bne $2, $0, 60 [$L2-0x004001f8]; 125: bne $v0, $zero, $L2 # branch if n > 0 
/*[004001fc]*/ StrUInt("00000000"), //  nop                      ; 126: nop 
/*[00400200]*/ StrUInt("00000000"), //  nop                      ; 127: nop 
/*[00400204]*/ StrUInt("00000000"), //  nop                      ; 128: nop 
/*[00400208]*/ StrUInt("00000000"), //  nop                      ; 129: nop 
/*[0040020c]*/ StrUInt("34020000"), //  ori $2, $0, 0            ; 130: li $v0, 0 # if n=0 return 0 
/*[00400210]*/ StrUInt("00000000"), //  nop                      ; 131: nop 
/*[00400214]*/ StrUInt("00000000"), //  nop                      ; 132: nop 
/*[00400218]*/ StrUInt("00000000"), //  nop                      ; 133: nop 
/*[0040021c]*/ StrUInt("00000000"), //  nop                      ; 134: nop 
/*[00400220]*/ StrUInt("081000d8"), //  j 0x00400360 [$L1]       ; 135: j $L1 # and jump to code to return 
/*[00400224]*/ StrUInt("00000000"), //  nop                      ; 136: nop 
/*[00400228]*/ StrUInt("00000000"), //  nop                      ; 137: nop 
/*[0040022c]*/ StrUInt("00000000"), //  nop                      ; 138: nop 
/*[00400230]*/ StrUInt("00000000"), //  nop                      ; 139: nop 
/*[00400234]*/ StrUInt("34080001"), //  ori $8, $0, 1            ; 141: li $t0, 1 
/*[00400238]*/ StrUInt("00000000"), //  nop                      ; 142: nop 
/*[0040023c]*/ StrUInt("00000000"), //  nop                      ; 143: nop 
/*[00400240]*/ StrUInt("00000000"), //  nop                      ; 144: nop 
/*[00400244]*/ StrUInt("00000000"), //  nop                      ; 145: nop 
/*[00400248]*/ StrUInt("0102482b"), //  sltu $9, $8, $2          ; 147: sltu $t1, $t0, $v0 
/*[0040024c]*/ StrUInt("00000000"), //  nop                      ; 148: nop 
/*[00400250]*/ StrUInt("00000000"), //  nop                      ; 149: nop 
/*[00400254]*/ StrUInt("00000000"), //  nop                      ; 150: nop 
/*[00400258]*/ StrUInt("00000000"), //  nop                      ; 151: nop 
/*[0040025c]*/ StrUInt("1520000a"), //  bne $9, $0, 40 [$L3-0x0040025c]; 152: bne $t1, $zero, $L3 
/*[00400260]*/ StrUInt("00000000"), //  nop                      ; 153: nop 
/*[00400264]*/ StrUInt("00000000"), //  nop                      ; 154: nop 
/*[00400268]*/ StrUInt("00000000"), //  nop                      ; 155: nop 
/*[0040026c]*/ StrUInt("00000000"), //  nop                      ; 156: nop 
/*[00400270]*/ StrUInt("081000d8"), //  j 0x00400360 [$L1]       ; 157: j $L1 # $v0 was 1 so we can return 
/*[00400274]*/ StrUInt("00000000"), //  nop                      ; 158: nop 
/*[00400278]*/ StrUInt("00000000"), //  nop                      ; 159: nop 
/*[0040027c]*/ StrUInt("00000000"), //  nop                      ; 160: nop 
/*[00400280]*/ StrUInt("00000000"), //  nop                      ; 161: nop 
/*[00400284]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 163: lw $v1, 0($fp) # load n 
/*[00400288]*/ StrUInt("00000000"), //  nop                      ; 164: nop 
/*[0040028c]*/ StrUInt("00000000"), //  nop                      ; 165: nop 
/*[00400290]*/ StrUInt("00000000"), //  nop                      ; 166: nop 
/*[00400294]*/ StrUInt("00000000"), //  nop                      ; 167: nop 
/*[00400298]*/ StrUInt("2462ffff"), //  addiu $2, $3, -1         ; 168: subu $v0, $v1, 1 # compute n-1 
/*[0040029c]*/ StrUInt("00000000"), //  nop                      ; 169: nop 
/*[004002a0]*/ StrUInt("00000000"), //  nop                      ; 170: nop 
/*[004002a4]*/ StrUInt("00000000"), //  nop                      ; 171: nop 
/*[004002a8]*/ StrUInt("00000000"), //  nop                      ; 172: nop 
/*[004002ac]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 173: move $a0, $v0 # move to argument register 
/*[004002b0]*/ StrUInt("00000000"), //  nop                      ; 174: nop 
/*[004002b4]*/ StrUInt("00000000"), //  nop                      ; 175: nop 
/*[004002b8]*/ StrUInt("00000000"), //  nop                      ; 176: nop 
/*[004002bc]*/ StrUInt("00000000"), //  nop                      ; 177: nop 
/*[004002c0]*/ StrUInt("0c100060"), //  jal 0x00400180 [fib]     ; 178: jal fib # call fib function result in $v0 
/*[004002c4]*/ StrUInt("00000000"), //  nop                      ; 179: nop 
/*[004002c8]*/ StrUInt("00000000"), //  nop                      ; 180: nop 
/*[004002cc]*/ StrUInt("00000000"), //  nop                      ; 181: nop 
/*[004002d0]*/ StrUInt("00000000"), //  nop                      ; 182: nop 
/*[004002d4]*/ StrUInt("afc20004"), //  sw $2, 4($30)            ; 183: sw $v0, 4($fp) # fib(n-1) is in 4($fp) 
/*[004002d8]*/ StrUInt("00000000"), //  nop                      ; 184: nop 
/*[004002dc]*/ StrUInt("00000000"), //  nop                      ; 185: nop 
/*[004002e0]*/ StrUInt("00000000"), //  nop                      ; 186: nop 
/*[004002e4]*/ StrUInt("00000000"), //  nop                      ; 187: nop 
/*[004002e8]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 189: lw $v1, 0($fp) # load n 
/*[004002ec]*/ StrUInt("00000000"), //  nop                      ; 190: nop 
/*[004002f0]*/ StrUInt("00000000"), //  nop                      ; 191: nop 
/*[004002f4]*/ StrUInt("00000000"), //  nop                      ; 192: nop 
/*[004002f8]*/ StrUInt("00000000"), //  nop                      ; 193: nop 
/*[004002fc]*/ StrUInt("2462fffe"), //  addiu $2, $3, -2         ; 194: subu $v0, $v1, 2 # compute n-2 
/*[00400300]*/ StrUInt("00000000"), //  nop                      ; 195: nop 
/*[00400304]*/ StrUInt("00000000"), //  nop                      ; 196: nop 
/*[00400308]*/ StrUInt("00000000"), //  nop                      ; 197: nop 
/*[0040030c]*/ StrUInt("00000000"), //  nop                      ; 198: nop 
/*[00400310]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 199: move $a0, $v0 # move to argument register 
/*[00400314]*/ StrUInt("00000000"), //  nop                      ; 200: nop 
/*[00400318]*/ StrUInt("00000000"), //  nop                      ; 201: nop 
/*[0040031c]*/ StrUInt("00000000"), //  nop                      ; 202: nop 
/*[00400320]*/ StrUInt("00000000"), //  nop                      ; 203: nop 
/*[00400324]*/ StrUInt("0c100060"), //  jal 0x00400180 [fib]     ; 204: jal fib # call fib function result in $v0 
/*[00400328]*/ StrUInt("00000000"), //  nop                      ; 205: nop 
/*[0040032c]*/ StrUInt("00000000"), //  nop                      ; 206: nop 
/*[00400330]*/ StrUInt("00000000"), //  nop                      ; 207: nop 
/*[00400334]*/ StrUInt("00000000"), //  nop                      ; 208: nop 
/*[00400338]*/ StrUInt("8fc30004"), //  lw $3, 4($30)            ; 210: lw $v1, 4($fp) 
/*[0040033c]*/ StrUInt("00000000"), //  nop                      ; 211: nop 
/*[00400340]*/ StrUInt("00000000"), //  nop                      ; 212: nop 
/*[00400344]*/ StrUInt("00000000"), //  nop                      ; 213: nop 
/*[00400348]*/ StrUInt("00000000"), //  nop                      ; 214: nop 
/*[0040034c]*/ StrUInt("00431021"), //  addu $2, $2, $3          ; 215: addu $v0, $v0, $v1 
/*[00400350]*/ StrUInt("00000000"), //  nop                      ; 216: nop 
/*[00400354]*/ StrUInt("00000000"), //  nop                      ; 217: nop 
/*[00400358]*/ StrUInt("00000000"), //  nop                      ; 218: nop 
/*[0040035c]*/ StrUInt("00000000"), //  nop                      ; 219: nop 
/*[00400360]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 222: lw $ra, 20($sp) # restore return address 
/*[00400364]*/ StrUInt("00000000"), //  nop                      ; 223: nop 
/*[00400368]*/ StrUInt("00000000"), //  nop                      ; 224: nop 
/*[0040036c]*/ StrUInt("00000000"), //  nop                      ; 225: nop 
/*[00400370]*/ StrUInt("00000000"), //  nop                      ; 226: nop 
/*[00400374]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 227: lw $fp, 16($sp) # restore frame pointer 
/*[00400378]*/ StrUInt("00000000"), //  nop                      ; 228: nop 
/*[0040037c]*/ StrUInt("00000000"), //  nop                      ; 229: nop 
/*[00400380]*/ StrUInt("00000000"), //  nop                      ; 230: nop 
/*[00400384]*/ StrUInt("00000000"), //  nop                      ; 231: nop 
/*[00400388]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 232: addu $sp, $sp, 32 # pop stack 
/*[0040038c]*/ StrUInt("00000000"), //  nop                      ; 233: nop 
/*[00400390]*/ StrUInt("00000000"), //  nop                      ; 234: nop 
/*[00400394]*/ StrUInt("00000000"), //  nop                      ; 235: nop 
/*[00400398]*/ StrUInt("00000000"), //  nop                      ; 236: nop 
/*[0040039c]*/ StrUInt("03e00008"), //  jr $31                   ; 237: jr $ra # return to caller 
/*[004003a0]*/ StrUInt("00000000"), //  nop                      ; 238: nop 
/*[004003a4]*/ StrUInt("00000000"), //  nop                      ; 239: nop 
/*[004003a8]*/ StrUInt("00000000"), //  nop                      ; 240: nop 
/*[004003ac]*/ StrUInt("00000000") //  nop                      ; 241: nop 
)
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  for (i <- 0 until 100000) {
  step(1)
    peek(c.io.out)
  }
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
          // chiselMainTest(tutArgs, () => Module(new Core())){
          // c => new BranchTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new FibTest(c)}
      }
  }
}

