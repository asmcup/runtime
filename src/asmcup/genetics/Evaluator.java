package asmcup.genetics;

import java.util.*;

import asmcup.runtime.*;
import asmcup.vm.VM;

// TODO: Add Evaluator test(s)!
// - Evaluate same spawn x times

// TODO: The split into an actual scoring unit and a "spawn manager" could be made
// more decisively, but the lines should be clear for now.

public class Evaluator {
	protected final ArrayList<Spawn> spawns = new ArrayList<>();
	public int maxSimFrames;
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
		Scorer scorer = new Scorer();
		float score = 0.0f;
		
		for (Spawn spawn : spawns) {
			score += scorer.calculate(ram, spawn);
			
			for (int i = 1; i <= extraWorldCount; i++) {
				score += scorer.calculate(ram, spawn.search(i));
			}
		}
		
		return score;
	}
	
	private class Scorer {
		private Robot robot;
		private VM vm;
		private World world;
		
		private int lastGold;
		private int lastBattery;

		private HashSet<Integer> rammed;
		private HashSet<Integer> explored;
		private int lastExplored;
		
		public float calculate(byte[] ram, Spawn spawn) {
			vm = new VM(ram.clone());
			robot = new Robot(1, vm);
			world = spawn.getNewWorld();
			
			world.addRobot(robot);
			robot.position(spawn.x, spawn.y);
			robot.setFacing(spawn.facing);
			
			float score = 0.0f;
			
			lastGold = 0;
			lastBattery = robot.getBattery();
			explored = new HashSet<>();
			rammed = new HashSet<>();
			lastExplored = 0;
			
			for (int frame = 0; frame < maxSimFrames; frame++) {
				world.tick();
				
				if (violatesStackRules()) {
					break;
				}
				if (violatesIoRules()) {
					break;
				}
				if (robot.isDead()) {
					break;
				}
				
				float t = getTimeBenefitFactor(frame);
				
				score += rewardGoldCollection(t);
				score += rewardBatteryCollection(t);

				int tileKey = getTileKey();
				score += rewardExploration(tileKey, t, frame);
				score -= penaliseRamming(tileKey, t);
				
				lastGold = robot.getGold();
				lastBattery = robot.getBattery();

				if (idledTooLong(frame)) {
					break;
				}
				if (ioIdledTooLong(frame)) {
					break;
				}
			}
			
			return score;
		}

		private boolean violatesIoRules() {
			return forceIO && robot.getLastInvalidIO() > 0;
		}
	
		private boolean violatesStackRules() {
			if (forceStack <= 0) {
				return false;
			}
			
			int stackLimit = (0xFF - forceStack);
			int sp = vm.getStackPointer();
			int pc = vm.getProgramCounter();
			return sp < stackLimit || pc > stackLimit;
		}
	
		private float getTimeBenefitFactor(int frame) {
			return (temporal ? 1.0f - (float)frame / (float)maxSimFrames : 1.0f);
		}
		
		private float rewardGoldCollection(float t) {
			return (robot.getGold() == lastGold) ? 0 : t * goldReward;
		}
		
		private float rewardBatteryCollection(float t) {
			return (robot.getBattery() <= lastBattery) ? 0 : t * batteryReward;
		}

		private int getTileKey() {
			int col = (int)(robot.getX() / World.TILE_SIZE);
			int row = (int)(robot.getY() / World.TILE_SIZE);
			return col | (row << 16);
		}
	
		private float rewardExploration(int key, float t, int frame) {
			if (explored.add(key)) {
				lastExplored = frame;
				return t * exploreReward;
			}
			return 0;
		}
	
		private float penaliseRamming(int tileKey, float t) {
			if (ramPenalty != 0) {
				if (robot.isRamming() && rammed.add(tileKey)) {
					return t * ramPenalty;
				}
			}
			return 0;
		}

		private boolean idledTooLong(int frame) {
			return idleMax > 0 && (frame - lastExplored) > idleMax;
		}
		
		private boolean ioIdledTooLong(int frame) {
			return idleIoMax > 0 && (frame - robot.getLastIO()) > idleIoMax;
		}
	}

	public void addSpawn(Spawn spawn) {
		spawns.add(spawn);
	}

	public void clearSpawns() {
		spawns.clear();
	}
	
	public Iterable<Spawn> getSpawns() {
		return spawns;
	}
	
	public int getSpawnCount() {
		return spawns.size();
	}
}