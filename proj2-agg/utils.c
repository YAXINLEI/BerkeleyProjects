#include "utils.h"
#include <stdio.h>
#include <stdlib.h>

/* Sign extends the given field to a 32-bit integer where field is
 * interpreted an n-bit integer. Look in test_utils.c for examples. */ 
int sign_extend_number(unsigned int field, unsigned int n) {
    /* YOUR CODE HERE */
    if( (field>>(n-1)) == 0 ){
        int mask = 0x00000000;
        field = mask | field;
    } else {
        int mask = 0xffffffff;
        mask = (mask>>n)<<n;
        field = mask | field;
    }
    return (int) field;
}





/* Unpacks the 32-bit machine code instruction given into the correct
 * type within the instruction struct. Look at types.h */ 
Instruction parse_instruction(uint32_t instruction_bits) {
    Instruction instruction;
    /* YOUR CODE HERE */

    uint32_t temp1 = instruction_bits;
    temp1 = (temp1<<25)>>25;
    uint32_t temp2 = instruction_bits;
    temp2 = (temp2>>7);

    instruction.opcode = temp1;
    instruction.rest = temp2;
    return instruction;
}





/* Return the number of bytes (from the current PC) to the branch label using the given
 * branch instruction */
int get_branch_offset(Instruction instruction) {
    /* YOUR CODE HERE */
    uint32_t offset = instruction.sbtype.imm5;
    offset = ((offset>>1)<<1);                 //set immediate index[4:0]

    uint32_t mask_10_5 = instruction.sbtype.imm7;
    mask_10_5 = ((mask_10_5<<26)>>26)<<5;
    offset = offset | mask_10_5;              //set immediate index[10:5]

    uint32_t mask_11 = instruction.sbtype.imm5;
    mask_11 = ((mask_11<<31)>>31)<<11;   
    offset = offset | mask_11;                //set immediate index[11]

    uint32_t mask_12 = instruction.sbtype.imm7;
    mask_12 = (mask_12>>6)<<12;
    offset = offset | mask_12;                //set immediate index[12]

    offset = sign_extend_number((int) offset, 13);

    return (int) offset; 
}





/* Returns the number of bytes (from the current PC) to the jump label using the given
 * jump instruction */
int get_jump_offset(Instruction instruction) {
    /* YOUR CODE HERE */
    uint32_t offset = instruction.ujtype.imm;
    offset = ((offset<<13)>>22)<<1;                       //set immediate index [10:0]
    
    uint32_t mask_11 = instruction.ujtype.imm;
    mask_11 = ((mask_11<<23)>>31)<<11;
    offset = offset | mask_11;                       //set immediate index [1]

    uint32_t mask_19_12 = instruction.ujtype.imm;
    mask_19_12 = ((mask_19_12<<24)>>24)<<12;
    offset = offset | mask_19_12;

    uint32_t mask_20 = instruction.ujtype.imm;
    mask_20 = (mask_20>>19)<<20;
    offset = offset | mask_20;

    offset = sign_extend_number((int) offset, 20);

    return (int) offset;
 


}






/* Returns the byte offset (from the address in rs2) for storing info using the given
 * store instruction */ 
int get_store_offset(Instruction instruction) {
    /* YOUR CODE HERE */
    uint32_t offset = instruction.stype.imm5;           //set immediate index[4:0]
    uint32_t mask_11_5 = instruction.stype.imm7;
    mask_11_5 = mask_11_5<<5;
    offset = offset | mask_11_5;                       //set immediate index[11:5]

    offset = sign_extend_number((int) offset, 12);
    return (int) offset;
}





void handle_invalid_instruction(Instruction instruction) {
    printf("Invalid Instruction: 0x%08x\n", instruction.bits); 
    exit(-1);
}

void handle_invalid_read(Address address) {
    printf("Bad Read. Address: 0x%08x\n", address);
    exit(-1);
}

void handle_invalid_write(Address address) {
    printf("Bad Write. Address: 0x%08x\n", address);
    exit(-1);
}

