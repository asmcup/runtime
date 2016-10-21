package asmcup.compiler;

import java.util.*;

import asmcup.vm.VMConsts;

public class Compiler implements VMConsts {
	protected ArrayList<Statement> statements;
	protected HashMap<String, Integer> labels;
	protected byte[] ram;
	protected int pc;

	protected int bytesUsed = 0;
	protected int currentLine = 0;
		
	protected void write8(int value) {
		ram[pc] = (byte)(value & 0xFF);
		pc = (pc + 1) & 0xFF;
		++bytesUsed;
	}
	
	protected void writeOp(int op, int data) {
		if ((op >> 2) != 0) {
			throw new IllegalArgumentException("Opcode is greater than 2-bits");
		}
		
		if ((data >> 6) != 0) {
			throw new IllegalArgumentException("Opcode data is greater than 6-bits");
		}
		
		write8(op | (data << 2));
	}
	
	protected void write16(int value) {
		write8(value & 0xFF);
		write8(value >> 8);
	}
	
	protected void write32(int value) {
		write16(value & 0xFFFF);
		write16(value >> 16);
	}
	
	protected void writeFloat(float value) {
		write32(Float.floatToRawIntBits(value));
	}
	
	public byte[] compile(String[] lines) {
		ram = new byte[256];
		labels = new HashMap<>();
		statements = new ArrayList<>();
		pc = 0;
		bytesUsed = 0;
		
		for (String line : lines) {
			currentLine++;
			parseLine(line);
		}
		
		pc = 0;
		
		for (Statement statement : statements) {
			pc += statement.measure();
		}
		
		pc = 0;
		
		for (Statement statement : statements) {
			statement.compile();
		}
		
		labels.clear();
		statements.clear();
		labels = null;
		statements = null;
		pc = 0;
		
		byte[] compiled = ram;
		ram = null;
		return compiled;
	}
	
	public byte[] compile(String source) {
		return compile(source.split("\n"));
	}
	
	protected void parseLine(String line) {
		line = line.trim();
		
		if (line.isEmpty()) {
			return;
		}
		
		line = parseComments(line);
		line = parseLabels(line);
		
		if (line.isEmpty()) {
			return;
		}
		
		String[] parts = line.split("\\s+", 2);
		
		if (parts.length <= 0) {
			return;
		}
		
		String cmd = parts[0].toLowerCase().trim();
		String[] args = parseArgs(parts);
		parseStatement(cmd, args);
	}
	
	protected void parseStatement(String cmd, String[] args) {
		switch (cmd) {
		case "db":
		case "db8":
			db(args);
			break;
		case "dbf":
			dbf(args);
			break;
		case "push8":
			push8(args);
			break;
		case "push8r":
			push8r(args);
			break;
		case "pop8":
			pop8(args);
			break;
		case "pop8r":
			pop8r(args);
			break;
		case "pushf":
			pushf(args);
			break;
		case "popf":
			popf(args);
			break;
		case "jne":
		case "jnz":
			jnz(args);
			break;
		case "jnzr":
		case "jner":
			jnzr(args);
			break;
		case "jmp":
			jmp(args);
			break;
		default:
			func(cmd, args);
			break;
		}
	}
	
	final static byte[] NO_DATA = {};
	
	protected void func(String cmd, String[] args) {
		if (!VMFuncTable.exists(cmd)) {
			throw new IllegalArgumentException("Unknown function " + cmd);
		}
		
		immediate(OP_FUNC, VMFuncTable.parse(cmd), NO_DATA);
	}
	
	protected void db(String[] args) {
		statements.add(new Statement() {
			public int measure() {
				return args.length;
			}
			
			public void compile() {
				for (String s : args) {
					write8(parseLiteral(s));
				}
			}
		});
	}
	
	protected void dbf(String[] args) {
		statements.add(new Statement() {
			public int measure() {
				return args.length * 4;
			}
			
			public void compile() {
				for (String s : args) {
					writeFloat(Float.parseFloat(s));
				}
			}
		});
	}
	
