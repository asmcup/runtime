package asmcup.evaluation;

import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.genetics.Spawn;
import asmcup.sandbox.*;

public class SpawnsWindow extends JFrame {
	protected final Sandbox sandbox;
	protected final Spawns spawns;
	protected final FrontPanel panel = new FrontPanel();
	protected JList<Spawn> spawnList;

	protected JButton addButton = new JButton("Add current");
	protected JButton deleteButton = new JButton("Delete");
	protected JButton applyButton = new JButton("Show");
	protected JButton clearButton = new JButton("Clear");

	public SpawnsWindow(Sandbox sandbox) throws IOException {
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
		setIconImage(ImageIO.read(getClass().getResource("/plus.png")));
		setResizable(false);
		pack();
	}

	public void addCurrent() {
		spawns.addSpawnAtRobot();
		spawnList.setSelectedIndex(spawns.size() - 1);
	}
	
	public void deleteOne() {
		selectLastIfNone();
		int index = spawnList.getSelectedIndex();
		// Error handling happens in there. Can't trust JList apparently.
		spawns.remove(index);
		selectLastIfNone();
	}
	
	public void applyOne() {
		selectLastIfNone();
		Spawn spawn = spawnList.getSelectedValue();
		if (spawn != null) {
			sandbox.loadSpawn(spawn);
		}
	}
	
	public void selectLastIfNone() {
		if (spawnList.getSelectedValue() == null) {
			spawnList.setSelectedIndex(spawns.size() - 1);
		}
	}
	
	public void clear() {
		spawns.clear();
	}
}
