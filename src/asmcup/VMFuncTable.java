package asmcup;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class VMFuncTable {
	private static final Map<String, Integer> funcs;
	private static final Map<Integer, String> reverse;

	static {
		funcs = new HashMap<>();
		reverse = new HashMap<>();

		for (Field field : VMFuncs.class.getDeclaredFields()) {
			try {
				bind(field);
			} catch (Exception e) {
				throw new RuntimeException("Failed to access static member via reflection");
			}
		}
	}
	
	private static void bind(Field field) throws Exception {
		String s = field.getName();

		if (!s.startsWith("F_")) {
			return;
		}

		int value = field.getInt(null);
		s = s.substring("F_".length()).toLowerCase();
		bind(s, value);
	}

	private static void bind(String name, int code) {
		funcs.put(name, code);
		reverse.put(code, name);
	}
	
	public static int parse(String s) {
		return funcs.get(s.toLowerCase().trim());
	}
	
	public static String unparse(int index) {
		return reverse.get(index);
	}
	
	public static boolean exists(String name) {
		return funcs.containsKey(name.toLowerCase().trim());
	}
}
