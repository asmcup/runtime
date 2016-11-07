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
	public void setUp() {
		decompiler = new Decompiler();
	}

	@Before
	public void setUpStreams() {
		try {
			System.setOut(new PrintStream(out, false, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
		}
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void testDump() throws UnsupportedEncodingException {
		decompiler.dump(0xff, "blabla");
		assertEquals("ff: blabla\n", out.toString("UTF-8"));
	}

	@Test
	public void testDecompile() throws UnsupportedEncodingException {
		// This code is not sensible and does not produce a good or valid program
		byte[] ram = (new Compiler()).compile(getProgram(
				"start:",
				"push8 #0",
				"push8 #42",
				"pushf 42.0",
				"pushf start",
				"popf $fa",
				"pop8 $fb",
				"push8 $fc",
				"jmp start",
				"jnz start",
				"jmp ($cc)"
				// https://github.com/asmcup/runtime/issues/99
				// "popf ($a)",
				// "pop8 [$b]"
		));

		decompiler.decompile(ram);

		assertEquals(getProgram(
				// "start:" label produces no output!
				"00: c_0",
				"01: push8 #$2a",
				"03: pushf 42.000000",
				"08: pushf $00",
				"0a: popf $fa",
				"0c: pop8 $fb",
				"0e: push8 $fc",
				"10: jmp $00",
				"12: jnz $00",
				"14: jmp [$cc]"
		), out.toString("UTF-8"));
	}

	private static String getProgram(String... lines) {
		StringBuilder ret = new StringBuilder();
		for (String line : lines) {
			ret.append(line);
			ret.append("\n");
		}

		return ret.toString();
	}
}
