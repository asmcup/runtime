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

	protected void init() {
		ram = new byte[256];
		labels = new HashMap<>();
		statements = new ArrayList<>();
		pc = 0;
		bytesUsed = 0;
	}
		
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
		init();
		
		try {
			for (String line : lines) {
				currentLine++;
				parseLine(line);
			}
		} catch (IllegalArgumentException e) {
			rethrowWithLine(e, currentLine);
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
		String[] args = parseArgs(parts.length > 1 ? parts[1] : "");
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
		if (args.length > 0) {
			throw new IllegalArgumentException("Too many arguments (0 expected)");
		}
		
		immediate(OP_FUNC, VMFuncTable.parse(cmd), NO_DATA);
	}
	
	protected void db(String[] args) {
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return args.length;
			}
			
			public void compileImpl() {
				for (String s : args) {
					write8(parseLiteralByte(s));
				}
			}
		});
	}
	
	protected void dbf(String[] args) {
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return args.length * 4;
			}
			
			public void compileImpl() {
				for (String s : args) {
					writeFloat(parseLiteralFloat(s));
				}
			}
		});
	}
	
	protected void push8(String[] args) {
		String s = expectOne(args);
		
		if (isLiteral(s)) {
			pushLiteral8(s);
		} else if (s.startsWith("&")) {
			s = s.substring(1);
			if (!isSymbol(s)) {
				throw new IllegalArgumentException("Invalid label: " + s);
			}
			reference(OP_PUSH, MAGIC_PUSH_BYTE_IMMEDIATE, s);
		} else {
			reference(OP_PUSH, MAGIC_PUSH_BYTE_MEMORY, s);
		}
	}
	
	protected void pushLiteral8(String s) {
		switch (parseLiteralByte(s)) {
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
		String s = expectOne(args);

		if (isIndirect(s)) {
			s = s.substring(1, s.length() - 1);
			reference(OP_POP, MAGIC_POP_BYTE_INDIRECT, s);
		} else {
			reference(OP_POP, MAGIC_POP_BYTE, s);
		}
	}
	
	protected void pop8r(String[] args) {
		relative(OP_POP, args);
	}
	
	protected void pushf(String[] args) {
		String s = expectOne(args);
		
		if (isLiteral(s)) {
			pushLiteralFloat(s);
		} else {
			pushMemoryFloat(s);
		}
	}
	
	protected void pushMemoryFloat(String args) {
		reference(OP_PUSH, MAGIC_PUSH_FLOAT_MEMORY, args);
	}
	
	protected void pushLiteralFloat(String s) {
		float f = parseLiteralFloat(s);
		
		if (f == -1.0f) {
			immediate(OP_FUNC, F_C_M1F, NO_DATA);
		} else if (f == 0.0f) {
			immediate(OP_FUNC, F_C_0F, NO_DATA);
		} else if (f == 1.0f) {
			immediate(OP_FUNC, F_C_1F, NO_DATA);
		} else if (f == 2.0f) {
			immediate(OP_FUNC, F_C_2F, NO_DATA);
		} else if (f == 3.0f) {
			immediate(OP_FUNC, F_C_3F, NO_DATA);
		} else if (Float.isInfinite(f)) {
			immediate(OP_FUNC, F_C_INF, NO_DATA);
		} else {
			immediateFloat(OP_PUSH, MAGIC_PUSH_FLOAT_IMMEDIATE, s);
		}
	}
	
	protected void popf(String[] args) {
		String s = expectOne(args);

		if (isIndirect(s)) {
			s = s.substring(1, s.length() - 1);
			reference(OP_POP, MAGIC_POP_FLOAT_INDIRECT, s);
		} else {
			reference(OP_POP, MAGIC_POP_FLOAT, s);
		}
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
				throw new IllegalArgumentException(String.format("Expected '%s'", end));
			}
			return true;
		}
		return false;
	}
	
	public static String expectOne(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Wrong number of arguments (1 expected, "
					+ args.length + " received)");
		}
		return args[0];
	}
	
	protected static String parseComments(String line) {
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
			
			String name = line.substring(0, pos).trim();
			if (!isSymbol(name)) {
				throw new IllegalArgumentException("Invalid label name: " + name);
			}

			statements.add(new Label(name, currentLine));
			line = line.substring(pos + 1).trim();
		}
		
		return line.trim();
	}
	
	protected static final String[] EMPTY_ARGS = {};
	
	protected static String[] parseArgs(String arguments) {
		if (arguments.trim().isEmpty()) {
			return EMPTY_ARGS;
		}

		String[] args = arguments.split(",");
		
		for (int i=0; i < args.length; i++) {
			args[i] = args[i].trim();
		}
		
		return args;
	}
	
	protected static int parseLiteralByte(String s) {
		if (!isLiteral(s)) {
			throw new IllegalArgumentException("Expected #");
		}
		
		return parseByteValue(s.substring(1));
	}
	
	protected static int parseByteValue(String s) {
		if (RobotConstsTable.contains(s)) {
			return RobotConstsTable.get(s) & 0xFF;
		}

		try {
			if (s.startsWith("$")) {
				return Integer.parseInt(s.substring(1), 16) & 0xFF;
			}
			
			return Integer.parseInt(s, 10) & 0xFF;
		}
		catch (NumberFormatException e) {
			// Accommodate for recent change to float literals
			try {
				Float.parseFloat(s);
			} catch (NumberFormatException ef) {
				// Ok, this simply isn't a number.
				throw new IllegalArgumentException(String.format("Invalid value: %s", s));
			}
			// It's a valid float but not an int. Maybe the user meant a literal?
			throw new IllegalArgumentException(String.format("Invalid value: %s" +
					"%nNote: Float literals must begin with '#'."
					+ " This is a recent change that might cause the problem", s));
		}
	}
	
	protected static float parseLiteralFloat(String s) {
		if (!isLiteral(s)) {
			throw new IllegalArgumentException("Float literals must begin with '#'."
					+ " This is a recent change that might cause the problem");
		}
		
		try {
			return Float.parseFloat(s.substring(1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					String.format("Invalid float value: %s", s));
		}
	}

	protected int getAddress(String s) {
		int addr;

		if (isLiteral(s)) {
			throw new IllegalArgumentException("Cannot address a literal");
		}
		if (isSymbol(s)) {
			if (!labels.containsKey(s)) {
				throw new IllegalArgumentException(
						String.format("Cannot find label '%s'", s));
			}
			
			addr = labels.get(s);
		} else {
			addr = parseByteValue(s);
		}
		return addr;
	}
	
	protected void reference(int op, int data, String s) {
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return 2;
			}
			
			public void compileImpl() {
				int addr = getAddress(s);
				
				writeOp(op, data);
				write8(addr);
			}
		});
	}
	
	protected void reference(int op, int data, String[] args) {
		reference(op, data, expectOne(args));
	}
	
	protected void immediate(int op, int data, byte[] payload) {
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return 1 + payload.length;
			}
			
			public void compileImpl() {
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
		byte[] payload = new byte[] { (byte)parseLiteralByte(s) };
		immediate(op, data, payload);
	}
	
	protected void immediateFloat(int op, int data, String s) {
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return 5;
			}
			
			public void compileImpl() {
				writeOp(op, data);
				writeFloat(parseLiteralFloat(s));
			}
		});
	}
	
	protected void relative(int op, String s) {
		if (isLiteral(s)) {
			throw new IllegalArgumentException("Cannot address a literal for relative instruction");
		}
		
		statements.add(new Statement(currentLine) {
			public int measureImpl() {
				return 1;
			}
			
			public void compileImpl() {
				int addr = getAddress(s);
				int r = addr - (pc + 1);
				
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
	
	private void rethrowWithLine(IllegalArgumentException e, int currentLine) {
		String messageWithLine =
				String.format("%s on line %d", e.getMessage(), currentLine);
		throw new IllegalArgumentException(messageWithLine, e);
	}

	/**
	 * @return the number of bytes used
	 */
	public int getBytesUsed() {
		return bytesUsed;
	}
	
	protected abstract class Statement {
		public final int line;
		
		public Statement(int line) {
			this.line = line;
		}
		
		public int measure() {
			try {
				return measureImpl();
			} catch (IllegalArgumentException e) {
				rethrowWithLine(e, line);
				return 0;
			}
		}
		
		public void compile() {
			try {
				compileImpl();
			} catch (IllegalArgumentException e) {
				rethrowWithLine(e, line);
			}
		}
		
		protected abstract int measureImpl();
		protected abstract void compileImpl();
	}
	
	protected class Label extends Statement {
		final String name;
		
		public Label(String name, int line) {
			super(line);
			this.name = name;
		}
		
		public int measureImpl() {
			if (labels.containsKey(name)) {
				throw new IllegalArgumentException(String.format("Redefined label '%s'", name));
			}
			labels.put(name, pc);
			return 0;
		}
		
		public void compileImpl() {
			
		}
	}
}