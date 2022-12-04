# Pansy Say "Hi~" to you!
.macro putstr
	li	$v0,	4
	syscall
.end_macro

.macro putint
	li	$v0,	1
	syscall
.end_macro

.macro getint
	li	$v0,	5
	syscall
.end_macro

.data
STR0:
.asciiz	"j : "

STR1:
.asciiz	", k : "

STR2:
.asciiz	", l : "

STR3:
.asciiz	"\n"

STR4:
.asciiz	"a! = "

STR5:
.asciiz	", num = "

STR6:
.asciiz	"19373373\n"

STR7:
.asciiz	"scanf a, b to get gcd and lcm\n"

STR8:
.asciiz	"gcd is "

STR9:
.asciiz	"lcm is "

STR10:
.asciiz	"scanf a to get Fibonacci\n"

STR11:
.asciiz	"fib is "

.text
main:
	add $sp,	$sp,	-16
Basic_b156_42:
	la $v0,	STR6
	# GEP base: @STR6
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	jal fun1
	li $a0,	6
	jal fun2
	move $v0,	$v0
	li $a0,	3
	li $a1,	6
	jal fun3
	move $v0,	$v0
	li $a0,	2
	move $a1,	$v0
	jal fun3
	move $v0,	$v0
	move $a0,	$v0
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v1,	$v0,	0
	# the second index
	addiu $v1,	$v1,	0
	move $a0,	$v1
	putstr
	la $v0,	STR7
	# GEP base: @STR7
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	getint
	move $t0,	$v0
	getint
	move $t1,	$v0
	la $v0,	STR8
	# GEP base: @STR8
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	move $a1,	$t1
	jal gcd
	move $v0,	$v0
	move $a0,	$v0
	putint
	move $a0,	$v1
	putstr
	la $v0,	STR9
	# GEP base: @STR9
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	move $a1,	$t1
	jal lcm
	move $a0,	$v0
	move $a0,	$a0
	putint
	move $a0,	$v1
	putstr
	getint
	move $v0,	$v0
	move $a0,	$v0
	li $a1,	3
	li $a2,	10
	jal fun4
	la $v0,	STR10
	# GEP base: @STR10
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	getint
	move $t0,	$v0
	la $v0,	STR11
	# GEP base: @STR11
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	jal fun5
	move $v0,	$v0
	move $a0,	$v0
	putint
	move $a0,	$v1
	putstr
	li $v0,	0
	add $sp, 	$sp,	16
	li	$v0,	10
	syscall

fun1:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	add $sp,	$sp,	-8
Basic_b0_0:
	li $v1,	4
	li $t0,	11
	li $a0,	0
Basic_b12_1:
	bgt $a0,	7,	Basic_b14_3
Basic_b15_4:
Basic_b13_2:
	addiu $v0,	$a0,	1
	bne $t0,	$v0,	Basic_b21_6
Basic_b22_7:
Basic_b20_5:
	addu $v1,	$v1,	$t0
	move $v1,	$v1
	move $a0,	$v0
	move $t0,	$t0
	j Basic_b12_1
Basic_b21_6:
	addiu $t0,	$t0,	-1
	move $t0,	$t0
	move $a0,	$v0
	move $v0,	$v1
	j Basic_b12_1
Basic_b14_3:
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	li $a0,	4
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	add $sp, 	$sp,	8
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	jr $ra

fun2:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	add $sp,	$sp,	-8
Basic_b38_8:
	move $a0,	$a0
	li $t0,	1
	move $v0,	$a0
	li $a0,	1
Basic_b42_9:
	blt $v0,	1,	transfer_43
Basic_b45_12:
Basic_b43_10:
	addiu $v1,	$v0,	-1
	# %p5 mul %p7
	mul $a0,	$a0,	$v0
	bne $v1,	1,	Basic_b55_15
Basic_b56_16:
Basic_b53_13:
	move $v1,	$a0
