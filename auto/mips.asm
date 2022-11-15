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
g_con_a:
.word	1
.word	2
.word	3
.word	4

g_con_b:
.word	1
.word	4
.word	2
.word	4

g_con_c:
.word	0
.word	0

g_a:
.space	16

g_b:
.word	5
.word	9
.word	10
.word	2

g_c:
.space	8

STR0:
.asciiz	"20374042\n"

STR1:
.asciiz	"g_con_a: "

STR2:
.asciiz	" "

STR3:
.asciiz	"\n"

STR4:
.asciiz	"g_con_b: "

STR5:
.asciiz	"g_con_c: "

STR6:
.asciiz	"g_a: "

STR7:
.asciiz	"g_b: "

STR8:
.asciiz	"g_c: "

STR9:
.asciiz	"con_a: "

STR10:
.asciiz	"con_b: "

STR11:
.asciiz	"con_c: "

STR12:
.asciiz	"a: "

STR13:
.asciiz	"b: "

STR14:
.asciiz	"c: "

STR15:
.asciiz	"g_a sum: "

STR16:
.asciiz	"g_con_b[1] sum: "

STR17:
.asciiz	"g_con_b sum: "

STR18:
.asciiz	"real global const b sum: "

.text
main:
	sub $sp,	$sp,	220
