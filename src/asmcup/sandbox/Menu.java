package asmcup.sandbox;

import java.awt.event.*;

import javax.swing.*;

public class Menu extends JMenuBar {
	protected final Sandbox sandbox;
	
	public Menu(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		addWorldMenu();
		addRobotMenu();
	}
	
	public AbstractAction item(String name, ActionListener listener) {
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}
		};
	}
		
	public void teleport() {
		sandbox.getMouse().startTeleport();
	}
	
	public void showCodeEditor() {
		sandbox.getCodeEditor().setVisible(true);
	}
	
	public void showDebugger() {
		sandbox.getDebugger().setVisible(true);
	}
	
	public void reseed() {
		sandbox.reseed();
	}
	
	public void showWorldInfo() {
		
	}
	
	public void centerView() {
		sandbox.centerView();
	}
	
	protected void addRobotMenu() {
		JMenu menu = new JMenu("Robot");
		menu.add(item("Teleport", e -> teleport()));
		menu.addSeparator();
		menu.add(item("Center View", e -> centerView()));
		menu.addSeparator();
		menu.add(item("Show Code Editor", e -> showCodeEditor()));
		menu.add(item("Show Debugger", e -> showDebugger()));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", e -> { reseed(); }));
		menu.addSeparator();
		menu.add(item("Load Snapshot...", e -> {  }));
		menu.addSeparator();
		menu.add(item("Save Snapshot", e -> {  }));
		menu.add(item("Save Snapshot As...", e -> {  }));
		menu.addSeparator();
		menu.add(item("Show Info", e -> { showWorldInfo(); }));
		menu.addSeparator();
		menu.add(item("Quit", e -> { sandbox.quit(); }));
		add(menu);
	}
}