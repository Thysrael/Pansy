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
.text
main:
	add $sp,	$sp,	-16
Basic_b0_0:
	# alloca from the offset: 0, size is: 8
	move $a0,	$sp
	# alloca from the offset: 8, size is: 4
	addiu $v0,	$sp,	8
	# GEP base: %v1
	# the first index
	# the second index
	# GEP base: %v2
	# the first index
	# the second index
	li $v1,	2
	sw $v1,	0($v0)
	# GEP base: %v4
	# the first index
	move $v0,	$a0
	# the second index
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v5
	addiu $v0,	$v0,	4
	li $v1,	2
	sw $v1,	0($v0)
	# GEP base: %v4
	# the first index
	move $v0,	$a0
	# the second index
	lw $a0,	0($v0)
	putint
	li $v0,	0
	add $sp, 	$sp,	16
	li	$v0,	10
	syscall

