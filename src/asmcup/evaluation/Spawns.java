package asmcup.evaluation;

import java.util.*;

import javax.swing.ListModel;
import javax.swing.event.*;

import asmcup.genetics.Spawn;
import asmcup.runtime.*;
import asmcup.sandbox.*;

public class Spawns implements ListModel<Spawn> {
	private ArrayList<Spawn> spawns = new ArrayList<>();
	protected final Sandbox sandbox;
	
	ArrayList<ListDataListener> listeners = new ArrayList<>();
	
	public Spawns(Sandbox sandbox) {
		this.sandbox = sandbox;
	}
	
	public AbstractCollection<Spawn> getIterable() {
		return new ArrayList<Spawn>(spawns);
	}
	
	public void addSpawnAtMouse() {
		Mouse mouse = sandbox.mouse;
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(mouse.getWorldX(), mouse.getWorldY(), robot.getFacing(), world.getSeed());
		add(spawn);
    }
	public void addSpawnAtRobot() {
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(robot.getX(), robot.getY(), robot.getFacing(), world.getSeed());
		add(spawn);
	}

	public boolean add(Spawn spawn) {
		spawns.add(spawn);
		notifyListeners(spawns.size()-1, spawns.size()-1);
		return true;
	}
	
	public Spawn remove(int index) {
		if (index < 0 || index >= spawns.size()) {
			return null;
		}
		Spawn ret = spawns.remove(index);
		notifyListeners(index, size());
		return ret;
	}
	
	public void clear() {
		int previousSize = spawns.size();
		spawns.clear();
		notifyListeners(0, previousSize-1);
	}
	
	public int getCombinedSeed() {
		int seed = 0;
		
		for (Spawn spawn : spawns) {
			seed += spawn.seed;
		}
		return seed;
	}

	public int size() {
		return spawns.size();
	}

	public int getSize() {
		return spawns.size();
	}
	
	public Spawn getElementAt(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return spawns.get(index);
	}

	private void notifyListeners(int index0, int index1) {
		for (ListDataListener l : listeners) {
			// Yeah, lazy, I know.
			l.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, index0, index1));
		}
		sandbox.redraw();
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
		
	}
}