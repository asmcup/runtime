package asmcup.runtime;

import java.io.*;
import java.util.*;

public class World {
	protected final HashMap<Integer, Cell> cells;
	protected int seed;
	protected int frame;
	protected Random random;
	
	public World() {
		this(new Random().nextInt());
	}
	
	public World(int seed) {
		this.cells = new HashMap<>();
		this.random = new Random(seed);
		this.seed = seed;
		this.frame = 0;
	}
	
	public World(DataInputStream stream) throws IOException {
		this.cells = new HashMap<>();
		this.seed = stream.readInt();
		this.random = new Random(seed);
		this.frame = stream.readInt();
	}
	
	public int getSeed() {
		return seed;
	}
	
	public Random getCellRandom(int cellX, int cellY) {
		return new Random(seed ^ Cell.key(cellX, cellY));
	}
	
	public void addRobot(Robot robot) {
		getCellXY(robot.getX(), robot.getY()).addRobot(robot);
	}
	
	public void position(Robot robot, float x, float y) {
		Cell oldCell = getCellXY(robot.getX(), robot.getY());
		Cell cell = getCellXY(x, y);
		
		if (oldCell != cell) {
			oldCell.removeRobot(robot);
			cell.addRobot(robot);
		}
		
		robot.position(x, y);
	}
	
	public Cell getCell(int cellX, int cellY) {
		int key = Cell.key(cellX, cellY);
		Cell cell = cells.get(key);
		
		if (cell == null) {
			cell = new Cell(this, cellX, cellY);
			cells.put(key, cell);
		}
		
		return cell;
	}
	
	public Cell getCellXY(int x, int y) {
		return getCell(x / (16 * 32), y / (16 * 32));
	}
	
	public Cell getCellXY(float x, float y) {
		return getCellXY((int)x, (int)y);
	}
	
	public int getTile(int column, int row) {
		int cellX = column / 16;
		int cellY = row / 16;
		Cell cell = getCell(cellX, cellY);
		return cell.getTile(column - cellX * 16, row - cellY * 16);
	}
	
	public int getTileXY(float x, float y) {
		return getTile((int)(x / 32), (int)(y / 32));
	}
	
	public boolean isSolid(float x, float y) {
		if (x <= 0 || y <= 0) {
			return true;
		}
		
		return (getTileXY(x, y) & 0b11) >= 2;
	}
	
	public boolean isSolid(float x, float y, float r) {
		return isSolid(x - r, y - r) || isSolid(x + r, y + r) || isSolid(x - r, y + r) || isSolid(x + r, y - r);
	}
	
	public float ray(float x, float y, float theta) {
		float distance = 0.0f;
		
		for (int i=0; i < 10; i++) {
			if (isSolid(x, y)) {
				return distance;
			}
			
			x += Math.cos(theta) * 16;
			y += Math.sin(theta) * 16;
		}
		
		return Float.POSITIVE_INFINITY;
	}
	
	public void save(DataOutputStream stream) throws IOException {
		
	}
	
	public void tick() { 
		for (Cell cell : cells.values()) {
			cell.tick(this);
		}
		
		frame++;
	}
	
	public void update(DataInputStream stream) throws IOException {
		stream.mark(4);
		int n = stream.readInt();
		
		if (n != frame) {
			stream.reset();
			return;
		}
		
		int count = stream.readInt();
		
		for (int i=0; i < count; i++) {
			addRobot(new Robot(stream));
		}
	}

	public void mark(Robot robot, int offset, int value) {
		
	}

	public int markRead(Robot robot, int offset) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static final int SIZE_TILE = 32;
	public static final int SIZE_CELL = 16;
	public static final int SIZE = SIZE_TILE * SIZE_CELL * 0xFFFF;
}