Basic_b68_15:
	# alloca from the offset: 0, size is: 4
	addiu $s5,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $s6,	$sp,	4
	# alloca from the offset: 8, size is: 16
	addiu $s2,	$sp,	8
	# alloca from the offset: 24, size is: 16
	addiu $s3,	$sp,	24
	# alloca from the offset: 40, size is: 16
	addiu $s4,	$sp,	40
	# alloca from the offset: 56, size is: 16
	addiu $t3,	$sp,	56
	# alloca from the offset: 72, size is: 8
	addiu $t4,	$sp,	72
	# alloca from the offset: 80, size is: 16
	addiu $t5,	$sp,	80
	# alloca from the offset: 96, size is: 16
	addiu $t6,	$sp,	96
	# alloca from the offset: 112, size is: 8
	addiu $t7,	$sp,	112
	# alloca from the offset: 120, size is: 16
	addiu $s0,	$sp,	120
	# alloca from the offset: 136, size is: 16
	addiu $s1,	$sp,	136
	la $v0,	STR0
	# GEP base: @STR0
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v70
	# the first index
	addiu $a0,	$s1,	0
	# the second index
	addiu $a0,	$a0,	0
	li $v0,	1
	sw $v0,	0($a0)
	# GEP base: %v71
	addiu $v0,	$a0,	4
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v71
	addiu $v0,	$a0,	8
	li $v1,	4
	sw $v1,	0($v0)
	# GEP base: %v71
	addiu $v0,	$a0,	12
	li $v1,	19
	sw $v1,	0($v0)
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v76
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	li $v0,	1
	sw $v0,	0($a0)
	# GEP base: %v77
	addiu $v0,	$a0,	4
	li $v1,	9
	sw $v1,	0($v0)
	# GEP base: %v77
	addiu $v0,	$a0,	8
	li $v1,	3
	sw $v1,	0($v0)
	# GEP base: %v77
	addiu $v0,	$a0,	12
	li $v1,	8
	sw $v1,	0($v0)
	# GEP base: %v81
	# the first index
	addiu $v0,	$t7,	0
	# the second index
	addiu $v0,	$v0,	0
	sw $zero,	0($v0)
	# GEP base: %v82
	addiu $v0,	$v0,	4
	sw $zero,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $a3,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $v1,	0($v0)
	la $v0,	g_b
	# GEP base: @g_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v89
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	addu $a1,	$v1,	$v0
	li $a2,	6
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v94
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v84
	# the first index
	addiu $a0,	$t6,	0
	# the second index
	addiu $a0,	$a0,	0
	sw $a3,	0($a0)
	# GEP base: %v97
	addiu $v0,	$a0,	4
	sw $a1,	0($v0)
	# GEP base: %v97
	addiu $v0,	$a0,	8
	sw $a2,	0($v0)
	# GEP base: %v97
	addiu $v0,	$a0,	12
	sw $v1,	0($v0)
	li $a1,	-2
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v103
	# the first index
	addiu $v1,	$v0,	0
	# the second index
	addiu $v1,	$v1,	0
	li $v0,	4
	sw $v0,	0($v1)
	# GEP base: %v104
	addiu $a0,	$v1,	4
	li $v0,	8
	sw $v0,	0($a0)
	# GEP base: %v104
	addiu $v0,	$v1,	8
	sw $a1,	0($v0)
	# GEP base: %v104
	addiu $v0,	$v1,	12
	li $v1,	249
	sw $v1,	0($v0)
	# GEP base: %v108
	# the first index
	addiu $v0,	$t4,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v101
	# the first index
	addiu $v1,	$t5,	0
	# the second index
	addiu $v1,	$v1,	8
	# GEP base: %v110
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	addiu $v1,	$v1,	4
	lw $v1,	0($v1)
	sw $v1,	0($v0)
	# GEP base: %v108
	# the first index
	addiu $v0,	$t4,	0
	# the second index
	addiu $v0,	$v0,	4
	li $v1,	2
	sw $v1,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v1,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t2,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t0,	0($v0)
	la $v0,	g_con_a
	# GEP base: @g_con_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $t1,	0($v0)
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
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
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v127
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v1,	0($v0)
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v130
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t1,	0($v0)
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v133
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v136
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t2,	0($v0)
	la $v0,	STR4
	# GEP base: @STR4
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	la $v0,	g_con_c
	# GEP base: @g_con_c
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	la $v0,	g_con_c
	# GEP base: @g_con_c
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR5
	# GEP base: @STR5
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	la $v0,	g_a
	# GEP base: @g_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v1,	0($v0)
	la $v0,	g_a
	# GEP base: @g_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t1,	0($v0)
	la $v0,	g_a
	# GEP base: @g_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t0,	0($v0)
	la $v0,	g_a
	# GEP base: @g_a
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $t2,	0($v0)
	la $v0,	STR6
	# GEP base: @STR6
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	la $v0,	g_b
	# GEP base: @g_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v164
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t2,	0($v0)
	la $v0,	g_b
	# GEP base: @g_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v167
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t1,	0($v0)
	la $v0,	g_b
	# GEP base: @g_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v170
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	la $v0,	g_b
	# GEP base: @g_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v173
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR7
	# GEP base: @STR7
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
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
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	la $v0,	g_c
	# GEP base: @g_c
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	la $v0,	g_c
	# GEP base: @g_c
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR8
	# GEP base: @STR8
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v70
	# the first index
	addiu $v0,	$s1,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	# GEP base: %v70
	# the first index
	addiu $v0,	$s1,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v70
	# the first index
	addiu $v0,	$s1,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t1,	0($v0)
	# GEP base: %v70
	# the first index
	addiu $v0,	$s1,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $t2,	0($v0)
	la $v0,	STR9
	# GEP base: @STR9
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
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
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v201
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v1,	0($v0)
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v204
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t0,	0($v0)
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v207
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t1,	0($v0)
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v210
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t2,	0($v0)
	la $v0,	STR10
	# GEP base: @STR10
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
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v81
	# the first index
	addiu $v0,	$t7,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	# GEP base: %v81
	# the first index
	addiu $v0,	$t7,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR11
	# GEP base: @STR11
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
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
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v84
	# the first index
	addiu $v0,	$t6,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t2,	0($v0)
	# GEP base: %v84
	# the first index
	addiu $v0,	$t6,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t1,	0($v0)
	# GEP base: %v84
	# the first index
	addiu $v0,	$t6,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t0,	0($v0)
	# GEP base: %v84
	# the first index
	addiu $v0,	$t6,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $v1,	0($v0)
	la $v0,	STR12
	# GEP base: @STR12
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v238
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v241
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v244
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t2,	0($v0)
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v247
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t1,	0($v0)
	la $v0,	STR13
	# GEP base: @STR13
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
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
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v108
	# the first index
	addiu $v0,	$t4,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v1,	0($v0)
	# GEP base: %v108
	# the first index
	addiu $v0,	$t4,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t0,	0($v0)
	la $v0,	STR14
	# GEP base: @STR14
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v75
	# the first index
	addiu $v0,	$s0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v262
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	# GEP base: %v264
	# the first index
	addiu $v0,	$t3,	0
	# the second index
	addiu $v0,	$v0,	0
	li $v1,	2
	sw $v1,	0($v0)
	# GEP base: %v265
	addiu $v1,	$v0,	4
	li $a0,	4
	sw $a0,	0($v1)
	# GEP base: %v265
	addiu $v1,	$v0,	8
	li $a0,	1
	sw $a0,	0($v1)
	# GEP base: %v265
	addiu $v0,	$v0,	12
	li $v1,	1
	sw $v1,	0($v0)
	# GEP base: %v264
	# the first index
	addiu $v0,	$t3,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t0,	0($v0)
	# GEP base: %v264
	# the first index
	addiu $v0,	$t3,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v264
	# the first index
	addiu $v0,	$t3,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t1,	0($v0)
	# GEP base: %v264
	# the first index
	addiu $v0,	$t3,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $t2,	0($v0)
	la $v0,	STR1
	# GEP base: @STR1
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
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
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	la $v0,	g_c
	# GEP base: @g_c
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v0,	0($v0)
	# GEP base: %v264
	# the first index
	addiu $v1,	$t3,	0
	# the second index
	addiu $v1,	$v1,	8
	lw $v1,	0($v1)
	li $a1,	-282
	# GEP base: %v282
	# the first index
	addiu $a0,	$s4,	0
	# the second index
	addiu $a0,	$a0,	0
	sw $v0,	0($a0)
	# GEP base: %v288
	addiu $v0,	$a0,	4
	sw $v1,	0($v0)
	# GEP base: %v288
	addiu $v0,	$a0,	8
	li $v1,	239
	sw $v1,	0($v0)
	# GEP base: %v288
	addiu $v0,	$a0,	12
	sw $a1,	0($v0)
	# GEP base: %v282
	# the first index
	addiu $v0,	$s4,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t1,	0($v0)
	# GEP base: %v282
	# the first index
	addiu $v0,	$s4,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	# GEP base: %v282
	# the first index
	addiu $v0,	$s4,	0
	# the second index
	addiu $v0,	$v0,	8
	lw $t0,	0($v0)
	# GEP base: %v282
	# the first index
	addiu $v0,	$s4,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $t2,	0($v0)
	la $v0,	STR6
	# GEP base: @STR6
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
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
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v306
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	li $v0,	1
	sw $v0,	0($a0)
	# GEP base: %v307
	addiu $v0,	$a0,	4
	li $v1,	9
	sw $v1,	0($v0)
	# GEP base: %v307
	addiu $v0,	$a0,	8
	li $v1,	-139
	sw $v1,	0($v0)
	# GEP base: %v307
	addiu $v0,	$a0,	12
	li $v1,	24
	sw $v1,	0($v0)
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v311
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t2,	0($v0)
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v314
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t0,	0($v0)
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v317
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t1,	0($v0)
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v320
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR7
	# GEP base: @STR7
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t1
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v329
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	li $v1,	-1823
	sw $v1,	0($v0)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v332
	# the first index
	addiu $v1,	$v0,	0
	# the second index
	addiu $v1,	$v1,	4
	# GEP base: %v101
	# the first index
	addiu $v0,	$t5,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v334
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	sw $v0,	0($v1)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v337
	# the first index
	addiu $v1,	$v0,	0
	# the second index
	addiu $v1,	$v1,	0
	# GEP base: %v84
	# the first index
	addiu $v0,	$t6,	0
	# the second index
	addiu $v0,	$v0,	12
	lw $a0,	0($v0)
	# GEP base: %v108
	# the first index
	addiu $v0,	$t4,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v0,	0($v0)
	addu $v0,	$a0,	$v0
	sw $v0,	0($v1)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v344
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	li $v1,	29424
	sw $v1,	0($v0)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v346
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t2,	0($v0)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v349
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $t0,	0($v0)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v352
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $t1,	0($v0)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v355
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v1,	0($v0)
	la $v0,	STR4
	# GEP base: @STR4
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t2
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$t0
	putint
	la $v0,	STR2
	# GEP base: @STR2
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$t1
	putint
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
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	li $v0,	19
	sw $v0,	0($s6)
	lw $v1,	0($s6)
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v365
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $a0,	0($v0)
	# GEP base: %v305
	# the first index
	addiu $v0,	$s3,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v368
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	4
	lw $v0,	0($v0)
	addu $v0,	$a0,	$v0
	# %v364 mul %v371
	mul $v0,	$v1,	$v0
	sw $v0,	0($s6)
	lw $v1,	0($s6)
	la $v0,	STR8
	# GEP base: @STR8
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v282
	# the first index
	addiu $v0,	$s4,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	li $a1,	4
	jal getArraySum
	move $v0,	$v0
	sw $v0,	0($s5)
	lw $v1,	0($s5)
	la $v0,	STR15
	# GEP base: @STR15
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v382
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	li $a1,	2
	jal getArraySum
	move $v0,	$v0
	sw $v0,	0($s5)
	lw $v1,	0($s5)
	la $v0,	STR16
	# GEP base: @STR16
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	# GEP base: %v328
	# the first index
	addiu $v0,	$s2,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	li $a1,	2
	jal getTwoDimArraySum
	move $v0,	$v0
	sw $v0,	0($s5)
	lw $v1,	0($s5)
	la $v0,	STR17
	# GEP base: @STR17
	# the first index
	addiu $a0,	$v0,	0
	# the second index
	addiu $a0,	$a0,	0
	move $a0,	$a0
	putstr
	move $a0,	$v1
	putint
	la $v0,	STR3
	# GEP base: @STR3
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	jal get_g_con_b_sum
	move $v0,	$v0
	sw $v0,	0($s5)
	lw $v1,	0($s5)
	la $v0,	STR18
	# GEP base: @STR18
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	move $a0,	$v0
	putstr
	move $a0,	$v1
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
	add $sp, 	$sp,	220
	li	$v0,	10
	syscall

