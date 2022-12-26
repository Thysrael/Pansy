# ---> init memory <---
addi $1, $0, -9669
sw $1, 0($0)
addi $1, $0, 22910
sw $1, 4($0)
addi $1, $0, 16177
sw $1, 8($0)
addi $1, $0, 31813
sw $1, 12($0)
addi $1, $0, 21948
sw $1, 16($0)
addi $1, $0, -25495
sw $1, 20($0)
addi $1, $0, 28578
sw $1, 24($0)
addi $1, $0, -15267
sw $1, 28($0)
addi $1, $0, 16748
sw $1, 32($0)
addi $1, $0, -19625
sw $1, 36($0)
addi $1, $0, 8537
sw $1, 40($0)
addi $1, $0, 17725
sw $1, 44($0)
addi $1, $0, 11929
sw $1, 48($0)
addi $1, $0, 10639
sw $1, 52($0)
addi $1, $0, -26693
sw $1, 56($0)
addi $1, $0, -29404
sw $1, 60($0)
addi $1, $0, 19265
sw $1, 64($0)
addi $1, $0, -7634
sw $1, 68($0)
addi $1, $0, 11585
sw $1, 72($0)

# ---> init hi lo <---
addi $1, $0, 17019
mthi $1
addi $1, $0, 24416
mtlo $1

# ---> init register <---
addi $1, $0, 21664
addi $2, $0, 17274
addi $3, $0, -709
addi $4, $0, 21716
addi $5, $0, 18976
addi $6, $0, -32217
addi $7, $0, 8211
addi $8, $0, 31109
addi $9, $0, -12942
addi $10, $0, -16597
addi $11, $0, -30657
addi $12, $0, -11935
addi $13, $0, 25954
addi $14, $0, 6535
addi $15, $0, -13571
addi $16, $0, -19235
addi $17, $0, -20948
addi $18, $0, -24116
addi $19, $0, -18924
addi $20, $0, 3895
addi $21, $0, 26439
addi $22, $0, 4823
addi $23, $0, -9902
addi $24, $0, -6218
addi $25, $0, 22300
addi $26, $0, -26087
addi $27, $0, -6180
addi $28, $0, 29020
addi $29, $0, 22105
addi $30, $0, -62
addi $31, $0, -8745

# ---> start at 3120 <---

# ---> block head <---
# ---> block body <---
mfhi $20
mflo $20
lui $20, 26668
mult $20, $19
mfhi $19
mflo $19
# ---> block tail <---
addi $19, $0, -17936
addi $20, $0, 12160
# ---> endpc 3140 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
lui $0, 40768
multu $21, $0
mfhi $21
mflo $21
# ---> block tail <---
# ---> endpc 3158 <---

# ---> block head <---
lui $19, 43707
ori $19, $19, 52445
sw $19, 80($0)
# ---> block body <---
mfhi $22
mflo $22
lui $22, 36192
sb $22, -21581($1)
# ---> block tail <---
addi $19, $0, 10286
addi $22, $0, 2950
# ---> endpc 317c <---

# ---> block head <---
lui $29, 43707
ori $29, $29, 52445
sw $29, 96($0)
# ---> block body <---
mfhi $24
mflo $23
lui $23, 6514
sh $23, -28922($28)
# ---> block tail <---
addi $23, $0, -27923
addi $29, $0, 4568
# ---> endpc 31a0 <---

# ---> block head <---
lui $10, 43707
ori $10, $10, 52445
sw $10, 124($0)
lui $11, 43707
ori $11, $11, 52445
sw $11, 132($0)
# ---> block body <---
mfhi $0
mflo $0
lui $0, 39240
sw $25, 124($0)
sw $0, -22168($25)
# ---> block tail <---
addi $10, $0, -7659
addi $11, $0, 12204
# ---> endpc 31d4 <---

# ---> block head <---
addi $13, $0, 16
# ---> block body <---
mfhi $31
mflo $31
jal TAG_JAL_1
lb $14, -12768($31)
add $31, $31, $13
TAG_JAL_1:
# ---> block tail <---
# ---> endpc 31ec <---

# ---> block head <---
addi $15, $0, 16
# ---> block body <---
mfhi $31
mflo $31
jal TAG_JAL_3
lh $26, -12740($31)
add $31, $31, $15
TAG_JAL_3:
# ---> block tail <---
# ---> endpc 3204 <---

# ---> block head <---
addi $17, $0, 16
# ---> block body <---
mfhi $0
mflo $0
jal TAG_JAL_5
lw $31, 32($0)
add $31, $31, $17
TAG_JAL_5:
# ---> block tail <---
# ---> endpc 321c <---