	protected void push8(String[] args) {
		String s = expectOne(args);
		
		if (isLiteral(s)) {
			pushLiteral8(s);
		} else if (s.startsWith("&")) {
			reference(OP_PUSH, MAGIC_PUSH_BYTE_IMMEDIATE, s.substring(1));
		} else {
			reference(OP_PUSH, MAGIC_PUSH_BYTE_MEMORY, s);
		}
	}
	
	protected void pushLiteral8(String s) {
		switch (parseLiteral(s)) {
		case 0:
			immediate(OP_FUNC, F_C_0, NO_DATA);
			break;
		case 1:
			immediate(OP_FUNC, F_C_1, NO_DATA);
			break;
		case 2:
			immediate(OP_FUNC, F_C_2, NO_DATA);
			break;
		case 3:
			immediate(OP_FUNC, F_C_3, NO_DATA);
			break;
		case 4:
			immediate(OP_FUNC, F_C_4, NO_DATA);
			break;
		case 255:
			immediate(OP_FUNC, F_C_255, NO_DATA);
			break;
		default:
			immediate(OP_PUSH, MAGIC_PUSH_BYTE_IMMEDIATE, s);
			break;
		}
	}
	
	protected void push8r(String[] args) {
		relative(OP_PUSH, args);
	}
	
	protected void pop8(String[] args) {
		reference(OP_POP, MAGIC_POP_BYTE, args);
	}
	
	protected void pop8r(String[] args) {
		relative(OP_POP, args);
	}
	
	protected void pushf(String[] args) {
		String s = expectOne(args);
		
		if (isSymbol(s)) {
			pushMemoryFloat(s);
		} else {
			pushLiteralFloat(s);
		}
	}
	
	protected void pushMemoryFloat(String args) {
		reference(OP_PUSH, MAGIC_PUSH_FLOAT_MEMORY, args);
	}
	
	protected void pushLiteralFloat(String s) {
		float f = Float.parseFloat(s);
		
		if (f == 0.0f) {
			immediate(OP_FUNC, F_C_0F, NO_DATA);
		} else if (f == 1.0f) {
			immediate(OP_FUNC, F_C_1F, NO_DATA);
		} else if (f == 2.0f) {
			immediate(OP_FUNC, F_C_2F, NO_DATA);
		} else if (f == 3.0f) {
			immediate(OP_FUNC, F_C_3F, NO_DATA);
		} else if (f == 4.0f) {
			immediate(OP_FUNC, F_C_4F, NO_DATA);
		} else if (Float.isInfinite(f)) {
			immediate(OP_FUNC, F_C_INF, NO_DATA);
		} else {
			immediateFloat(OP_PUSH, MAGIC_PUSH_FLOAT_IMMEDIATE, s);
		}
	}
	
	protected void popf(String[] args) {
		reference(OP_POP, MAGIC_POP_FLOAT, args);
	}
	
	protected void jnz(String[] args) {
		String s = expectOne(args);
		
		if (isIndirect(s)) {
			throw new IllegalArgumentException("jnz cannot be indirect");
		} else {
			reference(OP_BRANCH, MAGIC_BRANCH_IMMEDIATE, s);
		}
	}
	
	protected void jnzr(String[] args) {
		relative(OP_BRANCH, args);
	}
	
	protected void jmp(String[] args) {
		String s = expectOne(args);
		
		if (isIndirect(s)) {
			s = s.substring(1, s.length() - 1);
			reference(OP_BRANCH, MAGIC_BRANCH_INDIRECT, s);
		} else {
			reference(OP_BRANCH, MAGIC_BRANCH_ALWAYS, s);
		}
	}
	
	public static boolean isLiteral(String s) {
		return s.startsWith("#");
	}
	
	public static boolean isSymbol(String s) {
		return s.matches("^[a-zA-Z_]+[a-zA-Z_0-9]*$");
	}
	
	public static boolean isIndirect(String s) {
		return isEnclosed(s, "(", ")") || isEnclosed(s, "[", "]");
	}
	
	public static boolean isEnclosed(String s, String start, String end) {
		if (s.startsWith(start)) {
			if (!s.endsWith(end)) {
				throw new IllegalArgumentException(String.format("Expected '%s'", s));
			}
			
			return true;
		}
		
		return false;
	}
	
