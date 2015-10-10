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
    /*[00400030]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 7: sw $ra, 20($sp) 
    /*[00400034]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 8: sw $fp, 16($sp) 
    /*[00400038]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 9: addu $fp, $sp, 28 
    /*[0040003c]*/ StrUInt("34100000"), //  ori $16, $0, 0           ; 10: li $s0, 0 
    /*[00400040]*/ StrUInt("34110014"), //  ori $17, $0, 20          ; 11: li $s1, 20 
    /*[00400044]*/ StrUInt("00102021"), //  addu $4, $0, $16         ; 12: move $a0, $s0 
    /*[00400048]*/ StrUInt("0c10001d"), //  jal 0x00400074 [fib]     ; 13: jal fib 
    /*[0040004c]*/ StrUInt("0002c821"), //  addu $25, $0, $2         ; 14: move $t9, $v0 
    /*[00400050]*/ StrUInt("22100001"), //  addi $16, $16, 1         ; 15: addi $s0, $s0, 1 
    /*[00400054]*/ StrUInt("0211402b"), //  sltu $8, $16, $17        ; 16: sltu $t0, $s0, $s1 
    /*[00400058]*/ StrUInt("1500fffa"), //  bne $8, $0, -24 [l1-0x00400058]; 17: bne $t0, $zero, l1 
    /*[0040005c]*/ StrUInt("00000000"), //  nop                      ; 18: nop 
    /*[00400060]*/ StrUInt("00051021"), //  addu $2, $0, $5          ; 19: move $v0, $a1 
    /*[00400064]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 20: lw $ra, 20($sp) 
    /*[00400068]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 21: lw $fp, 16($sp) 
    /*[0040006c]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 22: addu $sp, $sp, 32 
    /*[00400070]*/ StrUInt("03e00008"), //  jr $31                   ; 23: jr $ra 
    /*[00400074]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 24: subu $sp, $sp, 32 
    /*[00400078]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 25: sw $ra, 20($sp) 
    /*[0040007c]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 26: sw $fp, 16($sp) 
    /*[00400080]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 27: addu $fp, $sp, 28 
    /*[00400084]*/ StrUInt("afc40000"), //  sw $4, 0($30)            ; 28: sw $a0, 0($fp) 
    /*[00400088]*/ StrUInt("8fc20000"), //  lw $2, 0($30)            ; 29: lw $v0, 0($fp) 
    /*[0040008c]*/ StrUInt("00000000"), //  nop                      ; 30: nop 
    /*[00400090]*/ StrUInt("00000000"), //  nop                      ; 31: nop 
    /*[00400094]*/ StrUInt("00000000"), //  nop                      ; 32: nop 
    /*[00400098]*/ StrUInt("14400004"), //  bne $2, $0, 16 [$L2-0x00400098]; 33: bne $v0, $zero, $L2 
    /*[0040009c]*/ StrUInt("00000000"), //  nop                      ; 34: nop 
    /*[004000a0]*/ StrUInt("34020000"), //  ori $2, $0, 0            ; 35: li $v0, 0 
    /*[004000a4]*/ StrUInt("08100045"), //  j 0x00400114 [$L1]       ; 36: j $L1 
    /*[004000a8]*/ StrUInt("34080001"), //  ori $8, $0, 1            ; 37: li $t0, 1 
    /*[004000ac]*/ StrUInt("0102482b"), //  sltu $9, $8, $2          ; 38: sltu $t1, $t0, $v0 
    /*[004000b0]*/ StrUInt("15200003"), //  bne $9, $0, 12 [$L3-0x004000b0]; 39: bne $t1, $zero, $L3 
    /*[004000b4]*/ StrUInt("00000000"), //  nop                      ; 40: nop 
    /*[004000b8]*/ StrUInt("08100045"), //  j 0x00400114 [$L1]       ; 41: j $L1 
    /*[004000bc]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 42: lw $v1, 0($fp) 
    /*[004000c0]*/ StrUInt("00000000"), //  nop                      ; 43: nop 
    /*[004000c4]*/ StrUInt("00000000"), //  nop                      ; 44: nop 
    /*[004000c8]*/ StrUInt("00000000"), //  nop                      ; 45: nop 
    /*[004000cc]*/ StrUInt("00000000"), //  nop                      ; 46: nop 
    /*[004000d0]*/ StrUInt("2462ffff"), //  addiu $2, $3, -1         ; 47: subu $v0, $v1, 1 
    /*[004000d4]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 48: move $a0, $v0 
    /*[004000d8]*/ StrUInt("0c10001d"), //  jal 0x00400074 [fib]     ; 49: jal fib 
    /*[004000dc]*/ StrUInt("afc20004"), //  sw $2, 4($30)            ; 50: sw $v0, 4($fp) 
    /*[004000e0]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 51: lw $v1, 0($fp) 
    /*[004000e4]*/ StrUInt("00000000"), //  nop                      ; 52: nop 
    /*[004000e8]*/ StrUInt("00000000"), //  nop                      ; 53: nop 
    /*[004000ec]*/ StrUInt("00000000"), //  nop                      ; 54: nop 
    /*[004000f0]*/ StrUInt("00000000"), //  nop                      ; 55: nop 
    /*[004000f4]*/ StrUInt("2462fffe"), //  addiu $2, $3, -2         ; 56: subu $v0, $v1, 2 
    /*[004000f8]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 57: move $a0, $v0 
    /*[004000fc]*/ StrUInt("0c10001d"), //  jal 0x00400074 [fib]     ; 58: jal fib 
    /*[00400100]*/ StrUInt("8fc30004"), //  lw $3, 4($30)            ; 59: lw $v1, 4($fp) 
    /*[00400104]*/ StrUInt("00000000"), //  nop                      ; 60: nop 
    /*[00400108]*/ StrUInt("00000000"), //  nop                      ; 61: nop 
    /*[0040010c]*/ StrUInt("00000000"), //  nop                      ; 62: nop 
    /*[00400110]*/ StrUInt("00431021"), //  addu $2, $2, $3          ; 63: addu $v0, $v0, $v1 
    /*[00400114]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 64: lw $ra, 20($sp) 
    /*[00400118]*/ StrUInt("00000000"), //  nop                      ; 65: nop 
    /*[0040011c]*/ StrUInt("00000000"), //  nop                      ; 66: nop 
    /*[00400120]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 67: lw $fp, 16($sp) 
    /*[00400124]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 68: addu $sp, $sp, 32 
    /*[00400128]*/ StrUInt("03e00008")  //  jr $31                   ; 69: jr $ra 
)
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  for (i <- 0 until 500000) {
    step(1)
    peek(c.io.pc)
    peek(c.io.out)
  }
  //step(500000)
  //expect(c.io.out, 0x3db)
  //step(3000)
  //expect(c.io.out, 5)
  step(100000)
}

class ForwardingTest(c: Core) extends BaseTester(c) {
  val app = Array(
    StrUInt("240904d2"), // addiu $t1, $zero, 1234
    StrUInt("0129c821"), // addu  $t9, $t1, $t1
    StrUInt("0139c821"), // addu $t9, $t1, $t9
    //StrUInt("0004c880"),t("00c23021"),// addu $6, $6, $2          ; 187: addu $a2 $a2 $v0 
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
  step(5)
  expect(c.io.out, 2468)
  step(1)
  expect(c.io.out, 3702)
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
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new BranchTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new FibTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new ForwardingTest(c)}
      }
  }
}

