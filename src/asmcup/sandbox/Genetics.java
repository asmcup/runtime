package asmcup.sandbox;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.*;
import asmcup.vm.VM;

public class Genetics extends JFrame {
	protected final Sandbox sandbox;
	protected int programSize = 256;
	protected int fitnessFrames = 10 * 60;
	protected Gene[] population = new Gene[100];
	protected Random random = new Random();
	protected Thread thread;
	protected boolean running = false;
	protected int generation = 0;
	protected Spawn user = new Spawn();
	protected int mutationRate = 1;
	protected int minMutationRate = 1;
	protected int maxMutationRate = 100;
	protected int controlCount = 0;
	protected int chunkSize = 4;
	protected int idleMax = 0;
	protected int idleIoMax = 0;
	protected int exploreReward = 4;
	protected int ramPenalty = 2;
	protected int goldReward = 50;
	protected int batteryReward = 100;
	protected int forceStack;
	protected boolean temporal = true;
	protected boolean forceIO = false;
	protected ArrayList<Gene> pinned = new ArrayList<>();
	protected ArrayList<Spawn> spawns = new ArrayList<>();
	protected JLabel bestLabel = new JLabel("0");
	protected JLabel worstLabel = new JLabel("0");
	protected JLabel genLabel = new JLabel("0");
	protected JLabel mutationLabel = new JLabel("0");
	protected JButton flashButton = new JButton("Flash");
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected JButton saveButton = new JButton("Save");
	protected JButton pinButton = new JButton("Pin");
	protected JButton unpinButton = new JButton("Unpin");
	protected JButton spawnButton = new JButton("Spawn");
	protected JButton unspawnButton = new JButton("Unspawn");
	protected ArrayList<JSpinner> spinners = new ArrayList<>();
	protected JSpinner popSpinner = createSpinner(population.length, 1, 1000 * 1000);
	protected JSpinner controlSpinner = createSpinner(0, 0, 100);
	protected JSpinner mutationSpinner = createSpinner(100, 0, 100);
	protected JSpinner sizeSpinner = createSpinner(256, 1, 256);
	protected JSpinner frameSpinner = createSpinner(10 * 60, 1, 10 * 60 * 60 * 24);
	protected JSpinner idleSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner idleIoSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner chunkSpinner = createSpinner(chunkSize, 0, 256);
	protected JSpinner exploreSpinner = createSpinner(exploreReward, -1000, 1000);
	protected JSpinner rammingSpinner = createSpinner(ramPenalty, -1000, 1000);
	protected JSpinner goldSpinner = createSpinner(goldReward, -1000, 1000);
	protected JSpinner batterySpinner = createSpinner(batteryReward, -1000, 1000);
	protected JSpinner temporalSpinner = createSpinner(temporal ? 1 : 0, 0, 1);
	protected JSpinner stackSpinner = createSpinner(forceStack, 0, 256);
	protected JSpinner ioSpinner = createSpinner(forceIO ? 1 : 0, 0, 1);
	protected JPanel panel = new JPanel(new GridLayout(23, 2));
	
	public static class Spawn {
		public float x, y, facing;
		public int seed;
	}
	
	public static class Gene implements Comparable<Gene> {
		byte[] ram;
		float score;
		int gen;
		
		public int compareTo(Gene other) {
			float d = score - other.score;
			
			if (d == 0) {
				return other.gen - gen;
			} else if (d < 0) {
				return 1;
			}
			
			return -1;
		}
	}
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		
		for (int i=0; i < population.length; i++) {
			population[i] = random();
		}
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		pinButton.addActionListener(e -> pin());
		unpinButton.addActionListener(e -> unpin());
		spawnButton.addActionListener(e -> spawn());
		unspawnButton.addActionListener(e -> unspawn());
		
