# Patterson and Hennessy, 2nd ed. modified for single cycle processor in Chisel.
	.text			# following are instructions			
	.globl	main
main:
	nop			# subu	$sp, $zero, 0xE000 # hack to initialise SP
	nop
	subu	$sp, $sp, 32	# pseudo: actual: addi
				# move stack pointer to new frame
	sw	$ra, 20($sp)	# save return address
	sw	$fp, 16($sp)	# save old frame pointer
	addu	$fp, $sp, 28	# set up new frame pointer
	li	$s0, 0		# i is counter
l1:	li	$s1, 20		# up to this number
	move	$a0, $s0	# counter
	jal	fib		# call fib function
	move	$t9, $v0	# move result to $t9
	addi	$s0, $s0, 1		# increment s0
	sltu	$t0, $s0, $s1
	bne	$t0, $zero, l1
	move	$v0, $a1	# restore result to result register (SPIM)
	lw	$ra, 20($sp)	# restore return address
	lw	$fp, 16($sp)	# restore frame pointer
	addu	$sp, $sp, 32	# pop stack frame
	jr	$ra		# return to caller

fib:
	subu	$sp, $sp, 32	# stack frame is 32 bytes
	sw	$ra, 20($sp)	# save return address
	sw	$fp, 16($sp)	# save frame pointer
	addu	$fp, $sp, 28	# set up frame pointer
	sw	$a0, 0($fp)	# save argument (n)
	lw	$v0, 0($fp)	# load n
	bne	$v0, $zero, $L2	# branch if n > 0
	li	$v0, 0		# if n=0 return 0
	j	$L1		# and jump to code to return
$L2:
	li	$t0, 1
	# bgt	$v0, $t0, $L3	# branch if n > 1
	sltu	$t1, $t0, $v0
	bne	$t1, $zero, $L3
	j	$L1		# $v0 was 1 so we can return
$L3:
	lw	$v1, 0($fp)	# load n
	subu	$v0, $v1, 1	# compute n-1
	move	$a0, $v0	# move to argument register
	jal	fib		# call fib function result in $v0
	sw	$v0, 4($fp)	# fib(n-1) is in 4($fp)

	lw	$v1, 0($fp)	# load n
	subu	$v0, $v1, 2	# compute n-2
	move	$a0, $v0	# move to argument register
	jal	fib		# call fib function result in $v0

	lw	$v1, 4($fp)	
	addu	$v0, $v0, $v1	

$L1:				# result is in $v0
	lw	$ra, 20($sp)	# restore return address
	lw	$fp, 16($sp)	# restore frame pointer
	addu	$sp, $sp, 32	# pop stack
	jr	$ra		# return to caller	
	
