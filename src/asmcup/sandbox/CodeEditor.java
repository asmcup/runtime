package asmcup.sandbox;

import java.awt.Font;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.PlainDocument;

import asmcup.compiler.Compiler;

public class CodeEditor extends JFrame {
	protected final Sandbox sandbox;
	protected JEditorPane editor;
	protected Menu menu;
	protected byte[] ram = new byte[256];
	protected File currentFile;
	
	public CodeEditor(Sandbox sandbox) {
		this.sandbox = sandbox;
		this.editor = new JEditorPane();
		this.menu = new Menu();
		
		setTitle("Code Editor");
		setSize(400, 400);
		setContentPane(new JScrollPane(editor));
		setJMenuBar(menu);
		
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

		for (File file : droppedFiles) {
			openFile(file);
			return;
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
			
			if (text != null) {
				editor.setText(text);
			}
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
		try {
			Compiler compiler = new Compiler();
			ram = compiler.compile(editor.getText());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
			return false;
		}
		
		return true;
	}

	public void flash() {
		synchronized (sandbox.getWorld()) {
			sandbox.getRobot().flash(ram);
		}
	}
	
	public void compileAndFlash() {
		if (compile()) {
			flash();
		}
	}
	
	public void checkSyntax() {
		
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
	
	public void saveROM() {
		
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
			menu.add(item("Save ROM", e -> saveROM(), null));
			menu.addSeparator();
			menu.add(item("Close Editor", (e) -> { closeEditor(); },
					KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK)));
			add(menu);
		}
		
		protected void addCompileMenu() {
			JMenu menu = new JMenu("Tools");
			menu.add(item("Compile & Flash", e -> compileAndFlash(),
					KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK)));
			menu.add(item("Compile", e -> compile(), null));
			menu.add(item("Flash", e -> flash(), null));
			menu.addSeparator();
			menu.add(item("Check Syntax", e -> checkSyntax(), null));
			add(menu);
		}
	}
}
