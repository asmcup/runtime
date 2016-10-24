package asmcup.vm;

import org.junit.Before;
import org.junit.Test;

import asmcup.vm.VM;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VMTest {

    private VM vm = null;
    
    
    @Before
    public void setUp() {
    	vm = new VM();
    }
    		

    @Test
    public void testStackOpsAdd() {
    	vm.push8(13);
    	vm.push8(37);
    	vm.op_func(VMFuncs.F_ADD8);
    	assertEquals(vm.pop8(), 50);
    }
    
    @Test
    public void testStackOpsXor() {
    	vm.push8(0b001101);
    	vm.push8(0b010111);
    	vm.op_func(VMFuncs.F_XOR);
    	assertEquals(vm.pop8(), 0b011010);
    }

    @Test
    public void testStackOpsDup8() {
    	vm.push8(0x2A);
    	vm.op_func(VMFuncs.F_DUP8);
    	assertEquals(vm.pop8(), 0x2A);
    	assertEquals(vm.pop8(), 0x2A);
    }

    @Test
    public void testStackOpsDupf() {
    	vm.pushFloat(0.1f);
    	vm.op_func(VMFuncs.F_DUPF);
    	// Yes, binary equivalence should remain.
    	assertTrue(vm.popFloat() == 0.1f);
    	assertTrue(vm.popFloat() == 0.1f);
    }
    
    @Test
    public void testStackOpsSubroutine() {
    	int startPC = vm.getProgramCounter();
    	vm.push8(0xab);
    	vm.op_func(VMFuncs.F_JSR);
    	assertEquals(vm.getProgramCounter(), 0xab);
    	vm.op_func(VMFuncs.F_RET);
    	assertEquals(vm.getProgramCounter(), startPC);
    }
}
