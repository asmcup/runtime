package asmcup.runtime;

// This class houses constants related to tiles as well as tile classification
// functionality.
// Please be aware that actual tiles in the code are represented as int.
// This class cannot (and should not) be instantiated.
public enum TILE {; // Courtesy of http://stackoverflow.com/a/9618724

	public static final int GROUND = 0;
	public static final int HAZARD = 1;
	public static final int WALL = 2;
	public static final int OBSTACLE = 3;
	public static final int FLOOR = 4;

	public static final int TYPE_BITS = 0b111; 
	public static final int VARIATION_BITS = 0b11000;
	
	public interface TileProperty {
		public boolean presentIn(int tile); 
	}

	public static final TileProperty IS_GROUND   = isType(GROUND);
	public static final TileProperty IS_HAZARD   = isType(HAZARD);
	public static final TileProperty IS_WALL     = isType(WALL);
	public static final TileProperty IS_OBSTACLE = isType(OBSTACLE);
	public static final TileProperty IS_FLOOR    = isType(FLOOR);
	
	public static TileProperty isType(int type) {
		return (int tile) -> (tile & TYPE_BITS) == type;
	}

	public static final TileProperty IS_SOLID       = isSolid();
	public static final TileProperty IS_SPAWNABLE   = isSpawnable();
	public static final TileProperty IS_UNSPAWNABLE = not(IS_SPAWNABLE);

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
