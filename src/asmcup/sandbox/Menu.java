package asmcup.sandbox;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.Base64;

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
	
	public void setSpeed(float speed) {
		sandbox.setFramerate(Sandbox.DEFAULT_FRAMERATE * speed);
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
	
	public void reset() {
		sandbox.reset();
	}
	
	public void showWorldInfo() {
		
	}
	
	public void centerView() {
		sandbox.centerView();
	}
	
	public void toggleGrid() {
		sandbox.toggleGrid();
	}

	public void toggleLockCenter() {
		sandbox.toggleLockCenter();
	}
	
	public void showGenetics() {
		sandbox.getGenetics().setVisible(true);
	}
	
	public void loadROM() {
		try {
			byte[] rom = Utils.readAsBytes(sandbox.getFrame(), "bin", "Program Binary");
			sandbox.loadROM(rom);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void copyROM() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		String encoded = Base64.getEncoder().encodeToString(sandbox.getROM());
		
		try {
			clipboard.setContents(new StringSelection(encoded), null);
		} catch (Exception e) {
			e.printStackTrace();
			sandbox.showError("Unable to copy ROM: " + e.getMessage());
		}
	}
	
	public void pasteROM() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		String text = "";
		byte[] rom;
		
		try {
			text = (String)clipboard.getData(DataFlavor.stringFlavor);
			rom = Base64.getDecoder().decode(text);
			
			if (rom == null || rom.length != 256) {
				sandbox.showError("Program must be 256 bytes");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			sandbox.showError("Unable to paste: " + e.getMessage());
			return;
		}
		
		sandbox.loadROM(rom);
	}
	
	protected void addRobotMenu() {
		JMenu menu = new JMenu("Robot");
		menu.add(item("Load ROM...", e-> loadROM()));
		menu.add(item("Paste ROM", e-> pasteROM()));
		menu.add(item("Copy ROM", e -> copyROM()));
		menu.addSeparator();
		menu.add(item("Teleport", e -> teleport(), KeyEvent.VK_T));
		menu.addSeparator();
		menu.add(item("Show Code Editor", e -> showCodeEditor(), KeyEvent.VK_E));
		menu.add(item("Show Debugger", e -> showDebugger(), KeyEvent.VK_D));
		menu.add(item("Show Genetics", e-> showGenetics(), KeyEvent.VK_G));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", e -> reseed(), KeyEvent.VK_N));
		menu.add(item("Reset", e -> reset(), KeyEvent.VK_R));
		menu.addSeparator();
		menu.add(item("Pause/Resume", e -> pauseResume(), KeyEvent.VK_P));
		menu.add(item("Single tick", e -> singleTick(), KeyEvent.VK_S));
		addSimspeedMenu(menu);
		menu.addSeparator();
		menu.add(item("Quit", e -> sandbox.quit(), KeyEvent.VK_ESCAPE));
		add(menu);
	}
	
	private void addSimspeedMenu(JMenu menu) {
		JMenu speedMenu = new JMenu("Simulation speed");
		speedMenu.add(item("0.5x", e -> setSpeed(0.5f)));
		speedMenu.add(item("1x", e -> setSpeed(1f)));
		speedMenu.add(item("2x", e -> setSpeed(2f)));
		speedMenu.add(item("4x", e -> setSpeed(4f)));
		speedMenu.add(item("10x", e -> setSpeed(10f)));
		menu.add(speedMenu);
	}

	protected void addViewMenu() {
		JMenu menu = new JMenu("View");
		menu.add(item("Toggle Grid", e -> toggleGrid(),
				KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK)));
		menu.addSeparator();
		menu.add(item("Center View", e -> centerView(), KeyEvent.VK_SPACE));
		menu.add(item("Lock view to center", e -> toggleLockCenter(), KeyEvent.VK_C));
		add(menu);
	}
}