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
    /*[00400000]*/ StrUInt("8fa40000"), // lw $4, 0($29)            ; 183: lw $a0 0($sp) # argc 
    /*[00400004]*/ StrUInt("27a50004"), // addiu $5, $29, 4         ; 184: addiu $a1 $sp 4 # argv 
    /*[00400008]*/ StrUInt("24a60004"), // addiu $6, $5, 4          ; 185: addiu $a2 $a1 4 # envp 
    /*[0040000c]*/ StrUInt("00041080"), // sll $2, $4, 2            ; 186: sll $v0 $a0 2 
    /*[00400010]*/ StrUInt("00c23021"), // addu $6, $6, $2          ; 187: addu $a2 $a2 $v0 
    /*[00400014]*/ StrUInt("0c100009"), // jal 0x00400024 [main]    ; 188: jal main 
    /*[00400018]*/ StrUInt("00000000"), // nop                      ; 189: nop 
    /*[0040001c]*/ StrUInt("3402000a"), // ori $2, $0, 10           ; 191: li $v0 10 
    /*[00400020]*/ StrUInt("0000000c"), // syscall                  ; 192: syscall # syscall 10 (exit) 
    /*[00400024]*/ StrUInt("00000000"), // nop                      ; 4: nop 
    /*[00400028]*/ StrUInt("00000000"), // nop                      ; 5: nop 
    /*[0040002c]*/ StrUInt("27bdffe0"), // addiu $29, $29, -32      ; 6: subu $sp, $sp, 32 
    /*[00400030]*/ StrUInt("afbf0014"), // sw $31, 20($29)          ; 7: sw $ra, 20($sp) 
    /*[00400034]*/ StrUInt("afbe0010"), // sw $30, 16($29)          ; 8: sw $fp, 16($sp) 
    /*[00400038]*/ StrUInt("27be001c"), // addiu $30, $29, 28       ; 9: addu $fp, $sp, 28 
    /*[0040003c]*/ StrUInt("34100000"), // ori $16, $0, 0           ; 10: li $s0, 0 
    /*[00400040]*/ StrUInt("34110014"), // ori $17, $0, 20          ; 11: li $s1, 20 
    /*[00400044]*/ StrUInt("00102021"), // addu $4, $0, $16         ; 12: move $a0, $s0 
    /*[00400048]*/ StrUInt("0c100021"), // jal 0x00400084 [fib]     ; 13: jal fib 
    /*[0040004c]*/ StrUInt("00000000"), // nop                      ; 14: nop 
    /*[00400050]*/ StrUInt("0002c821"), // addu $25, $0, $2         ; 15: move $t9, $v0 
    /*[00400054]*/ StrUInt("22100001"), // addi $16, $16, 1         ; 16: addi $s0, $s0, 1 
    /*[00400058]*/ StrUInt("0211402b"), // sltu $8, $16, $17        ; 17: sltu $t0, $s0, $s1 
    /*[0040005c]*/ StrUInt("1500fff9"), // bne $8, $0, -28 [l1-0x0040005c]; 18: bne $t0, $zero, l1 
    /*[00400060]*/ StrUInt("00000000"), // nop                      ; 19: nop 
    /*[00400064]*/ StrUInt("00000000"), // nop                      ; 20: nop 
    /*[00400068]*/ StrUInt("00000000"), // nop                      ; 21: nop 
    /*[0040006c]*/ StrUInt("00051021"), // addu $2, $0, $5          ; 22: move $v0, $a1 
    /*[00400070]*/ StrUInt("8fbf0014"), // lw $31, 20($29)          ; 23: lw $ra, 20($sp) 
    /*[00400074]*/ StrUInt("8fbe0010"), // lw $30, 16($29)          ; 24: lw $fp, 16($sp) 
    /*[00400078]*/ StrUInt("27bd0020"), // addiu $29, $29, 32       ; 25: addu $sp, $sp, 32 
    /*[0040007c]*/ StrUInt("03e00008"), // jr $31                   ; 26: jr $ra 
    /*[00400080]*/ StrUInt("00000000"), // nop                      ; 27: nop 
    /*[00400084]*/ StrUInt("27bdffe0"), // addiu $29, $29, -32      ; 28: subu $sp, $sp, 32 
    /*[00400088]*/ StrUInt("afbf0014"), // sw $31, 20($29)          ; 29: sw $ra, 20($sp) 
    /*[0040008c]*/ StrUInt("afbe0010"), // sw $30, 16($29)          ; 30: sw $fp, 16($sp) 
    /*[00400090]*/ StrUInt("27be001c"), // addiu $30, $29, 28       ; 31: addu $fp, $sp, 28 
    /*[00400094]*/ StrUInt("afc40000"), // sw $4, 0($30)            ; 32: sw $a0, 0($fp) 
    /*[00400098]*/ StrUInt("8fc20000"), // lw $2, 0($30)            ; 33: lw $v0, 0($fp) 
    /*[0040009c]*/ StrUInt("00000000"), // nop                      ; 34: nop 
    /*[004000a0]*/ StrUInt("00000000"), // nop                      ; 35: nop 
    /*[004000a4]*/ StrUInt("00000000"), // nop                      ; 36: nop 
    /*[004000a8]*/ StrUInt("14400007"), // bne $2, $0, 28 [$L2-0x004000a8]; 37: bne $v0, $zero, $L2 
    /*[004000ac]*/ StrUInt("00000000"), // nop                      ; 38: nop 
    /*[004000b0]*/ StrUInt("00000000"), // nop                      ; 39: nop 
    /*[004000b4]*/ StrUInt("00000000"), // nop                      ; 40: nop 
    /*[004000b8]*/ StrUInt("34020000"), // ori $2, $0, 0            ; 41: li $v0, 0 
    /*[004000bc]*/ StrUInt("08100051"), // j 0x00400144 [$L1]       ; 42: j $L1 
    /*[004000c0]*/ StrUInt("00000000"), // nop                      ; 43: nop 
    /*[004000c4]*/ StrUInt("34080001"), // ori $8, $0, 1            ; 44: li $t0, 1 
    /*[004000c8]*/ StrUInt("0102482b"), // sltu $9, $8, $2          ; 45: sltu $t1, $t0, $v0 
    /*[004000cc]*/ StrUInt("15200006"), // bne $9, $0, 24 [$L3-0x004000cc]; 46: bne $t1, $zero, $L3 
    /*[004000d0]*/ StrUInt("00000000"), // nop                      ; 47: nop 
    /*[004000d4]*/ StrUInt("00000000"), // nop                      ; 48: nop 
    /*[004000d8]*/ StrUInt("00000000"), // nop                      ; 49: nop 
    /*[004000dc]*/ StrUInt("08100051"), // j 0x00400144 [$L1]       ; 50: j $L1 
    /*[004000e0]*/ StrUInt("00000000"), // nop                      ; 51: nop 
    /*[004000e4]*/ StrUInt("8fc30000"), // lw $3, 0($30)            ; 52: lw $v1, 0($fp) 
    /*[004000e8]*/ StrUInt("00000000"), // nop                      ; 53: nop 
    /*[004000ec]*/ StrUInt("00000000"), // nop                      ; 54: nop 
    /*[004000f0]*/ StrUInt("00000000"), // nop                      ; 55: nop 
    /*[004000f4]*/ StrUInt("00000000"), // nop                      ; 56: nop 
    /*[004000f8]*/ StrUInt("2462ffff"), // addiu $2, $3, -1         ; 57: subu $v0, $v1, 1 
    /*[004000fc]*/ StrUInt("00022021"), // addu $4, $0, $2          ; 58: move $a0, $v0 
    /*[00400100]*/ StrUInt("0c100021"), // jal 0x00400084 [fib]     ; 59: jal fib 
    /*[00400104]*/ StrUInt("00000000"), // nop                      ; 60: nop 
    /*[00400108]*/ StrUInt("afc20004"), // sw $2, 4($30)            ; 61: sw $v0, 4($fp) 
    /*[0040010c]*/ StrUInt("8fc30000"), // lw $3, 0($30)            ; 62: lw $v1, 0($fp) 
    /*[00400110]*/ StrUInt("00000000"), // nop                      ; 63: nop 
    /*[00400114]*/ StrUInt("00000000"), // nop                      ; 64: nop 
    /*[00400118]*/ StrUInt("00000000"), // nop                      ; 65: nop 
    /*[0040011c]*/ StrUInt("00000000"), // nop                      ; 66: nop 
    /*[00400120]*/ StrUInt("2462fffe"), // addiu $2, $3, -2         ; 67: subu $v0, $v1, 2 
    /*[00400124]*/ StrUInt("00022021"), // addu $4, $0, $2          ; 68: move $a0, $v0 
    /*[00400128]*/ StrUInt("0c100021"), // jal 0x00400084 [fib]     ; 69: jal fib 
    /*[0040012c]*/ StrUInt("00000000"), // nop                      ; 70: nop 
    /*[00400130]*/ StrUInt("8fc30004"), // lw $3, 4($30)            ; 71: lw $v1, 4($fp) 
    /*[00400134]*/ StrUInt("00000000"), // nop                      ; 72: nop 
    /*[00400138]*/ StrUInt("00000000"), // nop                      ; 73: nop 
    /*[0040013c]*/ StrUInt("00000000"), // nop                      ; 74: nop 
    /*[00400140]*/ StrUInt("00431021"), // addu $2, $2, $3          ; 75: addu $v0, $v0, $v1 
    /*[00400144]*/ StrUInt("8fbf0014"), // lw $31, 20($29)          ; 76: lw $ra, 20($sp) 
    /*[00400148]*/ StrUInt("00000000"), // nop                      ; 77: nop 
    /*[0040014c]*/ StrUInt("00000000"), // nop                      ; 78: nop 
    /*[00400150]*/ StrUInt("8fbe0010"), // lw $30, 16($29)          ; 79: lw $fp, 16($sp) 
    /*[00400154]*/ StrUInt("27bd0020"), // addiu $29, $29, 32       ; 80: addu $sp, $sp, 32 
    /*[00400158]*/ StrUInt("03e00008"), // jr $31                   ; 81: jr $ra 
    /*[0040015c]*/ StrUInt("00000000")  // nop                      ; 82: nop 

)
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  for (i <- 0 until 500000) {
    step(1)
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

