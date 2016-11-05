package asmcup.sandbox;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Base64;

import javax.swing.*;

public class Menu extends JMenuBar {
	protected final Sandbox sandbox;
	protected JDialog about;
	
	public Menu(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		addWorldMenu();
		addRobotMenu();
		addViewMenu();
		addToolsMenu();
		addGeneticsMenu();
		addHelpMenu();
	}
	
	protected JMenuItem item(String label, ActionListener f, KeyStroke k) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(f);
		
		if (k != null) {
			item.setAccelerator(k);
		}
		
		return item;
	}
	
	protected JMenuItem item(String label, ActionListener f) {
		return item(label, f, null);
	}
	
	protected JMenuItem item(String label, ActionListener f, int key) {
		return item(label, f, KeyStroke.getKeyStroke(key, 0));
	}
	
	public void showLoadWorld() {
		sandbox.loadWorld.update();
		sandbox.loadWorld.setVisible(true);
	}
	
	public void singleTick() {
		sandbox.singleTick();
	}
	
	public void setSpeed(float speed) {
		sandbox.setFramerate(Sandbox.DEFAULT_FRAMERATE * speed);
	}
	
	public void showCodeEditor() {
		sandbox.codeEditor.setVisible(true);
	}
	
	public void showDebugger() {
		sandbox.debugger.setVisible(true);
	}
	
	public void showEvaluator() {
		sandbox.evaluator.setVisible(true);
	}
	
	public void showGenetics() {
		sandbox.genetics.setVisible(true);
	}
	
	public void showAbout() {
		if (about == null) {
			createAbout();
		}
		
		about.setVisible(true);
	}
	
	protected void createAbout() {
		String text;
		
		try {
			text = Utils.readAsString(getClass().getResourceAsStream("/about.txt"));
		} catch (Exception e) {
			e.printStackTrace();
			sandbox.showError("Unable to load about.txt");
			return;
		}
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setText(text);
		
		about = new JDialog(sandbox.frame, "About");
		about.setSize(500, 350);
		about.setLocationRelativeTo(sandbox.frame);
		about.setContentPane(new JScrollPane(textArea));
	}
	
	public void showGithub() {
		browse("https://github.com/asmcup/runtime");
	}
	
	public void showHomepage() {
		browse("https://asmcup.github.io");
	}
	
	public void browse(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception e) {
			e.printStackTrace();
			sandbox.showError("Unable to open browser");
		}
	}
	
	public void loadROM() {
		try {
			byte[] rom = Utils.readAsBytes(sandbox.frame, "bin", "Program Binary");
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
		menu.add(item("Flash", e -> sandbox.flash(), KeyEvent.VK_F));
		add(menu);
	}
	
	protected void addWorldMenu() {
		JMenu menu = new JMenu("World");
		menu.add(item("Generate New", e -> sandbox.reseed(), KeyEvent.VK_N));
		menu.add(item("Reset", e -> sandbox.resetWorld(), KeyEvent.VK_R));
		menu.add(item("Load World", e -> showLoadWorld(), KeyEvent.VK_L));
		menu.addSeparator();
		menu.add(item("Pause/Resume", e -> sandbox.togglePaused(), KeyEvent.VK_P));
		menu.add(item("Single tick", e -> singleTick(), KeyEvent.VK_S));
		addSpeedMenu(menu);
		menu.addSeparator();
		menu.add(item("Quit", e -> sandbox.quit(), KeyEvent.VK_ESCAPE));
		add(menu);
	}
	
	private void addSpeedMenu(JMenu menu) {
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
		menu.add(item("Toggle Grid", e -> sandbox.toggleGrid(),
				KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK)));
		menu.addSeparator();
		menu.add(item("Center Camera", e -> sandbox.centerView(), KeyEvent.VK_SPACE));
		menu.add(item("Lock Camera", e -> sandbox.toggleLockCenter(), KeyEvent.VK_C));
		add(menu);
	}
	
	protected void addToolsMenu() {
		JMenu menu = new JMenu("Tools");
		menu.add(item("Code Editor", e -> showCodeEditor(), KeyEvent.VK_E));
		menu.add(item("Debugger", e -> showDebugger(), KeyEvent.VK_D));
		menu.add(item("Evaluator", e -> showEvaluator(), KeyEvent.VK_V));
		menu.add(item("Genetics", e-> showGenetics(), KeyEvent.VK_G));
		add(menu);
	}
	
	protected void addHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.add(item("Homepage", e -> showHomepage()));
		menu.add(item("Github Project", e -> showGithub()));
		menu.add(item("About", e -> showAbout()));
		add(menu);
	}
	
	protected void addGeneticsMenu() {
		add(sandbox.genetics.getMenu());
	}
}