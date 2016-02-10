// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/06/max/Min.asm

// Computes Min = min(M[0], M[1])  where M stands for RAM and Min is a symbol
// defined in this program

   @10
   D=A
   @1
   D=A-D
   @0
   M=D

   @11
   D=A
   @1
   M=D


   @0
   D=M              // D = first number
   @1
   D=D-M            // D = first number - second number
   @OUTPUT_FIRST
   D;JLT            // if D>0 (first is lower) goto output_first
   @1
   D=M              // D = second number
   @OUTPUT_D
   0;JMP            // goto output_d
(OUTPUT_FIRST)
   @0             
   D=M              // D = first number
(OUTPUT_D)
   @Min
   M=D              // Min = D (minimum number)
(INFINITE_LOOP)
   @INFINITE_LOOP
   0;JMP            // infinite loop
