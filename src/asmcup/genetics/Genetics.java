package asmcup.genetics;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.Robot;
import asmcup.sandbox.*;

public class Genetics extends JFrame {
	protected final Sandbox sandbox;
	protected int programSize = 256;
	protected Thread thread;
	protected boolean running = false;
	public Evaluator evaluator = new Evaluator();
	public GeneticAlgorithm ga = new GeneticAlgorithm(evaluator);
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
	protected FrontPanel panel = new FrontPanel();
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		pinButton.addActionListener(e -> ga.pin());
		unpinButton.addActionListener(e -> ga.unpin());
		spawnButton.addActionListener(e -> spawn());
		unspawnButton.addActionListener(e -> evaluator.unspawn());
		
		panel.addRow("Population:", popSpinner, "Number of robots that are kept in the gene pool");
		panel.addRow("Frames:", frameSpinner, "Maximum number of frames for the simulation (10 frames = 1 second)");
		panel.addRow("Random Tests:", extraWorldSpinner, "Bots are placed into a set of random worlds");
		panel.addRow("Mutation Chance:", mutationSpinner, "Maximum chance that mutation will occur during mating");
		panel.addRow("Mutation Size:", chunkSpinner, "Maximum number of bytes that will be changed per mutation");
		panel.addRow("Program Size:", sizeSpinner, "Number of bytes in the ROM that will be used");
		panel.addRow("Idle Timeout:", idleSpinner, "Number of frames a bot has to move before being killed (0 is disabled)");
		panel.addRow("IO Idle Timeout:", idleIoSpinner, "Number of frames a bot has to use IO before being killed (0 is disabled)");
		panel.addRow("Gold Reward:", goldSpinner, "Number of points earned by collecting some gold");
		panel.addRow("Battery Reward:", batterySpinner, "Number of points earned by collecting some battery");
		panel.addRow("Explore Reward:", exploreSpinner, "Number of points earned by touching a new tile");
		panel.addRow("Collide Penalty:", rammingSpinner, "Number of points lost by ramming a tile for the first time");
		panel.addRow("Early Reward:", temporalSpinner, "Scale mpoints so earlier activity is worth more");
		panel.addRow("Force Stack:", stackSpinner, "Kill a bot if the stack pointer ever goes outside this much (0 is disabled)");
		panel.addRow("Force IO:", ioSpinner, "Kill a bot if it ever generates an invalid IO command");
		panel.addRow("Best:", bestLabel, "Highest score in the gene pool");
		panel.addRow("Worst:", worstLabel, "Lowest score in the gene pool");
		panel.addRow("Mutation:", mutationLabel, "Current chance of mutation");
		panel.addRow("Generation:", genLabel, "Current generation of gene pool");
		panel.addItems(pinButton, unpinButton);
		panel.addItems(spawnButton, unspawnButton);
		panel.addItems(saveButton, flashButton);
		panel.addItems(stopButton, startButton);

		// The order is important here!
		configureEvaluator();
		configureGA();
		
		setContentPane(panel);
		pack();
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
			sandbox.loadROM(ga.getBestDNA());
			sandbox.reset();
			sandbox.getRobot().setFacing(evaluator.userSpawn.facing);
			sandbox.getRobot().position(evaluator.userSpawn.x, evaluator.userSpawn.y);
			sandbox.redraw();
		}
	}
	
	public void save() {
		byte[] best = ga.getBestDNA();
		
		try {
			Utils.write(sandbox.getFrame(), "bin", "Program Binary", best);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	public void updateStats() {
		worstLabel.setText(String.valueOf(ga.getWorstScore()));
		bestLabel.setText(String.valueOf(ga.getBestScore()));
		genLabel.setText(String.valueOf(ga.generation));
		mutationLabel.setText(String.valueOf(ga.mutationRate) + "%");
	}
}
