
## Virtual Machine

Robots are powered by a simple virtual machine with a RISC instruction
set for managing memory via a stack. The instruction language supports native
operations on 8-bit integers and 32-bit floats.

## Memory Size

All programs are 256 bytes in size and are mapped into an 8-bit address
space. While there are instructions to operate 32-bit float data the address
bus itself is 8-bits.

## Stack

The stack is located in memory at `0xFF` and grows downwards as
you push data onto it. Since the memory size is limited to 256 bytes
this means your stack can overflow into your memory if not careful.
Note that there are no registers, although you can of course reserve
a part of the stack for this purpose.

## Battery

Robots are powered via their internal battery. Each instruction executed consumes
one unit of power. Once a robot's battery reaches zero, it dies. Note that robots
may also gain (and lose) charges through interaction with their environment.
Robots can control their CPU clock speed from one instruction per world tick to 100
instructions per world tick.
The internal battery currently holds enough charge for executing 86400 instructions.

## Instructions

Every instruction consists of an opcode (2 least significant bits) and a data part
(6 remaining bits). It may be followed by a data block to hold e.g. a float constant.

There are four basic types of operations:

* `FUNC` executes functions on the stack
* `PUSH` loads data onto the stack
* `POP` saves data from the stack
* `BRANCH` jumps if the top 8-bits of the stack are non-zero

## Values and Addressing

Literal values are denoted by a `#` in front of them. Float values can only be literals,
so the `#` can be omitted for float literals.
Any values that are not literals are taken to be memory addresses.
Values preceded by a `$` are interpreted as hexadecimal, otherwise they
will be treated as decimals.

For example, `pushf #1.0` and `pushf 1.0` will push the float value 1.0 to the stack.
`pushf 1` would however push the content of the float (4 bytes of memory starting at)
address 1 to the stack, as would `pushf $01`. Another legal example would be
`push8 #$ff`, which pushes the literal value 255 to the stack.

## FUNC opcode

There are 63 total functions. Note that each of these instructions
are the same size (1 byte) but that they can modify the stack differently.
The *In* column is the number of bytes popped from the stack.
The *Out* column is the number of bytes pushed to the stack.

Value | Command   | In | Out | Notes
------|-----------|----|-----|-------------------------
0     | nop       | 0  | 0   | No Operation
1     | b2f       | 1  | 4   | Byte to Float
2     | f2b       | 4  | 1   | Float to Byte
3     | not       | 1  | 1   | Byte NOT
4     | or        | 2  | 1   | Byte OR
5     | and       | 2  | 1   | Byte AND
6     | xor       | 2  | 1   | Byte XOR
7     | shl       | 1  | 1   | Byte Shift Left
8     | shr       | 1  | 1   | Byte Shift Right
9     | add8      | 2  | 1   | Byte Add
10    | sub8      | 2  | 1   | Byte Subtract
11    | div8      | 2  | 1   | Byte Divide
12    | mul8      | 2  | 1   | Byte Multiply
13    | madd8     | 3  | 1   | Byte Multiply with Add
14    | negf      | 4  | 4   | Float Negate
15    | addf      | 8  | 4   | Float Add
16    | subf      | 8  | 4   | Float Subtract
17    | divf      | 8  | 4   | Float Divide
18    | mulf      | 8  | 4   | Float Multiply
19    | maddf     | 12 | 4   | Float Multiply with Add
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
52    | c_m1f     | 0  | 4   | Push Float `-1.0f`
53    | c_inf     | 0  | 4   | Push Float Infinity
54    | if_nan    | 4  | 1   | Float NaN Check
55    | dup8      | 1  | 2   | Byte Duplicate
56    | dupf      | 4  | 8   | Float Duplicate
57    | jsr       | 1  | 1   | Jump Subroutine
58    | ret       | 1  | 0   | Return
59    |           |    |     | Unused 3
60    |           |    |     | Unused 4
61    |           |    |     | Unused 5
62    |           |    |     | Unused 6
63    | io        | ?  | ?   | Input / Output (IO)

