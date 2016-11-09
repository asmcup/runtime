package asmcup.evaluation;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import asmcup.sandbox.FrontPanel;
import asmcup.sandbox.Sandbox;

public class EvaluatorWindow extends JFrame {
	protected final Sandbox sandbox;
	public final SpawnEvaluator evaluator;
	public final EvaluatorFrontPanel evalPanel;
	public final FrontPanel panel = new FrontPanel();
	
	protected JButton startButton = new JButton("Start");
	protected JButton stopButton = new JButton("Stop");
	protected JLabel scoreLabel = new JLabel("0");
	
	public EvaluatorWindow(Sandbox sandbox) {
		this.sandbox = sandbox;
		evaluator = new SpawnEvaluator(sandbox.spawns);
		evalPanel = new EvaluatorFrontPanel(evaluator);

		panel.addWideItem(evalPanel);

		panel.addItems(startButton, stopButton);
		panel.addRow("ROM score:", scoreLabel);
		startButton.addActionListener(e -> evaluate());
		stopButton.addActionListener(e -> stop());
		
		setContentPane(panel);
		
		setTitle("User Bot Evaluator");
		setResizable(false);
		// TODO: Icon
		pack();
	}
	
	public void evaluate() {
		evalPanel.update();
		if (!quickScoring()) {
			// TODO: Threading, don't want to block the UI.
			// Bit difficult with how Evaluator works though.
		}
		// (else)
		float score = evaluator.score(sandbox.getROM());
		scoreLabel.setText(String.valueOf(score));
	}
	
	public void stop() {
		
	}
	
	protected boolean quickScoring() {
		return evaluator.maxSimFrames * evaluator.extraWorldCount < 2000;
	}
}
