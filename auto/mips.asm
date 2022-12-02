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
b1:
.word	1
.word	2
.word	3
.word	4
.word	5

c1:
.word	1
.word	2
.word	3
.word	4

test:
.word	0

STR0:
.asciiz	"20373275\n"

STR1:
.asciiz	"\n"

STR2:
.asciiz	"m:"

.text
main:
	add $sp,	$sp,	-52
Basic_b24_7:
	# alloca from the offset: 0, size is: 16
	addiu $t1,	$sp,	0
	# alloca from the offset: 16, size is: 20
	addiu $t0,	$sp,	16
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	0
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v34
	addiu $v1,	$v0,	4
	li $a0,	2
	sw $a0,	0($v1)
	# GEP base: %v34
	addiu $v1,	$v0,	8
	li $a0,	3
	sw $a0,	0($v1)
	# GEP base: %v34
	addiu $v1,	$v0,	12
	li $a0,	4
	sw $a0,	0($v1)
	# GEP base: %v34
	addiu $v0,	$v0,	16
	li $v1,	5
	sw $v1,	0($v0)
	# GEP base: %v39
	# the first index
	addiu $v0,	$t1,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v40
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v41
	addiu $v1,	$v0,	4
	li $a0,	2
	sw $a0,	0($v1)
	# GEP base: %v41
	addiu $v1,	$v0,	8
	li $a0,	3
	sw $a0,	0($v1)
	# GEP base: %v41
	addiu $v0,	$v0,	12
	li $v1,	4
	sw $v1,	0($v0)
	jal f1
	# GEP base: %v33
	# the first index
	addiu $a0,	$t0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	jal f5
	move $v0,	$v0
	# GEP base: %v39
	# the first index
	addiu $a0,	$t1,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	jal f6
	move $v0,	$v0
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $a1,	0($v0)
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $a2,	0($v0)
	move $a0,	$v1
	move $a1,	$a1
	move $a2,	$a2
	jal f7
	move $v0,	$v0
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	# GEP base: %v33
	# the first index
	addiu $v1,	$t0,	0
	# the second index
	addiu $v1,	$v1,	4
	lw $v1,	0($v1)
	move $a0,	$v0
	move $a1,	$v1
	jal f3
	move $a0,	$v0
	move $a0,	$a0
	putint
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v39
	# the first index
	addiu $v0,	$t1,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v66
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	bne $v0,	1,	Basic_b64_10
Basic_b65_11:
Basic_b62_8:
	getint
	move $v1,	$v0
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
Basic_b63_9:
	beq $zero,	0,	Basic_b76_13
Basic_b77_14:
	jal f4
	move $v0,	$v0
	beq $v0,	0,	Basic_b76_13
Basic_b79_15:
Basic_b75_12:
Basic_b76_13:
	la $v0,	test
	lw $v0,	0($v0)
	move $a0,	$v0
	putint
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	li $v0,	1
	beq $v0,	0,	Basic_b86_18
Basic_b87_19:
Basic_b84_16:
Basic_b85_17:
	la $v0,	test
	lw $v0,	0($v0)
	move $a0,	$v0
	putint
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	li $v0,	-20
	li $v0,	31
	addiu $v0,	$v0,	1
	li $v0,	-1
	# %v99 mul 1
	sll $v0,	$v0,	0
	li $v0,	1
	move $zero,	$zero
	# 1 div 1
	li $a0,	1
	bge $a0,	0,	Basic_b108_23
Basic_b109_24:
Basic_b106_21:
Basic_b107_22:
	ble $a0,	0,	Basic_b121_28
Basic_b122_29:
Basic_b119_26:
Basic_b120_27:
	beq $a0,	0,	Basic_b133_32
Basic_b134_33:
Basic_b132_31:
Basic_b133_32:
	li $v0,	1
	beq $v0,	0,	Basic_b139_35
Basic_b140_36:
Basic_b138_34:
Basic_b139_35:
Basic_b144_37:
	bne $a0,	0,	Basic_b146_39
Basic_b147_40:
Basic_b145_38:
	bne $zero,	1,	Basic_b152_42
Basic_b153_43:
Basic_b151_41:
	j Basic_b144_37
Basic_b152_42:
Basic_b146_39:
	li $v0,	0
	add $sp, 	$sp,	52
	li	$v0,	10
	syscall

Basic_b121_28:
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $v1,	0($v0)
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $v0,	0($v0)
	bgt $v1,	$v0,	Basic_b120_27
Basic_b126_30:
	j Basic_b119_26
Basic_b108_23:
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v33
	# the first index
	addiu $v0,	$t0,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $v0,	0($v0)
	blt $v1,	$v0,	Basic_b107_22
Basic_b113_25:
	j Basic_b106_21
Basic_b86_18:
	jal f4
	move $v0,	$v0
	beq $v0,	0,	Basic_b85_17
Basic_b89_20:
	j Basic_b84_16
Basic_b64_10:
	j Basic_b63_9
f1:
Basic_b0_0:
		jr $ra

f2:
Basic_b1_1:
	move $a0,	$a0
	addiu $v0,	$a0,	1
	li $v0,	0
		jr $ra

f3:
Basic_b5_2:
	move $a1,	$a1
	move $a0,	$a0
	addu $v0,	$a0,	$a1
	move $v0,	$v0
		jr $ra

f4:
	sw $v1,	-4($sp)
	add $sp,	$sp,	-4
Basic_b13_3:
	la $v0,	test
	lw $v0,	0($v0)
	addiu $v0,	$v0,	1
	la $v1,	test
	sw $v0,	0($v1)
	li $v0,	1
	add $sp, 	$sp,	4
	lw $v1,	-4($sp)
	jr $ra

f5:
Basic_b16_4:
	li $v0,	1
		jr $ra

f6:
Basic_b18_5:
	li $v0,	0
		jr $ra

f7:
Basic_b20_6:
	li $v0,	0
		jr $ra

