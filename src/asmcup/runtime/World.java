package asmcup.runtime;

import java.io.*;
import java.util.*;

public class World {
	protected final ArrayList<Robot> robots;
	protected final HashMap<Integer, Cell> cells;
	protected int seed;
	protected int frame;
	
	public World() {
		this(new Random().nextInt());
	}
	
	public World(int seed) {
		this.robots = new ArrayList<>();
		this.cells = new HashMap<>();
		this.seed = seed;
		this.frame = 0;
	}
	
	public World(DataInputStream stream) throws IOException {
		this.robots = new ArrayList<>();
		this.cells = new HashMap<>();
		this.seed = stream.readInt();
		this.frame = stream.readInt();
		
		int count = stream.readInt();
		
		for (int i=0; i < count; i++) {
			robots.add(new Robot(stream));
		}
	}
	
	public void save(DataOutputStream stream) throws IOException {
		stream.writeInt(seed);
		stream.writeInt(frame);
		stream.writeInt(robots.size());
		
		for (Robot robot : robots) {
			robot.save(stream);
		}
	}
	
	public Iterable<Robot> getRobots() {
		return robots;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public Random getCellRandom(int cellX, int cellY) {
		return new Random(seed ^ Cell.key(cellX, cellY));
	}
	
	public void addRobot(Robot robot) {
		robots.add(robot);
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
		return getCell(x / CELL_SIZE, y / CELL_SIZE);
	}
	
	public Cell getCellXY(float x, float y) {
		return getCellXY((int)x, (int)y);
	}
	
	public int getTile(int column, int row) {
		int cellX = column / TILES_PER_CELL;
		int cellY = row / TILES_PER_CELL;
		Cell cell = getCell(cellX, cellY);
		return cell.getTile(column - cellX * TILES_PER_CELL,
		                    row - cellY * TILES_PER_CELL);
	}
	
	public int getTileXY(float x, float y) {
		return getTile((int)(x / TILE_SIZE), (int)(y / TILE_SIZE));
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
		float cos = (float)Math.cos(theta);
		float sin = (float)Math.sin(theta);
		
		for (int i = 0; i < RAY_STEPS; i++) {
			float tx = x + (cos * i * RAY_INTERVAL);
			float ty = y + (sin * i * RAY_INTERVAL);
			
			if (isSolid(tx, ty)) {
				return i * RAY_INTERVAL;
			}
		}
		
		return RAY_STEPS * RAY_INTERVAL;
	}
	
	public void tick() {
		for (Robot robot : robots) {
			robot.tick(this);
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
	
	public static final int RAY_INTERVAL = 16;
	public static final int RAY_STEPS = 16;
	
	public static final int TILE_SIZE = 32;
	public static final int TILES_PER_CELL = 16;
	public static final int CELL_SIZE = TILES_PER_CELL * TILE_SIZE;
	public static final int CELL_COUNT = 0xFFFF;
	public static final int SIZE = TILE_SIZE * TILES_PER_CELL * CELL_COUNT;
}
