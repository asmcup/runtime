package asmcup.compiler;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.printf("USAGE: asmcup-compile <in> <out>\n");
			System.exit(1);
			return;
		}

		File inFile = new File(args[0]);
		File outFile = new File(args[1]);
		String[] lines = readLines(inFile.toPath());
		FileOutputStream output = new FileOutputStream(outFile);

		Compiler compiler = new Compiler();
		byte[] program = compiler.compile(lines);
		output.write(program);
		output.close();
	}
	
	protected static String[] readLines(Path path) throws IOException {
		List<String> list = Files.readAllLines(path);
		return list.toArray(new String[list.size()]);
	}
}
