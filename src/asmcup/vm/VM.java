package asmcup.vm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class VM implements VMConsts {
	private final byte[] ram;
	private int pc, sp;
	private boolean io;

	public VM() {
		this.ram = new byte[256];
	}
	
	public VM(byte[] ram) {
		if (ram.length != 256) {
			throw new IllegalArgumentException("Memory must be 256 bytes");
		}
		
		this.ram = ram;
	}
	
	public VM(DataInputStream stream) throws IOException {
		ram = new byte[256];
		stream.readFully(ram);
		pc = stream.readUnsignedByte() & 0xFF;
		sp = stream.readUnsignedByte() & 0xFF;
		io = stream.readBoolean();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VM)) return false;

		VM vm = (VM) obj;
		return Arrays.equals(getMemory(), vm.getMemory()) &&
				getProgramCounter() == vm.getProgramCounter() &&
				getStackPointer() == vm.getStackPointer() &&
				io == vm.io;
	}
	
	public void save(DataOutputStream stream) throws IOException {
		stream.write(ram);
		stream.writeByte(pc);
		stream.writeByte(sp);
		stream.writeBoolean(io);
	}

	public int getProgramCounter() {
		return pc;
	}

	public int getStackPointer() {
		return 0xFF - sp;
	}

	public byte[] getMemory() {
		return ram;
	}

	public int read8() {
		int value = ram[pc] & 0xFF;
		pc = (pc + 1) & 0xFF;
		return value;
	}
	
	public int read8(int addr) {
		return ram[addr & 0xFF] & 0xFF;
	}
	
	public int read8indirect() {
		return ram[read8()] & 0xFF;
	}

	public int read16() {
		return read8() | (read8() << 8);
	}
	
	public int read16(int addr) {
		return read8(addr) | (read8(addr + 1) << 8);
	}

	public int read32() {
		return read16() | (read16() << 16);
	}
	
	public int read32(int addr) {
		return read16(addr) | (read16(addr + 2) << 16);
	}

	public float readFloat() {
		return Float.intBitsToFloat(read32());
	}
	
	public float readFloatIndirect() {
		return Float.intBitsToFloat(read32(read8()));
	}
	
	public void write8(int addr, int value) {
		ram[addr & 0xFF] = (byte)value;
	}
	
	public void write16(int addr, int value) {
		write8(addr, value);
		write8(addr + 1, value >> 8);
	}
	
	public void write32(int addr, int value) {
		write16(addr, value);
		write16(addr + 2, value >> 16);
	}
	
	public void writeFloat(int addr, float value) {
		write32(addr, Float.floatToRawIntBits(value));
	}

	public void push8(int x) {
		ram[0xFF - sp] = (byte) x;
		sp = (sp + 1) & 0xFF;
	}
	
	public void push8(boolean x) {
		push8(x ? 1 : 0);
	}

	public void push16(int x) {
		push8(x);
		push8(x >> 8);
	}

	public void push32(int x) {
		push16(x);
		push16(x >> 16);
	}

	public void pushFloat(float x) {
		push32(Float.floatToRawIntBits(x));
	}
	
	public void pushFloat(double x) {
		pushFloat((float)x);
	}

	public int pop8() {
		sp = (sp - 1) & 0xFF;
		return ram[0xFF - sp] & 0xFF;
	}

	public int pop16() {
		return (pop8() << 8) | pop8();
	}

	public int pop32() {
		return (pop16() << 16) | pop16();
	}

	public float popFloat() {
		return Float.intBitsToFloat(pop32());
	}
	
	public int peek8() {
		return peek8(0);
	}
	
	public int peek8(int r) {
		return ram[0xFF - ((sp - r - 1) & 0xFF)] & 0xFF;
	}
	
	public int peek16() {
		return peek8(1) | (peek8(0) << 8); 
	}
	
	public int peek16(int r) {
		return peek8(r + 1) | (peek8(r) << 8);
	}
	
	public int peek32() {
		return peek16(2) | (peek16(0) << 16);
	}
	
	public float peekFloat() {
		return Float.intBitsToFloat(peek32());
	}
	
	public boolean checkIO() {
		boolean x = io;
		io = false;
		return x;
	}
	
	public void setIO(boolean io) {
		this.io = io;
	}

	public void tick() {
		int bits = read8();
		int opcode = bits & 0b11;
		int data = bits >> 2;
		
		switch (opcode) {
		case OP_FUNC:
			op_func(data);
			break;
		case OP_PUSH:
			op_push(data);
			break;
		case OP_POP:
			op_pop(data);
			break;
		case OP_BRANCH:
			op_branch(data);
			break;
		}
	}

	public void op_func(int data) {
		switch (data) {
		case F_NOP:
			break;
			
		case F_B2F:
			pushFloat(pop8());
			break;
		case F_F2B:
			push8((int)popFloat());
			break;
			
		case F_NOT:
			push8(~pop8());
			break;
		case F_OR:
			push8(pop8() | pop8());
			break;
		case F_AND:
			push8(pop8() & pop8());
			break;
		case F_XOR:
			push8(pop8() ^ pop8());
			break;
		case F_SHL:
			push8(pop8() << 1);
			break;
		case F_SHR:
			push8(pop8() >> 1);
			break;
		case F_ADD8:
			push8(pop8() + pop8());
			break;
		case F_SUB8:
			push8(pop8() - pop8());
			break;
		case F_MUL8:
			push8(pop8() * pop8());
			break;
		case F_DIV8:
			int a = pop8();
			int b = pop8();
			push8(b == 0 ? 0 : a / b);
			break;
		case F_MADD8:
			push8(pop8() * pop8() + pop8());
			break;
		
		case F_NEGF:
			pushFloat(-popFloat());
			break;
		case F_ADDF:
			pushFloat(popFloat() + popFloat());
			break;
		case F_SUBF:
			pushFloat(popFloat() - popFloat());
			break;
		case F_MULF:
			pushFloat(popFloat() * popFloat());
			break;
		case F_DIVF:
			pushFloat(popFloat() / popFloat());
			break;
		case F_MADDF:
			pushFloat(popFloat() * popFloat() + popFloat());
			break;
			
		case F_COS:
			pushFloat(Math.cos(popFloat()));
			break;
		case F_SIN:
			pushFloat(Math.sin(popFloat()));
			break;
		case F_TAN:
			pushFloat(Math.tan(popFloat()));
			break;
		case F_ACOS:
			pushFloat(Math.acos(popFloat()));
			break;
		case F_ASIN:
			pushFloat(Math.asin(popFloat()));
			break;
		case F_ATAN:
			pushFloat(Math.atan(popFloat()));
			break;
		case F_ABSF:
			pushFloat(Math.abs(popFloat()));
			break;
		case F_MINF:
			pushFloat(Math.min(popFloat(), popFloat()));
			break;
		case F_MAXF:
			pushFloat(Math.max(popFloat(), popFloat()));
			break;
		case F_POW:
			pushFloat(Math.pow(popFloat(), popFloat()));
			break;
		case F_LOG:
			pushFloat(Math.log(popFloat()));
			break;
		case F_LOG10:
			pushFloat(Math.log10(popFloat()));
			break;
			
		case F_IF_EQ8:
			push8(pop8() == pop8());
			break;
		case F_IF_NE8:
			push8(pop8() != pop8());
			break;
		case F_IF_LT8:
			push8(pop8() < pop8());
			break;
		case F_IF_LTE8:
			push8(pop8() <= pop8());
			break;
			
		case F_IF_LTF:
			push8(popFloat() < popFloat());
			break;
		case F_IF_LTEF:
			push8(popFloat() <= popFloat());
			break;
		case F_IF_GTF:
			push8(popFloat() > popFloat());
			break;
		case F_IF_GTEF:
			push8(popFloat() <= popFloat());
			break;
			
		case F_C_0:
			push8(0);
			break;
		case F_C_1:
			push8(1);
			break;
		case F_C_2:
			push8(2);
			break;
		case F_C_3:
			push8(3);
			break;
		case F_C_4:
			push8(4);
			break;
		case F_C_255:
			push8(0xFF);
			break;
			
		case F_C_M1F:
			pushFloat(-1.0f);
			break;
		case F_C_0F:
			pushFloat(0.0f);
			break;
		case F_C_1F:
			pushFloat(1.0f);
			break;
		case F_C_2F:
			pushFloat(2.0f);
			break;
		case F_C_3F:
			pushFloat(3.0f);
			break;
		case F_C_INF:
			pushFloat(Float.POSITIVE_INFINITY);
			break;
		case F_ISNAN:
			push8(Float.isNaN(popFloat()));
			break;
			
		case F_DUP8:
			push8(peek8());
			break;
		case F_DUPF:
			pushFloat(peekFloat());
			break;
		
		case F_JSR:
			int ret = pc;
			pc = pop8();
			push8(ret);
			break;
		case F_RET:
			pc = pop8();
			break;
			
		case F_IO:
			io = true;
			break;
		}
	}

	public void op_push(int data) {
		switch (data) {
		case MAGIC_PUSH_BYTE_IMMEDIATE:
			push8(read8());
			break;
		case MAGIC_PUSH_BYTE_MEMORY:
			push8(read8indirect());
			break;
		case MAGIC_PUSH_FLOAT_IMMEDIATE:
			pushFloat(readFloat());
			break;
		case MAGIC_PUSH_FLOAT_MEMORY:
			pushFloat(readFloatIndirect());
			break;
		default:
			push8(read8(pc + data - 32));
			break;
		}
	}
	
	public void op_pop(int data) {
		switch (data) {
		case MAGIC_POP_BYTE:
			write8(read8(), pop8());
			break;
		case MAGIC_POP_FLOAT:
			writeFloat(read8(), popFloat());
			break;
		case MAGIC_POP_BYTE_INDIRECT:
			write8(read8indirect(), pop8());
			break;
		case MAGIC_POP_FLOAT_INDIRECT:
			writeFloat(read8indirect(), popFloat());
			break;
		default:
			write8(pc + data - 32, pop8());
			break;
		}
	}
	
	public void op_branch(int data) {
		int addr;
		
		switch (data) {
		case MAGIC_BRANCH_ALWAYS:
			pc = read8();
			break;
		case MAGIC_BRANCH_IMMEDIATE:
			addr = read8();
			
			if (pop8() != 0) {
				pc = addr;
			}
			
			break;
		case MAGIC_BRANCH_INDIRECT:
			pc = read8indirect();
			break;
		default:
			if (pop8() != 0) {
				pc = (pc + data - 32) & 0xFF;
			}
			
			break;
		}
	}
}
