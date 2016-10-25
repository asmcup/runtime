package asmcup.compiler;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompilerTest {

    private Compiler compiler = null;

    @Before
    public void setUp() {
        compiler = new Compiler();
        compiler.statements = new ArrayList<>();
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
        compiler.init();
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
        compiler.init();
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
        compiler.init();
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
        compiler.init();
        ByteBuffer bb = getByteBuffer(compiler.ram);
        compiler.writeFloat(3.14159f);
        assertEquals(3.14159f, bb.getFloat(0), 0.1);
        assertEquals(0x00, bb.getInt(4)); // We're not overflowing

        // check pc
        compiler.writeFloat(2.7182f);
        assertEquals(2.7182f, bb.getFloat(4), 0.1);
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
        assertEquals("kein label", compiler.parseLabels("kein label"));
    }

    @Test
    public void testParseArgs() {
        // Parse args assumes that parseLabels and parseComments already ran.
        assertArrayEquals(new String[]{}, Compiler.parseArgs(""));
        assertArrayEquals(new String[]{"argument1"}, Compiler.parseArgs("argument1"));
        assertArrayEquals(new String[]{"one argument"}, Compiler.parseArgs("one argument"));
        assertArrayEquals(new String[]{"two", "arguments"}, Compiler.parseArgs("two, arguments"));
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
