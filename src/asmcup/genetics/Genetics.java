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
	protected Thread thread;
	protected boolean running = false;
	protected JButton flashButton = new JButton("Flash");
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected JButton saveButton = new JButton("Save");
	// TODO: This doesn't need to be a front panel
	protected FrontPanel panel = new FrontPanel();
	public EvaluatorFrontPanel evalPanel;
	public GAFrontPanel gaPanel;
	
	public Genetics(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		setTitle("Genetics");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/dna.png")));
		
		evalPanel = new EvaluatorFrontPanel(sandbox);
		gaPanel = new GAFrontPanel(evalPanel.evaluator);
		
		flashButton.addActionListener(e -> flash());
		startButton.addActionListener(e -> start());
		stopButton.addActionListener(e -> stop());
		saveButton.addActionListener(e -> save());
		
		panel.addWideItem(evalPanel);
		panel.addWideItem(gaPanel);
		panel.addItems(saveButton, flashButton);
		panel.addItems(stopButton, startButton);

		// The order is important here!
		evalPanel.update();
		gaPanel.update();
		
		setContentPane(panel);
		pack();
	}
	
	public void start() {
		if (thread != null && thread.isAlive()) {
			return;
		}

		evalPanel.setSpinnersEnabled(false);
		gaPanel.setSpinnersEnabled(false);

		evalPanel.update();
		gaPanel.update();
		
		thread = new Thread(new Runnable() {
			public void run() {
				while (running) {
					gaPanel.ga.nextGeneration();

					gaPanel.updateStats();
				}
			}
		});
		
		running = true;
		thread.start();
	}
	
	public void stop() {
		running = false;
		evalPanel.setSpinnersEnabled(true);
		gaPanel.setSpinnersEnabled(true);
	}
	
	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.loadROM(gaPanel.ga.getBestDNA());
			sandbox.applySpawn(evalPanel.evaluator.userSpawn);
		}
	}
	
	public void save() {
		byte[] best = gaPanel.ga.getBestDNA();
		
		try {
			Utils.write(sandbox.getFrame(), "bin", "Program Binary", best);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