Basic_b44_11:
	la $v0,	STR4
	# GEP base: @STR4
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR5
	# GEP base: @STR5
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	li $v0,	0
	add $sp, 	$sp,	8
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	jr $ra
Basic_b55_15:
	beq $v1,	1,	transfer_44
Basic_b61_19:
Basic_b59_17:
	addiu $v0,	$t0,	1
	move $v0,	$v0
Basic_b60_18:
Basic_b54_14:
	move $t0,	$v0
	move $v0,	$v1
	move $a0,	$a0
	j Basic_b42_9
transfer_44:
	move $v0,	$t0
	j Basic_b60_18
transfer_43:
	move $v1,	$a0
	j Basic_b44_11

fun3:
Basic_b71_20:
	move $a1,	$a1
	move $v0,	$a0
	ble $v0,	$a1,	Basic_b76_23
Basic_b77_24:
Basic_b74_21:
	move $v0,	$v0
		jr $ra
Basic_b76_23:
	ble $v0,	$a1,	Basic_b83_26
Basic_b84_27:
Basic_b82_25:
	move $v0,	$a1
		jr $ra
Basic_b83_26:
Basic_b75_22:
	move $v0,	$v0
		jr $ra

gcd:
	sw $v1,	-4($sp)
	sw $ra,	-8($sp)
	add $sp,	$sp,	-8
Basic_b90_28:
	move $v0,	$a1
	move $a0,	$a0
	# %a0 div %a1
	div $a0,	$v0
	mflo $v1
	# %v98 mul %a1
	mul $v1,	$v1,	$v0
	subu $a1,	$a0,	$v1
	bne $a1,	0,	Basic_b94_30
Basic_b95_31:
Basic_b93_29:
	move $v0,	$v0
	add $sp, 	$sp,	8
	lw $v1,	-4($sp)
	lw $ra,	-8($sp)
	jr $ra
Basic_b94_30:
	move $a0,	$v0
	move $a1,	$a1
	jal gcd
	move $v0,	$v0
	move $v0,	$v0
	add $sp, 	$sp,	8
	lw $v1,	-4($sp)
	lw $ra,	-8($sp)
	jr $ra

lcm:
	sw $v1,	-4($sp)
	sw $ra,	-8($sp)
	add $sp,	$sp,	-8
Basic_b110_32:
	move $a1,	$a1
	move $v0,	$a0
	# %a0 mul %a1
	mul $v1,	$v0,	$a1
	move $a0,	$v0
	move $a1,	$a1
	jal gcd
	move $v0,	$v0
	# %v119 div %v116
	div $v1,	$v0
	mflo $v0
	move $v0,	$v0
	add $sp, 	$sp,	8
	lw $v1,	-4($sp)
	lw $ra,	-8($sp)
	jr $ra

fun4:
	sw $v1,	-4($sp)
	add $sp,	$sp,	-4
Basic_b122_33:
	move $v1,	$a2
	move $a1,	$a1
	move $a0,	$a0
	addu $v0,	$a0,	$a1
	subu $v0,	$v0,	$v1
	# %v131 mul %a0
	mul $v0,	$v0,	$a0
	move $a0,	$v0
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	add $sp, 	$sp,	4
	lw $v1,	-4($sp)
	jr $ra

fun5:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $ra,	-12($sp)
	add $sp,	$sp,	-12
Basic_b136_34:
	move $v1,	$a0
	bne $v1,	1,	Basic_b140_37
Basic_b141_38:
Basic_b138_35:
	li $v0,	1
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $ra,	-12($sp)
	jr $ra
Basic_b140_37:
	bne $v1,	2,	Basic_b145_40
Basic_b146_41:
Basic_b144_39:
	li $v0,	1
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $ra,	-12($sp)
	jr $ra
Basic_b145_40:
Basic_b139_36:
	addiu $a0,	$v1,	-2
	move $a0,	$a0
	jal fun5
	move $t0,	$v0
	addiu $a0,	$v1,	-1
	move $a0,	$a0
	jal fun5
	move $v0,	$v0
	addu $v0,	$v0,	$t0
	move $v0,	$v0
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $ra,	-12($sp)
	jr $ra

