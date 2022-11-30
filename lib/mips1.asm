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
.asciiz	"\n"

.text
main:
	add $sp,	$sp,	-412
Basic_b78_19:
	# alloca from the offset: 0, size is: 4
	addiu $v1,	$sp,	0
	# alloca from the offset: 4, size is: 400
	addiu $v0,	$sp,	4
	# GEP base: %v79
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	li $a1,	10
	jal setArray2D
	# GEP base: %v79
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	li $a1,	10
	jal getArray2DSum
	move $v0,	$v0
	sw $v0,	0($v1)
	lw $a0,	0($v1)
	move $a0,	$a0
	putint
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	li $v0,	0
	add $sp, 	$sp,	412
	li	$v0,	10
	syscall

setArray2D:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $t1,	-12($sp)
	sw $t2,	-16($sp)
	sw $t3,	-20($sp)
	sw $t4,	-24($sp)
	sub $sp,	$sp,	44
Basic_b0_0:
	move $v0,	$a1
	move $a0,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $t4,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $t2,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $t3,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $t0,	$sp,	12
	# alloca from the offset: 16, size is: 4
	addiu $t1,	$sp,	16
	sw $a0,	0($t1)
	sw $v0,	0($t0)
	sw $zero,	0($t3)
	sw $zero,	0($t2)
	getint
	move $v0,	$v0
	sw $v0,	0($t4)
Basic_b7_1:
	lw $a0,	0($t3)
	lw $v1,	0($t0)
	bge $a0,	$v1,	Basic_b9_3
Basic_b10_4:
Basic_b8_2:
	sw $zero,	0($t2)
Basic_b14_5:
	lw $v1,	0($t2)
	bge $v1,	10,	Basic_b16_7
Basic_b17_8:
Basic_b15_6:
	lw $v1,	0($t1)
	lw $a0,	0($t3)
	lw $a1,	0($t2)
	# GEP base: %v20
	# the first index
	# %v21 mul 40
	li $a2,	40
	mul $a2,	$a0,	$a2
	addu $a2,	$a2,	$v1
	# the second index
	# %v22 mul 4
	sll $at,	$a1,	2
	addu $a2,	$at,	$a2
	lw $v1,	0($t3)
	lw $a0,	0($t2)
	# %v24 mul %v25
	mul $v1,	$v1,	$a0
	lw $a0,	0($t4)
	# %v26 div %v27
	div $v1,	$a0
	mflo $a1
	# %v28 mul %v27
	mul $a0,	$a1,	$a0
	subu $v1,	$v1,	$a0
	sw $v1,	0($a2)
	lw $v1,	0($t2)
	addiu $v1,	$v1,	1
	sw $v1,	0($t2)
	j Basic_b14_5
Basic_b16_7:
	lw $v1,	0($t3)
	addiu $v1,	$v1,	1
	sw $v1,	0($t3)
	j Basic_b7_1
Basic_b9_3:
	add $sp, 	$sp,	44
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $t1,	-12($sp)
	lw $t2,	-16($sp)
	lw $t3,	-20($sp)
	lw $t4,	-24($sp)
	jr $ra

getArray1DSum:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sub $sp,	$sp,	24
Basic_b35_9:
	move $a1,	$a1
	move $a0,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $v1,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $a2,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $a3,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $t0,	$sp,	12
	sw $a0,	0($t0)
	sw $a1,	0($a3)
	sw $zero,	0($a2)
	sw $zero,	0($v1)
Basic_b40_10:
	lw $v0,	0($v1)
	lw $a0,	0($a3)
	bge $v0,	$a0,	Basic_b42_12
Basic_b43_13:
Basic_b41_11:
	lw $a0,	0($a2)
	lw $a1,	0($t0)
	lw $v0,	0($v1)
	# GEP base: %v48
	# %v49 mul 4
	sll $v0,	$v0,	2
	addu $v0,	$v0,	$a1
	lw $v0,	0($v0)
	addu $v0,	$a0,	$v0
	sw $v0,	0($a2)
	lw $v0,	0($v1)
	addiu $v0,	$v0,	1
	sw $v0,	0($v1)
	j Basic_b40_10
Basic_b42_12:
	lw $v0,	0($a2)
	move $v0,	$v0
	add $sp, 	$sp,	24
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	jr $ra

getArray2DSum:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $t1,	-12($sp)
	sw $t2,	-16($sp)
	sw $t3,	-20($sp)
	sw $ra,	-24($sp)
	sub $sp,	$sp,	40
Basic_b56_14:
	move $v0,	$a1
	move $v1,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $t0,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $t1,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $t2,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $t3,	$sp,	12
	sw $v1,	0($t3)
	sw $v0,	0($t2)
	sw $zero,	0($t1)
	sw $zero,	0($t0)
Basic_b61_15:
	lw $v0,	0($t0)
	lw $v1,	0($t2)
	bge $v0,	$v1,	Basic_b63_17
Basic_b64_18:
Basic_b62_16:
	lw $v1,	0($t1)
	lw $v0,	0($t3)
	lw $a0,	0($t0)
	# GEP base: %v69
	# %v70 mul 40
	li $a1,	40
	mul $a0,	$a0,	$a1
	addu $a0,	$a0,	$v0
	# GEP base: %v71
	# the first index
	addiu $a0,	$a0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	li $a1,	10
	jal getArray1DSum
	move $v0,	$v0
	addu $v0,	$v1,	$v0
	sw $v0,	0($t1)
	lw $v0,	0($t0)
	addiu $v0,	$v0,	1
	sw $v0,	0($t0)
	j Basic_b61_15
Basic_b63_17:
	lw $v0,	0($t1)
	move $v0,	$v0
	add $sp, 	$sp,	40
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $t1,	-12($sp)
	lw $t2,	-16($sp)
	lw $t3,	-20($sp)
	lw $ra,	-24($sp)
	jr $ra

