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
		menu.add(item("Teleport", e -> teleport(),
				KeyStroke.getKeyStroke(KeyEvent.VK_T, 0)));
		menu.addSeparator();
		menu.add(item("Center View", e -> centerView(),
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)));
		menu.addSeparator();
		menu.add(item("Show Code Editor", e -> showCodeEditor(),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, 0)));
		menu.add(item("Show Debugger", e -> showDebugger(),
				KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", e -> { reseed(); }, null));
		menu.addSeparator();
		menu.add(item("Load Snapshot...", e -> {  }, null));
		menu.addSeparator();
		menu.add(item("Save Snapshot", e -> {  }, null));
		menu.add(item("Save Snapshot As...", e -> {  }, null));
		menu.addSeparator();
		menu.add(item("Show Info", e -> { showWorldInfo(); }, null));
		menu.addSeparator();
		menu.add(item("Quit", e -> { sandbox.quit(); },
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));
		add(menu);
	}
}