## PUSH opcode

Here are the basic ways to push data onto the stack:

Statement     | Size | Description
--------------|------|--------------------------
pushf 0.1     | 5    | Push immediate float to stack
push8 #42     | 2    | Push immediate byte to stack
push8 $f0     | 2    | Push value at memory address 0xf0 to stack
push8r $f0    | 1    | Push value at memory address 0xf0. Only legal if executed within 31 bytes of the target address

`push8r` stores the relative location of the target address in its data bytes,
thereby saving ROM space but being restricted to nearby addresses.

Note that the compiler will transform pushes of common constants into function calls
as appropriate. For example, `pushf 0.0` would be translated into function call c_0f
instead of the push, thereby only using 1 byte of memory instead of 5.

## POP opcode

Popping data from the stack can be done using multiple variants:

Statement     | Size | Description
--------------|------|--------------------------
popf $f0      | 2    | Pop float from stack to memory address 0xf0-0xf3
pop8 $f0      | 2    | Pop byte from stack to memory address 0xf0
pop8r $f0     | 1    | Pop byte from stack to memory address 0xf0. Only legal if executed within 31 bytes of the target address

`pop8r` stores the relative location of the target address in its data bytes,
thereby saving ROM space but being restricted to nearby addresses.

## BRANCH opcode

Statement     | Size | Description
--------------|------|--------------------------
jmp $f0       | 2    | Jump Always
jnz $f0       | 2    | Jump Not Zero
jmp [$f0]     | 2    | Jump Always Indirect
jnzr $f0      | 1    | Jump Not Zero Relative. Only legal if executed within 31 bytes of the target address

`jnzr` stores the relative location of the target address in its data bytes,
thereby saving ROM space but being restricted to nearby addresses.
The `jne` and `jner` commands are equivalent to `jnz` and `jnzr`, respectively.

## Further compiler information

Line contents after a semicolon (`;`) are ignored by the compiler.

You may define labels (e.g. `start:`) and refer to them using their name (e.g.
`jmp start`), which will be replaced by the memory location (address) in the
compiled code that they were defined at. A statement (including more labels)
may follow on the same line.
The literal value of the label address can be obtained with the `&` operator.
See below (JSR and RET) for the use of this.

The compiler also accepts statements of these forms:

Statement     | Size | Description
--------------|------|--------------------------
db8 #$f0      | 1    | Data byte (replaced by 0xf0 in ROM)
db #$f0       | 1    | Same as db8
dbf 0.1       | 4    | Data bytes from float

A common idiom is using `myVar: db8 #0` to create named variables.
This allows using statements like `push8 myVar`.

### JSR and RET

The `ret` function pops and jumps to the address at the top of the stack. `jsr` does
the same, but also pushes the address of the instruction following it. Consider this
example:

```
push8 &half_speed
jsr

half_speed:
  pushf 0.5
  push8 #IO_MOTOR
  io
  ret
```

## IO

While the VM itself allows you to construct arbitrary programs, the IO
controls the robot itself. The `io` command  takes the top value from the stack
and executes a command:

 Value | Constant     | Function  
-------|--------------|----------
 0     | IO_SENSOR        | Beam Sensor
 1     | IO_MOTOR         | Control Motor
 2     | IO_STEER         | Control Steering
 3     | IO_OVERCLOCK     | CPU Clock Control
 4     | IO_LASER         | Laser Attack (not yet implemented)
 5     | IO_BATTERY       | Read Battery
 6     | IO_MARK          | Mark ("pee")
 7     | IO_MARK_READ     | Mark Read ("smell") 
 8     | IO_ACCELEROMETER | Accelerometer
 9     | IO_RADIO         | (Planned) Set radio strength
 10    | IO_SEND          | (Planned) Emit data via radio
 10    | IO_RECV          | (Planned) Receive data via radio

### Beam Sensor

Casts a sensor beam at the current looking direction.