# ---> block head <---
addi $19, $0, 16
# ---> block body <---
mfhi $31
mflo $31
jal TAG_JAL_7
add $31, $31, $20
add $31, $31, $19
TAG_JAL_7:
# ---> block tail <---
# ---> endpc 3234 <---

# ---> block head <---
addi $22, $0, 16
# ---> block body <---
mfhi $0
mflo $0
jal TAG_JAL_10
ori $31, $0, 21035
add $31, $31, $22
TAG_JAL_10:
# ---> block tail <---
# ---> endpc 324c <---

# ---> block head <---
addi $24, $0, 16
# ---> block body <---
mfhi $0
mflo $0
jal TAG_JAL_12
mthi $0
add $31, $31, $24
TAG_JAL_12:
mfhi $31
mflo $31
# ---> block tail <---
# ---> endpc 326c <---

# ---> block head <---
addi $20, $0, 16
lui $21, 43707
ori $21, $21, 52445
sw $21, 120($0)
lui $22, 43707
ori $22, $22, 52445
sw $22, 108($0)
# ---> block body <---
mfhi $31
mflo $31
jal TAG_JAL_20
sb $1, -12833($31)
add $31, $31, $20
TAG_JAL_20:
sb $31, -21555($1)
# ---> block tail <---
addi $21, $0, 23131
addi $22, $0, -18806
# ---> endpc 32a8 <---

# ---> block head <---
nop
addi $7, $0, 16
lui $8, 43707
ori $8, $8, 52445
sw $8, 92($0)
# ---> block body <---
mfhi $0
mflo $0
jal TAG_JAL_26
sw $31, 108($0)
add $31, $31, $7
TAG_JAL_26:
sw $0, -12916($31)
# ---> block tail <---
addi $8, $0, 7768
# ---> endpc 32d8 <---

# ---> block head <---
# ---> block body <---
mfhi $7
mflo $7
slt $7, $9, $7
lb $10, 31($7)
# ---> block tail <---
# ---> endpc 32e8 <---

# ---> block head <---
# ---> block body <---
sltu $8, $8, $9
lh $9, 27($8)
# ---> block tail <---
# ---> endpc 32f0 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
sub $0, $0, $10
lw $10, 68($0)
# ---> block tail <---
# ---> endpc 3300 <---

# ---> block head <---
# ---> block body <---
mfhi $11
mflo $11
add $11, $10, $11
slt $11, $12, $11
# ---> block tail <---
# ---> endpc 3310 <---

# ---> block head <---
# ---> block body <---
mfhi $13
mflo $12
and $12, $12, $13
sltu $12, $13, $12
# ---> block tail <---
# ---> endpc 3320 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
or $0, $14, $0
sub $14, $14, $0
# ---> block tail <---
# ---> endpc 3330 <---

# ---> block head <---
addi $17, $0, 1
# ---> block body <---
mfhi $15
mflo $15
slt $15, $15, $14
beq $17, $15, TAG_BEQ_1
addi $18, $0, 1
addi $18, $0, 1
TAG_BEQ_1:
# ---> block tail <---
# ---> endpc 334c <---

# ---> block head <---
# ---> block body <---
mfhi $16
mflo $17
sltu $16, $16, $17
bne $0, $16, TAG_BNE_0
addi $15, $0, 1
addi $15, $0, 1
TAG_BNE_0:
# ---> block tail <---
# ---> endpc 3364 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
sub $0, $18, $0
beq $18, $0, TAG_BEQ_2
addi $15, $0, 1
addi $15, $0, 1
TAG_BEQ_2:
# ---> block tail <---
# ---> endpc 337c <---

# ---> block head <---
# ---> block body <---
mfhi $19
mflo $19
add $19, $19, $15
addi $18, $19, 13324
# ---> block tail <---
# ---> endpc 338c <---

# ---> block head <---
# ---> block body <---
mfhi $21
mflo $21
and $20, $20, $21
andi $21, $20, 13224
# ---> block tail <---
# ---> endpc 339c <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
or $0, $22, $0
ori $22, $0, 40843
# ---> block tail <---
addi $22, $0, -29772
# ---> endpc 33b0 <---

# ---> block head <---
# ---> block body <---
mfhi $23
mflo $23
slt $23, $18, $23
mtlo $23
mfhi $23
mflo $23
# ---> block tail <---
# ---> endpc 33c8 <---

# ---> block head <---
# ---> block body <---
mfhi $25
mflo $24
sltu $24, $25, $24
mult $24, $25
mfhi $24
mflo $24
# ---> block tail <---
# ---> endpc 33e0 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
sub $0, $0, $26
multu $0, $26
mfhi $26
mflo $26
# ---> block tail <---
# ---> endpc 33f8 <---