		hitem("Population:", popSpinner);
		hitem("Frames:", frameSpinner);
		hitem("Control:", controlSpinner);
		hitem("Mutate Chance:", mutationSpinner);
		hitem("Mutate Size:", chunkSpinner);
		hitem("Size:", sizeSpinner);
		hitem("Idle:", idleSpinner);
		hitem("Idle IO:", idleIoSpinner);
		hitem("Gold Reward:", goldSpinner);
		hitem("Battery Reward:", batterySpinner);
		hitem("Explore Reward:", exploreSpinner);
		hitem("Collide Penalty:", rammingSpinner);
		hitem("Temporal:", temporalSpinner);
		hitem("Force Stack:", stackSpinner);
		hitem("Force IO:", ioSpinner);
		hitem("Best:", bestLabel);
		hitem("Worst:", worstLabel);
		hitem("Mutation:", mutationLabel);
		hitem("Generation:", genLabel);
		hitem(pinButton, unpinButton);
		hitem(spawnButton, unspawnButton);
		hitem(saveButton, flashButton);
		hitem(stopButton, startButton);
		
		setContentPane(panel);
		pack();
	}
	
	public void hitem(String label, JComponent component) {
		hitem(new JLabel(label), component);
	}
	
	public void hitem(JComponent a, JComponent b) {
		panel.add(a);
		panel.add(b);
	}
	
	public JSpinner createSpinner(int value, int min, int max) {
		SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
		JSpinner spinner = new JSpinner(model);
		spinners.add(spinner);
		return spinner;
	}
	
	public void setSpinnersEnabled(boolean enabled) {
		for (JSpinner spinner : spinners) {
			spinner.setEnabled(enabled);
		}
	}
	
	public int getInt(JSpinner spinner) {
		return (Integer)spinner.getValue();
	}
	
	public Iterable<Spawn> getSpawns() {
		return spawns;
	}
	
	public void clearSpawns() {
		spawns.clear();
	}
	
	public void spawn() {
		Spawn s = new Spawn();
		Robot robot = sandbox.getRobot();
		s.x = robot.getX();
		s.y = robot.getY();
		s.facing = robot.getFacing();
		s.seed = sandbox.getWorld().getSeed();
		spawns.add(s);
	}
	
	public void unspawn() {
		if (!spawns.isEmpty()) {
			spawns.remove(spawns.size() - 1);
		}
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
		setSpinnersEnabled(false);
		
		controlCount = getInt(controlSpinner);
		maxMutationRate = getInt(mutationSpinner);
		programSize = getInt(sizeSpinner);
		fitnessFrames = getInt(frameSpinner);
		chunkSize = getInt(chunkSpinner);
		idleMax = getInt(idleSpinner);
		idleIoMax = getInt(idleIoSpinner);
		exploreReward = getInt(exploreSpinner);
		ramPenalty = getInt(rammingSpinner);
		temporal = getInt(temporalSpinner) > 0;
		forceStack = getInt(stackSpinner);
		forceIO = getInt(ioSpinner) > 0;
		
		resizePopulation();
		
		user.x = sandbox.getRobot().getX();
		user.y = sandbox.getRobot().getY();
		user.facing = sandbox.getRobot().getFacing();
		user.seed = sandbox.getWorld().getSeed();
		
		thread = new Thread(new Runnable() {
			public void run() {
				while (running) {
					generation();
				}
			}
		});
		
		for (Gene gene : population) {
			gene.score = score(gene.ram);
		}
		
		running = true;
		thread.start();
	}
	
	public void resizePopulation() {
		int newSize = getInt(popSpinner);
		
		if (population.length != newSize) {
			Gene[] newPop = new Gene[newSize];
			
			for (int i=0; i < newSize; i++) {
				if (i < population.length) {
					newPop[i] = population[i];
				} else {
					newPop[i] = random();
				}
			}
			
			population = newPop;
		}
	}
	
	public void stop() {
		running = false;
		setSpinnersEnabled(true);
	}
	
	public Gene random() {
		Gene gene = new Gene();
		gene.ram = sandbox.getROM().clone();
		gene.score = score(gene.ram);
		return gene;
	}
	
	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.loadROM(getBest().ram.clone());
			sandbox.reset();
			sandbox.getRobot().setFacing(user.facing);
			sandbox.getRobot().position(user.x, user.y);
			sandbox.redraw();
		}
	}
	
	public void save() {
		Gene best = getBest();
		
		try {
			Utils.write(sandbox.getFrame(), "bin", "Program Binary", best.ram);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pin() {
		pinned.add(getBest());
	}
	
	public void unpin() {
		pinned.clear();
	}
	
	public void generation() {
		int halfPoint = population.length / 2;
		int pin = pinned.size();
		
		for (int i=halfPoint; i < population.length; i++) {
			if (pin > 0) {
				pin--;
				population[i] = cross(pinned.get(pin), randomGene());
			} else {
				population[i] = cross();
			}
		}
		
		Arrays.sort(population);
		float p = getWorst().score / getBest().score;
		mutationRate = minMutationRate + (int)(p * maxMutationRate);
		mutationRate = Math.max(minMutationRate, mutationRate);
		mutationRate = Math.min(maxMutationRate, mutationRate);
		generation++;
		updateStats();
	}
	
	public Gene randomGene() {
		int i = random.nextInt(population.length / 2);
		return population[i];
	}
	
	public Gene cross() {
		int a, b;
		
		do {
			a = random.nextInt(population.length / 2);
			b = random.nextInt(population.length / 2);
		} while (a == b);
		
		return cross(population[a], population[b]);
	}
	
	public Gene cross(Gene mom, Gene dad) {
		Gene gene = new Gene();
		gene.ram = mom.ram.clone();
		gene.gen = generation;
		
		int src, dest, size;
		
		if (random.nextInt(100) <= mutationRate) {
			dest = random.nextInt(programSize);
			size = 1 + random.nextInt(chunkSize);
			
			for (int i=0; i < size; i++) {
				gene.ram[(dest + i) % programSize] = (byte)random.nextInt(256);
			}
		}
		
		src = random.nextInt(programSize);
		dest = random.nextInt(programSize);
		size = 1 + random.nextInt(programSize);
		
		for (int i=0; i < size; i++) {
			gene.ram[(dest + i) % programSize] = dad.ram[(src + i) % programSize];
		}
		
		gene.score = score(gene.ram);
		return gene;
	}
	
	public float score(byte[] ram) {
		float score = score(ram, user);
		
		for (Spawn s : spawns) {
			score += score(ram, s);
		}
		
		for (int i=1; i <= controlCount; i++) {
			score += score(ram, randomSpawn(i));
		}
		
		return score;
	}
	
	public Spawn randomSpawn(int i) {
		Spawn spawn = new Spawn();
		spawn.seed = (int)(Math.random() * 0xFFFFFF);
		spawn.x = user.y;
		spawn.y = user.y;
		spawn.facing = user.facing + (float)(i * Math.PI * 0.25);
		
		World world = new World(user.seed + i);
		Random random = new Random(user.seed + i);
		
		while (world.isSolid(spawn.x, spawn.y, 25)) {
			spawn.x += (random.nextFloat() - 0.5f) * World.CELL_SIZE * 0.1f;
			spawn.y += (random.nextFloat() - 0.5f) * World.CELL_SIZE * 0.1f;
		}
		
		return spawn;
	}
	
	public float score(byte[] ram, Spawn spawn) {
		Robot robot = new Robot(1);
		World world = new World(spawn.seed);
		
		world.addRobot(robot);
		robot.position(spawn.x, spawn.y);
		robot.setFacing(spawn.facing);
		robot.flash(ram.clone());
		
		float score = 0.0f;
		int lastGold = 0;
		int lastBattery = robot.getBattery();
		HashSet<Integer> explored = new HashSet<>();
		HashSet<Integer> rammed = new HashSet<>();
		int lastExplored = 0;
		VM vm = robot.getVM();
		
		for (int frame=0; frame < fitnessFrames; frame++) {
			world.tick();
			
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
				t = 1.0f - (float)frame / (float)fitnessFrames;
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
	
	public Gene getWorst() {
		return population[population.length / 2 - 1];
	}
	
	public Gene getBest() {
		return population[0];
	}
	
	public void updateStats() {
		worstLabel.setText(String.valueOf(getWorst().score));
		bestLabel.setText(String.valueOf(getBest().score));
		genLabel.setText(String.valueOf(generation));
		mutationLabel.setText(String.valueOf(mutationRate) + "%");
	}
}
