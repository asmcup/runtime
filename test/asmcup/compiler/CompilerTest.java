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

}
