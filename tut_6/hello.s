        .data
str:
        .asciiz "Hello world\n"

        .text
main:
        li      $v1, 10
        li      $t0, 1
        li      $v0, 4
l1:
        la      $a0, str
        syscall
        addi    $t0, $t0, 1
        ble     $t0, $v1, l1
        jr      $ra
