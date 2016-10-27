package asmcup.genetics;

import java.util.Random;

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
	
	public Spawn search(int offset) {
		float sx = x, sy = y;
		World world = getNewWorld();
		Random random = new Random(seed + offset);
		
		// Wiggle around until the start position is fair.
		// TODO ? Make this the job of world ("deterministic" random)?
		// TODO needs to check for an isSpawnable
		
		while (world.isSolid(sx, sy, 25)) {
			sx += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
			sy += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
		}
		
		return new Spawn(sx, sy, facing, seed);
	}
	
	public World getNewWorld() {
		return new World(seed);
	}
}
