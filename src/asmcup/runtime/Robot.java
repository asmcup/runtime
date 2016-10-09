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
	
	public Robot(int id) {
		this.id = id;
		this.vm = new VM();
		this.battery = MAX_BATTERY;
	}
	
	public Robot(DataInputStream stream) throws IOException {
		this.id = stream.readInt();
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.facing = stream.readFloat();
		this.battery = stream.readInt();
		this.overclock = stream.readUnsignedByte() & 0xFF;
		this.motor = stream.readUnsignedByte() & 0xFF;
		this.steer = stream.readUnsignedByte() & 0xFF;
		this.lazer = stream.readInt() & 0xFF;
		this.vm = new VM(stream);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public boolean isDead() {
		return battery <= 0;
	}
	
	public void flash(byte[] ram) {
		this.vm = new VM(ram);
	}
	
	public void save(DataOutputStream stream) throws IOException {
		stream.writeInt(id);
		stream.writeFloat(x);
		stream.writeFloat(y);
		stream.writeFloat(facing);
		stream.writeInt(battery);
		stream.writeByte(overclock);
		stream.writeFloat(motor);
		stream.writeFloat(steer);
		stream.writeFloat(lazer);
		vm.save(stream);
	}
	
	public void tick(World world) {
		int cycles = 1 + overclock * 4;
		
		while (cycles > 0) {
			vm.tick();
			handleIO(world);
			cycles--;
		}
		
		facing += steer;
		speed += motor;
		
		if (Math.abs(steer) <= 0.01f) {
			steer = 0.0f;
		}
		
		if (Math.abs(motor) <= 0.01f) {
			motor = 0.0f;
		}
		
		if (Math.abs(speed) <= 0.01f) {
			speed = 0.0f;
		}
		
		if (speed > MAX_SPEED) {
			speed = MAX_SPEED;
		}
		
		x = x + (float)Math.cos(facing) * speed;
		y = y + (float)Math.sin(facing) * speed;
		
		//if (!world.isSolid(x, y)) {
		//	x = tx;
		//	y = ty;
		//}
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
		}
	}
	
	protected float popFloatSafe(float min, float max) {
		float f = vm.popFloat();
		
		if (Float.isNaN(f)) {
			return min;
		}
		
		if (f < min) {
			return min;
		}
		
		if (f > max) {
			return max;
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
	
	public static final int MAX_SPEED = 16;
	public static final int MAX_BATTERY = 60 * 60 * 24;
}
