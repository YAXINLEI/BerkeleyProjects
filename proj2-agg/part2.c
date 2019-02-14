#include <stdio.h> // for stderr
#include <stdlib.h> // for exit()
#include "types.h"
#include "utils.h"
#include "riscv.h"

void execute_rtype(Instruction, Processor *);
void execute_itype_except_load(Instruction, Processor *);
void execute_branch(Instruction, Processor *);
void execute_jal(Instruction, Processor *);
void execute_jalr(Instruction, Processor *);
void execute_load(Instruction, Processor *, Byte *);
void execute_store(Instruction, Processor *, Byte *);
void execute_ecall(Processor *, Byte *);
void execute_lui(Instruction, Processor *);
void execute_auipc(Instruction, Processor *);


Word makemask(unsigned x, unsigned y) {
    unsigned r = 0;
    for (unsigned i=x; i<=y; i++)
        r = r | (1 << i);
    return (Word)r;
}


void execute_instruction(uint32_t instruction_bits, Processor *processor,Byte *memory) {    
    Instruction instruction = parse_instruction(instruction_bits); // Look in utils.c
    switch(instruction.opcode) {
        case 0x33:
            /* YOUR CODE HERE */
            execute_rtype(instruction, processor);
            processor->PC += 4;
            break;
        case 0x13:
            /* YOUR CODE HERE */
            execute_itype_except_load(instruction, processor);
            processor->PC += 4;
            break;
        case 0x3:
            /* YOUR CODE HERE */
            execute_load(instruction, processor, memory);
            processor->PC += 4;
            break;
        case 0x67:
            /* YOUR CODE HERE */
            execute_jalr(instruction, processor);
            break;
        case 0x23:
            /* YOUR CODE HERE */
            execute_store(instruction, processor, memory);
            processor->PC += 4;
            break;
        case 0x63:
            /* YOUR CODE HERE */
            execute_branch(instruction, processor);
            break;
        case 0x37:
            /* YOUR CODE HERE */
            execute_lui(instruction, processor);
            processor->PC += 4;
            break;
        case 0x17:
            /* YOUR CODE HERE */
            execute_auipc(instruction, processor);
            processor->PC += 4;
            break;
        case 0x6F:
            /* YOUR CODE HERE */
            execute_jal(instruction, processor);
            break;
        case 0x73:
            /* YOUR CODE HERE */
            execute_ecall(processor, memory);
            break;
        default: // undefined opcode
            handle_invalid_instruction(instruction);
            break;
    }
}