	public static String expectOne(String[] args) {
		return args[0];
	}
	
	protected String parseComments(String line) {
		int pos = line.indexOf(';');
		
		if (pos < 0) {
			return line.trim();
		}
		
		return line.substring(0, pos).trim();
	}
	
	protected String parseLabels(String line) {
		int pos;
		
		while ((pos = line.indexOf(':')) >= 0) {
			if (pos == 0) {
				throw new IllegalArgumentException("Expected label name");
			}
			
			String name = line.substring(0, pos);
			statements.add(new Label(name));
			line = line.substring(pos + 1).trim();
		}
		
		return line.trim();
	}
	
	protected static final String[] EMPTY_ARGS = {};
	
	protected String[] parseArgs(String[] parts) {
		if (parts.length <= 1) {
			return EMPTY_ARGS;
		}
		
		String[] args = parts[1].split(",");
		
		for (int i=0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		
		return args;
	}
	
	protected static int parseLiteral(String s) {
		if (!s.startsWith("#")) {
			throw new IllegalArgumentException("Expected #");
		}
		
		return parseValue(s.substring(1));
	}
	
	protected static int parseValue(String s) {
		if (RobotConstsTable.contains(s)) {
			return RobotConstsTable.get(s);
		}
		
		if (s.startsWith("$")) {
			return Integer.parseInt(s.substring(1), 16);
		}
		
		return Integer.parseInt(s, 10);
	}
	
	protected void reference(int op, int data, String s) {
		statements.add(new Statement() {
			public int measure() {
				return 2;
			}
			
			public void compile() {
				int addr;
				
				if (isSymbol(s)) {
					if (!labels.containsKey(s)) {
						throw new IllegalArgumentException(String.format("Cannot find label '%s'", s));
					}
					
					addr = labels.get(s);
				} else {
					addr = parseValue(s);
				}
				
				writeOp(op, data);
				write8(addr);
			}
		});
	}
	
	protected void reference(int op, int data, String[] args) {
		reference(op, data, expectOne(args));
	}
	
	protected void immediate(int op, int data, byte[] payload) {
		statements.add(new Statement() {
			public int measure() {
				return 1 + payload.length;
			}
			
			public void compile() {
				writeOp(op, data);
				
				for (byte b : payload) {
					write8(b);
				}
			}
		});
	}
	
	protected void immediate(int op, int data, String[] args) {
		immediate(op, data, expectOne(args));
	}
	
	protected void immediate(int op, int data, String s) {
		byte[] payload = new byte[] { (byte)parseLiteral(s) };
		immediate(op, data, payload);
	}
	
	protected void immediateFloat(int op, int data, String s) {
		statements.add(new Statement() {
			public int measure() {
				return 5;
			}
			
			public void compile() {
				writeOp(op, data);
				writeFloat(Float.parseFloat(s));
			}
		});
	}
	
	protected void relative(int op, String s) {
		if (isLiteral(s)) {
			throw new IllegalArgumentException("Cannot address a literal for relative instruction");
		}
		
		statements.add(new Statement() {
			public int measure() {
				return 1;
			}
			
			public void compile() {
				int addr = labels.get(s);
				int r = addr - pc;
				
				if (r < -32 || r > 31) {
					throw new IllegalArgumentException("Address is not within range");
				}
				
				writeOp(op, 32 + r);
			}
		});
	}
	
	protected void relative(int op, String[] args) {
		relative(op, expectOne(args));
	}

	/**
	 * Returns the current line number. Useful for display a compiler error
	 * @return current line number the compiler is looking at
	 */
	public int getCurrentLine() {
		return currentLine;
	}

	/**
	 * @return the number of bytes used
	 */
	public int getBytesUsed() {
		return bytesUsed;
	}
	
	protected abstract class Statement {
		public abstract int measure();
		public abstract void compile();
	}
	
	protected class Label extends Statement {
		final String name;
		
		public Label(String name) {
			this.name = name;
		}
		
		public int measure() {
			labels.put(name, pc);
			return 0;
		}
		
		public void compile() {
			
		}
	}
}