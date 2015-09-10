main:
        li      $a0, 5
        jal     fact
        ;li      $v0, 1
        ;syscall
        jr      $ra
fact:
        addi $sp, $sp, -8     # adjust stack for 2 items
        sw   $ra, 4($sp)      # save return address
        sw   $a0, 0($sp)      # save argument
        li   $v0, 1
        slti $t0, $a0, 1      # test for n < 1
        beq  $t0, $0, L1
        addi $v0, $0, 1       # if so, result is 1
        addi $sp, $sp, 8      #   pop 2 items from stack
        jr   $ra              #   and return
L1:     addi $a0, $a0, -1     # else decrement n
        jal  fact             # recursive call
        lw   $a0, 0($sp)      # restore original n
        lw   $ra, 4($sp)      #   and return address
        addi $sp, $sp, 8      # pop 2 items from stack
        mul  $v0, $a0, $v0    # multiply to get result
        jr   $ra              # and return