void execute_rtype(Instruction instruction, Processor *processor) {
    switch (instruction.rtype.funct3){
        case 0x0:
            /* YOUR CODE HERE */
             switch(instruction.rtype.funct7){
                case 0x00:         //add
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1]+(sWord)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x01:         //mul
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1]*(sWord)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x20:         //sub
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1]-(sWord)processor->R[instruction.rtype.rs2]);
                    break;
             }
            break;
        case 0x1:
            /* YOUR CODE HERE */
            switch(instruction.rtype.funct7){
                case 0x00:               //sll
                    processor->R[instruction.rtype.rd] = (Word)processor->R[instruction.rtype.rs1] << (Word)processor->R[instruction.rtype.rs2];
                    break;
                case 0x01:              //mulh
                    ;
                    sDouble d = (sDouble)((sDouble)processor->R[instruction.rtype.rs1] * (sDouble)processor->R[instruction.rtype.rs2]);
                    d = d >> 32;
                    processor->R[instruction.rtype.rd] = (Word)d;
                    break;
            }   
            break;
        case 0x2:
            /* YOUR CODE HERE */        //slt
            if((sWord)processor->R[instruction.rtype.rs1] < (sWord)processor->R[instruction.rtype.rs2]){
                processor->R[instruction.rtype.rd] = (Word)1;
            }else{
                processor->R[instruction.rtype.rd] = (Word)0;
            }
            break;
        case 0x3:
            /* YOUR CODE HERE */       //sltu
            switch(instruction.rtype.funct7){
                case 0x00:
                    if(processor->R[instruction.rtype.rs1] < processor->R[instruction.rtype.rs2]){
                        processor->R[instruction.rtype.rd] = (Word)1;
                    }else{
                        processor->R[instruction.rtype.rd] = (Word)0;
                    }
                    break;
                case 0x01:             //mulhu
                    ;
                    Double d = (Double)((Double)processor->R[instruction.rtype.rs1] * (Double)processor->R[instruction.rtype.rs2]);
                    d = d >>32;
                    processor->R[instruction.rtype.rd] = (Word)d;
                    break;
            }
            break;
        case 0x4:
            switch(instruction.rtype.funct7){
                case 0x00:           //XOR
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] ^ (Word)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x01:          //div
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1] / (sWord)processor->R[instruction.rtype.rs2]);
                    break;
            }
            break;
        case 0x5:
            /* YOUR CODE HERE */
            switch(instruction.rtype.funct7){
                case 0x00:       //srl
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] >> (Word)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x01:      //divu
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] / (Word)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x20:      //sra
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1] >> (Word)processor->R[instruction.rtype.rs2]);
                    break;
            }
            break;
        case 0x6:
            /* YOUR CODE HERE */
            switch(instruction.rtype.funct7){
                case 0x00:       //or
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] | (Word)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x01:       //rem
                    processor->R[instruction.rtype.rd] = (Word)((sWord)processor->R[instruction.rtype.rs1] % (sWord)processor->R[instruction.rtype.rs2]);
                    break;
            }
            break;
        case 0x7:
            /* YOUR CODE HERE */
            switch(instruction.rtype.funct7){
                case 0x00:       //and
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] & (Word)processor->R[instruction.rtype.rs2]);
                    break;
                case 0x01:       //remu
                    processor->R[instruction.rtype.rd] = (Word)((Word)processor->R[instruction.rtype.rs1] % (Word)processor->R[instruction.rtype.rs2]);
                    break;
            }
            break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_itype_except_load(Instruction instruction, Processor *processor) {
    switch (instruction.itype.funct3) {
        case 0x0:                     //addi
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)((sWord)processor->R[instruction.itype.rs1] + (sWord)sign_extend_number(instruction.itype.imm, 12));
            break;
        case 0x1:                     //slli
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)((Word)processor->R[instruction.itype.rs1] << (Word)sign_extend_number(instruction.itype.imm, 12));
            break;
        case 0x2:                      //slti
            /* YOUR CODE HERE */
            if((sWord)processor->R[instruction.itype.rs1] < (sWord)sign_extend_number(instruction.itype.imm, 12)){
                processor->R[instruction.itype.rd] = (Word)1;
            }else{
                processor->R[instruction.itype.rd] = (Word)0;
            }
            break;
        case 0x3:                   //sltiu
            /* YOUR CODE HERE */
            if((Word)processor->R[instruction.itype.rs1] < (Word)instruction.itype.imm){
                processor->R[instruction.itype.rd] = (Word)1;
            } else {
                processor->R[instruction.itype.rd] = (Word)0;
            }
            break;
        case 0x4:                 //xori
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)((Word)processor->R[instruction.itype.rs1] ^ (Word)sign_extend_number(instruction.itype.imm, 12));
            break;
        case 0x5:                    //srli??????????????????????????
            /* YOUR CODE HERE */
            switch(instruction.itype.imm>>5){
                case 0x00:
                    processor->R[instruction.itype.rd] = (Word)(processor->R[instruction.itype.rs1] >> sign_extend_number(instruction.itype.imm,12));
                    break;
                case 0b0100000:        //srai
                    processor->R[instruction.itype.rd] = (Word)((sWord)processor->R[instruction.itype.rs1] >> (Word)(instruction.itype.imm&(~0b010000000000)));
                    break;
            }
            break;
        case 0x6:           //ori
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)(processor->R[instruction.itype.rs1] | (Word)sign_extend_number(instruction.itype.imm, 12));
            break;
        case 0x7:
            processor->R[instruction.itype.rd] = (Word)(processor->R[instruction.itype.rs1] & (Word)sign_extend_number(instruction.itype.imm, 12));
            break;
        default:
            handle_invalid_instruction(instruction);
            break;
    }
}

