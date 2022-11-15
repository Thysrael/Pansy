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
a_to_the_a:
.word	0

cnt:
.word	0

n:
.word	0

STR0:
.asciiz	"funcTest: move disk from "

STR1:
.asciiz	" to "

STR2:
.asciiz	"\n"

.text
main:
	sub $sp,	$sp,	4
Basic_b42_8:
	# 778 div 389
	li $v1,	778
	li $v0,	-1468459255
	# mthi $v1
	mult $v1,	$v0
	mfhi $v0
	add $11, $v1, $v0
	sra $v0,	$v0,	8
	srl $at,	$v1,	31
	addu $v1,	$v0,	$at
	li $v0,	389
	mul $v0,	$v1,	$v0
	li $v1,	778
	subu $a0,	$v1,	$v0
	move $a0,	$a0
	putint