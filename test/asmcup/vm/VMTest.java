package asmcup.vm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Random;

public class VMTest {

    private VM vm = null;
    
    
    @Before
    public void setUp() {
    	vm = new VM();
    }

    @Test(expected = IllegalArgumentException.class)
	public void testCtorWithRamError() {
		new VM(new byte[255]);
	}

	@Test
	public void testCtorWithRam() {
		byte[] ram = new byte[256];
		vm = new VM(ram);
		assert(vm.getMemory() == ram); // point to the same array
	}

	@Test
	public void testSave() throws IOException {
		byte[] ram = new byte[256];
		for (int i = 0; i < ram.length; ++i) {
			ram[i] = (byte) i;
		}

		vm = new VM(ram);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(260);
		DataOutputStream out = new DataOutputStream(baos);
		vm.save(out);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		VM saved = new VM(new DataInputStream(bais));
		Assert.assertEquals(vm, saved);
	}
    		

    @Test
    public void testStackOpsAdd() {
    	vm.push8(13);
    	vm.push8(37);
    	vm.op_func(VMFuncs.F_ADD8);
    	Assert.assertEquals(vm.pop8(), 50);
    }
    
    @Test
    public void testStackOpsXor() {
    	vm.push8(0b001101);
    	vm.push8(0b010111);
    	vm.op_func(VMFuncs.F_XOR);
    	Assert.assertEquals(vm.pop8(), 0b011010);
    }

    @Test
    public void testStackOpsDup8() {
    	vm.push8(0x2A);
    	vm.op_func(VMFuncs.F_DUP8);
    	Assert.assertEquals(vm.pop8(), 0x2A);
    	Assert.assertEquals(vm.pop8(), 0x2A);
    }

    @Test
    public void testStackOpsDupf() {
    	vm.pushFloat(0.1f);
    	vm.op_func(VMFuncs.F_DUPF);
    	// Yes, binary equivalence should remain.
    	Assert.assertTrue(vm.popFloat() == 0.1f);
    	Assert.assertTrue(vm.popFloat() == 0.1f);
    }
    
    @Test
    public void testStackOpsSubroutine() {
    	int startPC = vm.getProgramCounter();
    	vm.push8(0xab);
    	vm.op_func(VMFuncs.F_JSR);
    	Assert.assertEquals(vm.getProgramCounter(), 0xab);
    	vm.op_func(VMFuncs.F_RET);
    	Assert.assertEquals(vm.getProgramCounter(), startPC);
    }
}
