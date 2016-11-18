package asmcup.runtime;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TileTest {
	@Test
	public void testIsSolid() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>() {{
			put(TILE.GROUND, false);
			put(TILE.HAZARD, false);
			put(TILE.WALL, true);
			put(TILE.OBSTACLE, true);
			put(TILE.FLOOR, false);
		}};
		World world = new World();
		for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
			world.setTileXY(0, 0, entry.getKey());
			assertEquals("Failed key:" + entry.getKey(), entry.getValue(), world.isSolid(0, 0));
			assertEquals("Failed key:" + entry.getKey(), entry.getValue(),
					!world.canRobotGoTo(World.TILE_HALF, World.TILE_HALF));
		}
	}

	@Test
	public void testIsSpawnable() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>() {{
			put(TILE.GROUND, true);
			put(TILE.HAZARD, false);
			put(TILE.WALL, false);
			put(TILE.OBSTACLE, false);
			put(TILE.FLOOR, true);
		}};
		World world = new World();
		for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
			world.setTileXY(0, 0, entry.getKey());
			assertEquals("Failed entry: " + entry.getKey(), entry.getValue(),
					world.canSpawnRobotAt(World.TILE_HALF, World.TILE_HALF));
		}
	}
}
