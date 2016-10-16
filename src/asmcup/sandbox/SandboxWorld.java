package asmcup.sandbox;

import java.util.ArrayList;

import asmcup.runtime.World;

public class SandboxWorld extends World {
	private ArrayList<Ray> lines = new ArrayList<>();
	
	@Override
	public float ray(float x, float y, float theta) {
		float d = super.ray(x, y, theta);
		lines.add(new Ray(x, y, theta, d));
		return d;
	}
	
	public Iterable<Ray> getRays() {
		Iterable<Ray> list = this.lines;
		this.lines = new ArrayList<Ray>();
		return list;
	}
	
	public static class Ray {
		public float x, y, theta, d;
		
		public Ray(float x, float y, float theta, float d) {
			this.x = x;
			this.y = y;
			this.theta = theta;
			this.d = d;
		}
	}
}
