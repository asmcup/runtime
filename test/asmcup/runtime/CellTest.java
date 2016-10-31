package asmcup.runtime;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CellTest {
	@Test
	public void testIsSolid() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>() {{
			put(Cell.TILE_GROUND, false);
			put(Cell.TILE_HAZARD, false);
			put(Cell.TILE_WALL, true);
			put(Cell.TILE_OBSTACLE, true);
			put(Cell.TILE_FLOOR, false);
		}};
		Cell cell = new Cell(new World(), 0, 0);
		for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
			cell.setTile(0, 0, entry.getKey());
			assertEquals("Failed key:" + entry.getKey(), entry.getValue(), cell.isSolid(0, 0));
		}
	}

	@Test
	public void testIsSpawnable() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>() {{
			put(Cell.TILE_GROUND, true);
			put(Cell.TILE_HAZARD, false);
			put(Cell.TILE_WALL, false);
			put(Cell.TILE_OBSTACLE, false);
			put(Cell.TILE_FLOOR, true);
		}};
		Cell cell = new Cell(new World(), 0, 0);
		for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
			cell.setTile(0, 0, entry.getKey());
			assertEquals("Failed entry: " + entry.getKey(), entry.getValue(), cell.isSpawnable(0, 0));
		}
	}
}
