package asmcup.sandbox;

import javax.swing.JFrame;

public class Frame extends JFrame {
	protected final Sandbox sandbox;
	
	public Frame(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Sandbox");
		setResizable(false);
		setJMenuBar(sandbox.getMenu());
		setContentPane(sandbox.getCanvas());
		pack();
	}
}
