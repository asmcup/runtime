package asmcup.runtime;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemTest {
	@Test
	public void testWithinDistance() {
		Item item = new Item.Gold(42);
		item.position(42f, 42f);
		final int distance = 20;
		for (int i = 42 - distance; i < 42 + distance; ++i) {
			assertTrue("Failed: (" + i + ",42)", item.withinDistance(i, 42));
			assertTrue("Failed: (42, " + i + ")", item.withinDistance(42, i));
		}
	}

	@Test
	public void testCollectGold() {
		Robot robot = new Robot(42);
		Item.Gold gold = new Item.Gold(10);
		assertEquals(0, robot.getGold());
		gold.collect(robot);
		assertEquals(10, robot.getGold());
	}

	@Test
	public void testCollectBattery() {
		Robot robot = new Robot(42);
		Item.Battery battery = new Item.Battery(10);
		robot.battery = 0;
		assertEquals(0, robot.getBattery());
		battery.collect(robot);
		assertEquals(1000, robot.getBattery());
	}
}
