package asmcup.genetics;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.runtime.*;
import asmcup.sandbox.*;

public class Genetics extends JFrame {
	public final Sandbox sandbox;
	public final GeneticsMenu menu;
	public final GeneticAlgorithm ga;
	public final Evaluator evaluator;
	public final FrontPanel panel = new FrontPanel();
	public final EvaluatorFrontPanel evalPanel;
	public final GAFrontPanel gaPanel;
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected Thread thread;
	protected boolean running = false;
	// TODO: This doesn't need to be a front panel
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		menu = new GeneticsMenu(this);
		evaluator = new Evaluator();
		ga = new GeneticAlgorithm(evaluator);
		evalPanel = new EvaluatorFrontPanel(evaluator);
		gaPanel = new GAFrontPanel(ga);
		
		panel.addWideItem(evalPanel);
		panel.addWideItem(gaPanel);
		panel.addItems(stopButton, startButton);
		
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		
		// The order is important here!
		evalPanel.update();
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
		
		if (evaluator.getSpawnCount() <= 0) {
			addSpawnAtRobot();
		}
		
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		evalPanel.setSpinnersEnabled(false);
		gaPanel.setSpinnersEnabled(false);

		evalPanel.update();
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
		evalPanel.setSpinnersEnabled(true);
		gaPanel.setSpinnersEnabled(true);
	}
	
	public void flash() {
		sandbox.loadROM(ga.getBestDNA());
	}
	
	public void addSpawnAtMouse() {
		Mouse mouse = sandbox.mouse;
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(mouse.getWorldX(), mouse.getWorldY(), robot.getFacing(), world.getSeed());
		evaluator.addSpawn(spawn);
		sandbox.redraw();
	}
	
	public void addSpawnAtRobot() {
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(robot.getX(), robot.getY(), robot.getFacing(), world.getSeed());
		evaluator.addSpawn(spawn);
	}
}
