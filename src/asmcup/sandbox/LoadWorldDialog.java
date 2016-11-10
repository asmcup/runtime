package asmcup.sandbox;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JSpinner;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

import asmcup.genetics.Spawn;
import asmcup.runtime.World;

public class LoadWorldDialog extends JFrame {
	protected final Sandbox sandbox;
	protected final FrontPanel panel = new FrontPanel();

	protected JSpinner spinnerSeed; 
	protected JSpinner spinnerX, spinnerY;
	protected JSpinner spinnerFacing;
	protected JButton loadButton = new JButton("Load");
	protected JButton closeButton = new JButton("Close");
	
	public LoadWorldDialog(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		
		spinnerSeed   = panel.createSpinner(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		spinnerX      = panel.createSpinner(0.0f, 0f, (float)World.SIZE);
		spinnerY      = panel.createSpinner(0.0f, 0f, (float)World.SIZE);
		spinnerFacing = panel.createSpinner(0.0f, -(float)Math.PI, (float)Math.PI * 2, 0.1f);
		panel.addRow("World Seed:", spinnerSeed);
		panel.addRow("Robot X:", spinnerX);
		panel.addRow("Robot Y:", spinnerY);
		panel.addRow("Robot Facing:", spinnerFacing);
		panel.setBorder(BorderFactory.createTitledBorder("Spawn Location"));
		
		loadButton.addActionListener(e -> load());
		closeButton.addActionListener(e -> close());
		panel.addItems(loadButton, closeButton);
		
		setTitle("Load World");
		setResizable(false);
		setIconImage(ImageIO.read(getClass().getResource("/world.png")));
		setContentPane(panel);
		pack();
	}
	
	public void load() {
		Spawn spawn = new Spawn(panel.getFloat(spinnerX),
		                        panel.getFloat(spinnerY),
		                        panel.getFloat(spinnerFacing),
		                        panel.getInt(spinnerSeed));
		sandbox.loadSpawn(spawn);
	}
	
	public void update() {
		spinnerSeed.setValue(sandbox.getWorld().getSeed());
		spinnerX.setValue(sandbox.getRobot().getX());
		spinnerY.setValue(sandbox.getRobot().getY());
		spinnerFacing.setValue(sandbox.getRobot().getFacing());
	}
	
	public void close() {
		setVisible(false);
	}
}
