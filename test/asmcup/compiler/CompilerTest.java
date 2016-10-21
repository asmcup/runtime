package asmcup.compiler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompilerTest {

    @Test
    public void testUndefinedLabel() {
        try {
            Compiler compiler = new Compiler();
            compiler.compile("push8 notfoundlabel");
            fail("Compiler did not fail on undefined label.");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot find label 'notfoundlabel'", e.getMessage());
        }
    }

}
