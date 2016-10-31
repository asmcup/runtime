package asmcup.genetics;

import asmcup.runtime.World;

public class Spawn {
	public final float x, y, facing;
	public final int seed;
	
	public Spawn(float x, float y, float facing, int seed) {
		this.x = x;
		this.y = y;
		this.facing = facing;
		this.seed = seed;
	}
	
	public World getNewWorld() {
		return new World(seed);
	}
}
