package asmcup.evaluation;

import javax.swing.*;

import asmcup.sandbox.*;

public class EvaluatorFrontPanel extends FrontPanel {
	protected final Evaluator evaluator;
	// Note: These initial values are not authoritative, the ones in Evaluator are.
	protected JSpinner frameSpinner = createSpinner(10 * 60, 1, 10 * 60 * 60 * 24);
	protected JSpinner extraWorldSpinner = createSpinner(0, 0, 100);
	protected JSpinner idleSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner idleIoSpinner = createSpinner(0, 0, 1000 * 1000);
	protected JSpinner exploreSpinner = createSpinner(4, -1000, 1000);
	protected JSpinner rammingSpinner = createSpinner(2, -1000, 1000);
	protected JSpinner goldSpinner = createSpinner(50, -1000, 1000);
	protected JSpinner batterySpinner = createSpinner(100, -1000, 1000);
	protected JCheckBox temporalCheckBox = createCheckBox();
	protected JSpinner stackSpinner = createSpinner(0, 0, 256);
	protected JCheckBox ioCheckBox = createCheckBox();
	
	public EvaluatorFrontPanel(Evaluator evaluator) {
		this.evaluator = evaluator;
		
		setBorder(BorderFactory.createTitledBorder("Evaluation Metrics"));
		
		addRow("Random Tests:", extraWorldSpinner, "Bots are evaluated in an additional set of random worlds");
		addRow("Frames:", frameSpinner, "Maximum number of frames for the simulation (10 frames = 1 second)");
		addRow("Gold Reward:", goldSpinner, "Number of points earned by collecting a gold item");
		addRow("Battery Reward:", batterySpinner, "Number of points earned by collecting a battery item");
		addRow("Explore Reward:", exploreSpinner, "Number of points earned by touching a new tile");
		addRow("Collide Penalty:", rammingSpinner, "Number of points lost by ramming a tile for the first time");
		if (!evaluator.simplified) {
			addRow("Idle Timeout:", idleSpinner, "Number of frames without movement before a bot is killed (0 is disabled)");
			addRow("IO Idle Timeout:", idleIoSpinner, "Number of frames without IO before a bot is killed (0 is disabled)");
			addRow("Force Stack:", stackSpinner, "Kill a bot if the stack pointer ever goes beyond this (0 is disabled)");
			addRow("Force IO:", ioCheckBox, "Kill a bot if it ever generates an invalid IO command");
		}
		addRow("Early Reward:", temporalCheckBox, "Scale points so earlier activity is worth more");
		updateSliders();
	}
	
	public void updateSliders() {
		extraWorldSpinner.setValue(evaluator.extraWorldCount);
		frameSpinner.setValue(evaluator.maxSimFrames);
		goldSpinner.setValue(evaluator.goldReward);
		batterySpinner.setValue(evaluator.batteryReward);
		exploreSpinner.setValue(evaluator.exploreReward);
		idleSpinner.setValue(evaluator.idleMax);
		idleIoSpinner.setValue(evaluator.idleIoMax);
		rammingSpinner.setValue(evaluator.ramPenalty);
		stackSpinner.setValue(evaluator.forceStack);
		ioCheckBox.setSelected(evaluator.forceIO);
		temporalCheckBox.setSelected(evaluator.temporal);
	}

	public void updateEvaluator() {
		evaluator.extraWorldCount = getInt(extraWorldSpinner);
		evaluator.maxSimFrames = getInt(frameSpinner);
		evaluator.goldReward = getInt(goldSpinner);
		evaluator.batteryReward = getInt(batterySpinner);
		evaluator.exploreReward = getInt(exploreSpinner);
		evaluator.idleMax = getInt(idleSpinner);
		evaluator.idleIoMax = getInt(idleIoSpinner);
		evaluator.ramPenalty = getInt(rammingSpinner);
		evaluator.forceStack = getInt(stackSpinner);
		evaluator.forceIO = ioCheckBox.isSelected();
		evaluator.temporal = temporalCheckBox.isSelected();
	}
}
