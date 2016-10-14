package asmcup.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import asmcup.vm.VM;

public class Robot {
	protected final int id;
	protected VM vm;
	protected float x, y;
	protected float facing;
	protected float speed;
	protected int overclock;
	protected int battery;
	protected float motor;
	protected float steer;
	protected float lazer;
	protected float lastX, lastY;
	
	public Robot(int id) {
		this.id = id;
		this.vm = new VM();
		this.battery = MAX_BATTERY;
	}
	
	public Robot(DataInputStream stream) throws IOException {
		this.id = stream.readInt();
		this.x = stream.readFloat();
		this.y = stream.readFloat();
		this.facing = stream.readFloat();
		this.speed = stream.readFloat();
		this.battery = stream.readInt();
		this.overclock = stream.readUnsignedByte() & 0xFF;
		this.motor = stream.readFloat();
		this.steer = stream.readFloat();
		this.lazer = stream.readFloat();
		this.lastX = stream.readFloat();
		this.lastY = stream.readFloat();
		this.vm = new VM(stream);
	}
	
	public void save(DataOutputStream stream) throws IOException {
		stream.writeInt(id);
		stream.writeFloat(x);
		stream.writeFloat(y);
		stream.writeFloat(facing);
		stream.writeFloat(speed);
		stream.writeInt(battery);
		stream.writeByte(overclock);
		stream.writeFloat(motor);
		stream.writeFloat(steer);
		stream.writeFloat(lazer);
		stream.writeFloat(lastX);
		stream.writeFloat(lastY);
		vm.save(stream);
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
	
	public float getFacing() {
		return facing;
	}
	
	public float getMotor() {
		return motor;
	}
	
	public float getSteer() {
		return steer;
	}
	
	public void setMotor(float f) {
		motor = clampSafe(f, -1, 1);
	}
	
	public void setSteer(float f) {
		steer = clampSafe(f, -1, 1);
	}
	
	public void position(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean isDead() {
		return battery <= 0;
	}
	
	public void flash(byte[] ram) {
		this.vm = new VM(ram);
	}
	
	public void tick(World world) {
		tickSoftware(world);
		tickHardware(world);
	}
	
	protected void tickSoftware(World world) {
		int cycles = 1 + Math.min(100, overclock);
		
		while (cycles > 0) {
			vm.tick();
			handleIO(world);
			cycles--;
			battery--;
		}
	}
	
	protected void tickHardware(World world) {
		facing += steer;
		speed += motor;
		
		if (Math.abs(steer) <= 0.01f) {
			steer = 0.0f;
		}
		
		if (Math.abs(motor) <= 0.01f) {
			motor = 0.0f;
		}
		
		if (speed > MAX_SPEED) {
			speed = MAX_SPEED;
		} else if (speed < MIN_SPEED) {
			speed = MIN_SPEED;
		} else if (Math.abs(speed) <= 0.01f) {
			speed = 0.0f;
			return;
		}
		
		float tx = x + (float)Math.cos(facing) * speed;
		float ty = y + (float)Math.sin(facing) * speed;
		
		if (!world.isSolid(tx, ty, 15)) {
			x = tx;
			y = ty;
		} else if (!world.isSolid(tx, y, 15)) {
			x = tx;
		} else if (!world.isSolid(x, ty, 15)) {
			y = ty;
		}
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
			vm.pushFloat(world.ray(x, y, facing));
			break;
		case IO_OVERCLOCK:
			overclock = vm.pop8();
			break;
		case IO_LAZER:
			lazer = popFloatSafe(0.0f, 1.0f);
			break;
		case IO_BATTERY:
			vm.pushFloat(battery);
			break;
		case IO_MARK:
			offset = vm.pop8();
			value = vm.pop8();
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
		}
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
	public static final int IO_BATTERY = 5;
	public static final int IO_MARK = 6;
	public static final int IO_MARK_READ = 7;
	public static final int IO_ACCELEROMETER = 8;
	
	public static final float MAX_SPEED = 3.33f;
	public static final float MIN_SPEED = MAX_SPEED * -0.5f;
	public static final int MAX_BATTERY = 60 * 60 * 24;
}
