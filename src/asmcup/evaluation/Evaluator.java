package asmcup.evaluation;

import java.util.HashSet;

import asmcup.genetics.Spawn;
import asmcup.runtime.Robot;
import asmcup.runtime.World;
import asmcup.vm.VM;

//TODO: Add Evaluator test(s)!
//- Evaluate same spawn x times

public class Evaluator {

	final public boolean simplified;
	
	public int maxSimFrames;
	public int directionsPerSpawn;
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
	
	public long scoringCount = 0;
	
	public int baseSeed = 0;
	
	public Evaluator(boolean simplified) {
		this.simplified = simplified;
		
		maxSimFrames = 10 * 60;
		directionsPerSpawn = simplified ? 1 : 8;
		extraWorldCount = 0;
		idleMax = 0;
		idleIoMax = 0;
		exploreReward = simplified ? 0 : 4;
		ramPenalty = simplified ? 0 : 2;
		goldReward = 50;
		batteryReward = simplified ? 0 : 50;
		temporal = simplified ? false : true;
		forceIO = false;
	}
	
	// TODO: Make more transparent for threading...
	public float score(byte[] ram) {
		scoringCount = 0;
		Scorer scorer = new Scorer();
		float score = 0.0f;

		for (int i = 0; i < extraWorldCount; i++) {
			score += scorer.calculate360(ram, Spawn.randomFromSeed(baseSeed + i));
		}
		
		return score / scoringCount;
	}
	
	protected class Scorer {
		private Robot robot;
		private VM vm;
		private World world;
		
		private int lastGold;
		private int lastBattery;

		private HashSet<Integer> rammed;
		private HashSet<Integer> explored;
		private int lastExplored;
		
		public float calculate360(byte[] ram, Spawn spawn) {
			float score = 0.0f;
			
			for (float turn = 0; turn < 360f; turn += 360f / directionsPerSpawn) {
				score += calculate(ram, spawn, (float)Math.toRadians(turn));
			}
			
			return score;
		}
		
		public float calculate(byte[] ram, Spawn spawn, float turn) {
			vm = new VM(ram.clone());
			robot = new Robot(1, vm);
			world = spawn.getNewWorld();
			
			world.addRobot(robot);
			robot.position(spawn.x, spawn.y);
			robot.setFacing(spawn.facing + turn);
			
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
				
				score += 0.001f;
			}
			
			scoringCount++;
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
			if (ramPenalty == 0) {
				return 0;
			}
			
			if (robot.isRamming()) {
				if (rammed.add(tileKey)) {
					return t * ramPenalty;
				} else {
					return t * ramPenalty * 0.01f;
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
}