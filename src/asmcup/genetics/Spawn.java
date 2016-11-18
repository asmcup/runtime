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
	
	public World getNewWorld() {
		return new World(seed);
	}

	public static Spawn randomFromSeed(int seed) {
		Random random = new Random(seed);
		float sx = random.nextFloat() * World.SIZE;
		float sy = random.nextFloat() * World.SIZE;
		float facing = random.nextFloat() * (float)Math.PI * 2;
		World world = new World(seed);
		
		// Wiggle around until the start position is fair.
		// TODO ? Make this the job of world ("deterministic" random)?
		// TODO needs to check for an isSpawnable
		
		while (!world.canSpawnRobotAt(sx, sy)) {
			sx += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
			sy += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
		}
		
		return new Spawn(sx, sy, facing, seed);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + " -> " + facing + ") in " + seed;
	}
}
