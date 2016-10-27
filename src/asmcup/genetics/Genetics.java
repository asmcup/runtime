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
	protected ArrayList<JSpinner> spinners = new ArrayList<>();
	protected JSpinner popSpinner = createSpinner(100, 1, 1000 * 1000);
	protected JSpinner mutationSpinner = createSpinner(100, 0, 100);
	protected JSpinner sizeSpinner = createSpinner(256, 1, 256);
	protected JSpinner chunkSpinner = createSpinner(4, 0, 256);
	protected FrontPanel panel = new FrontPanel();
	public EvaluatorFrontPanel evalPanel;
	public GeneticAlgorithm ga;
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		
		evalPanel = new EvaluatorFrontPanel(sandbox);
		ga = new GeneticAlgorithm(evalPanel.evaluator);
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		pinButton.addActionListener(e -> ga.pin());
		unpinButton.addActionListener(e -> ga.unpin());
		
		panel.addItem(evalPanel);
		panel.addRow("Population:", popSpinner, "Number of robots that are kept in the gene pool");
		panel.addRow("Mutation Chance:", mutationSpinner, "Maximum chance that mutation will occur during mating");
		panel.addRow("Mutation Size:", chunkSpinner, "Maximum number of bytes that will be changed per mutation");
		panel.addRow("Program Size:", sizeSpinner, "Number of bytes in the ROM that will be used");
		panel.addRow("Best:", bestLabel, "Highest score in the gene pool");
		panel.addRow("Worst:", worstLabel, "Lowest score in the gene pool");
		panel.addRow("Mutation:", mutationLabel, "Current chance of mutation");
		panel.addRow("Generation:", genLabel, "Current generation of gene pool");
		panel.addItems(pinButton, unpinButton);
		panel.addItems(saveButton, flashButton);
		panel.addItems(stopButton, startButton);

		// The order is important here!
		evalPanel.update();
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
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
		setSpinnersEnabled(false);

		evalPanel.update();
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
			sandbox.applySpawn(evalPanel.evaluator.userSpawn);
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
