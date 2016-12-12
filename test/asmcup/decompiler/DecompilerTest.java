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
				"L03: pushf #4.200000000e+01",
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
	
	@Test 
	public void testIdentityByteConstants() throws UnsupportedEncodingException {
		byte[] ram = (new Compiler()).compile(getProgram(
				"push8 #-1",
				"push8 #0",
				"push8 #1",
				"push8 #2",
				"push8 #3",
				"push8 #4",
				"push8 #5",
				"push8 #6",
				"push8 #255",
				"push8 #256",
				"push8 #257",
				"push8 #-1000",
				"push8 #1001"
		));
		
		checkDecompileCompileIdentity(ram);
	}
	
	@Test 
	public void testIdentityFloatConstants() throws UnsupportedEncodingException {
		byte[] ram = (new Compiler()).compile(getProgram(
				"pushf #1.0",
				"pushf #-0.0",
				"pushf #42",
				"pushf #NaN",
				"pushf #0.1",
				"pushf #7.0",
				"pushf #7.0e20",
				"pushf #13.37e-20",
				"pushf #Infinity",
				"pushf #-Infinity"
		));
		
		checkDecompileCompileIdentity(ram);
	}

	@Test
	public void testFuzzedPattern0() throws UnsupportedEncodingException {
		testFuzzedWithPadding(0);
	}

	@Test
	public void testFuzzedPattern1() throws UnsupportedEncodingException {
		testFuzzedWithPadding(1);
	}

	@Test
	public void testFuzzedPattern4() throws UnsupportedEncodingException {
		testFuzzedWithPadding(4);
	}
	
	public void testFuzzedWithPadding(int pad) throws UnsupportedEncodingException {
		int step = pad + 1;
		byte[] ram = new byte[256];
		
		int instruction = 0;
		for (int repeats = 0; repeats < step; repeats++) {
			for (int i = 0; i < 256; i++) {
				if (i % step == 0) {
					ram[i] = (byte)(instruction++ & 0xFF);
				} else {
					ram[i] = 0;
				}
			}
			
			checkDecompileCompileIdentity(ram);
		}
	}
	
	private void checkDecompileCompileIdentity(byte[] ram)
			throws UnsupportedEncodingException {
		Compiler compiler = new Compiler();
		out.reset();
		
		decompiler.decompile(ram);
		byte[] newRam = compiler.compile(out.toString("UTF-8"));
		
		for (int i = 0; i < 256; i++) {
			assertEquals(String.format("Memory differs at %02x", i), ram[i], newRam[i]);
		}
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
