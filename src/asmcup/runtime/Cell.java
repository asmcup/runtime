package asmcup.runtime;

import java.util.*;

public class Cell {
	protected final World world;
	protected final int cellX, cellY;
	protected final int[] tiles = new int[World.TILES_PER_CELL * World.TILES_PER_CELL];
	protected final ArrayList<Item> items = new ArrayList<>();
	
	public Cell(World world, int cellX, int cellY) {
		this.world = world;
		this.cellX = cellX;
		this.cellY = cellY;
		
		Random random = world.getCellRandom(cellX, cellY);
		
		for (int i = 0; i < World.TILES_PER_CELL * World.TILES_PER_CELL; i++) {
			int p = random.nextInt(100);
			
			if (p < 80) {
				tiles[i] = TILE_GROUND;
			} else {
				tiles[i] = TILE_GROUND | ((1 + random.nextInt(2)) << 2);
			}
		}
		
		if (random.nextInt(10) < 2) {
			generateRoom(random);
		} else {
			generateOpenArea(random);
		}
	}
	
	public int getX() {
		return cellX;
	}
	
	public int getY() {
		return cellY;
	}
	
	public int getKey() {
		return key(cellX, cellY);
	}
	
	public static int key(int cellX, int cellY) {
		return clampCell(cellX) | (clampCell(cellY) << 16);
	}
	
	protected static int clampCell(int i) {
		return Math.max(0, Math.min(World.CELL_COUNT, i));
	}
	
	public Iterable<Item> getItems() {
		return items;
	}
	
	protected void generateRoom(Random random) {
		int wpad = 1 + random.nextInt(5);
		int hpad = 1 + random.nextInt(5);
		int width = World.TILES_PER_CELL - 1 - wpad * 2;
		int height = World.TILES_PER_CELL - 1 - hpad * 2;
		
		if (width < 3 || height < 3)
			return;
		
		for (int i = 0; i <= width; i++) {
			setTile(wpad + i, hpad, TILE_WALL | (random.nextInt(4) << 2));
			setTile(wpad + i, World.TILES_PER_CELL - 1 - hpad,
					TILE_WALL | (random.nextInt(4) << 2));
		}
		
		for (int i = 0; i < height; i++) {
			setTile(wpad, hpad + i, TILE_WALL | (random.nextInt(4) << 2));
			setTile(World.TILES_PER_CELL - 1 - wpad, hpad + i,
					TILE_WALL | (random.nextInt(4) << 2));
		}
		
		int exits = 1 + random.nextInt(3);
		
		for (int i=0; i < exits; i++) {
			int variant = TILE_GROUND | (random.nextInt(4) << 2);
			
			switch (random.nextInt(4)) {
			case 0:
				setTile(wpad + 1 + random.nextInt(width - 2), hpad, variant);
				break;
			case 1:
				setTile(wpad, hpad + 1 + random.nextInt(height - 2), variant);
				break;
			case 2:
				setTile(wpad + 1 + random.nextInt(width - 2),
						World.TILES_PER_CELL - 1 - hpad, variant);
				break;
			case 3:
				setTile(World.TILES_PER_CELL - 1 - wpad,
						hpad + 1 + random.nextInt(height - 2), variant);
				break;
			}
		}
		
		int count = random.nextInt(10);
		int goldLimit = random.nextInt(1000 * 10) - random.nextInt(5000);
		
		for (int i = 0; i < count; i++) {
			Item item;
			
			switch (random.nextInt(2)) {
			case 0:
				goldLimit = Math.max(goldLimit, 0);
				Item.Gold gold = new Item.Gold(random, goldLimit);
				goldLimit -= gold.getValue();
				item = gold;
				break;
			default:
				Item.Battery battery = new Item.Battery();
				item = battery;
				break;
			}
			
			float x = (cellX + random.nextFloat()) * World.CELL_SIZE;
			float y = (cellY + random.nextFloat()) * World.CELL_SIZE;
			
			x = Math.max(x, (cellX * World.TILES_PER_CELL + 1 + wpad) *     World.TILE_SIZE);
			x = Math.min(x, ((cellX+1) * World.TILES_PER_CELL - wpad - 2) * World.TILE_SIZE);
			y = Math.max(y, (cellY * World.TILES_PER_CELL + 1 + hpad) *     World.TILE_SIZE);
			y = Math.min(y, ((cellY+1) * World.TILES_PER_CELL - hpad - 2) * World.TILE_SIZE);
			
			item.position(x, y);
			items.add(item);
		}
	}
	
	protected void generateOpenArea(Random random) {
		int count = random.nextInt(15);
		
		for (int i = 0; i < count; i++) {
			int col = random.nextInt(World.TILES_PER_CELL);
			int row = random.nextInt(World.TILES_PER_CELL);
			int p = random.nextInt(100);
			
			if (p < 10) {
				generateHazards(random, col, row);
			} else if (p < 33) {
				generateRubble(random, col, row);
			} else {
				generateObstacle(random, col, row);
			}
		}
	}
	
	protected void generateObstacle(Random random, int col, int row) {
		setTile(col, row, TILE_OBSTACLE | (random.nextInt(4) << 2));
	}
	
	protected void generateRubble(Random random, int col, int row) {
		int count = 1 + random.nextInt(10);
		
		for (int i=0; i < count; i++) {
			setTile(col, row, TILE_WALL | (random.nextInt(4) << 2));
			
			if (random.nextBoolean()) {
				col = wiggle(random, col);
			} else {
				row = wiggle(random, row);
			}
		}
	}
	
	protected void generateHazards(Random random, int col, int row) {
		int count = 3 + random.nextInt(10);
		int variant = random.nextInt(4);
		
		switch (variant) {
		case 0:
			count = 3 + random.nextInt(10);
			break;
		case 1:
			count = 2 + random.nextInt(5);
			break;
		case 2:
			count = 1 + random.nextInt(3);
			break;
		case 3:
			count = 1;
			break;
		}
		
		for (int i=0; i < count; i++) {
			setTile(col, row, TILE_HAZARD | (variant << 2));
			col = wiggle(random, col);
			row = wiggle(random, row);
		}
	}
	
	protected int wiggle(Random random, int x) {
		x += random.nextInt(3) - 1;
		x = Math.min(x, World.TILES_PER_CELL - 1);
		x = Math.max(x, 0);
		return x;
	}
	
	public int getTile(int col, int row) {
		return tiles[clampTile(col) + (clampTile(row) * World.TILES_PER_CELL)];
	}
	
	public static int clampTile(int i) {
		return Math.max(0, Math.min(World.TILES_PER_CELL - 1, i));
	}
	
	public void setTile(int col, int row, int value) {
		tiles[col + (row * World.TILES_PER_CELL)] = value;
	}
	
	public static final int TILE_GROUND = 0;
	public static final int TILE_HAZARD = 1;
	public static final int TILE_WALL = 2;
	public static final int TILE_OBSTACLE = 3;
}
