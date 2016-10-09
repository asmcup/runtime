
# asmcup

`asmcup` is a game where players create small and limited programs
to power robots in a virtual environment to compete for prizes.

## Install

 * Requires Java 7 or later
 * [Download Jar](https://github.com/asmcup/runtime/releases/latest/asmcup.jar)
 * `asmcup.tools.Compiler` compiles assembly source into binaries
 * `asmcup.runtime.Sandbox` provides an environment for testing your programs

## Compete

Made a robot you think can hold it's own on our servers?

 * [Upload your robot](https://asmcup.github.io)

## Virtual Machine

Robots are powered by a simple virtual machine with a simple RISC instruction
set for managing memory and a stack. The instruction language supports native
operations on 8-bit integers and 32-bit floats.

## Compiler

To compile an assembly program use:

    java -cp asmcup.jar asmcup.tools.Compiler program.asm program.bin

The resulting `program.bin` can be ran using the VM via:

    java -cp asmcup.jar asmcup.runtime.Sandbox program.bin

## Memory Size

All programs are 256-bytes in size and are mapped into an 8-bit address
space. While there are instructions to operate 32-bit float data the address
bus itself is 8-bits.

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
1     | b2f       | 2  | 1   | Byte to Float
2     | f2b       | 4  | 2   | Float to Byte
3     | not8      | 1  | 1   | Byte NOT
4     | or8       | 2  | 1   | Byte OR
5     | and8      | 2  | 1   | Byte AND
6     | xor8      | 2  | 1   | Byte XOR
7     | shl8      | 2  | 1   | Byte Shift Left
8     | shr8      | 2  | 1   | Byte Shift Right
9     | add8      | 2  | 1   | Byte Add
10    | sub8      | 2  | 1   | Byte Subtract
11    | div8      | 2  | 1   | Byte Divide
12    | mul8      | 2  | 1   | Byte Multiply
13    | madd8     | 3  | 1   | Byte Multiply with Add 
14    | negf      | 4  | 4   | Float Negate
15    | addf      | 8  | 4   | Float Add
16    | sub8      | 8  | 4   | Float Subtract
17    | sub16     | 8  | 4   | Float Divide
18    | sub32     | 8  | 4   | Float Multiply
19    | mul8      | 12 | 4   | Float Multiply with Add
20    | cosf      | 4  | 4   | Float Cosine
21    | sinf      | 4  | 4   | Float Sine
22    | tanf      | 4  | 4   | Float Tangent
23    | acosf     | 4  | 4   | Float Arc Cosine
24    | asinf     | 4  | 4   | Float Arc Sine
25    | atanf     | 4  | 4   | Float Arc Tangent
26    | absf      | 4  | 4   | Float Absolute Value
27    | minf      | 8  | 4   | Float Minimum Value
28    | maxf      | 8  | 4   | Float Maximum Value
29    | powf      | 8  | 4   | Float Raise Power
30    | logf      | 4  | 4   | Float Natural Logarithm
31    | log10f    | 4  | 4   | Float Logorithm Base 10
32    | if_eq8    | 2  | 1   | Byte Equal
33    | if_ne8    | 2  | 1   | Byte Not Equal
34    | if_lt8    | 2  | 1   | Byte Less Than
35    | if_lte8   | 2  | 1   | Byte Less Than or Equal
36    | if_gt8    | 2  | 1   | Byte Greater Than
37    | if_gte8   | 2  | 1   | Byte Greater Than or Equal
38    | if_ltf    | 8  | 1   | Float Less Than
39    | if_ltef   | 8  | 1   | Float Less Than or Equal
40    | if_gtf    | 8  | 1   | Float Greater Than
41    | if_gtef   | 8  | 1   | Float Greater Than or Equal
42    | c_0       | 0  | 1   | Push Byte `0x00`
43    | c_1       | 0  | 1   | Push Byte `0x01`
44    | c_2       | 0  | 1   | Push Byte `0x02`
45    | c_3       | 0  | 1   | Push Byte `0x03`
46    | c_4       | 0  | 1   | Push Byte `0x04`
47    | c_255     | 0  | 1   | Push Byte `0xFF`
48    | c_0f      | 0  | 4   | Push Float `0.0f`
49    | c_1f      | 0  | 4   | Push Float `1.0f`
50    | c_2f      | 0  | 4   | Push Float `2.0f`
51    | c_3f      | 0  | 4   | Push Float `3.0f`
52    | c_4f      | 0  | 4   | Push Float `4.0f`
53    | c_inf     | 0  | 4   | Push Float Infinity
54    | if_nan    | 4  | 1   | Float NaN Check
55    | dup8      | 1  | 2   | Byte Duplicate
56    | dupf      | 4  | 8   | Float Duplicate
57    |           |    |     | Unused 1 
58    |           |    |     | Unused 2
59    |           |    |     | Unused 3
60    |           |    |     | Unused 4
61    |           |    |     | Unused 5
62    |           |    |     | Unused 6
63    | io        | ?  | ?   | Input / Output (IO)


## PUSH

Here are the basic ways to push data onto the stack:

Variant       | Size | Description
--------------|------|--------------------------
push8 $f0     | 1-2  | Push Memory Byte
push8 #42     | 2    | Push Immediate Byte
pushf 0.0     | 5    | Push Immediate Float

Note that push8 is 1 byte when pushing a value from memory within 63 bytes of the
instruction doing the pushing.

## POP

Popping data from the stack can be done using many variants:

Variant       | Size | Description
--------------|------|--------------------------
pop8 $f0      | 1-2  | Pop Byte
popf $f0      | 2    | Pop Float

Note that pop8 is 1-byte when storing a value within 63 bytes of the
instruction doing the popping.

## BRANCH

Variant       | Size | Description
--------------|------|--------------------------
jnz $f0       | 1-2  | Jump Not Zero
jnz [$f0]     | 2    | Jump Not Zero Indirect
jmp $f0       | 2    | Jump Always

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
pop8 distance
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
