package asmcup.runtime;

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
	
	public boolean withinDistance(float tx, float ty) {
		float dx = tx - x;
		float dy = ty - y;
		return StrictMath.sqrt(dx * dx + dy * dy) <= 20;
	}
	
	public abstract void collect(Robot robot);
	
	public static class Battery extends Item {
		protected int value;
		
		public Battery() {
			this(DEFAULT_VALUE);
		}
		
		public Battery(int value) {
			this.value = value;
		}
		
		public void collect(Robot robot) {
			robot.addBattery(value * 100);
		}
		
		public int getVariant() {
			return value / 25;
		}
		
		public static final int DEFAULT_VALUE = 50;
	}
	
	public static class Gold extends Item {
		protected int value;
		
		public Gold() {
			this(DEFAULT_VALUE);
		}
		
		private Gold(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public int getVariant() {
			return value / 25;
		}
		
		public void collect(Robot robot) {
			robot.addGold(value);
		}
		
		public static final int DEFAULT_VALUE = 50;
	}
}
