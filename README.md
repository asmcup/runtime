
# bitwars

`bitwars` is a game where players create small and limited programs
to power robots in a virtual environment to compete for prizes.

## Install

 * Requires Java 7 or later
 * [Download Jar](https://github.com/krisives/asmcup/releases/latest/asmcup.jar)
 * `asmcup.Compiler` compiles assembly source into binaries
 * `asmcup.Runtime` provides an environment for testing your programs

## Compete

Made a robot you think can hold it's own on our servers?

 * [Upload your robot](https://asmcup.github.io)

## Virtual Machine

Robots are powered by a simple virtual machine with a simple RISC instruction
set for managing memory and a stack. The instruction language supports native
operations on 8-bit integers and 32-bit floats.

Execution begins at the first address (`0x00`) in memory.

## Compiler

To compile an assembly program use:

    java -cp bitwars.jar bitwars.Compiler program.asm program.bin

The resulting `program.bin` can be ran using the VM via:

    java -cp bitwars.jar bitwars.Runtime program.bin

## Memory Size

All programs are 256-bytes in size and are mapped into an 8-bit address
space. While there are instructions to operate with 16-bit and 32-bit
data the address bus itself is 8-bits.

## Stack

The stack is located in memory at `0xFF` and grows downwards as
you push data onto it. Since the memory size is limited to 256 bytes
this means your stack can overflow into your memory if not careful.

## Battery

Robots are powered via their internal battery used to power each instruction. If
the battery reaches zero they will die. Robots can control their CPU clock speed
from 1 hz (1 instruction per second) to 1 khz (1024 instructions per second).


## Instructions

There are four basic types of operations:

* `FUNC` executes functions on the stack
* `PUSH` loads data onto the stack
* `POP` saves data from the stack
* `BRANCH` jumps if the top 8-bits of the stack are non-zero


## FUNC

There are 63 total functions. Note that each of these instructions
are the same size (1 byte) but that they can modify the stack differently.

Value | Command   | In | Out | Notes
------|-----------|----|-----|-------------------------
0     | nop       | 0  | 0   | No Operation
1     | or8       | 2  | 1   | OR Byte
2     | or16      | 4  | 2   | OR Short
3     | or32      | 8  | 4   | OR Int
4     | and8      | 2  | 1   | AND Byte
5     | and16     | 4  | 2   | AND Short
6     | and32     | 8  | 4   | AND Int
7     | xor8      | 2  | 1   | XOR Byte
8     | xor16     | 4  | 2   | XOR Short
9     | xor32     | 8  | 4   | XOR Int
10    | not8      | 1  | 1   | Not Byte
11    | not16     | 2  | 2   | Not Short
12    | not32     | 4  | 4   | Not Int
13    | add8      | 2  | 1   | Add Byte
14    | add16     | 4  | 2   | Add Short
15    | add32     | 8  | 4   | Add Int
16    | sub8      | 2  | 1   | Subtract Byte
17    | sub16     | 4  | 2   | Subtract Short
18    | sub32     | 8  | 4   | Subtract Int
19    | mul8      | 2  | 1   | Multiply Byte
20    | mul16     | 4  | 2   | Multiply Short
21    | mul32     | 8  | 2   | Multiply Int
22    | div8      | 2  | 1   | Divide Byte
23    | div16     | 4  | 2   | Divide Short
24    | div32     | 8  | 4   | Divide Int
25    | addmul8   | 3  | 1   | Add Multiply Byte
26    | addmul16  | 6  | 2   | Add Multiply Short
27    | addmul32  | 12 | 4   | Add Multiply Int
28    | pushsp    | 0  | 1   | Push Stack Pointer (SP)
29    | popsp     | 1  | 0   | Pop Stack Pointer (SP)
30    | pushpc    | 0  | 1   | Push Program Counter (PC)
31    | poppc     | 1  | 0   | Pop Program Counter (PC)
32    | lt8       | 1  | 1   | Less Than Byte
33    | lt16      | 4  | 1   | Less Than Short
34    | lt32      | 8  | 1   | Less Than Int
35    |           |    |     |
36    | eq8       | 2  | 1   | Equal Byte
37    | eq16      | 4  | 1   | Equal Short
38    | eq32      | 8  | 1   | Equal Int
39    | ne8       | 2  | 1   | Not Equal Byte
40    | ne16      | 4  | 1   | Not Equal Short
41    | ne32      | 8  | 1   | Not Equal Int
42    | dup8      | 0  | 1   | Duplicate Byte
43    | dup16     | 0  | 2   | Duplicate Short
44    | dup32     | 0  | 4   | Duplicate Int
45    | zero8     | 0  | 1   | Push Zero Byte
46    | zero16    | 0  | 2   | Push Zero Short
47    | zero32    | 0  | 4   | Push Zero Int
48    | one8      | 0  | 1   | Push One Byte
49    | one16     | 0  | 2   | Push One Short
50    | one32     | 0  | 4   | Push One Int
51    | read8     | 1  | 1   | Read Byte
52    | read16    | 1  | 2   | Read Short
53    | read32    | 1  | 4   | Read Int
54    | write8    | 2  | 0   | Write Byte
55    | write16   | 3  | 0   | Write Short
56    | write32   | 5  | 0   | Write Int
57    | b2s       | 1  | 2   | Byte to short
58    | b2i       | 1  | 4   | Byte to Int
59    | s2b       | 2  | 1   | Short to Byte
60    | s2i       | 2  | 4   | Short to Int
61    | i2b       | 4  | 1   | Int to Byte
62    | i2s       | 4  | 2   | Int to Short
63    | io        | ?  | ?   | Input / Output (IO)


## PUSH

Here are the basic ways to push data onto the stack:

Variant       | Size | Description
--------------|------|--------------------------
push8 $f0     | 1-2  | Push Memory Byte
push16 $f0    | 2    | Push Memory Short
push32 $f0    | 2    | Push Memory Int
push8 #42     | 2    | Push Immediate Byte
push16 #beb   | 3    | Push Immediate Short
push32 #dead1 | 5    | Push Immediate Int
zero8         | 1    | Push Zero Byte
zero16        | 1    | Push Zero Short
zero32        | 1    | Push Zero Int
one8          | 1    | Push One Byte
one16         | 1    | Push One Short
one32         | 1    | Push One Int

Note that push8 is 1 byte when pushing a value from memory within 63 bytes of the
instruction doing the pushing.

## POP

Popping data from the stack can be done using many variants:

Variant       | Size | Description
--------------|------|--------------------------
pop8 $f0      | 1-2  | Pop Byte Save
pop16 $f0     | 2    | Pop Short Save
pop32 $f0     | 2    | Pop Int Save

Note that pop8 is 1-byte when storing a value within 63 bytes of the
instruction doing the popping.

## BRANCH

Variant       | Size | Description
--------------|------|--------------------------
jnz  $f0      | 1-2  | Jump Not Zero

## IO

While the VM itself allows you to construct arbitrary programs the IO
controls the robot itself. The `io` command  takes the top value from the stack
and executes a command:

 Value | Function  
-------|-----------
 0     | Beam Sensor
 1     | Proximity Sensor
 2     | Motor Control
 3     | Steering Control
 4     | CPU Clock Control
 5     | Mark
 6     | Laser Attack
 7     | Read Battery

### Beam Sensor

Casts a beam at the current looking direction.

```
push8 #00
io
pull8 distance
 ```

After `io` the stack will contain a byte of how far the beam
traveled until it hit an obstacle.

### Motor Control

```
motorSpeed: db 55

push8 #02
push8 motorSpeed
io
```

### Steering Control

```
steeringWheel: db 128
steerLeft:     db 0
steerRight:    db 255

push8 #03
push8 steeringWheel
io
```


### CPU Clock Control

The CPU speed of the robot can be "overclocked" by pushing two
bytes to the stack and calling `io`:

```
cpuSpeed: db 125

push8 #04
push8 cpuSpeed
io
```

The effective CPU speed becomes `1 + (cpuSpeed * 4)`
