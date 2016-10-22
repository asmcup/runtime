package asmcup.sandbox;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.*;

public class Genetics extends JFrame {
	protected final Sandbox sandbox;
	protected int programSize = 256;
	protected int fitnessFrames = 10 * 60;
	protected JLabel bestLabel = new JLabel("0");
	protected JLabel worstLabel = new JLabel("0");
	protected JLabel genLabel = new JLabel("0");
	protected JLabel mutationLabel = new JLabel("0");
	protected JButton flashButton = new JButton("Flash");
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected JButton saveButton = new JButton("Save");
	protected JSpinner popSpinner = createSpinner(100, 1, 1000 * 1000);
	protected JSpinner controlSpinner = createSpinner(0, 0, 100);
	protected JSpinner mutationSpinner = createSpinner(100, 0, 100);
	protected JSpinner sizeSpinner = createSpinner(256, 1, 256);
	protected JSpinner frameSpinner = createSpinner(10 * 60, 1, 10 * 60 * 60 * 24);
	protected Gene[] population = new Gene[100];
	protected Random random = new Random();
	protected Thread thread;
	protected boolean running = false;
	protected int generation = 0;
	protected float startX, startY;
	protected int seed;
	protected int mutationRate = 1;
	protected int minMutationRate = 1;
	protected int maxMutationRate = 100;
	protected int controlCount = 0;
	
	public static class Gene implements Comparable<Gene> {
		byte[] ram;
		float score;
		int gen;
		
		public int compareTo(Gene other) {
			float d = score - other.score;
			
			if (d == 0) {
				return 0;
				//return other.gen - gen;
			} else if (d < 0) {
				return 1;
			}
			
			return -1;
		}
	}
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		for (int i=0; i < population.length; i++) {
			population[i] = random();
		}
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		
		JPanel panel = new JPanel(new GridLayout(11, 2));
		
		panel.add(new JLabel("Population:"));
		panel.add(popSpinner);
		
		panel.add(new JLabel("Frames:"));
		panel.add(frameSpinner);
		
		panel.add(new JLabel("Control:"));
		panel.add(controlSpinner);
		
		panel.add(new JLabel("Max Mutate:"));
		panel.add(mutationSpinner);
		
		panel.add(new JLabel("Size:"));
		panel.add(sizeSpinner);
		
		panel.add(new JLabel("Best:"));
		panel.add(bestLabel);
		
		panel.add(new JLabel("Worst:"));
		panel.add(worstLabel);
		
		panel.add(new JLabel("Mutation:"));
		panel.add(mutationLabel);
		
		panel.add(new JLabel("Generation:"));
		panel.add(genLabel);
		
		panel.add(saveButton);
		panel.add(flashButton);
		
		panel.add(stopButton);
		panel.add(startButton);
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		setContentPane(panel);
		pack();
	}
	
	public JSpinner createSpinner(int value, int min, int max) {
		SpinnerModel model = new SpinnerNumberModel(value, min, max, 1);
		JSpinner spinner = new JSpinner(model);
		return spinner;
	}
	
	public int getInt(JSpinner spinner) {
		return (Integer)spinner.getValue();
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
		popSpinner.setEnabled(false);
		controlSpinner.setEnabled(false);
		mutationSpinner.setEnabled(false);
		sizeSpinner.setEnabled(false);
		frameSpinner.setEnabled(false);
		
		controlCount = getInt(controlSpinner);
		maxMutationRate = getInt(mutationSpinner);
		programSize = getInt(sizeSpinner);
		fitnessFrames = getInt(frameSpinner);
		
		resizePopulation();
		
		startX = sandbox.getRobot().getX();
		startY = sandbox.getRobot().getY();
		seed = sandbox.getWorld().getSeed();
		
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
		
		popSpinner.setEnabled(true);
		controlSpinner.setEnabled(true);
		mutationSpinner.setEnabled(true);
		sizeSpinner.setEnabled(true);
		frameSpinner.setEnabled(true);
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
			sandbox.getRobot().setFacing(0);
			sandbox.getRobot().position(startX, startY);
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
	
	public void generation() {
		int halfPoint = population.length / 2;
		
		for (int i=halfPoint; i < population.length; i++) {
			population[i] = cross();
		}
		
		Arrays.sort(population);
		float p = getWorst().score / getBest().score;
		mutationRate = minMutationRate + (int)(p * maxMutationRate);
		mutationRate = Math.max(minMutationRate, mutationRate);
		mutationRate = Math.min(maxMutationRate, mutationRate);
		generation++;
		updateStats();
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
			size = 1 + random.nextInt(programSize);
			
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
		float user = score(ram, seed, startX, startY);
		
		for (int i=1; i <= controlCount; i++) {
			user += score(ram, seed + i, startX, startY);
		}
		
		return user;
	}
	
	public float score(byte[] ram, int seed, float x, float y) {
		Robot robot = new Robot(1);
		World world = new World(seed);
		world.addRobot(robot);
		Random random = null;
		
		while (world.isSolid(x, y, 25)) {
			if (random == null) {
				random = new Random(world.getSeed());
			}
			
			x = random.nextFloat() * World.SIZE;
			y = random.nextFloat() * World.SIZE;
		}
		
		robot.position(x, y);
		robot.flash(ram.clone());
		
		float score = 0.0f;
		int lastGold = 0;
		int lastBattery = robot.getBattery();
		HashSet<Integer> explored = new HashSet<Integer>();
		
		for (int frame=0; frame < fitnessFrames; frame++) {
			world.tick();
			
			if (robot.isDead()) {
				break;
			}
			
			if (robot.isRamming()) {
				score -= 0.1f;
			}
			
			int collected = robot.getGold() - lastGold;
			float t = 1.0f - (float)frame / (float)fitnessFrames;
			
			if (collected > 0) {
				score += t * 50;
			}
			
			int recharged = robot.getBattery() - lastBattery;
			
			if (recharged > 0) {
				score += t * 100;
			}
			
			int col = (int)(robot.getX() * 4.0f / World.TILE_SIZE);
			int row = (int)(robot.getY() * 4.0f / World.TILE_SIZE);
			int key = col | (row << 16);
			
			if (explored.add(key)) {
				score += t * 5;
			}
			
			lastGold = robot.getGold();
			lastBattery = robot.getBattery();
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
