init:
    add $28,$0,$0
    add $29,$0,$0
    add $30,$0,$0
    add $31,$0,$0
    ori $28,$28,4
    nop
    nop
    nop
block_0:
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    lui $8,0x0000
    ori $8,$8,0x228b
    lui $23,0xffff
    ori $23,$23,0xc411
    lui $15,0xffff
    ori $15,$15,0xf41d
    lui $21,0x0000
    ori $21,$21,0x0cc6
    lui $10,0xffff
    ori $10,$10,0xa772
    nop
    nop
    nop
    ori $23,$0,17490
    sub $23,$0,$23
    nop
    nop
    nop
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    add $10,$15,$21
    add $10,$15,$21
    add $10,$15,$21
    add $10,$15,$21
    add $10,$15,$21
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,17490($23)
    sw $8,0($29)
    add $29,$29,$28
    sw $23,0($29)
    add $29,$29,$28
    sw $15,0($29)
    add $29,$29,$28
    sw $21,0($29)
    add $29,$29,$28
    sw $10,0($29)
    add $29,$29,$28
    sw $8,0($29)
    add $29,$29,$28
    sw $23,0($29)
    add $29,$29,$28
    beq $0,$0,block_1
    nop
    
block_1:
    beq $0,$0,block_1
    nop
