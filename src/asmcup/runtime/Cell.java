package asmcup.runtime;

import java.util.*;

import asmcup.runtime.World.TILE;

public class Cell {
	protected final World world;
	protected final int cellX, cellY;
	protected final int[] tiles = new int[World.TILES_PER_CELL * World.TILES_PER_CELL];
	protected final ArrayList<Item> items = new ArrayList<>();
	
	public Cell(World world, int cellX, int cellY) {
		this.world = world;
		this.cellX = cellX;
		this.cellY = cellY;
	}
	
	public void generate() {
		Generator gen = new Generator(world, this);
		
		if (cellX == 0 || cellY == 0 || cellX == World.CELL_COUNT || cellY == World.CELL_COUNT) {
			gen.square(gen.same(TILE.HAZARD, 3), 0, 0, World.TILES_PER_CELL);
			return;
		}
		
		gen.square(gen.variantRare(TILE.GROUND), 0, 0, World.TILES_PER_CELL);
		
		if (gen.chance(33)) {
			gen.room();
		} else {
			gen.openArea();
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
		return StrictMath.max(0, StrictMath.min(World.CELL_COUNT, i));
	}
	
	public void addItem(Item item) {
		if (item == null) {
			throw new NullPointerException();
		}
		
		items.add(item);
	}
	
	public Iterable<Item> getItems() {
		return items;
	}
	
	public Item getItem(float x, float y) {
		for (Item item : items) {
			if (item.withinDistance(x, y)) {
				return item;
			}
		}
		
		return null;
	}
	
	public void removeItem(Item item) {
		items.remove(item);
	}
	
	protected int getTile(int col, int row) {
		return tiles[clampTile(col) + (clampTile(row) * World.TILES_PER_CELL)];
	}
	
	private static int clampTile(int i) {
		return StrictMath.max(0, StrictMath.min(World.TILES_PER_CELL - 1, i));
	}
	
	public void setTile(int col, int row, int value) {
		if (col < 0 || row < 0) {
			throw new IllegalArgumentException("Tile coordinates cannot be negative");
		}
		
		if (col >= World.TILES_PER_CELL || row >= World.TILES_PER_CELL) {
			throw new IllegalArgumentException("Tile coordinates outside of bounds");
		}
		
		tiles[col + (row * World.TILES_PER_CELL)] = value;
	}
}
