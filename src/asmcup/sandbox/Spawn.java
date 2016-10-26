package asmcup.sandbox;

import java.util.Random;

import asmcup.runtime.World;

public class Spawn {
	public float x, y, facing;
	private int seed;
	
	public Spawn(float x, float y, float facing, int seed) {
		super();
		this.x = x;
		this.y = y;
		this.facing = facing;
		this.seed = seed;
	}
	
	public Spawn applySeedOffset(int offset) {
		Spawn spawn = new Spawn(x, y,
				facing + (float)(offset * Math.PI * 0.25),
				seed + offset);
		
		World world = spawn.getNewWorld();
		
		Random random = new Random(offset);
		// Wiggle around until the start position is fair.
		// TODO ? Make this the job of world ("deterministic" random)?
		while (world.isSolid(spawn.x, spawn.y, 25)) {
			spawn.x += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
			spawn.y += (random.nextFloat() - 0.5f) * World.CELL_SIZE;
		}
		
		return spawn;
	}
	
	public World getNewWorld() {
		return new World(seed);
	}
}
