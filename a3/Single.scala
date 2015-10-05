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
  step(1)
  expect(c.io.pc, 0x400008)
}

class BranchTest(c: Core) extends BaseTester(c) {
  val app = Array(
    /*0x00400000*/  StrUInt("112a0007"), //  beq $9,$10,0x00000007 1    beq $t1, $t2, L1
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
  expect(c.io.pc, 0x40001c)
}

/**
 * The sequence looks like this: 
 * ['0x0', '0x1', '0x1', '0x2', '0x3', '0x5', '0x8', '0xd', '0x15', '0x22', '0x37', '0x59', '0x90', '0xe9', '0x179', '0x262', '0x3db', '0x63d', '0xa18', '0x1055', '0x1a6d', '0x2ac2']
 */
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
    /*[00400024]*/ StrUInt("00000000"), //  nop                      ; 4: nop 
    /*[00400028]*/ StrUInt("00000000"), //  nop                      ; 5: nop 
    /*[0040002c]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 6: subu $sp, $sp, 32 
    /*[00400030]*/ StrUInt("00000000"), //  nop                      ; 7: nop 
    /*[00400034]*/ StrUInt("00000000"), //  nop                      ; 8: nop 
    /*[00400038]*/ StrUInt("00000000"), //  nop                      ; 9: nop 
    /*[0040003c]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 10: sw $ra, 20($sp) 
    /*[00400040]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 11: sw $fp, 16($sp) 
    /*[00400044]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 12: addu $fp, $sp, 28 
    /*[00400048]*/ StrUInt("34100000"), //  ori $16, $0, 0           ; 13: li $s0, 0 
    /*[0040004c]*/ StrUInt("00000000"), //  nop                      ; 14: nop 
    /*[00400050]*/ StrUInt("00000000"), //  nop                      ; 15: nop 
    /*[00400054]*/ StrUInt("00000000"), //  nop                      ; 16: nop 
    /*[00400058]*/ StrUInt("34110014"), //  ori $17, $0, 20          ; 17: li $s1, 20 
    /*[0040005c]*/ StrUInt("00102021"), //  addu $4, $0, $16         ; 18: move $a0, $s0 
    /*[00400060]*/ StrUInt("0c10002d"), //  jal 0x004000b4 [fib]     ; 19: jal fib 
    /*[00400064]*/ StrUInt("00000000"), //  nop                      ; 20: nop 
    /*[00400068]*/ StrUInt("0002c821"), //  addu $25, $0, $2         ; 21: move $t9, $v0 
    /*[0040006c]*/ StrUInt("22100001"), //  addi $16, $16, 1         ; 22: addi $s0, $s0, 1 
    /*[00400070]*/ StrUInt("00000000"), //  nop                      ; 23: nop 
    /*[00400074]*/ StrUInt("00000000"), //  nop                      ; 24: nop 
    /*[00400078]*/ StrUInt("00000000"), //  nop                      ; 25: nop 
    /*[0040007c]*/ StrUInt("0211402b"), //  sltu $8, $16, $17        ; 26: sltu $t0, $s0, $s1 
    /*[00400080]*/ StrUInt("00000000"), //  nop                      ; 27: nop 
    /*[00400084]*/ StrUInt("00000000"), //  nop                      ; 28: nop 
    /*[00400088]*/ StrUInt("00000000"), //  nop                      ; 29: nop 
    /*[0040008c]*/ StrUInt("1500fff3"), //  bne $8, $0, -52 [l1-0x0040008c]; 30: bne $t0, $zero, l1 
    /*[00400090]*/ StrUInt("00000000"), //  nop                      ; 31: nop 
    /*[00400094]*/ StrUInt("00000000"), //  nop                      ; 32: nop 
    /*[00400098]*/ StrUInt("00000000"), //  nop                      ; 33: nop 
    /*[0040009c]*/ StrUInt("00051021"), //  addu $2, $0, $5          ; 34: move $v0, $a1 
    /*[004000a0]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 35: lw $ra, 20($sp) 
    /*[004000a4]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 36: lw $fp, 16($sp) 
    /*[004000a8]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 37: addu $sp, $sp, 32 
    /*[004000ac]*/ StrUInt("03e00008"), //  jr $31                   ; 38: jr $ra 
    /*[004000b0]*/ StrUInt("00000000"), //  nop                      ; 39: nop 
    /*[004000b4]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 40: subu $sp, $sp, 32 
    /*[004000b8]*/ StrUInt("00000000"), //  nop                      ; 41: nop 
    /*[004000bc]*/ StrUInt("00000000"), //  nop                      ; 42: nop 
    /*[004000c0]*/ StrUInt("00000000"), //  nop                      ; 43: nop 
    /*[004000c4]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 44: sw $ra, 20($sp) 
    /*[004000c8]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 45: sw $fp, 16($sp) 
    /*[004000cc]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 46: addu $fp, $sp, 28 
    /*[004000d0]*/ StrUInt("00000000"), //  nop                      ; 47: nop 
    /*[004000d4]*/ StrUInt("00000000"), //  nop                      ; 48: nop 
    /*[004000d8]*/ StrUInt("00000000"), //  nop                      ; 49: nop 
    /*[004000dc]*/ StrUInt("afc40000"), //  sw $4, 0($30)            ; 50: sw $a0, 0($fp) 
    /*[004000e0]*/ StrUInt("8fc20000"), //  lw $2, 0($30)            ; 51: lw $v0, 0($fp) 
    /*[004000e4]*/ StrUInt("00000000"), //  nop                      ; 52: nop 
    /*[004000e8]*/ StrUInt("00000000"), //  nop                      ; 53: nop 
    /*[004000ec]*/ StrUInt("00000000"), //  nop                      ; 54: nop 
    /*[004000f0]*/ StrUInt("14400007"), //  bne $2, $0, 28 [$L2-0x004000f0]; 55: bne $v0, $zero, $L2 
    /*[004000f4]*/ StrUInt("00000000"), //  nop                      ; 56: nop 
    /*[004000f8]*/ StrUInt("00000000"), //  nop                      ; 57: nop 
    /*[004000fc]*/ StrUInt("00000000"), //  nop                      ; 58: nop 
    /*[00400100]*/ StrUInt("34020000"), //  ori $2, $0, 0            ; 59: li $v0, 0 
    /*[00400104]*/ StrUInt("0810006f"), //  j 0x004001bc [$L1]       ; 60: j $L1 
    /*[00400108]*/ StrUInt("00000000"), //  nop                      ; 61: nop 
    /*[0040010c]*/ StrUInt("34080001"), //  ori $8, $0, 1            ; 62: li $t0, 1 
    /*[00400110]*/ StrUInt("00000000"), //  nop                      ; 63: nop 
    /*[00400114]*/ StrUInt("00000000"), //  nop                      ; 64: nop 
    /*[00400118]*/ StrUInt("00000000"), //  nop                      ; 65: nop 
    /*[0040011c]*/ StrUInt("0102482b"), //  sltu $9, $8, $2          ; 66: sltu $t1, $t0, $v0 
    /*[00400120]*/ StrUInt("00000000"), //  nop                      ; 67: nop 
    /*[00400124]*/ StrUInt("00000000"), //  nop                      ; 68: nop 
    /*[00400128]*/ StrUInt("00000000"), //  nop                      ; 69: nop 
    /*[0040012c]*/ StrUInt("15200006"), //  bne $9, $0, 24 [$L3-0x0040012c]; 70: bne $t1, $zero, $L3 
    /*[00400130]*/ StrUInt("00000000"), //  nop                      ; 71: nop 
    /*[00400134]*/ StrUInt("00000000"), //  nop                      ; 72: nop 
    /*[00400138]*/ StrUInt("00000000"), //  nop                      ; 73: nop 
    /*[0040013c]*/ StrUInt("0810006f"), //  j 0x004001bc [$L1]       ; 74: j $L1 
    /*[00400140]*/ StrUInt("00000000"), //  nop                      ; 75: nop 
    /*[00400144]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 76: lw $v1, 0($fp) 
    /*[00400148]*/ StrUInt("00000000"), //  nop                      ; 77: nop 
    /*[0040014c]*/ StrUInt("00000000"), //  nop                      ; 78: nop 
    /*[00400150]*/ StrUInt("00000000"), //  nop                      ; 79: nop 
    /*[00400154]*/ StrUInt("00000000"), //  nop                      ; 80: nop 
    /*[00400158]*/ StrUInt("2462ffff"), //  addiu $2, $3, -1         ; 81: subu $v0, $v1, 1 
    /*[0040015c]*/ StrUInt("00000000"), //  nop                      ; 82: nop 
    /*[00400160]*/ StrUInt("00000000"), //  nop                      ; 83: nop 
    /*[00400164]*/ StrUInt("00000000"), //  nop                      ; 84: nop 
    /*[00400168]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 85: move $a0, $v0 
    /*[0040016c]*/ StrUInt("0c10002d"), //  jal 0x004000b4 [fib]     ; 86: jal fib 
    /*[00400170]*/ StrUInt("00000000"), //  nop                      ; 87: nop 
    /*[00400174]*/ StrUInt("afc20004"), //  sw $2, 4($30)            ; 88: sw $v0, 4($fp) 
    /*[00400178]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 89: lw $v1, 0($fp) 
    /*[0040017c]*/ StrUInt("00000000"), //  nop                      ; 90: nop 
    /*[00400180]*/ StrUInt("00000000"), //  nop                      ; 91: nop 
    /*[00400184]*/ StrUInt("00000000"), //  nop                      ; 92: nop 
    /*[00400188]*/ StrUInt("00000000"), //  nop                      ; 93: nop 
    /*[0040018c]*/ StrUInt("2462fffe"), //  addiu $2, $3, -2         ; 94: subu $v0, $v1, 2 
    /*[00400190]*/ StrUInt("00000000"), //  nop                      ; 95: nop 
    /*[00400194]*/ StrUInt("00000000"), //  nop                      ; 96: nop 
    /*[00400198]*/ StrUInt("00000000"), //  nop                      ; 97: nop 
    /*[0040019c]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 98: move $a0, $v0 
    /*[004001a0]*/ StrUInt("0c10002d"), //  jal 0x004000b4 [fib]     ; 99: jal fib 
    /*[004001a4]*/ StrUInt("00000000"), //  nop                      ; 100: nop 
    /*[004001a8]*/ StrUInt("8fc30004"), //  lw $3, 4($30)            ; 101: lw $v1, 4($fp) 
    /*[004001ac]*/ StrUInt("00000000"), //  nop                      ; 102: nop 
    /*[004001b0]*/ StrUInt("00000000"), //  nop                      ; 103: nop 
    /*[004001b4]*/ StrUInt("00000000"), //  nop                      ; 104: nop 
    /*[004001b8]*/ StrUInt("00431021"), //  addu $2, $2, $3          ; 105: addu $v0, $v0, $v1 
    /*[004001bc]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 106: lw $ra, 20($sp) 
    /*[004001c0]*/ StrUInt("00000000"), //  nop                      ; 107: nop 
    /*[004001c4]*/ StrUInt("00000000"), //  nop                      ; 108: nop 
    /*[004001c8]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 109: lw $fp, 16($sp) 
    /*[004001cc]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 110: addu $sp, $sp, 32 
    /*[004001d0]*/ StrUInt("03e00008"), //  jr $31                   ; 111: jr $ra 
    /*[004001d4]*/ StrUInt("00000000")  //  nop                      ; 112: nop 
)
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  //for (i <- 0 until 500000) {
    //step(1)
    //peek(c.io.out)
  //}
  //step(500000)
  //expect(c.io.out, 0x3db)
  step(3000)
  expect(c.io.out, 5)
}

object MIPSlite {
  def main(args: Array[String]): Unit = {
    val tutArgs = args.slice(1, args.length)
    val res =
      args(0) match {
        case "Core" =>
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new SimplePipelineTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new BrokenHazardTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new HandlesHazardTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new JumpTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new BranchTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new FibTest(c)}
      }
  }
}

