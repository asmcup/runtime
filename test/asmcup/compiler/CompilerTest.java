package asmcup.compiler;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class CompilerTest {

    private Compiler compiler = null;

    @Before
    public void setUp() {
        compiler = new Compiler();
        compiler.init();
    }

    @Test
    public void testUndefinedLabel() {
        try {
            compiler.compile("push8 notfoundlabel");
            fail("Compiler did not fail on undefined label.");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot find label 'notfoundlabel'", e.getMessage());
        }
    }

    @Test
    public void testUndefinedFunction() {
        try {
            compiler.compile("undefinedfunction #0");
            fail("Compiler did not fail on undefined function.");
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown function undefinedfunction", e.getMessage());
        }
    }

    @Test
    public void testRedefinedLabel() {
        try {
            compiler.compile("label:\nlabel:");
            fail("Compiler did not fail on redefined label.");
        } catch (IllegalArgumentException e) {
            assertEquals("Redefined label 'label'", e.getMessage());
        }
    }

    @Test
    public void testCurrentLine() {
        try {
            compiler.compile("valid:\n\nundefinedfunction #0");
            fail("Compiler did not fail on undefined function.");
        } catch (IllegalArgumentException e) {
            assertEquals(3, compiler.getCurrentLine());
        }
    }

    @Test
	public void testBytesUsed() {
		compiler.compile("push8 #1");
		assertEquals(1, compiler.getBytesUsed()); // turned into c_1

		compiler.init();
		compiler.compile("push8 #42");
		assertEquals(2, compiler.getBytesUsed()); // one byte opcode, one byte literal 42

		compiler.init();
		compiler.compile("push8 #1\npush8 #0");
		assertEquals(2, compiler.getBytesUsed()); // 1 byte c_1, 1 byte c_0

		compiler.init();
		compiler.compile("start:end:");
		assertEquals(0, compiler.getBytesUsed());

		compiler.init();
		compiler.compile("start: \n jmp start");
		assertEquals(2, compiler.getBytesUsed()); // 1 byte opcode, 1 byte label addr
	}

    @Test
    public void testOutputSize() {
        assertEquals(256, compiler.compile("").length);
        assertEquals(256, compiler.compile(stringRepeat("push8 #0\n", 10)).length);
        // No error when overflowing?
        assertEquals(256, compiler.compile(stringRepeat("push8 #0\n", 290)).length);
    }

    @Test
    public void testTooFewArguments() {
        try {
            compiler.compile("push8");
            fail("Compiler did not fail on missing argument.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Wrong number of arguments"));
        }
    }
    
    @Test
    public void testTooManyArguments() {
        try {
            compiler.compile("push8 #12, #13");
            fail("Compiler did not fail on too many arguments.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Wrong number of arguments"));
        }
    }

    @Test
    public void testUnexpectedArgument() {
        try {
            compiler.compile("ret #12");
            fail("Compiler did not fail on unexpected argument.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Too many arguments"));
        }
    }
    
    @Test
    public void testAcceptWhitespace() {
    	byte[] ram1 = compiler.compile("\t  push8 \t #0  \t");
    	byte[] ram2 = compiler.compile("push8 #0");
    	assert(ramEquals(ram1, ram2));
    }

    @Test
    public void testWrite8() {
        compiler.write8(0xff);
        assertEquals((byte) 0xff, compiler.ram[0]);
        assertEquals(0x00, compiler.ram[1]); // We're not overflowing

        compiler.init();
        compiler.write8(0xcace);
        assertEquals((byte) 0xce, compiler.ram[0]); // we're only writing one byte
        assertEquals(0x00, compiler.ram[1]);

        // check pc
        compiler.write8(0xb00c);
        assertEquals(0x0c, compiler.ram[1]);
    }

    @Test
    public void testWrite16() {
        ByteBuffer bb = getByteBuffer(compiler.ram);
        compiler.write16(0xffff);
        assertEquals((short) 0xffff, bb.getShort(0));
        assertEquals(0x00, bb.getShort(2)); // We're not overflowing

        compiler.init();
        bb = getByteBuffer(compiler.ram);
        compiler.write16(0xfaceb00c);
        assertEquals((short) 0xb00c, bb.getShort(0)); // we're only writing two bytes
        assertEquals(0x00, bb.getShort(2));

        // check pc
        compiler.write16(0xb00c);
        assertEquals((short) 0xb00c, bb.getShort(2));
    }

    @Test
    public void testWrite32() {
        ByteBuffer bb = getByteBuffer(compiler.ram);
        compiler.write32(0xffffffff);
        assertEquals(0xffffffff, bb.getInt(0));
        assertEquals(0x00, bb.getInt(4)); // We're not overflowing

        // check pc
        compiler.write32(0xfaceb00c);
        assertEquals(0xfaceb00c, bb.getInt(4));
    }

    @Test
    public void testWriteFloat() {
        ByteBuffer bb = getByteBuffer(compiler.ram);
        compiler.writeFloat(1.23456f);
        assertEquals(1.23456f, bb.getFloat(0), 0.1);
        assertEquals(0x00, bb.getInt(4)); // We're not overflowing

        // check pc
        compiler.writeFloat(2.7182f);
        assertEquals(2.7182f, bb.getFloat(4), 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteOpcodeTooLarge() {
        compiler.writeOp(0b100, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteOpcodeDataTooLarge() {
        compiler.writeOp(0, 0b1000000);
    }

    @Test
    public void testWriteOpcode() {
        compiler.writeOp(0b11, 0b111111);
        assertEquals((byte) 0xff, compiler.ram[0]);

        compiler.writeOp(0, 0);
        assertEquals((byte) 0, compiler.ram[1]);

        compiler.writeOp(0b11, 0b000000);
        assertEquals((byte) 0b00000011, compiler.ram[2]);

        compiler.writeOp(0b00, 0b111111);
        assertEquals((byte) 0b11111100, compiler.ram[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLiteralError() {
        Compiler.parseLiteral("notaliteral");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLiteralNAN() {
        Compiler.parseLiteral("#nan");
    }

    @Test
    public void testParseLiteral() {
        assertEquals(42, Compiler.parseLiteral("#42"));
    }

    @Test
    public void testParseLiteralHex() {
        assertEquals(0xff, Compiler.parseLiteral("#$ff"));
    }
    
    @Test
    public void testParseFloats() {
        assertEquals(1.23456f, Compiler.parseFloat("1.23456"), 0.0001);
        assertEquals(12000f, Compiler.parseFloat("12e3"), 0.1);
    }

    @Test
    public void testParseLiteralFloats() {
        assertEquals(1.23456f, Compiler.parseFloat("#1.23456"), 0.0001);
        assertEquals(12000f, Compiler.parseFloat("#12e3"), 0.1);
    }

    @Test
    public void testParseIndirection() {
    	assertTrue(Compiler.isIndirect("[anything]"));
    	assertTrue(Compiler.isIndirect("(anything)"));
    }

    @Test
    public void testPushes() {
    	compiler.compile("push8 #13 \n push8 37 \n push8 #$ab \n push8 $cd \n" + 
				"pushf 1.2");
    }
    
    public void testPushesRelative() {
    	compiler.compile("label: dbf 0.0 \n push8r label");
    	compiler.compile("push8r 5 \n push8r $0a");
    }
    
    @Test
    public void testPops() {
    	compiler.compile("pop8 37 \n pop8 $ab \n popf 13 \n popf $3d");
    }
    
    @Test
    public void testPopsRelative() {
    	compiler.compile("label: dbf 0.0 \n pop8r label");
    	compiler.compile("pop8r 7 \n pop8r $0a");
    }

    @Test
    public void testPopsIndirect() {
    	compiler.compile("pop8 [13] \n pop8 [$cd] \n popf [13] \n popf [$cd]");
    }

    @Test
    public void testPushrLiteralFail() {
    	try {
    		compiler.compile("push8r #1");
	        fail("Compiler did not fail on referencing a literal.");
	    } catch (IllegalArgumentException e) {
	        assert(e.getMessage().startsWith("Cannot address a literal"));
	    }
	}
    
    @Test
    public void testPopLiteralFail() {
        try {
        	compiler.compile("pop8 #1");
            fail("Compiler did not fail on referencing a literal.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Cannot address a literal"));
        }
    }
    
    @Test
    public void testIllegalIndirect() {
        try {
        	compiler.compile("push8 [0]");
            fail("Compiler did not fail on indirect push.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Invalid value"));
        }
    }

    @Test
    public void testPush8LabelAddress() {
    	byte[] ram = compiler.compile("db8 #0 \n labelAt1: db8 #0 \n push8 &labelAt1");
    	// Note: If the compiler ever figures out that this can be collapsed to 
    	// one of the constant functions, this test will fail.
    	assertEquals(1, ram[3]);
    }
    
    @Test
    public void testPush8NonlabelAddress() {
        try {
	    	compiler.compile("push8 &2");
	        fail("Compiler did not fail on invalid label referencing.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Invalid label"));
        }
    }

    @Test
    public void testParseComments() {
        assertEquals("not a comment", Compiler.parseComments(" not a comment"));
        assertEquals("text before", Compiler.parseComments("text before ; this comment"));
        assertEquals("multiple", Compiler.parseComments("multiple ; comment ; another"));
    }

    @Test
    public void testParseLabels() {
        assertEquals("", compiler.parseLabels("start:"));
        assertEquals(1, compiler.statements.size());
        assertEquals("", compiler.parseLabels("  two:more:"));
        assertEquals(3, compiler.statements.size());
        assertEquals("", compiler.parseLabels("  with  : spaces\t: "));
        assertEquals(5, compiler.statements.size());
        assertEquals("kein label", compiler.parseLabels("kein label"));
    }

    @Test
    public void testInvalidLabelSpaces() {
        try {
            compiler.parseLabels("label with spaces:");
            fail("Compiler did not fail on invalid label name.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Invalid label name"));
        }
    }
    
    @Test
    public void testInvalidLabelNumbers() {
        try {
            compiler.parseLabels("123labelsMustntStartWithNumbers:");
            fail("Compiler did not fail on invalid label name.");
        } catch (IllegalArgumentException e) {
            assert(e.getMessage().startsWith("Invalid label name"));
        }
    }

    @Test
    public void testParseArgs() {
        // Parse args assumes that parseLabels and parseComments already ran.
        assertArrayEquals(new String[]{}, Compiler.parseArgs(""));
        assertArrayEquals(new String[]{"argument1"}, Compiler.parseArgs("argument1"));
        assertArrayEquals(new String[]{"one argument"}, Compiler.parseArgs("one argument"));
        assertArrayEquals(new String[]{"two", "arguments"}, Compiler.parseArgs("two, arguments"));
    }

    @Test
	public void testOverflowCode() {
		for (int i = 0; i < 256; ++i) {
			compiler.write8(0xff); // Fill ram to 100%
		}
		compiler.write8(42); // should overflow to addr 0

		assertEquals(42, compiler.ram[0]);
	}

    /**
     * Returns a little endian byte buffer for given ram
     * @param ram ram to wrap byte buffer around
     * @return little endian byte buffer
     */
    private ByteBuffer getByteBuffer(byte[] ram) {
        ByteBuffer bb = ByteBuffer.wrap(ram);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb;
    }


    /**
     * Repeats str
     *
     * @param str string to repeat
     * @param times number of times
     * @return repeated string
     */
    private static String stringRepeat(String str, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            builder.append(str);
        }

        return builder.toString();
    }
    
    private static boolean ramEquals(byte[] a, byte[] b)
    {
    	if (a.length != b.length) {
    		return false;
    	}
    	for (int i = 0; i < a.length; i++) {
    		if (a[i] != b[i]) {
    			return false;
    		}
    	}
    	return true;
    }
}
