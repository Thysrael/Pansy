# Pansy Say "Hi~" to you!
.data
STR0:
.asciiz	"\n"

.text
_start:
	jal main
	li	$v0,	10
	syscall

putstr:
	li	$v0,	4
	syscall
	jr	$ra

putint:
	li	$v0,	1
	syscall
	jr	$ra

getint:
	li	$v0,	5
	syscall
	jr	$ra

main:
	sw	$v1,	-4($sp)
	sw	$ra,	-8($sp)
	sub	$sp,	$sp,	20
Basic_0_0:
	add	$a1,	$sp,	0
	add	$v1,	$sp,	4
	add	$a0,	$sp,	8
	li	$v0,	3
	sw	$v0,	0($a0)
	li	$v0,	4
	sw	$v0,	0($v1)
	li	$v0,	5
	sw	$v0,	0($a1)
	lw	$a0,	0($a0)
	lw	$v0,	0($v1)
	lw	$v1,	0($a1)
	mul	$v0,	$v0,	$v1
	add	$a0,	$a0,	$v0
	move	$a0,	$a0
	jal	putint
	la	$v0,	STR0
	add	$v0,	$v0,	0
	add	$v0,	$v0,	0
	move	$a0,	$v0
	jal	putstr
	li	$v0,	0
	add	$sp, 	$sp,	20
	lw	$v1,	-4($sp)
	lw	$ra,	-8($sp)
	jr	$ra
