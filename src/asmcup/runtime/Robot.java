package asmcup.runtime;

import asmcup.vm.VM;

public class Robot {
	protected final int id;
	protected VM vm;
	protected float x, y;
	protected float facing;
	protected int overclock;
	protected int battery;
	protected float motor;
	protected float steer;
	protected float lazer;
	protected float lazerEnd;
	protected float lastX, lastY;
	protected float frequency;
	protected int gold;
	protected float sensor;
	protected int sensorIgnore;
	protected int sensorFrame;
	protected boolean ramming;
	protected int lastValidIO, lastInvalidIO;
	
	public Robot(int id) {
		this.id = id;
		this.vm = new VM();
		this.battery = BATTERY_MAX;
	}
	
	public VM getVM() {
		return vm;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public int getColumn() {
		return (int)(x / World.TILE_SIZE);
	}
	
	public int getRow() {
		return (int)(y / World.TILE_SIZE);
	}
	
	public int getCellX() {
		return getColumn() / World.TILES_PER_CELL;
	}
	
	public int getCellY() {
		return getRow() / World.TILES_PER_CELL;
	}
	
	public int getCellKey() {
		return getCellX() | (getCellY() << 16);
	}
	
	public float getFacing() {
		return facing;
	}
	
	public float getMotor() {
		return motor;
	}
	
	public float getSteer() {
		return steer;
	}
	
	public int getBattery() {
		return battery;
	}
	
	public int getOverclock() {
		return overclock;
	}
	
	public float getSensor() {
		return sensor;
	}
	
	public int getSensorFrame() {
		return sensorFrame;
	}
	
	public float getLazer() {
		return lazer;
	}
	
	public float getLazerEnd() {
		return lazerEnd;
	}
	
	public void setMotor(float f) {
		motor = clampSafe(f, -1, 1);
	}
	
	public void setSteer(float f) {
		steer = clampSafe(f, -1, 1);
	}
	
	public void setLazer(float f) {
		lazer = clampSafe(f, 0, 1.0f);
	}
	
	public void setOverclock(int v) {
		overclock = Math.min(100, Math.max(0, v));
	}
	
	public void setFacing(float facing) {
		this.facing = facing;
	}
	
	public void position(float x, float y) {
		this.x = x;
		this.y = y;
		lastX = x;
		lastY = y;
	}
	
	public boolean isRamming() {
		return ramming;
	}
	
	public int getLastIO() {
		return lastValidIO;
	}
	
	public int getLastInvalidIO() {
		return lastInvalidIO;
	}
	
	public void kill() {
		battery = 0;
	}
	
	public void damage(int dmg) {
		if (dmg < 0) {
			throw new IllegalArgumentException("Damage cannot be negative");
		}
		
		battery -= dmg;
	}
	
	public void addBattery(int charge) {
		if (charge < 0) {
			throw new IllegalArgumentException("Recharge amount cannot be negative");
		}
		
		battery += charge;
	}
	
	public boolean isDead() {
		return battery <= 0;
	}
	
	public int getGold() {
		return gold;
	}
	
	public void addGold(int g) {
		if (g < 0) {
			throw
			new IllegalArgumentException("Gold amount cannot be negetive");
		}
		
		gold += g;
	}
	
	public void flash(byte[] ram) {
		if (ram.length != 256) {
			throw new IllegalArgumentException("Program size must be 256");
		}
		
		this.vm = new VM(ram);
		motor = 0;
		steer = 0;
		lazer = 0;
		lazerEnd = 0;
		sensorIgnore = 0;
		sensorFrame = 0;
		lastX = x;
		lastY = y;
		overclock = 0;
		ramming = false;
	}
	
	public void tick(World world) {
		tickSoftware(world);
		tickHardware(world);
	}
	
	protected void tickSoftware(World world) {
		int cyclesUsed = 0;
		
		while (cyclesUsed <= overclock) {
			vm.tick();
			handleIO(world);
			cyclesUsed++;
			battery--;
		}
	}
	
	protected void tickHardware(World world) {
		tickSteer(world);
		tickMotor(world);
		tickLazer(world);
	}
	
	protected void tickSteer(World world) {
		if (Math.abs(steer) <= 0.01f) {
			steer = 0.0f;
		}
		
		facing += steer * STEER_RATE;
	}
	
	protected void tickMotor(World world) {
		float s;
		
		if (Math.abs(motor) <= 0.01f) {
			motor = 0.0f;
			return;
		}
		
		if (motor < 0) {
			s = motor * 0.5f * SPEED_MAX;
		} else {
			s = motor * SPEED_MAX;
		}
		
		float tx = x + (float)Math.cos(facing) * s;
		float ty = y + (float)Math.sin(facing) * s;
		int radius = COLLIDE_RADIUS;
		
		if (!world.isSolid(tx, ty, radius)) {
			x = tx;
			y = ty;
			ramming = true;
		} else if (!world.isSolid(tx, y, radius)) {
			x = tx;
			ramming = true;
		} else if (!world.isSolid(x, ty, radius)) {
			y = ty;
			ramming = true;
		} else {
			ramming = false;
		}
	}
	
	protected void tickLazer(World world) {
		if (lazer <= 0) {
			lazerEnd = 0;
			return;
		}
		
		float cos = (float)Math.cos(facing);
		float sin = (float)Math.sin(facing);
		
		for (int i=0; i < RAY_STEPS; i++) {
			if ((i * RAY_INTERVAL) >= (lazer * LAZER_RANGE)) {
				lazerEnd = lazer * LAZER_RANGE;
				return;
			}
			
			battery -= LAZER_BATTERY_COST;
			
			float tx = x + cos * (i * RAY_INTERVAL);
			float ty = y + sin * (i * RAY_INTERVAL);
			int tile = world.getTileXY(tx, ty);
			int type = tile & 0b111;
			int variant = (tile >> 3) & 0b11;
			
			if (type == Cell.TILE_WALL) {
				lazerEnd = i * RAY_INTERVAL;
				return;
			}
			
			if (type == Cell.TILE_OBSTACLE) {
				if (variant >= 2) {
					world.setTileXY(tx, ty, Cell.TILE_GROUND);
				}
				
				lazerEnd = i * RAY_INTERVAL;
				return;
			}
		}
		
		lazerEnd = RAY_INTERVAL * RAY_STEPS;
	}
	
	protected void handleIO(World world) {
		if (!vm.checkIO()) {
			return;
		}
		
		int offset, value;
		
		value = vm.pop8();
		
		switch (value) {
		case IO_MOTOR:
			motor = popFloatSafe(-1.0f, 1.0f);
			break;
		case IO_STEER:
			steer = popFloatSafe(-1.0f, 1.0f);
			break;
		case IO_SENSOR:
			sensorRay(world);
			break;
		case IO_SENSOR_CONFIG:
			sensorIgnore = vm.pop8();
			break;
		case IO_OVERCLOCK:
			setOverclock(vm.pop8());
			break;
		case IO_LAZER:
			lazer = popFloatSafe(0.0f, 1.0f);
			break;
		case IO_BATTERY:
			vm.pushFloat((float)battery / BATTERY_MAX);
			break;
		case IO_MARK:
			value = vm.pop8();
			offset = vm.pop8();
			world.mark(this, offset, value);
			break;
		case IO_MARK_READ:
			offset = vm.pop8();
			value = world.markRead(this, offset);
			vm.push8(value);
			break;
		case IO_ACCELEROMETER:
			vm.pushFloat(x - lastX);
			vm.pushFloat(y - lastY);
			lastX = x;
			lastY = y;
			break;
		case IO_RADIO:
			frequency = popFloatSafe(-FREQUENCY_MAX, FREQUENCY_MAX);
			break;
		case IO_SEND:
			world.send(this, frequency, vm.pop8());
			break;
		case IO_RECV:
			vm.push8(world.recv(this, frequency));
			break;
		case IO_COMPASS:
			vm.pushFloat(facing);
			break;
		default:
			lastInvalidIO = world.getFrame();
			return;
		}
		
		lastValidIO = world.getFrame();
	}
	
	protected void sensorRay(World world) {
		float cos = (float)Math.cos(facing);
		float sin = (float)Math.sin(facing);
		sensorFrame = world.getFrame();
		
		for (int i = 0; i < RAY_STEPS; i++) {
			float sx = x + (cos * i * RAY_INTERVAL);
			float sy = y + (sin * i * RAY_INTERVAL);
			int hit = sensorPoint(world, sx, sy);
			
			if (hit != 0) {
				sensor = i * RAY_INTERVAL;
				vm.pushFloat(sensor);
				vm.push8(hit);
				return;
			}
		}
		
		sensor = RAY_RANGE;
		vm.pushFloat(sensor);
		vm.push8(0);
	}
	
	protected int sensorPoint(World world, float sx, float sy) {
		if (world.isSolid(sx, sy)) {
			if ((sensorIgnore & SENSOR_SOLID) == 0) {
				return SENSOR_SOLID;
			} else {
				return 0;
			}
		}
		
		if ((sensorIgnore & SENSOR_HAZARD) == 0) {
			if (world.isHazard(sx, sy)) {
				return SENSOR_HAZARD;
			}
		}
		
		Item item = world.getItem(sx, sy);
		
		if ((sensorIgnore & SENSOR_GOLD) == 0) {
			if (item instanceof Item.Gold) {
				return SENSOR_GOLD;
			}
		}
		
		if ((sensorIgnore & SENSOR_BATTERY) == 0) {
			if (item instanceof Item.Battery) {
				return SENSOR_BATTERY;
			}
		}
		
		return 0;
	}
	
	protected float popFloatSafe(float min, float max) {
		return clampSafe(vm.popFloat(), min, max);
	}
	
	protected static float clampSafe(float f, float min, float max) {
		if (f > max) {
			return max;
		} else if (f < min) {
			return min;
		} else if (Float.isNaN(f)) {
			return 0;
		}
		
		return f;
	}
	
	public static final int IO_SENSOR = 0;
	public static final int IO_MOTOR = 1;
	public static final int IO_STEER = 2;
	public static final int IO_OVERCLOCK = 3;
	public static final int IO_LAZER = 4;
	public static final int IO_LASER = 4;
	public static final int IO_BATTERY = 5;
	public static final int IO_MARK = 6;
	public static final int IO_MARK_READ = 7;
	public static final int IO_ACCELEROMETER = 8;
	public static final int IO_RADIO = 9;
	public static final int IO_SEND = 10;
	public static final int IO_RECV = 11;
	public static final int IO_RECEIVE = 11;
	public static final int IO_SENSOR_CONFIG = 12;
	public static final int IO_COMPASS = 13;
	
	public static final float SPEED_MAX = 8;
	public static final float STEER_RATE = (float)(Math.PI * 0.1);
	public static final int BATTERY_MAX = 60 * 60 * 24;
	public static final int OVERCLOCK_MAX = 100;
	public static final float FREQUENCY_MAX = 1000 * 10;
	public static final int LAZER_RANGE = 100;
	public static final int LAZER_BATTERY_COST = 4;
	
	public static final int SENSOR_SOLID = 1;
	public static final int SENSOR_HAZARD = 2;
	public static final int SENSOR_GOLD = 4;
	public static final int SENSOR_BATTERY = 8;
	
	public static final int RAY_INTERVAL = 4;
	public static final int RAY_STEPS = 64;
	public static final int RAY_RANGE = RAY_INTERVAL * RAY_STEPS;
	
	public static final int COLLIDE_RADIUS = 10;
}
