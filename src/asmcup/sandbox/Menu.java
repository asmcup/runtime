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
	
	protected AbstractAction item(String name, ActionListener listener) {
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}
		};
	}
	
	protected void loadROM() {
		JFileChooser chooser = new JFileChooser();
		int result = chooser.showOpenDialog(sandbox.getFrame());
		
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}
	}
	
	protected void saveROM() {
		
	}
	
	protected void teleport() {
		
	}
	
	protected void showCodeEditor() {
		sandbox.getCodeEditor().setVisible(true);
	}
	
	protected void showDebugger() {
		
	}
	
	protected void reseed() {
		
	}
	
	protected void showWorldInfo() {
		
	}
	
	protected void addRobotMenu() {
		JMenu menu = new JMenu("Robot");
		menu.add(item("Load Robot", (e) -> { loadROM(); }));
		menu.addSeparator();
		menu.add(item("Save Robot", (e) -> { saveROM(); }));
		menu.add(item("Save Robot As...", (e) -> { }));
		menu.addSeparator();
		menu.add(item("Teleport", (e) -> { teleport(); }));
		menu.addSeparator();
		menu.add(item("Center View", (e) -> {  }));
		menu.addSeparator();
		menu.add(item("Show Code Editor", (e) -> { showCodeEditor(); }));
		menu.add(item("Show Debugger", (e) -> { showDebugger(); }));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", (e) -> { reseed(); }));
		menu.addSeparator();
		menu.add(item("Load Snapshot...", (e) -> {  }));
		menu.addSeparator();
		menu.add(item("Save Snapshot", (e) -> {  }));
		menu.add(item("Save Snapshot As...", (e) -> {  }));
		menu.addSeparator();
		menu.add(item("Show Info", (e) -> { showWorldInfo(); }));
		menu.addSeparator();
		menu.add(item("Quit", (e) -> { sandbox.quit(); }));
		add(menu);
	}
}