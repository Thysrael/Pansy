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
p:
.space	1024

.text
main:
	sub $sp,	$sp,	120
Basic_b94_22:
	# alloca from the offset: 0, size is: 52
	addiu $a0,	$sp,	0
	# alloca from the offset: 52, size is: 60
	addiu $a1,	$sp,	52
	# GEP base: %v95
	# the first index
	addiu $a2,	$a1,	0
	# the second index
	addiu $a2,	$a2,	0
	li $v0,	8
	sw $v0,	0($a2)
	# GEP base: %v96
	addiu $v0,	$a2,	4
	li $v1,	7
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	8
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	12
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	16
	li $v1,	2
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	20
	li $v1,	7
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	24
	sw $zero,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	28
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	32
	li $v1,	9
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	36
	li $v1,	3
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	40
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	44
	li $v1,	8
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	48
	li $v1,	3
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	52
	li $v1,	7
	sw $v1,	0($v0)
	# GEP base: %v96
	addiu $v0,	$a2,	56
	sw $zero,	0($v0)
	# GEP base: %v111
	# the first index
	addiu $a2,	$a0,	0
	# the second index
	addiu $a2,	$a2,	0
	li $v0,	3
	sw $v0,	0($a2)
	# GEP base: %v112
	addiu $v0,	$a2,	4
	li $v1,	9
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v0,	$a2,	8
	li $v1,	7
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v0,	$a2,	12
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v0,	$a2,	16
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v1,	$a2,	20
	li $v0,	2
	sw $v0,	0($v1)
	# GEP base: %v112
	addiu $v0,	$a2,	24
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v1,	$a2,	28
	li $v0,	3
	sw $v0,	0($v1)
	# GEP base: %v112
	addiu $v0,	$a2,	32
	li $v1,	6
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v0,	$a2,	36
	li $v1,	8
	sw $v1,	0($v0)
	# GEP base: %v112
	addiu $v0,	$a2,	40
	sw $zero,	0($v0)
	# GEP base: %v112
	addiu $v1,	$a2,	44
	li $v0,	1
	sw $v0,	0($v1)
	# GEP base: %v112
	addiu $v0,	$a2,	48
	li $v1,	5
	sw $v1,	0($v0)
	# GEP base: %v95
	# the first index
	addiu $v1,	$a1,	0
	# the second index
	addiu $v1,	$v1,	0
	# GEP base: %v111
	# the first index
	addiu $v0,	$a0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v1
	li $a1,	15
	move $a2,	$v0
	li $a3,	13
	jal longest_common_subseq
	move $a0,	$v0
	move $a0,	$a0
	putint
	li $v0,	0
	add $sp, 	$sp,	120
	li	$v0,	10
	syscall

MAX:
	sw $v1,	-4($sp)
	sub $sp,	$sp,	12
Basic_b0_0:
	move $v0,	$a1
	move $v1,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $a1,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $a0,	$sp,	4
	sw $v1,	0($a0)
	sw $v0,	0($a1)
	lw $v1,	0($a0)
	lw $v0,	0($a1)
	bne $v1,	$v0,	Basic_b5_3
Basic_b6_4:
Basic_b3_1:
	lw $v0,	0($a0)
	move $v0,	$v0
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	jr $ra
Basic_b5_3:
	lw $v0,	0($a0)
	lw $v1,	0($a1)
	ble $v0,	$v1,	Basic_b13_7
Basic_b14_8:
Basic_b11_5:
	lw $v0,	0($a0)
	move $v0,	$v0
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	jr $ra
Basic_b13_7:
	lw $v0,	0($a1)
	move $v0,	$v0
	add $sp, 	$sp,	12
	lw $v1,	-4($sp)
	jr $ra

longest_common_subseq:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $t1,	-12($sp)
	sw $t2,	-16($sp)
	sw $t3,	-20($sp)
	sw $t4,	-24($sp)
	sw $t5,	-28($sp)
	sw $t6,	-32($sp)
	sw $ra,	-36($sp)
	sub $sp,	$sp,	60
Basic_b20_9:
	move $a3,	$a3
	move $v1,	$a2
	move $a1,	$a1
	move $v0,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $t0,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $t1,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $t2,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $t3,	$sp,	12
	# alloca from the offset: 16, size is: 4
	addiu $t4,	$sp,	16
	# alloca from the offset: 20, size is: 4
	addiu $t5,	$sp,	20
	sw $v0,	0($t5)
	sw $a1,	0($t4)
	sw $v1,	0($t3)
	sw $a3,	0($t2)
	li $v0,	1
	sw $v0,	0($t1)
