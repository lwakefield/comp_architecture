# Patterson and Hennessy, 2nd ed. modified for single cycle processor in Chisel.
		.text
		.globl	main
main: 	nop
		nop
		subu	$sp, $sp, 32
		sw		$ra, 20($sp)
		sw		$fp, 16($sp)
		addu	$fp, $sp, 28
		li		$s0, 0
l1:		li		$s1, 20
		move	$a0, $s0
		jal		fib
		move	$t9, $v0
		addi	$s0, $s0, 1
		sltu	$t0, $s0, $s1
		bne		$t0, $zero, l1
		move	$v0, $a1
		lw		$ra, 20($sp)
		lw		$fp, 16($sp)
		addu	$sp, $sp, 32
		jr		$ra
fib: 	subu	$sp, $sp, 32
		sw		$ra, 20($sp)
		sw		$fp, 16($sp)
		addu	$fp, $sp, 28
		sw		$a0, 0($fp)
		lw		$v0, 0($fp)
		nop
		bne		$v0, $zero, $L2
		li		$v0, 0
		j		$L1
$L2: 	li		$t0, 1
		sltu	$t1, $t0, $v0
		bne		$t1, $zero, $L3
		j		$L1
$L3: 	lw		$v1, 0($fp)
		nop
		subu	$v0, $v1, 1
		move	$a0, $v0
		jal		fib
		sw		$v0, 4($fp)
		lw		$v1, 0($fp)
		nop
		subu	$v0, $v1, 2
		move	$a0, $v0
		jal		fib
		lw		$v1, 4($fp)	
		nop
		addu	$v0, $v0, $v1	
$L1: 	lw		$ra, 20($sp)
		nop
		nop
		lw		$fp, 16($sp)
		addu	$sp, $sp, 32
		jr		$ra
	
