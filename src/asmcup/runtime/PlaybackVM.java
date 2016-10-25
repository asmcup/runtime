package asmcup.runtime;

import asmcup.vm.VM;

public class PlaybackVM extends VM {
	protected byte[] data = {};
	
	@Override
	public void tick() {
		if (data == null) {
			setIO(false);
			return;
		}
		
		setIO(data.length > 0);
		
		for (int i=0 ; i < data.length; i++) {
			push8(data[i]);
		}
	}
}
