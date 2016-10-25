package asmcup.runtime;

import java.io.ByteArrayOutputStream;

import asmcup.vm.VM;

public class RecordedVM extends VM {
	protected boolean trapIO;
	protected ByteArrayOutputStream output;
	
	public RecordedVM(byte[] rom) {
		super(rom);
		output = new ByteArrayOutputStream();
	}
	
	public boolean hasRecordedIO() {
		return output.size() > 0;
	}
	
	public byte[] getRecordedIO() {
		byte[] data = output.toByteArray();
		output.reset();
		return data;
	}
	
	@Override
	public int pop8() {
		int value = super.pop8();
		
		if (trapIO) {
			output.write(value);
		}
		
		return value;
	}
}