package asmcup.sandbox;

import java.awt.event.*;

import javax.swing.*;

public class Menu extends JMenuBar {
	protected final Sandbox sandbox;
	
	public Menu(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		addWorldMenu();
		addRobotMenu();
		addViewMenu();
	}
	
	protected JMenuItem item(String label, ActionListener f,
			KeyStroke shortcut) {
		JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction(label) {
			public void actionPerformed(ActionEvent e) {
				f.actionPerformed(e);
			}
		});
		
		if (shortcut != null) {
			item.setAccelerator(shortcut);
		}
		
		return item;
	}
	
	protected JMenuItem item(String label, ActionListener f) {
		return item(label, f, null);
	}
	
	protected JMenuItem item(String label, ActionListener f, int key) {
		return item(label, f, KeyStroke.getKeyStroke(key, 0));
	}
	
	public void teleport() {
		sandbox.getMouse().startTeleport();
	}

	public void pauseResume() {
		sandbox.pauseResume();
	}
	
	public void singleTick() {
		sandbox.singleTick();
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
	
	public void toggleGrid() {
		sandbox.toggleGrid();
	}
	
	protected void addRobotMenu() {
		JMenu menu = new JMenu("Robot");
		menu.add(item("Teleport", e -> teleport(), KeyEvent.VK_T));
		menu.add(item("Pause/Resume", e -> pauseResume(), KeyEvent.VK_P));
		menu.add(item("Single tick", e -> singleTick(), KeyEvent.VK_S));
		menu.addSeparator();
		menu.add(item("Center View", e -> centerView(), KeyEvent.VK_SPACE));
		menu.addSeparator();
		menu.add(item("Show Code Editor", e -> showCodeEditor(), KeyEvent.VK_E));
		menu.add(item("Show Debugger", e -> showDebugger(), KeyEvent.VK_D));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", e -> reseed()));
		menu.addSeparator();
		menu.add(item("Show Info", e -> showWorldInfo()));
		menu.addSeparator();
		menu.add(item("Quit", e -> sandbox.quit(), KeyEvent.VK_ESCAPE));
		add(menu);
	}
	
	protected void addViewMenu() {
		JMenu menu = new JMenu("View");
		menu.add(item("Toggle Grid", e-> toggleGrid(), KeyEvent.VK_G));
		add(menu);
	}
}