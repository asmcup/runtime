package asmcup.vm;

public interface VMFuncs {
	public static final int F_NOP = 0;
	
	// Casting
	public static final int F_B2F = 1;
	public static final int F_F2B = 2;
	
	// Bitwise (8-bit)
	public static final int F_NOT = 3;
	public static final int F_OR = 4;
	public static final int F_AND = 5;
	public static final int F_XOR = 6;
	public static final int F_SHL = 7;
	public static final int F_SHR = 8;
	
	// Arithmetic (8-bit)
	public static final int F_ADD8 = 9;
	public static final int F_SUB8 = 10;
	public static final int F_MUL8 = 11;
	public static final int F_DIV8 = 12;
	public static final int F_MADD8 = 13;
	
	// Arithmetic (floating)
	public static final int F_NEGF = 14;
	public static final int F_ADDF = 15;
	public static final int F_SUBF = 16;
	public static final int F_MULF = 17;
	public static final int F_DIVF = 18;
	public static final int F_MADDF = 19;
	
	// Math (floating)
	public static final int F_COS = 20;
	public static final int F_SIN = 21;
	public static final int F_TAN = 22;
	public static final int F_ACOS = 23;
	public static final int F_ASIN = 24;
	public static final int F_ATAN = 25;
	public static final int F_ABSF = 26;
	public static final int F_MINF = 27;
	public static final int F_MAXF = 28;
	public static final int F_POW = 29;
	public static final int F_LOG = 30;
	public static final int F_LOG10 = 31;
	
	// Conditional
	public static final int F_IF_EQ8 = 32;
	public static final int F_IF_NE8 = 33;
	public static final int F_IF_LT8 = 34;
	public static final int F_IF_LTE8 = 35;
	public static final int F_IF_GT8 = 36;
	public static final int F_IF_GTE8 = 37;
	
	public static final int F_IF_LTF = 38;
	public static final int F_IF_LTEF = 39;
	public static final int F_IF_GTF = 40;
	public static final int F_IF_GTEF = 41;
	
	// Integer constants
	public static final int F_C_0 = 42;
	public static final int F_C_1 = 43;
	public static final int F_C_2 = 44;
	public static final int F_C_3 = 45;
	public static final int F_C_4 = 46;
	public static final int F_C_255 = 47;
	
	// Float constants
	public static final int F_C_0F = 48;
	public static final int F_C_1F = 49;
	public static final int F_C_2F = 50;
	public static final int F_C_3F = 51;
	public static final int F_C_4F = 52;
	public static final int F_C_INF = 53;
	public static final int F_ISNAN = 54;
	
	public static final int F_DUP8 = 55;
	public static final int F_DUPF = 56;
	
	public static final int F_JSR = 57;
	public static final int F_RET = 58;
	
	public static final int F_IO = 63;
}