```
push8 #IO_SENSOR
io
pop8 type
popf distance
```

After `io` the stack will contain a byte on the top with a float below it.
The float is the distance the beam travelled (maximum range is `256.0`).
The byte specifies which type of thing was hit by the beam:

Value | Meaning
------|---------
 0    | Nothing
 1    | Solid
 2    | Hazard
 4    | Gold
 8    | Battery

By default the beam will hit anything listed above and return what was hit.
This can be changed by using the `IO_SENSOR_CONFIG` command:

```
push8 what_to_ignore
push8 #IO_SENSOR_CONFIG
io
```

In the example above *what_to_ignore* is a bitmask of things to have the
beam ignore. For example, to have the beam hit everything but gold:

```
push8 #4
push8 #IO_SENSOR_CONFIG
io
```

Another way to change the sensor (and laser) beam behavior is to set the angle
at which it is cast:

```
pushf -1.0
push8 #IO_BEAM_DIRECTION
io
```

Acceptable values range from `-1.0` (90° to the left of the robot's facing)
to `1.0` (90° to the right of the robot's facing). This affects both the beam
sensor and the laser!

### Motor Control

```
; Maximum Speed
pushf 1.0
push8 #IO_MOTOR
io

; Unpower motor
pushf 0.0
push8 #IO_MOTOR
io

; Reverse
pushf -1.0
push8 #IO_MOTOR
io
```

### Steering Control

```
; Steer right
pushf 1.0
push8 #IO_STEER
io

pushf -1.0
push8 #IO_STEER
io
```


### CPU Clock Control

The CPU speed of the robot can be "overclocked" like this:

```
push8 #100
push8 #IO_OVERCLOCK
io
```

The maximum CPU speed is 100, setting any number higher is the same as setting
to 100. The game operates at 10 frames per second, meaning a fully overclocked
CPU will execute 1000 instructions per second, 100 per frame.

### Battery Check

A robot can query how much battery it has:

```
push8 #IO_BATTERY
io
popf batteryLevel
```

The float on the stack will scale between `0.0` (completely empty) and `1.0`
(starting amount) to indicate the amount of battery remaining.

### Laser

Robots get a laser beam which can damage some obstacles and other robots in
combat. Currently a laser can break rocks which may be blocking doors. Having
the laser on costs up to `256` battery per game frame, less if the laser is
not fully powered (which results in reduced range) or hits something early.

```
; Full power laser
pushf 1.0
push8 #IO_LASER
io

; Laser off
pushf 0.0
push8 #IO_LASER
io
```

Note that the laser beam's angle relative to the robot can be changed. See the
Beam Sensor section for details.

### Accelerometer

The accelerometer allows a robot to detect relative changes in its position.
Each time you use the IO_ACCELEROMETER command, the last position is saved and
the difference is pushed on the stack as two floats.

```
push8 #IO_ACCELEROMETER
io
popf relY
popf relX
```

### Compass

The compass allows a robot to determine which direction it is facing. Facing
is always positive and ranges from `0` (west) over `PI/2` (north) etc. up to
`2 PI`.

```
push8 #IO_COMPASS
io
popf facing
```

### World Marking

Robots can "mark" the world which is kind of like how animals can pee and smell
the pee. A robot uses `IO_MARK` to write bytes to the current tile and can
use `IO_MARK_READ` to read data of the current tile. Each tile has 8 bytes
of storage.

```
push8 #00  ; Offset (index) of byte to write
push8 #42  ; Value to save
push8 #IO_MARK
io
```

You can read the same data back using:

```
push8 #00 ; Offset (index) of byte to read
push8 #IO_MARK_READ
io
pop8 tileData
```

Note that other robots (if in a shared world) may also read or (over)write
this data from/to the same tile.

### Radio

*Note the radio isn't implemented entirely yet*

The radio allows robots to send and receive messages with one another. Robots
"tune" their radio using the `IO_RADIO` command setting a frequency and
transmit power. The `IO_SEND` and `IO_RECV` to send and receive messages.