void execute_ecall(Processor *p, Byte *memory) {

    Register i;
    
    // What do we switch on?
    switch(p->R[10]) {
        case 1: // print an integer
            printf("%d",p->R[11]);
            p->PC += 4;
            break;
        case 4: // print a string
            for(i = p->R[11]; i < MEMORY_SPACE && load(memory, i, LENGTH_BYTE); i++) {
                printf("%c",load(memory,i,LENGTH_BYTE));
            }
            p->PC += 4;
            break;
        case 10: // exit
            printf("exiting the simulator\n");
            exit(0);
            break;
        case 11: // print a character
            printf("%c",p->R[11]);
            p->PC += 4;
            break;
        default: // undefined ecall
            printf("Illegal ecall number %d\n", p->R[10]);
            exit(-1);
            break;
    }
}

void execute_branch(Instruction instruction, Processor *processor) {
    
    //mycode
    int branch_num = 4;

    switch (instruction.sbtype.funct3) {
        case 0x0:      //beq
            /* YOUR CODE HERE */
            if ((sWord)processor->R[instruction.sbtype.rs1] == (sWord)processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            break;
        case 0x1:      //bne
            if ((sWord)processor->R[instruction.sbtype.rs1] != (sWord)processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            /* YOUR CODE HERE */
            break;
        case 0x4:        //blt
            /* YOUR CODE HERE */
            if ( (sWord)processor->R[instruction.sbtype.rs1] < (sWord)processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            break;
        case 0x5:       //bge
            /* YOUR CODE HERE */
            if ((sWord)processor->R[instruction.sbtype.rs1] >= (sWord)processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            break;
        case 0x6:       //bltu
            /* YOUR CODE HERE */
            if (processor->R[instruction.sbtype.rs1] < processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            break;
        case 0x7:        //bgeu
            /* YOUR CODE HERE */
            if (processor->R[instruction.sbtype.rs1] >= processor->R[instruction.sbtype.rs2]){
                branch_num = (sWord)get_branch_offset(instruction);
            }
            break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
    processor->PC += branch_num;
}



void execute_load(Instruction instruction, Processor *processor, Byte *memory) {
    switch (instruction.itype.funct3) {
        case 0x0:       //lb
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)sign_extend_number(load(memory, (sWord)processor->R[instruction.itype.rs1]+(sWord)sign_extend_number(instruction.itype.imm, 12), LENGTH_BYTE),8);
            break;
        case 0x1:       //lh
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)sign_extend_number(
                load(memory, (sWord)processor->R[instruction.itype.rs1]+(sWord)sign_extend_number(instruction.itype.imm, 12), LENGTH_HALF_WORD),16);
            break;
        case 0x2:       //lw
            /* YOUR CODE HERE */
            processor->R[instruction.itype.rd] = (Word)load(memory, (sWord)processor->R[instruction.itype.rs1]+(sWord)sign_extend_number(instruction.itype.imm, 12), LENGTH_WORD);
            break;
        case 0x4:       //lbu
            processor->R[instruction.itype.rd] = (Word)load(memory, (sWord)processor->R[instruction.itype.rs1]+(sWord)sign_extend_number(instruction.itype.imm, 12), LENGTH_BYTE);
            break;
        case 0x5:       //lhu
             processor->R[instruction.itype.rd] = (Word)load(memory, (sWord)processor->R[instruction.itype.rs1]+(sWord)sign_extend_number(instruction.itype.imm, 12), LENGTH_HALF_WORD);
            break;
        default:
            handle_invalid_instruction(instruction);
            break;
    }
}

void execute_store(Instruction instruction, Processor *processor, Byte *memory) {
    switch (instruction.stype.funct3) {
        case 0x0:          //sb
            /* YOUR CODE HERE */
            store(memory, (sWord)processor->R[instruction.stype.rs1] + (sWord)get_store_offset(instruction), LENGTH_BYTE, processor->R[instruction.stype.rs2]);
            break;
        case 0x1:         //sh
            /* YOUR CODE HERE */
            store(memory, (sWord)processor->R[instruction.stype.rs1] + (sWord)get_store_offset(instruction), LENGTH_HALF_WORD, processor->R[instruction.stype.rs2]);
            break;
        case 0x2:         //swƒƒ
            /* YOUR CODE HERE */
            store(memory, (sWord)processor->R[instruction.stype.rs1] + (sWord)get_store_offset(instruction), LENGTH_WORD, processor->R[instruction.stype.rs2]);
            break;
        default:
            handle_invalid_instruction(instruction);
            exit(-1);
            break;
    }
}

void execute_jal(Instruction instruction, Processor *processor) {
    /* YOUR CODE HERE */
    ;
    Word next_PC = processor->PC + (sWord)get_jump_offset(instruction);
    processor->R[instruction.ujtype.rd] = processor->PC + 4;
    processor->PC = next_PC;
}

void execute_jalr(Instruction instruction, Processor *processor) {
    /* YOUR CODE HERE */
    ;
    Word next_PC = (Word)(sign_extend_number(instruction.itype.imm,12) + (sWord)processor->R[instruction.itype.rs1]);
    processor->R[instruction.itype.rd] = processor->PC + 4;
    processor->PC = next_PC;
}

void execute_lui(Instruction instruction, Processor *processor) {
    /* YOUR CODE HERE */
    processor->R[instruction.utype.rd] = (Word)((sWord)sign_extend_number(instruction.utype.imm,20) << 12);
}

void execute_auipc(Instruction instruction, Processor *processor) {
    processor->R[instruction.utype.rd] = (Word)(((sWord)sign_extend_number(instruction.utype.imm,20) << 12) + processor->PC);
}

void store(Byte *memory, Address address, Alignment alignment, Word value) {
    /* YOUR CODE HERE */
    if(address >= MEMORY_SPACE){
        handle_invalid_write(address);
    }
    switch(alignment){
        case LENGTH_BYTE:
            memory[address] = (Byte)(value&0x000000ff);
            break;
        case LENGTH_HALF_WORD:
            memory[address] = (Byte)(value&0x000000ff);
            memory[address+1] = (Byte)((value&0x0000ff00)>>8);
            break;
        case LENGTH_WORD:
            memory[address] = (Byte)(value&0x000000ff);
            memory[address+1] = (Byte)((value&0x0000ff00)>>8);
            memory[address+2] = (Byte)((value&0x00ff0000)>>16);
            memory[address+3] = (Byte)((value&0xff000000)>>24);
            break;
        default:
            break;
    }
}

Word load(Byte *memory, Address address, Alignment alignment) {
    /* YOUR CODE HERE */
    ;
    Word d = 0x00000000;
    if(address >= MEMORY_SPACE){
        handle_invalid_read(address);
    }
    switch(alignment){
        case LENGTH_BYTE:
            d =  d | (Word)memory[address];
            break;
        case LENGTH_HALF_WORD:
            d =  d | (Word)memory[address];
            d =  d | ((Word)memory[address + 1] << 8);
            break;
        case LENGTH_WORD:
            d =  d | (Word)memory[address];
            d =  d | ((Word)memory[address + 1] << 8);
            d =  d | ((Word)memory[address + 2] << 16);
            d =  d | ((Word)memory[address + 3] << 24);
            break;
        default:
            break;
    }
    return d;
}
