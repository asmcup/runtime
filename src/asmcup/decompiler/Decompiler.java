package asmcup.decompiler;

import asmcup.compiler.VMFuncTable;
import asmcup.vm.VMConsts;

public class Decompiler implements VMConsts {
	public void decompile(byte[] ram) {
		int pc = 0;
		int end = 255;
		
		while (read8(ram, end) == 0 && end > 0) {
			end--;
		}

		while (pc <= end) {
			pc += decompileCommand(ram, pc);
		}
	}

	public int read8(byte[] ram, int pc) {
		return ram[pc] & 0xFF;
	}

	public int read16(byte[] ram, int pc) {
		return read8(ram, pc) | (read8(ram, pc + 1) << 8);
	}

	public int read32(byte[] ram, int pc) {
		return read16(ram, pc) | (read16(ram, pc + 2) << 16);
	}

	public float readFloat(byte[] ram, int pc) {
		return Float.intBitsToFloat(read32(ram, pc));
	}
	
	public void dump(int pc, String s) {
		System.out.printf("%02x: %s%n", pc, s);
	}

	public int decompileCommand(byte[] ram, int pc) {
		int bits = ram[pc & 0xFF] & 0xFF;
		int opcode = bits & 0b11;
		int data = bits >> 2;

		switch (opcode) {
		case OP_BRANCH:
			return decompileBranch(ram, pc, data);
		case OP_PUSH:
			return decompilePush(ram, pc, data);
		case OP_POP:
			return decompilePop(ram, pc, data);
		case OP_FUNC:
			return decompileFunc(ram, pc, data);
		}

		return 1;
	}
	
	public int decompileFunc(byte[] ram, int pc, int data) {
		dump(pc, VMFuncTable.unparse(data));
		return 1;
	}
	
	public int decompilePop(byte[] ram, int pc, int data) {
		int addr;
		
		switch (data) {
		case MAGIC_POP_BYTE:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("pop8 $%02x", addr));
			return 2;
		case MAGIC_POP_BYTE_INDIRECT:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("pop8 [$%02x]", addr));
			return 2;
		case MAGIC_POP_FLOAT:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("popf $%02x", addr));
			return 2;
		case MAGIC_POP_FLOAT_INDIRECT:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("popf [$%02x]", addr));
			return 2;
		}
		
		int r = data - 32;
		addr = pc + r;
		dump(pc, String.format("pop8 $%02x ; relative %d", addr, r));
		return 1;
	}
	
	public int decompileBranch(byte[] ram, int pc, int data) {
		int addr;
		
		switch (data) {
		case MAGIC_BRANCH_ALWAYS:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("jmp $%02x", addr));
			return 2;
		case MAGIC_BRANCH_IMMEDIATE:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("jnz $%02x", addr));
			return 2;
		case MAGIC_BRANCH_INDIRECT:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("jmp [$%02x]", addr));
			return 2;
		}
		
		int r = data - 32;
		addr = (pc + r) & 0xFF;
		dump(pc, String.format("jnz $%02x  ; relative %d", addr, r));
		return 1;
	}

	public int decompilePush(byte[] ram, int pc, int data) {
		int addr;
		float f;

		switch (data) {
		case MAGIC_PUSH_BYTE_IMMEDIATE:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("push8 #$%02x", addr));
			return 2;
		case MAGIC_PUSH_BYTE_MEMORY:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("push8 $%02x", addr));
			return 2;
		case MAGIC_PUSH_FLOAT_IMMEDIATE:
			f = readFloat(ram, pc + 1);
			dump(pc, String.format("pushf %f", f));
			return 5;
		case MAGIC_PUSH_FLOAT_MEMORY:
			addr = read8(ram, pc + 1);
			dump(pc, String.format("pushf $%02x", addr));
			return 2;
		}

		int r = data - 32;
		addr = (pc + r) & 0xFF;
		dump(pc, String.format("push8 $%02x  ; relative %d", addr, r));
		return 1;
	}
}
