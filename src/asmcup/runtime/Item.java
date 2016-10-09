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
	
	public static class Battery extends Item {
		
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
	}
}
