package asmcup.genetics;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.evaluation.EvaluatorFrontPanel;
import asmcup.evaluation.SpawnEvaluator;
import asmcup.sandbox.*;

public class Genetics extends JFrame {
	public final Sandbox sandbox;
	public final GeneticsMenu menu;
	public final GeneticAlgorithm ga;
	public final SpawnEvaluator evaluator;
	public final FrontPanel panel = new FrontPanel();
	public final EvaluatorFrontPanel evalPanel;
	public final GAFrontPanel gaPanel;
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected Thread thread;
	protected boolean running = false;
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		menu = new GeneticsMenu(this);
		evaluator = new SpawnEvaluator(sandbox.spawns, false);
		ga = new GeneticAlgorithm(evaluator);
		evalPanel = new EvaluatorFrontPanel(evaluator);
		gaPanel = new GAFrontPanel(ga);
		
		panel.addWideItem(evalPanel);
		panel.addWideItem(gaPanel);
		panel.addItems(stopButton, startButton);
		
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		
		// The order is important here!
		evalPanel.updateEvaluator();
		gaPanel.update();
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		setContentPane(panel);
		pack();
	}
	
	public GeneticsMenu getMenu() {
		return menu;
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		
		if (sandbox.spawns.size() <= 0) {
			sandbox.spawns.addSpawnAtRobot();
		}
		
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		evalPanel.setComponentsEnabled(false);
		gaPanel.setComponentsEnabled(false);

		evalPanel.updateEvaluator();
		gaPanel.update();
		
		thread = new Thread(new Runnable() {
			public void run() {
				ga.resizePopulation(gaPanel.getPopulationSize());
				
				while (running) {
					ga.nextGeneration();
					gaPanel.updateStats();
				}
			}
		});
		
		running = true;
		thread.start();
	}
	
	public void stop() {
		running = false;
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		evalPanel.setComponentsEnabled(true);
		gaPanel.setComponentsEnabled(true);
	}
	
	public void flash() {
		sandbox.loadROM(ga.getBestDNA());
	}
}
