package evaluation;

import java.util.ArrayList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import asmcup.genetics.Spawn;
import asmcup.runtime.Robot;
import asmcup.runtime.World;
import asmcup.sandbox.Mouse;
import asmcup.sandbox.Sandbox;

public class Spawns extends ArrayList<Spawn> implements ListModel<Spawn> {
	protected final Sandbox sandbox;
	
	ArrayList<ListDataListener> listeners = new ArrayList<>();
	
	public Spawns(Sandbox sandbox) {
		this.sandbox = sandbox;
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

	@Override
	public boolean add(Spawn spawn) {
		super.add(spawn);
		notifyListeners(size()-1, size()-1);
		return true;
	}
	
	@Override
	public Spawn remove(int index) {
		Spawn ret = super.remove(index);
		notifyListeners(index, index);
		return ret;
	}
	
	@Override
	public void clear() {
		int previousSize = size();
		super.clear();
		notifyListeners(0, previousSize-1);
	}
	
	public int getCombinedSeed() {
		int seed = 0;
		
		for (Spawn spawn : this) {
			seed += spawn.seed;
		}
		return seed;
	}

	public int getSize() {
		return size();
	}
	
	public Spawn getElementAt(int index) {
		return get(index);
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