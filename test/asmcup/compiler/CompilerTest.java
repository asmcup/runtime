package asmcup.compiler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompilerTest {

    private Compiler compiler = null;

    @Before
    public void setUp() {
        compiler = new Compiler();
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
        fail("TEST");
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
    	assertEquals(ram1[0], ram2[0]);
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
