package asmcup.sandbox;

import java.awt.event.*;

import javax.swing.*;

import asmcup.runtime.*;

public class CanvasMenu extends JPopupMenu {
	public final Sandbox sandbox;
	public final Canvas canvas;
	
	public CanvasMenu(Canvas canvas) {
		this.canvas = canvas;
		this.sandbox = canvas.sandbox;
		
		add("Teleport", e -> teleport(e));
		add("Flash & Teleport", e -> flashAndTeleport(e));
		addSeparator();
		add("Add Gold Item", e -> addGoldItem(e));
		add("Add Battery Item", e -> addBatteryItem(e));
		addSeparator();
		add("Add Genetics Spawn", e -> addGeneticSpawn(e));
		add("Add Genetics Reward", e -> addGeneticReward(e));
	}
	
	public void teleport(ActionEvent e) {
		Mouse mouse = sandbox.mouse;
		World world = sandbox.getWorld();
		Robot robot = sandbox.getRobot();
		
		synchronized (world) {
			robot.position(mouse.getWorldX(), mouse.getWorldY());
			sandbox.redraw();
		}
	}
	
	public void flashAndTeleport(ActionEvent e) {
		sandbox.flash();
		teleport(e);
	}
	
	public void addGoldItem(ActionEvent e) {
		
	}
	
	public void addBatteryItem(ActionEvent e) {
		
	}
	
	public void addGeneticSpawn(ActionEvent e) {
		sandbox.spawns.addSpawnAtMouse();
	}
	
	public void addGeneticReward(ActionEvent e) {
		
	}
	
	protected void add(String label, ActionListener listener) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(listener);
		add(item);
	}
}