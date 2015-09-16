        .data
str:
        .asciiz "Fact is: "
newline:
        .asciiz "\n"

        .text
main:
        li      $t1, 0
        li      $v1, 9
L2:
        add     $a0, $t1, $zero
        jal     fact
        add     $a1, $v0, $zero
        li      $v0, 4
        la      $a0, str
        syscall
        add     $a0, $a1, $zero
        li      $v0, 1
        syscall
        li      $v0, 4
        la      $a0, newline
        syscall
        addi    $t1, $t1, 1
        ble     $t1, $v1, L2
        li      $v0, 10
        syscall
fact:
        addi $sp, $sp, -8     # adjust stack for 2 items
        sw   $ra, 4($sp)      # save return address
        sw   $a0, 0($sp)      # save argument
        slti $t0, $a0, 1      # test for n < 1
        beq  $t0, $0, L1
        addi $v0, $0, 1    # if so, result is 1
        addi $sp, $sp, 8      #   pop 2 items from stack
        jr   $ra              #   and return
L1:     addi $a0, $a0, -1     # else decrement n
        jal  fact             # recursive call
        lw   $a0, 0($sp)      # restore original n
        lw   $ra, 4($sp)      #   and return address
        addi $sp, $sp, 8      # pop 2 items from stack
        mul  $v0, $a0, $v0    # multiply to get result
        jr   $ra              # and return