# ---> block head <---
lui $13, 43707
ori $13, $13, 52445
sw $13, 92($0)
lui $14, 43707
ori $14, $14, 52445
sw $14, 100($0)
# ---> block body <---
mfhi $27
mflo $27
add $27, $27, $11
sb $27, 95($12)
sb $12, 99($27)
# ---> block tail <---
addi $13, $0, 22779
addi $14, $0, -27872
# ---> endpc 342c <---

# ---> block head <---
lui $2, 43707
ori $2, $2, 52445
sw $2, 152($0)
lui $3, 43707
ori $3, $3, 52445
sw $3, 120($0)
# ---> block body <---
mfhi $28
mflo $28
and $28, $28, $29
sh $28, -4416($29)
sh $29, 122($28)
# ---> block tail <---
addi $2, $0, -8509
addi $3, $0, -7394
# ---> endpc 3460 <---

# ---> block head <---
nop
nop
nop
# ---> block body <---
mfhi $0
mflo $0
or $0, $30, $0
sw $0, 96($0)
sw $0, 80($0)
# ---> block tail <---
# ---> endpc 3480 <---

# ---> block head <---
# ---> block body <---
mfhi $5
mflo $5
addi $5, $5, 24669
lb $8, -24637($5)
# ---> block tail <---
# ---> endpc 3490 <---

# ---> block head <---
# ---> block body <---
mfhi $7
mflo $7
andi $6, $7, 11065
lh $7, 8($6)
# ---> block tail <---
# ---> endpc 34a0 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
ori $0, $0, 1
lw $8, 36($0)
# ---> block tail <---
# ---> endpc 34b0 <---

# ---> block head <---
# ---> block body <---
mfhi $9
mflo $9
addi $9, $9, 21873
add $9, $8, $9
# ---> block tail <---
# ---> endpc 34c0 <---

# ---> block head <---
# ---> block body <---
mfhi $10
mflo $10
andi $10, $11, 24
and $10, $10, $11
# ---> block tail <---
# ---> endpc 34d0 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
ori $0, $0, 1
or $12, $12, $0
# ---> block tail <---
# ---> endpc 34e0 <---

# ---> block head <---
addi $11, $0, -28553
# ---> block body <---
mfhi $13
mflo $13
addi $13, $13, -28553
bne $13, $11, TAG_BNE_2
addi $12, $0, 1
addi $12, $0, 1
TAG_BNE_2:
# ---> block tail <---
# ---> endpc 34fc <---

# ---> block head <---
# ---> block body <---
mfhi $15
mflo $15
andi $14, $15, 2
beq $0, $14, TAG_BEQ_3
addi $12, $0, 1
addi $12, $0, 1
TAG_BEQ_3:
# ---> block tail <---
# ---> endpc 3514 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
ori $0, $0, 4
bne $16, $0, TAG_BNE_3
addi $12, $0, 1
addi $12, $0, 1
TAG_BNE_3:
# ---> block tail <---
# ---> endpc 352c <---

# ---> block head <---
# ---> block body <---
mfhi $17
mflo $17
addi $17, $17, -27537
addi $12, $17, -17022
# ---> block tail <---
addi $12, $0, -1337
# ---> endpc 3540 <---

# ---> block head <---
# ---> block body <---
mfhi $18
mflo $19
andi $19, $18, 23
andi $18, $19, 64605
# ---> block tail <---
# ---> endpc 3550 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
ori $0, $0, 4
ori $20, $0, 42082
# ---> block tail <---
addi $20, $0, -2491
# ---> endpc 3564 <---

# ---> block head <---
# ---> block body <---
mfhi $21
mflo $21
addi $21, $21, 28661
div $12, $21
mfhi $21
mflo $21
# ---> block tail <---
# ---> endpc 357c <---

# ---> block head <---
# ---> block body <---
mfhi $22
mflo $23
ori $23, $22, 26775
divu $22, $23
mfhi $22
mflo $22
# ---> block tail <---
# ---> endpc 3594 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
andi $0, $24, 23
mthi $0
mfhi $24
mflo $24
# ---> block tail <---
# ---> endpc 35ac <---

# ---> block head <---
nop
nop
lui $24, 43707
ori $24, $24, 52445
sw $24, 116($0)
# ---> block body <---
mfhi $25
mflo $25
addi $25, $25, 1798
sb $25, 1437($23)
sb $23, -1680($25)
# ---> block tail <---
addi $24, $0, -420
# ---> endpc 35d8 <---

# ---> block head <---
lui $14, 43707
ori $14, $14, 52445
sw $14, 120($0)
lui $15, 43707
ori $15, $15, 52445
sw $15, 156($0)
# ---> block body <---
mfhi $26
mflo $27
andi $26, $27, 29816
sh $26, 120($27)
sh $27, 156($26)
# ---> block tail <---
addi $14, $0, 9063
addi $15, $0, -1693
# ---> endpc 360c <---

