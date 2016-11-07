package asmcup.vm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;

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
	public void testWrite8() {
		vm.write8(0, 0xff);
		assertEquals((byte) 0xff, vm.getMemory()[0]);
		assertEquals(0x00, vm.getMemory()[1]); // We're not overflowing

		vm = new VM();
		vm.write8(0, 0xcace);
		assertEquals((byte) 0xce, vm.getMemory()[0]); // we're only writing one byte
		assertEquals(0x00, vm.getMemory()[1]);
	}

	@Test
	public void testWrite16() {
		ByteBuffer bb = getByteBuffer(vm.getMemory());
		vm.write16(0, 0xffff);
		assertEquals((short) 0xffff, bb.getShort(0));
		assertEquals(0x00, bb.getShort(2)); // We're not overflowing

		vm = new VM();
		bb = getByteBuffer(vm.getMemory());
		vm.write16(0, 0xfaceb00c);
		assertEquals((short) 0xb00c, bb.getShort(0)); // we're only writing two bytes
		assertEquals(0x00, bb.getShort(2));
	}

	@Test
	public void testWrite32() {
		ByteBuffer bb = getByteBuffer(vm.getMemory());
		vm.write32(0, 0xffffffff);
		assertEquals(0xffffffff, bb.getInt(0));
		assertEquals(0x00, bb.getInt(4)); // We're not overflowing
	}

	@Test
	public void testWriteFloat() {
		ByteBuffer bb = getByteBuffer(vm.getMemory());
		vm.writeFloat(0, 1.23456f);
		assertEquals(1.23456f, bb.getFloat(0), 0.1);
		assertEquals(0x00, bb.getInt(4)); // We're not overflowing
	}

	@Test
	public void testRead8() {
		vm.write8(0, 0xff);
		vm.write8(1, 0xee);
		assertEquals(0xff, vm.read8());
		assertEquals(0xff, vm.read8(0));
		assertEquals(0xee, vm.read8()); // increasing pc
		assertEquals(0xff, vm.read8(0));
	}

	@Test
	public void testRead8Indirect() {
		vm.write8(0, 42);
		vm.write8(42, 0xff);
		assertEquals(0xff, vm.read8indirect());
	}

	@Test
	public void testRead16() {
		vm.write16(0, 0xffff);
		assertEquals(0xffff, vm.read16());
	}

	@Test
	public void testRead32() {
		vm.write32(0, 0xffffffff);
		assertEquals(0xffffffff, vm.read32());
	}

	@Test
	public void testReadFloat() {
		vm.writeFloat(0, 1.23456f);
		assertEquals(1.23456f, vm.readFloat(), 0.00001f);
	}

	@Test
	public void testReadFloatIndirect() {
		vm.write8(0, 42);
		vm.writeFloat(42, 1.23456f);
		assertEquals(1.23456f, vm.readFloatIndirect(), 0.0001f);
	}


	@Test
	public void testPush8() {
		vm.push8(0xff);
		vm.push8(0xee);
		assertEquals(0xee, vm.pop8());
		assertEquals(0xff, vm.pop8());
	}

	@Test
	public void testPush16() {
		vm.push16(0xffff);
		vm.push16(0xeeee);
		assertEquals(0xeeee, vm.pop16());
		assertEquals(0xffff, vm.pop16());
	}

	@Test
	public void testPush32() {
		vm.push32(0xffffffff);
		vm.push32(0xeeeeeeee);
		assertEquals(0xeeeeeeee, vm.pop32());
		assertEquals(0xffffffff, vm.pop32());
	}

	@Test
	public void testPushFloat() {
		vm.pushFloat(1.23456f);
		vm.pushFloat(2.7182f);
		assertEquals(2.7182f, vm.popFloat(), 0.0001f);
		assertEquals(1.23456f, vm.popFloat(), 0.0001f);
	}

	@Test
	public void testStackPointer() {
		assertEquals(0xff, vm.getStackPointer());
		vm.push8(0xff);
		assertEquals(0xfe, vm.getStackPointer());
		vm.push8(0xff);
		vm.pop8();
		assertEquals(0xfe, vm.getStackPointer());
		vm.pop8();
		assertEquals(0xff, vm.getStackPointer());
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
}