getArraySum:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $t1,	-12($sp)
	sub $sp,	$sp,	28
Basic_b0_0:
	move $a1,	$a1
	move $a0,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $t1,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $a3,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $t0,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $a2,	$sp,	12
	sw $a0,	0($a2)
	sw $a1,	0($t0)
	sw $zero,	0($a3)
	sw $zero,	0($t1)
Basic_b5_1:
	lw $v1,	0($a3)
	lw $v0,	0($t0)
	bge $v1,	$v0,	Basic_b7_3
Basic_b8_4:
Basic_b6_2:
	lw $v1,	0($t1)
	lw $v0,	0($a2)
	lw $a0,	0($a3)
	# GEP base: %v13
	# %v14 mul 4
	sll $a0,	$a0,	2
	addu $a0,	$a0,	$v0
	lw $v0,	0($a0)
	addu $v0,	$v1,	$v0
	sw $v0,	0($t1)
	lw $v0,	0($a3)
	addiu $v0,	$v0,	1
	sw $v0,	0($a3)
	j Basic_b5_1
Basic_b7_3:
	lw $v0,	0($t1)
	move $v0,	$v0
	add $sp, 	$sp,	28
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $t1,	-12($sp)
	jr $ra

getTwoDimArraySum:
	sw $v1,	-4($sp)
	sw $t0,	-8($sp)
	sw $t1,	-12($sp)
	sw $t2,	-16($sp)
	sub $sp,	$sp,	36