# ---> block head <---
nop
nop
lui $23, 43707
ori $23, $23, 52445
sw $23, 92($0)
# ---> block body <---
mfhi $0
mflo $0
ori $0, $0, 2
sw $0, 116($0)
sw $0, 92($0)
# ---> block tail <---
addi $23, $0, 4864
# ---> endpc 3638 <---

# ---> block head <---
# ---> block body <---
mfhi $3
mflo $3
nop
lb $24, 52($3)
# ---> block tail <---
# ---> endpc 3648 <---

# ---> block head <---
# ---> block body <---
mfhi $4
mflo $4
nop
lh $5, 28($4)
# ---> block tail <---
# ---> endpc 3658 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
nop
lw $6, 76($0)
# ---> block tail <---
# ---> endpc 3668 <---

# ---> block head <---
# ---> block body <---
mfhi $7
mflo $7
nop
slt $7, $7, $24
# ---> block tail <---
# ---> endpc 3678 <---

# ---> block head <---
# ---> block body <---
mfhi $9
mflo $9
nop
sltu $8, $9, $8
# ---> block tail <---
# ---> endpc 3688 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
nop
sub $10, $0, $10
# ---> block tail <---
# ---> endpc 3698 <---

# ---> block head <---
# ---> block body <---
mfhi $11
mflo $11
nop
beq $0, $11, TAG_BEQ_4
addi $24, $0, 1
addi $24, $0, 1
TAG_BEQ_4:
# ---> block tail <---
# ---> endpc 36b0 <---

# ---> block head <---
addi $25, $0, 0
# ---> block body <---
mfhi $13
mflo $13
nop
bne $25, $13, TAG_BNE_5
addi $26, $0, 1
addi $26, $0, 1
TAG_BNE_5:
# ---> block tail <---
# ---> endpc 36cc <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
nop
beq $0, $14, TAG_BEQ_5
addi $26, $0, 1
addi $26, $0, 1
TAG_BEQ_5:
# ---> block tail <---
# ---> endpc 36e4 <---

# ---> block head <---
# ---> block body <---
mfhi $15
mflo $15
nop
addi $26, $15, -7422
# ---> block tail <---
# ---> endpc 36f4 <---

# ---> block head <---
# ---> block body <---
mfhi $17
mflo $16
nop
andi $16, $17, 52038
# ---> block tail <---
# ---> endpc 3704 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
nop
ori $18, $0, 20811
# ---> block tail <---
# ---> endpc 3714 <---

# ---> block head <---
# ---> block body <---
mfhi $19
mflo $19
nop
mtlo $19
mfhi $19
mflo $19
# ---> block tail <---
# ---> endpc 372c <---

# ---> block head <---
# ---> block body <---
mfhi $20
mflo $20
nop
mult $20, $21
mfhi $20
mflo $20
# ---> block tail <---
# ---> endpc 3744 <---

# ---> block head <---
# ---> block body <---
mfhi $0
mflo $0
nop
multu $22, $0
mfhi $22
mflo $22
# ---> block tail <---
# ---> endpc 375c <---

# ---> block head <---
lui $12, 43707
ori $12, $12, 52445
sw $12, 92($0)
lui $13, 43707
ori $13, $13, 52445
sw $13, 92($0)
# ---> block body <---
mfhi $23
mflo $23
nop
sb $23, 94($11)
sb $11, 95($23)
# ---> block tail <---
addi $12, $0, -29756
addi $13, $0, -22870
# ---> endpc 3790 <---

# ---> block head <---
lui $2, 43707
ori $2, $2, 52445
sw $2, 144($0)
lui $3, 43707
ori $3, $3, 52445
sw $3, 144($0)
# ---> block body <---
mfhi $24
mflo $24
nop
sh $25, 144($24)
sh $24, 146($25)
# ---> block tail <---
addi $2, $0, -27273
addi $3, $0, 1683
# ---> endpc 37c4 <---

# ---> block head <---
lui $14, 43707
ori $14, $14, 52445
sw $14, 96($0)
lui $15, 43707
ori $15, $15, 52445
sw $15, 92($0)
# ---> block body <---
mfhi $0
mflo $0
nop
sw $0, 96($0)
sw $0, 92($0)
# ---> block tail <---
addi $14, $0, 29268
addi $15, $0, -23921
# ---> endpc 37f8 <---

TAG_FINAL_2022_11_12_17_14_53_0:
beq $0, $0, TAG_FINAL_2022_11_12_17_14_53_0
nop
