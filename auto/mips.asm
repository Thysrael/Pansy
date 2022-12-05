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
.asciiz	"(20373335)\n"

STR1:
.asciiz	"C level,test a code has decl,no func\n"

STR2:
.asciiz	"con1 is "

STR3:
.asciiz	"\n"

STR4:
.asciiz	"con2 is "

STR5:
.asciiz	",con3 is "

STR6:
.asciiz	"the left val is "

STR7:
.asciiz	"var1 greater than 1\n"

STR8:
.asciiz	"var5 equal to con1\n"

.text
main:
	add $sp,	$sp,	-16
Basic_b0_0:
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	# the second index
	move $a0,	$v0
	putstr
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	move $a0,	$v0
	# the second index
	putstr
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	# the second index
	move $a0,	$v0
	putstr
	li $a0,	44
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	move $t0,	$v0
	# the second index
	move $a0,	$t0
	putstr
	la $v0,	STR4
	# GEP base: @STR4
	# the first index
	move $a0,	$v0
	# the second index
	putstr
	li $a0,	678
	putint
	la $v0,	STR5
	# GEP base: @STR5
	# the first index
	move $a0,	$v0
	# the second index
	putstr
	li $a0,	61
	putint
	move $a0,	$t0
	putstr
	getint
	move $t1,	$v0
	la $v0,	STR6
	# GEP base: @STR6
	# the first index
	move $a0,	$v0
	# the second index
	putstr
	# %v11 mul 678
	li $v0,	678
	mul $t2,	$t1,	$v0
	# %v18 div 105
	li $v0,	-1677082467
	mthi $t2
	madd $t2,	$v0
	mfhi $v0
	sra $v0,	$v0,	6
	srl $at,	$t2,	31
	addu $v0,	$v0,	$at
	# %v20 mul 105
	li $v1,	105
	mul $v0,	$v0,	$v1
	subu $v0,	$t2,	$v0
	# %v11 mul 61
	li $v1,	61
	mul $v1,	$t1,	$v1
	li $a0,	61
	subu $v1,	$a0,	$v1
	addu $t1,	$v1,	$v0
	move $a0,	$t1
	putint
	move $a0,	$t0
	putstr
	# %v18 div 105
	li $v0,	-1677082467
	mthi $t2
	madd $t2,	$v0
	mfhi $v0
	sra $v0,	$v0,	6
	srl $at,	$t2,	31
	addu $v0,	$v0,	$at
	# %v35 mul 105
	li $a0,	105
	mul $v0,	$v0,	$a0
	subu $v0,	$t2,	$v0
	addu $v0,	$v1,	$v0
	ble $t1,	1,	Basic_b44_2
Basic_b45_3:
Basic_b43_1:
	la $v0,	STR7
	# GEP base: @STR7
	# the first index
	move $a0,	$v0
	# the second index
	putstr
Basic_b44_2:
Basic_b52_7:
Basic_b49_4:
	# %v23 div 44
	li $v0,	780903145
	mult $t1,	$v0
	mfhi $v0
	sra $v0,	$v0,	3
	srl $at,	$t1,	31
	addu $v0,	$v0,	$at
	li $v1,	74725
	addu $v0,	$v0,	$v1
Basic_b50_5:
Basic_b67_13:
Basic_b65_11:
Basic_b71_15:
Basic_b78_19:
Basic_b76_17:
Basic_b77_18:
Basic_b66_12:
Basic_b90_20:
Basic_b93_23:
Basic_b91_21:
Basic_b100_26:
Basic_b97_24:
Basic_b92_22:
	li $v0,	0
	add $sp, 	$sp,	16
	li	$v0,	10
	syscall