Basic_b21_5:
	move $a1,	$a1
	move $a0,	$a0
	# alloca from the offset: 0, size is: 4
	addiu $v1,	$sp,	0
	# alloca from the offset: 4, size is: 4
	addiu $a2,	$sp,	4
	# alloca from the offset: 8, size is: 4
	addiu $a3,	$sp,	8
	# alloca from the offset: 12, size is: 4
	addiu $t1,	$sp,	12
	# alloca from the offset: 16, size is: 4
	addiu $t2,	$sp,	16
	sw $a0,	0($t2)
	sw $a1,	0($t1)
	sw $zero,	0($a3)
	sw $zero,	0($a2)
	sw $zero,	0($v1)
Basic_b27_6:
	lw $v0,	0($a3)
	lw $a0,	0($t1)
	bge $v0,	$a0,	Basic_b29_8
Basic_b30_9:
Basic_b28_7:
	sw $zero,	0($a2)
Basic_b34_10:
	lw $v0,	0($a2)
	bge $v0,	2,	Basic_b36_12
Basic_b37_13:
Basic_b35_11:
	lw $v0,	0($v1)
	lw $a0,	0($t2)
	lw $a1,	0($a3)
	lw $t0,	0($a2)
	# GEP base: %v41
	# the first index
	# %v42 mul 8
	sll $a1,	$a1,	3
	addu $a1,	$a1,	$a0
	# the second index
	# %v43 mul 4
	sll $at,	$t0,	2
	addu $a1,	$at,	$a1
	lw $a0,	0($a1)
	addu $v0,	$v0,	$a0
	sw $v0,	0($v1)
	lw $v0,	0($a2)
	addiu $v0,	$v0,	1
	sw $v0,	0($a2)
	j Basic_b34_10
Basic_b36_12:
	lw $v0,	0($a3)
	addiu $v0,	$v0,	1
	sw $v0,	0($a3)
	j Basic_b27_6
Basic_b29_8:
	lw $v0,	0($v1)
	move $v0,	$v0
	add $sp, 	$sp,	36
	lw $v1,	-4($sp)
	lw $t0,	-8($sp)
	lw $t1,	-12($sp)
	lw $t2,	-16($sp)
	jr $ra

get_g_con_b_sum:
	sw $v1,	-4($sp)
	sub $sp,	$sp,	4
Basic_b52_14:
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	# GEP base: %v53
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	la $v1,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	addiu $v1,	$v1,	0
	# GEP base: %v56
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	addiu $v1,	$v1,	4
	lw $v1,	0($v1)
	addu $v1,	$v0,	$v1
	la $v0,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	8
	# GEP base: %v60
	# the first index
	addiu $v0,	$v0,	0
	# the second index
	addiu $v0,	$v0,	0
	lw $v0,	0($v0)
	addu $v0,	$v1,	$v0
	la $v1,	g_con_b
	# GEP base: @g_con_b
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	addiu $v1,	$v1,	8
	# GEP base: %v64
	# the first index
	addiu $v1,	$v1,	0
	# the second index
	addiu $v1,	$v1,	4
	lw $v1,	0($v1)
	addu $v0,	$v0,	$v1
	move $v0,	$v0
	add $sp, 	$sp,	4
	lw $v1,	-4($sp)
	jr $ra

