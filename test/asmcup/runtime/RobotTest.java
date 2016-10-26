package asmcup.runtime;

import asmcup.vm.VM;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class RobotTest {
	Robot robot = null;

	@Before
	public void setUp() {
		robot = new Robot(42);
		robot.position(42, 21);
	}

	@Test
	public void testAccelerometer() {
		World world = generateEmptyWorld((int) robot.getX(), (int) robot.getY(), 2);
		// Read Accelerometer
		VM vm = robot.getVM();
		vm.push8(Robot.IO_ACCELEROMETER);
		vm.setIO(true);
		robot.handleIO(world);

		// We did not drive, so we should expect 0 values.
		float x = vm.popFloat();
		float y = vm.popFloat();
		assertEquals(0, x, 0.001f);
		assertEquals(0, y, 0.001f);
		assertEquals(x, y, 0.001f);

		// Drive a little bit
		robot.setMotor(1.0f);
		robot.setSteer(0.5f);
		robot.tickHardware(world);


		vm.push8(Robot.IO_ACCELEROMETER);
		vm.setIO(true);
		robot.handleIO(world);

		// We drove, so we should not expect 0 values.
		x = vm.popFloat();
		y = vm.popFloat();
		assertNotEquals(0f, x, 0.001f);
		assertNotEquals(0f, y, 0.001f);
		assertNotEquals(x, y, 0.001f);
	}

	public World generateEmptyWorld(int x, int y, int radius) {
		World world = new World();
		for (int i = x - radius; i < x + radius; ++i) {
			for (int j = y - radius; j < y + radius; ++j) {
				world.setTileXY(i, j, Cell.TILE_GROUND);
			}
		}

		return world;
	}
}
