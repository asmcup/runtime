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
		World world = generateEmptyWorld((int) robot.getX(), (int) robot.getY(), 50);
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

	@Test
	public void testSteer() {
		World world = new World();
		float previous = robot.getFacing();
		robot.tickSteer(world);
		assertEquals("Robot steered", previous, robot.getFacing(), 0.0001f);

		robot.setSteer(1.0f);
		robot.tickSteer(world);
		assertTrue("Robot did not steer.", previous < robot.getFacing());
	}

	@Test
	public void testMotor() {
		World world = generateEmptyWorld((int) robot.getX(), (int) robot.getY(), 30);
		float x = robot.getX();
		float y = robot.getY();
		robot.setMotor(1.0f);
		robot.setFacing(0.5f);
		robot.tickMotor(world);

		assertNotEquals(x, robot.getX(), 0.00001f);
		assertNotEquals(y, robot.getY(), 0.00001f);
	}

	@Test
	public void testIOMotor() {
		World world = new World();
		VM vm = robot.getVM();
		vm.pushFloat(0.5f);
		vm.push8(Robot.IO_MOTOR);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(0.5f, robot.getMotor(), 0.0001f);

		vm.pushFloat(1.5f);
		vm.push8(Robot.IO_MOTOR);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(1.0f, robot.getMotor(), 0.0001f);
	}

	@Test
	public void testIOSteer() {
		World world = new World();
		VM vm = robot.getVM();
		vm.pushFloat(0.5f);
		vm.push8(Robot.IO_STEER);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(0.5f, robot.getSteer(), 0.0001f);

		vm.pushFloat(1.5f);
		vm.push8(Robot.IO_STEER);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(1.0f, robot.getSteer(), 0.0001f);
	}

	@Test
	public void testIOOverclock() {
		World world = new World();
		VM vm = robot.getVM();
		vm.push8(200);
		vm.push8(Robot.IO_OVERCLOCK);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(100, robot.getOverclock());

		vm.push8(50);
		vm.push8(Robot.IO_OVERCLOCK);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(50, robot.getOverclock());
	}

	@Test
	public void testIOBattery() {
		World world = new World();
		VM vm = robot.getVM();
		robot.battery = Robot.BATTERY_MAX;
		vm.push8(Robot.IO_BATTERY);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(1.0f, vm.popFloat(), 0.0001f);
	}

	@Test
	public void testIOCompass() {
		World world = new World();
		VM vm = robot.getVM();
		robot.setFacing(0.0f);
		vm.push8(Robot.IO_COMPASS);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(0.0f, vm.popFloat(), 0.0001f);

		robot.setFacing((float) Math.PI * 2);
		vm.push8(Robot.IO_COMPASS);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(0.0f, vm.popFloat(), 0.0001f);
	}

	@Test
	public void testMark() {
		World world = new World();
		VM vm = robot.getVM();
		vm.push8(0);
		vm.push8(42);
		vm.push8(Robot.IO_MARK);
		vm.setIO(true);
		robot.handleIO(world);

		vm.push8(0);
		vm.push8(Robot.IO_MARK_READ);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(42, vm.pop8());

		// Test reading mark, when not marked
		robot.position(100, 100);
		vm.push8(0);
		vm.push8(Robot.IO_MARK_READ);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(0, vm.pop8());
	}

	@Test
	public void testMarkOffset() {
		World world = new World();
		VM vm = robot.getVM();
		// Mark offset 0
		vm.push8(0);
		vm.push8(42);
		vm.push8(Robot.IO_MARK);
		vm.setIO(true);
		robot.handleIO(world);
		// Mark offset 1
		vm.push8(1);
		vm.push8(4);
		vm.push8(Robot.IO_MARK);
		vm.setIO(true);
		robot.handleIO(world);

		// Read both
		vm.push8(1);
		vm.push8(Robot.IO_MARK_READ);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(4, vm.pop8());

		vm.push8(0);
		vm.push8(Robot.IO_MARK_READ);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals(42, vm.pop8());
	}
	
	public void testBeamAngle() {
		World world = generateEmptyWorld((int) robot.getX(), (int) robot.getY(), 50);
		VM vm = robot.getVM();
		// Face west
		robot.setFacing(0);
		// Beam south?
		vm.pushFloat(1.0f);
		vm.push8(Robot.IO_BEAM_DIRECTION);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals((float)Math.PI/2, robot.getBeamAngle(), 0.0001f);
		
		// Face north
		robot.setFacing(-(float)Math.PI/2);
		// Beam north-west?
		vm.pushFloat(-0.5f);
		vm.push8(Robot.IO_BEAM_DIRECTION);
		vm.setIO(true);
		robot.handleIO(world);
		assertEquals((float)-Math.PI * 0.75f, robot.getBeamAngle(), 0.0001f);
	}

	private World generateEmptyWorld(int x, int y, int radius) {
		World world = new World();
		for (int i = x - radius; i < x + radius; i += World.TILE_SIZE) {
			for (int j = y - radius; j < y + radius; j += World.TILE_SIZE) {
				world.setTileXY(i, j, Cell.TILE_GROUND);
			}
		}

		return world;
	}
}
