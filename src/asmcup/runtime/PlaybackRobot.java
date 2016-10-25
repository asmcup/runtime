package asmcup.runtime;

import java.util.HashMap;

public class PlaybackRobot extends Robot {
	protected final PlaybackVM vm;
	protected final HashMap<Integer, byte[]> frames;
	
	public PlaybackRobot(int id, PlaybackVM vm) {
		super(id, vm);
		this.vm = vm;
		this.frames = new HashMap<>();
	}
	
	@Override
	public void tick(World world) {
		vm.data = frames.get(world.getFrame());
		super.tick(world);
	}
}
