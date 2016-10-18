package asmcup.runtime;

import java.util.Random;

public class Generator {
	protected final World world;
	protected final Cell cell;
	protected final Random random;
	protected int wpad, hpad;
	protected int width, height;
	
	public Generator(World world, Cell cell) {
		this.world = world;
		this.cell = cell;
		this.random = new Random(world.getSeed() ^ cell.getKey());
	}
	
	public int nextInt(int bound) {
		return random.nextInt(bound);
	}
	
	public float nextFloat() {
		return random.nextFloat();
	}
	
	public int nextRare() {
		return chance(66) ? 0 : (1 + nextInt(3));
	}
	
	public boolean chance(int p) {
		return nextInt(100) <= p;
	}
	
	public TileFunc same(int type) {
		int variant = nextInt(4) << 3;
		return (col, row) -> { return type | variant; };
	}
	
	public TileFunc variant(int type) {
		return (col, row) -> { return type | (nextInt(4) << 3); };
	}
	
	public TileFunc variantRare(int type) {
		return (col, row) -> { return type | (nextRare() << 3); };
	}
	
	public void set(TileFunc f, int col, int row) {
		cell.setTile(col, row, f.tile(col, row));
	}
	
	public void hline(TileFunc f, int col, int row, int w) {
		for (int i=0; i < w; i++) {
			set(f, col + i, row);
		}
	}
	
	public void vline(TileFunc f, int col, int row, int h) {
		for (int i=0; i < h; i++) {
			set(f, col, row + i);
		}
	}
	
	public void rect(TileFunc f, int col, int row, int w, int h) {
		for (int r=0; r < h; r++) {
			for (int c=0; c < w; c++) {
				set(f, col + c, row + r);
			}
		}
	}
	
	public void square(TileFunc f, int col, int row, int s) {
		rect(f, col, row, s, s);
	}
	
	public void outline(TileFunc f, int col, int row, int w, int h) {
		hline(f, col, row, w);
		hline(f, col, row + h - 1, w);
		vline(f, col, row, h);
		vline(f, col + w - 1, row, h);
	}
	
	public void openArea() {
		int count = nextInt(15);
		
		for (int i = 0; i < count; i++) {
			int col = nextInt(World.TILES_PER_CELL);
			int row = nextInt(World.TILES_PER_CELL);
			int p = nextInt(100);
			
			if (p < 10) {
				hazards(col, row);
			} else if (p < 33) {
				rubble(col, row);
			} else {
				set(variant(Cell.TILE_OBSTACLE), col, row);
			}
		}
	}
	
	public void rubble(int col, int row) {
		int count = 1 + nextInt(10);
		
		for (int i=0; i < count; i++) {
			set(variant(Cell.TILE_WALL), col, row);
			
			if (chance(50)) {
				col = wiggle(col);
			} else {
				row = wiggle(row);
			}
		}
	}
	
	public void hazards(int col, int row) {
		int count = 3 + nextInt(10);
		int variant = nextInt(4);
		
		switch (variant) {
		case 0:
			count = 3 + nextInt(10);
			break;
		case 1:
			count = 2 + nextInt(5);
			break;
		case 2:
			count = 1 + nextInt(3);
			break;
		case 3:
			count = 1;
			break;
		}
		
		for (int i=0; i < count; i++) {
			set(variantRare(Cell.TILE_HAZARD), col, row);
			col = wiggle(col);
			row = wiggle(row);
		}
	}
	
	protected int wiggle(int x) {
		x += nextInt(3) - 1;
		x = Math.min(x, World.TILES_PER_CELL - 1);
		x = Math.max(x, 0);
		return x;
	}

	public void room() {
		wpad = nextInt(5);
		hpad = nextInt(5);
		width = World.TILES_PER_CELL - wpad * 2;
		height = World.TILES_PER_CELL - hpad * 2;
		
		if (width < 3 || height < 3) {
			return;
		}
		
		rect(same(Cell.TILE_FLOOR), wpad, hpad, width, height);
		outline(same(Cell.TILE_WALL), wpad, hpad, width, height);
		exits();
		items();
	}
	
	public void exits() {
		int count = 1 + nextInt(3);
		
		for (int i=0; i < count; i++) {
			int col, row;
			
			switch (nextInt(4)) {
			case 0:
				col = wpad + 1 + nextInt(width - 2);
				row = hpad;
				break;
			case 1:
				col = wpad;
				row = hpad + 1 + nextInt(height - 2);
				break;
			case 2:
				col = wpad + 1 + nextInt(width - 2);
				row = World.TILES_PER_CELL - 1 - hpad;
				break;
			default:
				col = World.TILES_PER_CELL - 1 - wpad;
				row = hpad + 1 + nextInt(height - 2);
				break;
			}
			
			set(variant(Cell.TILE_GROUND), col, row);
		}
	}
	
	public void items() {
		int count = nextInt(10);
		
		for (int i = 0; i < count; i++) {
			Item item;
			
			switch (nextInt(2)) {
			case 0:
				item = gold();
				break;
			default:
				item = battery();
				break;
			}
			
			spawnItem(item);
		}
	}
	
	public void spawnItem(Item item) {
		int left = cell.getX() * World.CELL_SIZE;
		int top = cell.getY() * World.CELL_SIZE;
		int w = width - 3;
		int h = height - 3;
		float x = left + (2 + wpad + nextFloat() * w) * World.TILE_SIZE;
		float y = top + (2 + hpad + nextFloat() * h) * World.TILE_SIZE;
		item.position(x, y);
		cell.addItem(item);
	}
	
	public Item.Gold gold() {
		return new Item.Gold(1 + nextInt(99));
	}
	
	public Item.Battery battery() {
		return new Item.Battery(1 + nextInt(99));
	}
	
	public static interface TileFunc {
		public int tile(int col, int row);
	}
}
