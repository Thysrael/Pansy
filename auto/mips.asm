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
.asciiz	"  "

STR1:
.asciiz	" = "

STR2:
.asciiz	"\n"

.text
main:
	add $sp,	$sp,	-20
Basic_b0_0:
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	move $t3,	$v0
	# the second index
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	move $t2,	$v0
	# the second index
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	move $t0,	$v0
	# the second index
	li $t1,	1
Basic_b3_1:
	bge $t1,	1000,	Basic_b5_3
Basic_b6_4:
Basic_b4_2:
	move $v1,	$t1
Basic_b10_5:
	bge $v1,	1000,	Basic_b12_7
Basic_b13_8:
Basic_b11_6:
	move $a0,	$v1
	putint
	move $a0,	$t0
	putstr
	move $a0,	$t1
	putint
	move $a0,	$t2
	putstr
	# %p0 div %p2
	div $v1,	$t1
	mflo $a0
	putint
	move $a0,	$t3
	putstr
	addiu $v1,	$v1,	1
	j Basic_b10_5
Basic_b12_7:
	addiu $v0,	$t1,	1
	move $t1,	$v0
	j Basic_b3_1
Basic_b5_3:
	li $v0,	0
	add $sp, 	$sp,	20
	li	$v0,	10
	syscall

