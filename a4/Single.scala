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

class SLTTest(c: Core) extends BaseTester(c) {
  val app = Array(
    /*[00400024]*/ StrUInt("2009000a"), //  addi $9, $0, 10          ; 1: addi $t1, $zero, 10 
    /*[00400028]*/ StrUInt("200a0005"), //  addi $10, $0, 5          ; 2: addi $t2, $zero, 5 
    /*[0040002c]*/ StrUInt("0149c82b"),  //  sltu $25, $10, $9        ; 3: sltu $t9, $t2, $t1
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
  step(6)
  peek(c.io.out)
  expect(c.io.out, 1)
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
    /*[00400048]*/ StrUInt("0c10001c"), //  jal 0x00400070 [fib]     ; 13: jal fib 
    /*[0040004c]*/ StrUInt("0002c821"), //  addu $25, $0, $2         ; 14: move $t9, $v0 
    /*[00400050]*/ StrUInt("22100001"), //  addi $16, $16, 1         ; 15: addi $s0, $s0, 1 
    /*[00400054]*/ StrUInt("0211402b"), //  sltu $8, $16, $17        ; 16: sltu $t0, $s0, $s1 
    /*[00400058]*/ StrUInt("1500fffa"), //  bne $8, $0, -24 [l1-0x00400058]; 17: bne $t0, $zero, l1 
    /*[0040005c]*/ StrUInt("00051021"), //  addu $2, $0, $5          ; 18: move $v0, $a1 
    /*[00400060]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 19: lw $ra, 20($sp) 
    /*[00400064]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 20: lw $fp, 16($sp) 
    /*[00400068]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 21: addu $sp, $sp, 32 
    /*[0040006c]*/ StrUInt("03e00008"), //  jr $31                   ; 22: jr $ra 
    /*[00400070]*/ StrUInt("27bdffe0"), //  addiu $29, $29, -32      ; 23: subu $sp, $sp, 32 
    /*[00400074]*/ StrUInt("afbf0014"), //  sw $31, 20($29)          ; 24: sw $ra, 20($sp) 
    /*[00400078]*/ StrUInt("afbe0010"), //  sw $30, 16($29)          ; 25: sw $fp, 16($sp) 
    /*[0040007c]*/ StrUInt("27be001c"), //  addiu $30, $29, 28       ; 26: addu $fp, $sp, 28 
    /*[00400080]*/ StrUInt("afc40000"), //  sw $4, 0($30)            ; 27: sw $a0, 0($fp) 
    /*[00400084]*/ StrUInt("8fc20000"), //  lw $2, 0($30)            ; 28: lw $v0, 0($fp) 
    /*[00400088]*/ StrUInt("00000000"), //  nop                      ; 29: nop 
    /*[0040008c]*/ StrUInt("14400003"), //  bne $2, $0, 12 [$L2-0x0040008c]; 30: bne $v0, $zero, $L2 
    /*[00400090]*/ StrUInt("34020000"), //  ori $2, $0, 0            ; 31: li $v0, 0 
    /*[00400094]*/ StrUInt("08100038"), //  j 0x004000e0 [$L1]       ; 32: j $L1 
    /*[00400098]*/ StrUInt("34080001"), //  ori $8, $0, 1            ; 33: li $t0, 1 
    /*[0040009c]*/ StrUInt("0102482b"), //  sltu $9, $8, $2          ; 34: sltu $t1, $t0, $v0 
    /*[004000a0]*/ StrUInt("15200002"), //  bne $9, $0, 8 [$L3-0x004000a0]; 35: bne $t1, $zero, $L3 
    /*[004000a4]*/ StrUInt("08100038"), //  j 0x004000e0 [$L1]       ; 36: j $L1 
    /*[004000a8]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 37: lw $v1, 0($fp) 
    /*[004000ac]*/ StrUInt("00000000"), //  nop                      ; 38: nop 
    /*[004000b0]*/ StrUInt("2462ffff"), //  addiu $2, $3, -1         ; 39: subu $v0, $v1, 1 
    /*[004000b4]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 40: move $a0, $v0 
    /*[004000b8]*/ StrUInt("0c10001c"), //  jal 0x00400070 [fib]     ; 41: jal fib 
    /*[004000bc]*/ StrUInt("afc20004"), //  sw $2, 4($30)            ; 42: sw $v0, 4($fp) 
    /*[004000c0]*/ StrUInt("8fc30000"), //  lw $3, 0($30)            ; 43: lw $v1, 0($fp) 
    /*[004000c4]*/ StrUInt("00000000"), //  nop                      ; 44: nop 
    /*[004000c8]*/ StrUInt("2462fffe"), //  addiu $2, $3, -2         ; 45: subu $v0, $v1, 2 
    /*[004000cc]*/ StrUInt("00022021"), //  addu $4, $0, $2          ; 46: move $a0, $v0 
    /*[004000d0]*/ StrUInt("0c10001c"), //  jal 0x00400070 [fib]     ; 47: jal fib 
    /*[004000d4]*/ StrUInt("8fc30004"), //  lw $3, 4($30)            ; 48: lw $v1, 4($fp) 
    /*[004000d8]*/ StrUInt("00000000"), //  nop                      ; 49: nop 
    /*[004000dc]*/ StrUInt("00431021"), //  addu $2, $2, $3          ; 50: addu $v0, $v0, $v1 
    /*[004000e0]*/ StrUInt("8fbf0014"), //  lw $31, 20($29)          ; 51: lw $ra, 20($sp) 
    /*[004000e4]*/ StrUInt("00000000"), //  nop                      ; 52: nop 
    /*[004000e8]*/ StrUInt("00000000"), //  nop                      ; 53: nop 
    /*[004000ec]*/ StrUInt("8fbe0010"), //  lw $30, 16($29)          ; 54: lw $fp, 16($sp) 
    /*[004000f0]*/ StrUInt("27bd0020"), //  addiu $29, $29, 32       ; 55: addu $sp, $sp, 32 
    /*[004000f4]*/ StrUInt("03e00008")  //  jr $31                   ; 56: jr $ra 
)
  wr(UInt(0), Bits(0)) // skip reset
  for (addr <- 0 until app.length) {
    wr(UInt(addr), app(addr))
  }
  boot()
  //for (i <- 0 until 100000000) {
    //step(1)
    //val pc = peek(c.io.pc)
    //if (pc == 0x00400020) {
      //throw new Exception("done")
    //}
    //peek(c.io.out)
  //}
  step(500000)
  expect(c.io.out, 0x63d)
  //step(3000)
  //expect(c.io.out, 5)
  //step(100000)
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
          //c => new HandlesHazardTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new JumpTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new BranchTest(c)}
          //chiselMainTest(tutArgs, () => Module(new Core())){
          //c => new ForwardingTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new SLTTest(c)}
          chiselMainTest(tutArgs, () => Module(new Core())){
          c => new FibTest(c)}
      }
  }
}

