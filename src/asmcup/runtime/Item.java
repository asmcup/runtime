package asmcup.runtime;

import java.util.Random;

public abstract class Item {
	protected float x, y;
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void position(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean withinDistance(Robot robot) {
		return withinDistance(robot.getX(), robot.getY());
	}
	
	public boolean withinDistance(float tx, float ty) {
		float dx = tx - (x + 16);
		float dy = ty - (y + 16);
		return Math.sqrt(dx * dx + dy * dy) <= 20;
	}
	
	public abstract void collect(Robot robot);
	
	public static class Battery extends Item {
		protected int value;
		
		public void collect(Robot robot) {
			robot.addBattery(value);
		}
	}
	
	public static class Gold extends Item {
		protected int value;
		
		public Gold(Random random, int limit) {
			int a = 1 + random.nextInt(10);
			int b = 1 + random.nextInt(100);
			int c = random.nextInt(1000);
			value = a * b - c;
			value = Math.min(value, limit);
			value = Math.max(value, 1);
		}
		
		public int getValue() {
			return value;
		}
		
		public int getVariant() {
			return (int)((value / 1000.0) * 4);
		}
		
		public void collect(Robot robot) {
			robot.addGold(value);
		}
	}
}
