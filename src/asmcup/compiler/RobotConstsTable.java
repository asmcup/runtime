package asmcup.compiler;

import java.lang.reflect.Field;
import java.util.*;

import asmcup.runtime.Robot;

public class RobotConstsTable {
	private static final Map<String, Integer> consts;

	static {
		consts = new HashMap<>();

		for (Field field : Robot.class.getDeclaredFields()) {
			if (isConst(field.getName())) {
				try {
					add(field);
				} catch (Exception e) {
					throw new RuntimeException("Failed to access static member via reflection");
				}
			}
		}
	}
	
	private static boolean isConst(String name) {
		return name.startsWith("IO_");
	}
	
	private static void add(Field field) throws Exception {
		consts.put(field.getName(), field.getInt(null));
	}
	
	public static boolean contains(String s) {
		return consts.containsKey(s);
	}
	
	public static int get(String s) {
		return consts.get(s);
	}
}
