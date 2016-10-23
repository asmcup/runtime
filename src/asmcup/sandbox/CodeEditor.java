package asmcup.sandbox;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.PlainDocument;

import asmcup.compiler.Compiler;

public class CodeEditor extends JFrame {
	protected final Sandbox sandbox;
	protected JEditorPane editor;
	protected JLabel statusLabel;
	protected Menu menu;
	protected byte[] ram = new byte[256];
	protected File currentFile;
	
	public CodeEditor(Sandbox sandbox) throws IOException {
		this.sandbox = sandbox;
		this.editor = new JEditorPane();
		this.statusLabel = new JLabel();
		this.menu = new Menu();

		JPanel statusBar = new JPanel();
		statusBar.add(statusLabel);
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(new JScrollPane(editor), BorderLayout.CENTER);
		contentPanel.add(statusBar, BorderLayout.PAGE_END);
		
		setTitle("Code Editor");
		setSize(400, 400);
		setContentPane(contentPanel);
		setJMenuBar(menu);
		setIconImage(ImageIO.read(getClass().getResource("/notepad.png")));
		
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
		new DefaultContextMenu(editor);
		
		editor.setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent e) {
				try {
					dropFiles(e);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	
	public void dropFiles(DropTargetDropEvent e) throws Exception {
		e.acceptDrop(DnDConstants.ACTION_COPY);
		Object obj = e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

		@SuppressWarnings("unchecked")
		List<File> droppedFiles = (List<File>) obj;

		if (droppedFiles.size() != 0) {
			openFile(droppedFiles.get(0));
		}
	}
	
	public void openFile() {
		if (currentFile == null) {
			currentFile = findFileOpen();
		}
		if (currentFile == null) {
			return;
		}
		
		try {
			String text = Utils.readAsString(currentFile);
			editor.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setTitle(currentFile.getName() + " - Code Editor");
	}
	
	public void openFile(File file) {
		currentFile = file;
		openFile();
	}
	
	public void closeFile() {
		editor.setText("");
		setTitle("Code Editor");
	}
	
	public void closeEditor() {
		setVisible(false);
	}
	
	public boolean compile() {
		Compiler compiler = new Compiler();
		try {
			ram = compiler.compile(editor.getText());
			statusLabel.setText(String.format("Bytes used: %d", compiler.getBytesUsed()));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					String.format("%s on line %d", e.getMessage(), compiler.getCurrentLine()));
			return false;
		}
		
		return true;
	}

	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.loadROM(ram.clone());
		}
	}
	
	public void compileAndFlash() {
		if (compile()) {
			flash();
		}
	}
	
	public File findFileSave() {
		return Utils.findFileSave(sandbox.getFrame(), "asm", "Source File (.asm)");
	}
	
	public File findFileOpen() {
		return Utils.findFileOpen(sandbox.getFrame(), "asm", "Source File (.asm)");
	}
	
	public void saveFile() {
		if (currentFile == null) {
			currentFile = findFileSave();
		}
		
		save(currentFile);
	}
	
	public void save(File file) {
		if (file == null) {
			return;
		}
		
		try {
			Utils.write(file, editor.getText());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveFileAs() {
		save(findFileSave());
	}
	
	protected class Menu extends JMenuBar {
		public Menu() {
			addFileMenu();
			addCompileMenu();
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
		
		protected void addFileMenu() {
			JMenu menu = new JMenu("File");
			menu.add(item("New Code", e -> closeFile(),
					KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)));
			menu.add(item("Open Code...", e -> openFile(null),
					KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)));
			menu.add(item("Reload Code", e -> openFile(),
					KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK)));
			menu.addSeparator();
			menu.add(item("Save Code", e -> saveFile(),
					KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)));
			menu.add(item("Save Code As...", e -> saveFileAs(),
					KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK)));
			menu.addSeparator();
			menu.add(item("Close Editor", (e) -> closeEditor(),
					KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK)));
			add(menu);
		}
		
		protected void addCompileMenu() {
			JMenu menu = new JMenu("Tools");
			menu.add(item("Compile & Flash", e -> compileAndFlash(),
					KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK)));
			menu.add(item("Compile", e -> compile(), null));
			menu.add(item("Flash", e -> flash(), null));
			add(menu);
		}
	}
}
