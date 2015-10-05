# Patterson and Hennessy, 2nd ed. modified for single cycle processor in Chisel.
		.text
		.globl	main
main: 	nop
		nop
		subu	$sp, $sp, 32
		nop
		nop
		nop
		sw		$ra, 20($sp)
		sw		$fp, 16($sp)
		addu	$fp, $sp, 28
		li		$s0, 0
		nop
		nop
		nop
l1:		li		$s1, 20
		move	$a0, $s0
		jal		fib
		nop
		nop
		nop
		move	$t9, $v0
		addi	$s0, $s0, 1
		nop
		nop
		nop
		sltu	$t0, $s0, $s1
		nop
		nop
		nop
		bne		$t0, $zero, l1
		nop
		nop
		nop
		move	$v0, $a1
		lw		$ra, 20($sp)
		lw		$fp, 16($sp)
		addu	$sp, $sp, 32
		jr		$ra
		nop
		nop
		nop
fib: 	subu	$sp, $sp, 32
		nop
		nop
		nop
		sw		$ra, 20($sp)
		sw		$fp, 16($sp)
		addu	$fp, $sp, 28
		nop
		nop
		nop
		sw		$a0, 0($fp)
		lw		$v0, 0($fp)
		nop
		nop
		nop
		bne		$v0, $zero, $L2
		nop
		nop
		nop
		li		$v0, 0
		j		$L1
		nop
		nop
		nop
$L2: 	li		$t0, 1
		nop
		nop
		nop
		sltu	$t1, $t0, $v0
		nop
		nop
		nop
		bne		$t1, $zero, $L3
		nop
		nop
		nop
		j		$L1
		nop
		nop
		nop
$L3: 	lw		$v1, 0($fp)
		nop
		nop
		nop
		nop
		subu	$v0, $v1, 1
		nop
		nop
		nop
		move	$a0, $v0
		jal		fib
		nop
		nop
		nop
		sw		$v0, 4($fp)
		lw		$v1, 0($fp)
		nop
		nop
		nop
		nop
		subu	$v0, $v1, 2
		nop
		nop
		nop
		move	$a0, $v0
		jal		fib
		nop
		nop
		nop
		lw		$v1, 4($fp)	
		nop
		nop
		nop
		addu	$v0, $v0, $v1	
$L1: 	lw		$ra, 20($sp)
		nop
		nop
		lw		$fp, 16($sp)
		addu	$sp, $sp, 32
		jr		$ra
		nop
		nop
		nop
	
