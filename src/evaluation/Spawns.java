package evaluation;

import java.util.ArrayList;

import asmcup.genetics.Spawn;
import asmcup.runtime.Robot;
import asmcup.runtime.World;
import asmcup.sandbox.Mouse;
import asmcup.sandbox.Sandbox;

public class Spawns extends ArrayList<Spawn> {
	protected final Sandbox sandbox;
	
	public Spawns(Sandbox sandbox) {
		this.sandbox = sandbox;
	}
	
	public int getCombinedSeed() {
		int seed = 0;
		
		for (Spawn spawn : this) {
			seed += spawn.seed;
		}
		return seed;
	}
	
	public void addSpawnAtMouse() {
		Mouse mouse = sandbox.mouse;
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(mouse.getWorldX(), mouse.getWorldY(), robot.getFacing(), world.getSeed());
		add(spawn);
		sandbox.redraw();
	}
	
	public void addSpawnAtRobot() {
		Robot robot = sandbox.getRobot();
		World world = sandbox.getWorld();
		Spawn spawn = new Spawn(robot.getX(), robot.getY(), robot.getFacing(), world.getSeed());
		add(spawn);
		sandbox.redraw();
	}
}