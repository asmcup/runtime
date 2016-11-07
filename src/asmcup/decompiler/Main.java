package asmcup.decompiler;

import java.io.*;
import java.nio.file.Files;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.printf("USAGE: asmcup-decompiler <file>%n");
			System.exit(1);
			return;
		}

		File in = new File(args[0]);
		byte[] ram = Files.readAllBytes(in.toPath());

		if (ram.length != 256) {
			System.err.printf("ERROR: Program must be 256 bytes not %d%n", ram.length);
			System.exit(1);
			return;
		}

		Decompiler decompiler = new Decompiler();
		decompiler.decompile(ram);
	}
}
