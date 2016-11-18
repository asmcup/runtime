package asmcup.runtime;

import java.util.*;

public class World {
	protected final ArrayList<Robot> robots;
	protected final HashMap<Integer, Cell> cells;
	protected final HashMap<Integer, byte[]> tileData;
	protected final int seed;
	protected int frame;

	private static final Random random = new Random();
	
	public World() {
		this(random.nextInt());
	}
	
	public World(int seed) {
		this.robots = new ArrayList<>();
		this.cells = new HashMap<>();
		this.tileData = new HashMap<>();
		this.seed = seed;
		this.frame = 0;
	}
	
	public Iterable<Robot> getRobots() {
		return robots;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public int getFrame() {
		return frame;
	}
	
	public void addRobot(Robot robot) {
		robots.add(robot);
	}
	
	public void removeRobot(Robot robot) {
		robots.remove(robot);
	}
	
	public Cell getCell(int cellCol, int cellRow) {
		int key = Cell.key(cellCol, cellRow);
		Cell cell = cells.get(key);
		
		if (cell == null) {
			cell = new Cell(this, cellCol, cellRow);
			cells.put(key, cell);
			cell.generate();
		}
		
		return cell;
	}
	
	public Cell getCellXY(float x, float y) {
		return getCell((int)(x / CELL_SIZE), (int)(y / CELL_SIZE));
	}
	
	public int getTile(int tileCol, int tileRow) {
		int cellCol = tileCol / TILES_PER_CELL;
		int cellRow = tileRow / TILES_PER_CELL;
		Cell cell = getCell(cellCol, cellRow);
		return cell.getTile(tileCol - cellCol * TILES_PER_CELL,
		                    tileRow - cellRow * TILES_PER_CELL);
	}
	
	public int getTileXY(float x, float y) {
		if (x < 0 || y < 0 || x > SIZE || y > SIZE) {
			return TILE.WALL;
		}
		return getTile((int)(x / TILE_SIZE), (int)(y / TILE_SIZE));
	}
	
	public boolean checkTile(TILE.TileProperty prop, float x, float y) {
		int tile = getTileXY(x, y);
		return prop.presentIn(tile);
	}
	
	public boolean isSolid(float x, float y) {
		return checkTile(TILE.IS_SOLID, x, y);
	}
	
	public boolean isHazard(float x, float y) {
		return checkTile(TILE.IS_HAZARD, x, y);
	}
	
	public boolean isObstacle(float x, float y) {
		return checkTile(TILE.IS_OBSTACLE, x, y);
	}
	
	public boolean checkTileNear(TILE.TileProperty prop, float x, float y, float r) {
		return checkTile(prop, x, y)
				|| checkTile(prop, x - r, y - r) || checkTile(prop, x + r, y + r)
				|| checkTile(prop, x - r, y + r) || checkTile(prop, x + r, y - r);
	}

	public boolean isSolidNear(float x, float y, float r) {
		return checkTileNear(TILE.IS_SOLID, x, y, r);
	}
	
	public boolean isUnspawnableNear(float x, float y, float r) {
		return checkTileNear(TILE.IS_UNSPAWNABLE, x, y, r);
	}
	
	public boolean canRobotGoTo(float x, float y) {
		return !isSolidNear(x, y, Robot.COLLIDE_RANGE);
	}

	public boolean canSpawnRobotAt(float x, float y) {
		return !isUnspawnableNear(x, y, Robot.COLLIDE_RANGE);
	}
	
	public int getHazard(float x, float y) {
		int tile = getTileXY(x, y);
		
		if ((tile & 0b111) != TILE.HAZARD) {
			return -1;
		}
		
		return tile >> 3;
	}
	
	public void setTileXY(float x, float y, int value) {
		Cell cell = getCellXY(x, y);
		int col = (int)(x / TILE_SIZE - cell.getX() * TILES_PER_CELL);
		int row = (int)(y / TILE_SIZE - cell.getY() * TILES_PER_CELL);
		
		col = StrictMath.max(col, 0);
		row = StrictMath.max(row, 0);
		col = StrictMath.min(col, TILES_PER_CELL * CELL_COUNT - 1);
		row = StrictMath.min(row, TILES_PER_CELL * CELL_COUNT - 1);
		
		cell.setTile(col, row, value);
	}
	
	public void randomizePosition(Robot robot)
	{
		int x, y;
		do {
			x = (int)(StrictMath.random() * SIZE);
			y = (int)(StrictMath.random() * SIZE);
		} while (!canSpawnRobotAt(x, y));
		robot.position(x, y);
	}
	
	public void tick() {
		for (Robot robot : robots) {
			robot.tick(this);
			tickItems(robot);
			tickHazards(robot);
		}
		
		frame++;
	}
	
	protected void tickHazards(Robot robot) {
		switch (getHazard(robot.getX(), robot.getY())) {
		case 0:
			robot.damage(Robot.BATTERY_MAX / 100);
			break;
		case 1:
			robot.damage(Robot.BATTERY_MAX / 75);
			break;
		case 2:
			robot.damage(Robot.BATTERY_MAX / 50);
			break;
		case 3:
			robot.kill();
			break;
		}
	}

	public Item getItem(float x, float y) {
		return getCellXY(x, y).getItem(x, y);
	}
	
	public void addItem(float x, float y, Item item) {
		item.position(x, y);
		getCellXY(x, y).addItem(item);
	}
	
	protected void tickItems(Robot robot) {
		Cell cell = getCellXY(robot.getX(), robot.getY());
		Item item = cell.getItem(robot.getX(), robot.getY());
		
		if (item == null) {
			return;
		}
		
		item.collect(robot);
		cell.removeItem(item);
	}
	
	public Robot getRobot(float x, float y) {
		for (Robot robot : robots) {
			if (Math.abs(robot.getX() - x) < Robot.COLLIDE_RANGE
			 && Math.abs(robot.getY() - y) < Robot.COLLIDE_RANGE) {
				return robot;
			}
		}
		return null;
	}

	public void mark(Robot robot, int offset, int value) {
		int key = robot.getColumn() | (robot.getRow() << 16);
		byte[] data = tileData.get(key);
		
		if (data == null) {
			data = new byte[8];
			tileData.put(key, data);
		}
		
		data[offset & 0b11] = (byte)(value & 0xFF);
	}

	public int markRead(Robot robot, int offset) {
		int key = robot.getColumn() | (robot.getRow() << 16);
		byte[] data = tileData.get(key);
		return (data == null) ? 0 : data[offset & 0b11];
	}
	
	public void send(Robot robot, float frequency, int data) {
		// TODO send data on radio
	}
	
	public int recv(Robot robot, float frequency) {
		// TODO read data on radio
		return 0;
	}
	
	public static final int TILE_SIZE = 32;
	public static final int TILE_HALF = TILE_SIZE / 2;
	public static final int TILES_PER_CELL = 20;
	public static final int CELL_SIZE = TILES_PER_CELL * TILE_SIZE;
	public static final int CELL_COUNT = 0xFF;
	public static final int SIZE = TILE_SIZE * TILES_PER_CELL * (CELL_COUNT + 1);
	public static final int CENTER = SIZE / 2;
	
	
	public static class TILE {

		public static final int GROUND = 0;
		public static final int HAZARD = 1;
		public static final int WALL = 2;
		public static final int OBSTACLE = 3;
		public static final int FLOOR = 4;
	
		public static final int TYPE_BITS = 0b111; 
		public static final int VARIATION_BITS = 0b11000;
		
		private interface TileProperty {
			public boolean presentIn(int tile); 
		}

		public static TileProperty IS_GROUND   = isType(GROUND);
		public static TileProperty IS_HAZARD   = isType(HAZARD);
		public static TileProperty IS_WALL     = isType(WALL);
		public static TileProperty IS_OBSTACLE = isType(OBSTACLE);
		public static TileProperty IS_FLOOR    = isType(FLOOR);
		
		public static TileProperty isType(int type) {
			return (int tile) -> (tile & TYPE_BITS) == type;
		}

		public static TileProperty IS_SOLID       = isSolid();
		public static TileProperty IS_SPAWNABLE   = isSpawnable();
		public static TileProperty IS_UNSPAWNABLE = not(IS_SPAWNABLE);

		public static TileProperty isSolid() {
			return (int tile) -> {
				switch (tile & TYPE_BITS) {
				case WALL:
				case OBSTACLE:
					return true;
				}
				return false;
			};
		}
		
		public static TileProperty isSpawnable() {
			return (int tile) -> {
				switch (tile & TYPE_BITS) {
				case HAZARD:
				case WALL:
				case OBSTACLE:
					return false;
				}
				return true;
			};
		}
		
		public static TileProperty not(TileProperty prop) {
			return (int tile) -> !prop.presentIn(tile);
		}
	}
}
