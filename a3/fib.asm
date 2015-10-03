# Patterson and Hennessy, 2nd ed. modified for single cycle processor in Chisel.
	.text			# following are instructions			
	.globl	main
main:
	nop			# subu	$sp, $zero, 0xE000 # hack to initialise SP
	nop
	subu	$sp, $sp, 32	# pseudo: actual: addi
	nop
	nop
	nop
	nop
				# move stack pointer to new frame
	sw	$ra, 20($sp)	# save return address
	nop
	nop
	nop
	nop
	sw	$fp, 16($sp)	# save old frame pointer
	nop
	nop
	nop
	nop
	addu	$fp, $sp, 28	# set up new frame pointer
	nop
	nop
	nop
	nop
	li	$s0, 0		# i is counter
	nop
	nop
	nop
	nop
l1:	li	$s1, 20		# up to this number
nop
nop
nop
nop
	move	$a0, $s0	# counter
	nop
	nop
	nop
	nop
	jal	fib		# call fib function
	nop
	nop
	nop
	nop
	move	$t9, $v0	# move result to $t9
	nop
	nop
	nop
	nop
	addi	$s0, $s0, 1		# increment s0
	nop
	nop
	nop
	nop
	sltu	$t0, $s0, $s1
	nop
	nop
	nop
	nop
	bne	$t0, $zero, l1
	nop
	nop
	nop
	nop
	move	$v0, $a1	# restore result to result register (SPIM)
	nop
	nop
	nop
	nop
	lw	$ra, 20($sp)	# restore return address
	nop
	nop
	nop
	nop
	lw	$fp, 16($sp)	# restore frame pointer
	nop
	nop
	nop
	nop
	addu	$sp, $sp, 32	# pop stack frame
	nop
	nop
	nop
	nop
	jr	$ra		# return to caller
	nop
	nop
	nop
	nop

fib:
	subu	$sp, $sp, 32	# stack frame is 32 bytes
	nop
	nop
	nop
	nop
	sw	$ra, 20($sp)	# save return address
	nop
	nop
	nop
	nop
	sw	$fp, 16($sp)	# save frame pointer
	nop
	nop
	nop
	nop
	addu	$fp, $sp, 28	# set up frame pointer
	nop
	nop
	nop
	nop
	sw	$a0, 0($fp)	# save argument (n)
	nop
	nop
	nop
	nop
	lw	$v0, 0($fp)	# load n
	nop
	nop
	nop
	nop
	bne	$v0, $zero, $L2	# branch if n > 0
	nop
	nop
	nop
	nop
	li	$v0, 0		# if n=0 return 0
	nop
	nop
	nop
	nop
	j	$L1		# and jump to code to return
	nop
	nop
	nop
	nop
$L2:
	li	$t0, 1
	nop
	nop
	nop
	nop
	# bgt	$v0, $t0, $L3	# branch if n > 1
	sltu	$t1, $t0, $v0
	nop
	nop
	nop
	nop
	bne	$t1, $zero, $L3
	nop
	nop
	nop
	nop
	j	$L1		# $v0 was 1 so we can return
	nop
	nop
	nop
	nop
$L3:
	lw	$v1, 0($fp)	# load n
	nop
	nop
	nop
	nop
	subu	$v0, $v1, 1	# compute n-1
	nop
	nop
	nop
	nop
	move	$a0, $v0	# move to argument register
	nop
	nop
	nop
	nop
	jal	fib		# call fib function result in $v0
	nop
	nop
	nop
	nop
	sw	$v0, 4($fp)	# fib(n-1) is in 4($fp)
	nop
	nop
	nop
	nop

	lw	$v1, 0($fp)	# load n
	nop
	nop
	nop
	nop
	subu	$v0, $v1, 2	# compute n-2
	nop
	nop
	nop
	nop
	move	$a0, $v0	# move to argument register
	nop
	nop
	nop
	nop
	jal	fib		# call fib function result in $v0
	nop
	nop
	nop
	nop

	lw	$v1, 4($fp)	
	nop
	nop
	nop
	nop
	addu	$v0, $v0, $v1	
	nop
	nop
	nop
	nop

$L1:				# result is in $v0
	lw	$ra, 20($sp)	# restore return address
	nop
	nop
	nop
	nop
	lw	$fp, 16($sp)	# restore frame pointer
	nop
	nop
	nop
	nop
	addu	$sp, $sp, 32	# pop stack
	nop
	nop
	nop
	nop
	jr	$ra		# return to caller	
	nop
	nop
	nop
	nop
	
