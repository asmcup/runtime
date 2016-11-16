package asmcup.decompiler;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import asmcup.compiler.Compiler;

import static org.junit.Assert.*;

public class DecompilerTest {
	private Decompiler decompiler = null;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void setUp() throws UnsupportedEncodingException {
		decompiler = new Decompiler(new PrintStream(out, false, "UTF-8"));
	}

	@Test
	public void testDump() throws UnsupportedEncodingException {
		decompiler.dump(0xff, "blabla");
		assertEquals(String.format("Lff: blabla%n"), out.toString("UTF-8"));
	}

	@Test
	public void testDecompile() throws UnsupportedEncodingException {
		// This code is not sensible and does not produce a good or valid program
		byte[] ram = (new Compiler()).compile(getProgram(
				"start:",
				"push8 #0",
				"push8 #42",
				"pushf #42.0",
				"pushf start",
				"popf $fa",
				"pop8 $fb",
				"push8 $fc",
				"jmp start",
				"jnz start",
				"jmp ($cc)",
				"popf ($a)",
				"pop8 [$b]",
				"pushf $ab"
		));

		decompiler.decompile(ram);

		assertEquals(getProgram(
				// "start:" label produces no output!
				"L00: c_0",
				"L01: push8 #$2a",
				"L03: pushf 42.000000",
				"L08: pushf $00",
				"L0a: popf $fa",
				"L0c: pop8 $fb",
				"L0e: push8 $fc",
				"L10: jmp $00",
				"L12: jnz $00",
				"L14: jmp [$cc]",
				"L16: popf [$0a]",
				"L18: pop8 [$0b]",
				"L1a: pushf $ab"
		), out.toString("UTF-8"));
	}

	private static String getProgram(String... lines) {
		StringBuilder ret = new StringBuilder();
		for (String line : lines) {
			ret.append(line);
			ret.append(System.lineSeparator());
		}

		return ret.toString();
	}
}