Basic_b27_10:
	lw $v0,	0($t1)
	lw $v1,	0($t4)
	bgt $v0,	$v1,	Basic_b29_12
Basic_b30_13:
Basic_b28_11:
	li $v0,	1
	sw $v0,	0($t0)
Basic_b34_14:
	lw $v0,	0($t0)
	lw $v1,	0($t2)
	bgt $v0,	$v1,	Basic_b36_16
Basic_b37_17:
Basic_b35_15:
	lw $v0,	0($t5)
	lw $v1,	0($t1)
	addiu $v1,	$v1,	-1
	# GEP base: %v45
	# %v47 mul 4
	sll $v1,	$v1,	2
	addu $v1,	$v1,	$v0
	lw $a0,	0($v1)
	lw $v0,	0($t3)
	lw $v1,	0($t0)
	addiu $v1,	$v1,	-1
	# GEP base: %v50
	# %v52 mul 4
	sll $v1,	$v1,	2
	addu $v1,	$v1,	$v0
	lw $v0,	0($v1)
	bne $a0,	$v0,	Basic_b43_20
Basic_b44_21:
Basic_b41_18:
	lw $v0,	0($t1)
	la $v1,	p
	# GEP base: @p
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v56 mul 64
	sll $at,	$v0,	6
	addu $v1,	$at,	$v1
	lw $v0,	0($t0)
	# GEP base: %v57
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v58 mul 4
	sll $at,	$v0,	2
	addu $v1,	$at,	$v1
	lw $v0,	0($t1)
	addiu $a0,	$v0,	-1
	la $v0,	p
	# GEP base: @p
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	# %v61 mul 64
	sll $at,	$a0,	6
	addu $v0,	$at,	$v0
	lw $a0,	0($t0)
	addiu $a0,	$a0,	-1
	# GEP base: %v62
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	# %v64 mul 4
	sll $at,	$a0,	2
	addu $v0,	$at,	$v0
	lw $v0,	0($v0)
	addiu $v0,	$v0,	1
	sw $v0,	0($v1)
Basic_b42_19:
	lw $v0,	0($t0)
	addiu $v0,	$v0,	1
	sw $v0,	0($t0)
	j Basic_b34_14
Basic_b43_20:
	lw $v1,	0($t1)
	la $v0,	p
	# GEP base: @p
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	# %v68 mul 64
	sll $at,	$v1,	6
	addu $v0,	$at,	$v0
	lw $v1,	0($t0)
	# GEP base: %v69
	# the first index
	addiu $t6,	$v0,	0
	# the second index
	# %v70 mul 4
	sll $at,	$v1,	2
	addu $t6,	$at,	$t6
	lw $v0,	0($t1)
	addiu $v0,	$v0,	-1
	la $v1,	p
	# GEP base: @p
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v73 mul 64
	sll $at,	$v0,	6
	addu $v1,	$at,	$v1
	lw $v0,	0($t0)
	# GEP base: %v74
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v75 mul 4
	sll $at,	$v0,	2
	addu $v1,	$at,	$v1
	lw $a0,	0($v1)
	lw $v1,	0($t1)
	la $v0,	p
	# GEP base: @p
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	# %v78 mul 64
	sll $at,	$v1,	6
	addu $v0,	$at,	$v0
	lw $v1,	0($t0)
	addiu $v1,	$v1,	-1
	# GEP base: %v79
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	# %v81 mul 4
	sll $at,	$v1,	2
	addu $v0,	$at,	$v0
	lw $a1,	0($v0)
	move $a0,	$a0
	move $a1,	$a1
	jal MAX
	move $v0,	$v0
	sw $v0,	0($t6)
	j Basic_b42_19
Basic_b36_16:
	lw $v0,	0($t1)
	addiu $v0,	$v0,	1
	sw $v0,	0($t1)
	j Basic_b27_10
Basic_b29_12:
	lw $v0,	0($t4)
	la $v1,	p
	# GEP base: @p
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v89 mul 64
	sll $at,	$v0,	6
	addu $v1,	$at,	$v1
	lw $v0,	0($t2)
	# GEP base: %v90
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	# %v91 mul 4
	sll $at,	$v0,	2
	addu $v1,	$at,	$v1
	lw $v0,	0($v1)
	move $v0,	$v0
	add $sp, 	$sp,	60
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $t1,	-12($sp)
	lw $t2,	-16($sp)
	lw $t3,	-20($sp)
	lw $t4,	-24($sp)
	lw $t5,	-28($sp)
	lw $t6,	-32($sp)
	lw $ra,	-36($sp)
	jr $ra

