package asmcup.sandbox;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.*;

public class Genetics extends JFrame {
	protected final Sandbox sandbox;
	protected int programSize = 32;
	protected int fitnessFrames = 10 * 60;
	protected JLabel bestLabel = new JLabel("0");
	protected JLabel worstLabel = new JLabel("0");
	protected JLabel genLabel = new JLabel("0");
	protected JButton flashButton = new JButton("Flash");
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected Gene[] population = new Gene[500];
	protected Random random = new Random();
	protected Thread thread;
	protected boolean running = false;
	protected int generation = 0;
	protected float startX, startY;
	protected int seed;
	protected int mutationChance = 10;
	
	public static class Gene implements Comparable<Gene> {
		byte[] ram;
		int score;
		
		public int compareTo(Gene other) {
			return other.score - score;
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
		
		JPanel panel = new JPanel(new GridLayout(5, 2));
		
		panel.add(new JLabel("Best:"));
		panel.add(bestLabel);
		
		panel.add(new JLabel("Worst:"));
		panel.add(worstLabel);
		
		panel.add(new JLabel("Generation:"));
		panel.add(genLabel);
		
		panel.add(new JLabel(""));
		panel.add(flashButton);
		
		panel.add(stopButton);
		panel.add(startButton);
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		setContentPane(panel);
		pack();
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
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
	
	public void stop() {
		running = false;
	}
	
	public Gene random() {
		Gene gene = new Gene();
		gene.ram = new byte[256];
		
		for (int i=0; i < programSize; i++) {
			gene.ram[i] = (byte)random.nextInt(256);
		}
		
		gene.score = score(gene.ram);
		return gene;
	}
	
	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.getRobot().flash(population[0].ram.clone());
			sandbox.reset();
			sandbox.getRobot().setFacing(0);
			sandbox.getRobot().position(startX, startY);
		}
	}
	
	public void generation() {
		int halfPoint = population.length / 2;
		
		for (int i=halfPoint; i < population.length; i++) {
			population[i] = cross();
		}
		
		Arrays.sort(population);
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
	
	public Gene cross(Gene a, Gene b) {
		Gene gene = new Gene();
		gene.ram = new byte[256];
		
		for (int i=0; i < programSize; i++) {
			gene.ram[i] = crossByte(a.ram[i], b.ram[i]);
		}
		
		gene.score = score(gene.ram);
		return gene;
	}
	
	public byte crossByte(byte a, byte b) {
		if (random.nextInt(mutationChance) == 0) {
			return (byte)random.nextInt(256);
		}
		
		if (random.nextBoolean()) {
			return a;
		} else {
			return b;
		}
	}
	
	public int score(byte[] ram) {
		Robot robot = new Robot(1);
		World world = new World(seed);
		world.addRobot(robot);
		robot.position(startX, startY);
		robot.flash(ram.clone());
		
		for (int frame=0; frame < fitnessFrames; frame++) {
			world.tick();
		}
		
		return robot.getGold();
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
	}
}
