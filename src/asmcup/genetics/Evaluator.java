package asmcup.genetics;

import java.util.*;

import asmcup.runtime.*;
import asmcup.vm.VM;

// TODO: Add Evaluator test(s)!
// - Evaluate same spawn x times

public class Evaluator {
	public int maxSimFrames;
	public Spawn userSpawn;
	public int extraWorldCount;
	public int idleMax;
	public int idleIoMax;
	public int exploreReward;
	public int ramPenalty;
	public int goldReward;
	public int batteryReward;
	public int forceStack;
	public boolean temporal;
	public boolean forceIO;
	protected ArrayList<Spawn> spawns = new ArrayList<>();

	public Evaluator() {
		maxSimFrames = 10 * 60;
		extraWorldCount = 0;
		idleMax = 0;
		idleIoMax = 0;
		exploreReward = 4;
		ramPenalty = 2;
		goldReward = 50;
		batteryReward = 100;
		temporal = true;
		forceIO = false;
	}
	
	public float score(byte[] ram) {
		float score = scoreForSpawn(ram, userSpawn);
		
		for (Spawn s : spawns) {
			score += scoreForSpawn(ram, s);
		}
		
		for (int i = 1; i <= extraWorldCount; i++) {
			score += scoreForSpawn(ram, randomSpawn(i));
		}
		
		return score;
	}

	
	public float scoreForSpawn(byte[] ram, Spawn spawn) {
		VM vm = new VM(ram.clone());
		Robot robot = new Robot(1, vm);
		World world = spawn.getNewWorld();
		
		world.addRobot(robot);
		robot.position(spawn.x, spawn.y);
		robot.setFacing(spawn.facing);
		
		float score = 0.0f;
		int lastGold = 0;
		int lastBattery = robot.getBattery();
		HashSet<Integer> explored = new HashSet<>();
		HashSet<Integer> rammed = new HashSet<>();
		int lastExplored = 0;
		
		for (int frame = 0; frame < maxSimFrames; frame++) {
			world.tick();
			
			// TODO: Break down into small functions
			if (forceStack > 0) {
				if (vm.getStackPointer() < (0xFF - forceStack)) {
					break;
				}
			}
			
			if (forceIO && robot.getLastInvalidIO() > 0) {
				break;
			}
			
			if (robot.isDead()) {
				break;
			}
			
			float t;
			
			if (temporal) {
				t = 1.0f - (float)frame / (float)maxSimFrames;
			} else {
				t = 1.0f;
			}
			
			int collected = robot.getGold() - lastGold;
			
			if (collected > 0) {
				score += t * goldReward;
			}
			
			int recharged = robot.getBattery() - lastBattery;
			
			if (recharged > 0) {
				score += t * batteryReward;
			}
			
			int col = (int)(robot.getX() / World.TILE_SIZE);
			int row = (int)(robot.getY() / World.TILE_SIZE);
			int key = col | (row << 16);
			
			if (explored.add(key)) {
				score += t * exploreReward;
				lastExplored = frame;
			}
			
			if (ramPenalty != 0) {
				if (robot.isRamming() && rammed.add(key)) {
					score -= t * ramPenalty;
				}
			}
			
			lastGold = robot.getGold();
			lastBattery = robot.getBattery();
			
			if (idleMax > 0 && frame > idleMax) {
				if ((frame - lastExplored) > idleMax) {
					break;
				}
			}
			
			if (idleIoMax > 0 && frame > idleIoMax) {
				if ((frame - robot.getLastIO()) > idleIoMax) {
					break;
				}
			}
		}
		
		return score;
	}
	
	public Spawn randomSpawn(int offset) {
		return userSpawn.applySeedOffset(offset);
	}

	public void addSpawn(Spawn spawn) {
		spawns.add(spawn);
	}
	
	public void unspawn() {
		if (!spawns.isEmpty()) {
			spawns.remove(spawns.size() - 1);
		}
	}

	public void clearSpawns() {
		spawns.clear();
	}
	
	public Iterable<Spawn> getSpawns() {
		return spawns;
	}
}