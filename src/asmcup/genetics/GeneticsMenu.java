package asmcup.genetics;

import java.awt.event.*;

import javax.swing.*;

public class GeneticsMenu extends JMenu {
	public final Genetics genetics;
	
	public GeneticsMenu(Genetics genetics) {
		super("Genetics");
		this.genetics = genetics;
		
		add("Flash Best", e -> genetics.flash());
		add("Pin Best", e -> genetics.ga.pin());
		add("Pin ROM", e -> genetics.ga.pin(genetics.sandbox.getROM()));
		addSeparator();
		add("Start Training", e -> genetics.start());
		add("Stop Training", e-> genetics.stop());
		addSeparator();
		add("Modify Parameters", e -> genetics.setVisible(true));
		addSeparator();
		add("Clear Pinned", e -> genetics.ga.clearPinned());
		add("Clear Spawns", e -> genetics.evaluator.clearSpawns());
	}
	
	protected void add(String label, ActionListener listener) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(listener);
		add(item);
	}
}
