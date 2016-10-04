package asmcup;

public interface VMConsts extends VMFuncs {
	public static final int OP_FUNC = 0;
	public static final int OP_PUSH = 1;
	public static final int OP_POP = 2;
	public static final int OP_BRANCH = 3;
	
	public static final int MAGIC_PUSH_BYTE_MEMORY = 0;
	public static final int MAGIC_PUSH_FLOAT_MEMORY = 31;
	public static final int MAGIC_PUSH_BYTE_IMMEDIATE = 32;
	public static final int MAGIC_PUSH_FLOAT_IMMEDIATE = 33;
	
	public static final int MAGIC_POP_BYTE = 0;
	public static final int MAGIC_POP_FLOAT = 31;
	public static final int MAGIC_POP_BYTE_INDIRECT = 32;
	public static final int MAGIC_POP_FLOAT_INDIRECT = 33;
	
	public static final int MAGIC_BRANCH_ALWAYS = 31;
	public static final int MAGIC_BRANCH_IMMEDIATE = 32;
	public static final int MAGIC_BRANCH_INDIRECT = 33;
	
}
