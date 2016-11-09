package asmcup.evaluation;

import java.awt.Dimension;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import asmcup.genetics.Spawn;
import asmcup.sandbox.FrontPanel;
import asmcup.sandbox.Sandbox;

public class SpawnsWindow extends JFrame {
	protected final Sandbox sandbox;
	protected final Spawns spawns;
	protected final FrontPanel panel = new FrontPanel();
	protected JList<Spawn> spawnList;
	protected ListModel<Spawn> listModel;

	protected JButton addButton = new JButton("Add current");
	protected JButton deleteButton = new JButton("Delete");
	protected JButton applyButton = new JButton("Show");
	protected JButton clearButton = new JButton("Clear");

	public SpawnsWindow(Sandbox sandbox) {
		this.sandbox = sandbox;
		spawns = sandbox.spawns;
		//listModel = new SpawnListModel();
		spawnList = new JList<Spawn>(spawns);
		spawnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spawnList.setLayoutOrientation(JList.VERTICAL);
		spawnList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(spawnList);
		listScroller.setPreferredSize(new Dimension(420, 240));

		addButton.addActionListener(e -> addCurrent());
		deleteButton.addActionListener(e -> deleteOne());
		applyButton.addActionListener(e -> applyOne());
		clearButton.addActionListener(e -> clear());
		
		panel.addWideItem(listScroller);
		panel.addItems(addButton, deleteButton);
		panel.addItems(applyButton, clearButton);
		
		setContentPane(panel);
		setTitle("Spawn Manager");
		setResizable(false);
		pack();
	}

	public void addCurrent() {
		spawns.addSpawnAtRobot();
		spawnList.setSelectedIndex(spawns.size() - 1);
	}
	
	public void deleteOne() {
		int index = spawnList.getSelectedIndex();
		if (index != -1) {
			spawns.remove(index);
		}
	}
	
	public void applyOne() {
		int index = spawnList.getSelectedIndex();
		if (index != -1) {
			sandbox.loadSpawn(spawns.get(index));
		}
	}
	
	public void clear() {
		spawns.clear();
	}
}
