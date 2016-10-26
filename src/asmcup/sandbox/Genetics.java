package asmcup.sandbox;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.*;
import asmcup.sandbox.GeneticAlgorithm.Gene;
import asmcup.sandbox.Evaluator;
import asmcup.vm.VM;

public class Genetics extends JFrame {
	protected final Sandbox sandbox;
	protected int programSize = 256;
	protected Thread thread;
	protected boolean running = false;
	protected Evaluator evaluator;
	protected GeneticAlgorithm ga;
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
	protected JSpinner popSpinner = createSpinner(100, 1, 1000 * 1000);
	protected JSpinner extraWorldSpinner = createSpinner(0, 0, 100);
	protected JSpinner mutationSpinner = createSpinner(100, 0, 100);
	protected JSpinner sizeSpinner = createSpinner(256, 1, 256);
	protected JSpinner frameSpinner = createSpinner(10 * 60, 1, 10 * 60 * 60 * 24);
	protected JSpinner idleSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner idleIoSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner chunkSpinner = createSpinner(4, 0, 256);
	protected JSpinner exploreSpinner = createSpinner(4, -1000, 1000);
	protected JSpinner rammingSpinner = createSpinner(2, -1000, 1000);
	protected JSpinner goldSpinner = createSpinner(50, -1000, 1000);
	protected JSpinner batterySpinner = createSpinner(100, -1000, 1000);
	protected JSpinner temporalSpinner = createSpinner(0, 0, 1);
	protected JSpinner stackSpinner = createSpinner(0, 0, 256);
	protected JSpinner ioSpinner = createSpinner(0, 0, 1);
	protected JPanel panel = new JPanel(new GridLayout(23, 2));
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));

		evaluator = new Evaluator(10 * 60, 0, 0, 0, 4, 2, 50, 100, true, false);
		ga = new GeneticAlgorithm(evaluator);
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		pinButton.addActionListener(e -> ga.pin());
		unpinButton.addActionListener(e -> ga.unpin());
		spawnButton.addActionListener(e -> spawn());
		unspawnButton.addActionListener(e -> evaluator.unspawn());
		
		hitem("Population:", popSpinner, "Number of robots that are kept in the gene pool");
		hitem("Frames:", frameSpinner, "Maximum number of frames for the simulation (10 frames = 1 second)");
		hitem("Random Tests:", extraWorldSpinner, "Bots are placed into a set of random worlds");
		hitem("Mutation Chance:", mutationSpinner, "Maximum chance that mutation will occur during mating");
		hitem("Mutation Size:", chunkSpinner, "Maximum number of bytes that will be changed per mutation");
		hitem("Program Size:", sizeSpinner, "Number of bytes in the ROM that will be used");
		hitem("Idle Timeout:", idleSpinner, "Number of frames a bot has to move before being killed (0 is disabled)");
		hitem("IO Idle Timeout:", idleIoSpinner, "Number of frames a bot has to use IO before being killed (0 is disabled)");
		hitem("Gold Reward:", goldSpinner, "Number of points earned by collecting some gold");
		hitem("Battery Reward:", batterySpinner, "Number of points earned by collecting some battery");
		hitem("Explore Reward:", exploreSpinner, "Number of points earned by touching a new tile");
		hitem("Collide Penalty:", rammingSpinner, "Number of points lost by ramming a tile for the first time");
		hitem("Early Reward:", temporalSpinner, "Scale mpoints so earlier activity is worth more");
		hitem("Force Stack:", stackSpinner, "Kill a bot if the stack pointer ever goes outside this much (0 is disabled)");
		hitem("Force IO:", ioSpinner, "Kill a bot if it ever generates an invalid IO command");
		hitem("Best:", bestLabel, "Highest score in the gene pool");
		hitem("Worst:", worstLabel, "Lowest score in the gene pool");
		hitem("Mutation:", mutationLabel, "Current chance of mutation");
		hitem("Generation:", genLabel, "Current generation of gene pool");
		hitem(pinButton, unpinButton);
		hitem(spawnButton, unspawnButton);
		hitem(saveButton, flashButton);
		hitem(stopButton, startButton);
		
		setContentPane(panel);
		pack();
	}
	
	public void hitem(String labelText, JComponent component, String hint) {
		JLabel label = new JLabel(labelText);
		component.setToolTipText(hint);
		label.setToolTipText(hint);
		hitem(label, component);
	}
	
	public void hitem(String label, JComponent component) {
		hitem(label, component, "");
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
	
	public Spawn getSandboxSpawn()
	{
		Robot robot = sandbox.getRobot();
		return new Spawn(robot.getX(), robot.getY(),
				robot.getFacing(), sandbox.getWorld().getSeed());
	}
	
	public void spawn() {
		evaluator.addSpawn(getSandboxSpawn());
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
		setSpinnersEnabled(false);

		configureEvaluator();
		configureGA();
		
		thread = new Thread(new Runnable() {
			public void run() {
				while (running) {
					ga.nextGeneration();

					updateStats();
				}
			}
		});
		
		running = true;
		thread.start();
	}
	
	public void configureEvaluator() {
		evaluator.maxSimFrames = getInt(frameSpinner);
		evaluator.extraWorldCount = getInt(extraWorldSpinner);
		evaluator.idleMax = getInt(idleSpinner);
		evaluator.idleIoMax = getInt(idleIoSpinner);
		evaluator.exploreReward = getInt(exploreSpinner);
		evaluator.ramPenalty = getInt(rammingSpinner);
		evaluator.temporal = getInt(temporalSpinner) > 0;
		evaluator.forceStack = getInt(stackSpinner);
		evaluator.forceIO = getInt(ioSpinner) > 0;
		
		evaluator.userSpawn = getSandboxSpawn();
	}

	public void configureGA() {
		ga.maxMutationRate = getInt(mutationSpinner);
		ga.dnaLength = getInt(sizeSpinner);
		ga.mutationSize = getInt(chunkSpinner);
		
		ga.resizePopulation(getInt(popSpinner));
	}
	
	public void stop() {
		running = false;
		setSpinnersEnabled(true);
	}
	
	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.loadROM(ga.getBest().dna.clone());
			sandbox.reset();
			sandbox.getRobot().setFacing(evaluator.userSpawn.facing);
			sandbox.getRobot().position(evaluator.userSpawn.x, evaluator.userSpawn.y);
			sandbox.redraw();
		}
	}
	
	public void save() {
		Gene best = ga.getBest();
		
		try {
			Utils.write(sandbox.getFrame(), "bin", "Program Binary", best.dna);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	public void updateStats() {
		worstLabel.setText(String.valueOf(ga.getWorst().score));
		bestLabel.setText(String.valueOf(ga.getBest().score));
		genLabel.setText(String.valueOf(ga.generation));
		mutationLabel.setText(String.valueOf(ga.mutationRate) + "%");
	}
}
