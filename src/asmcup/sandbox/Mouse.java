package asmcup.sandbox;

import java.awt.event.*;

public class Mouse extends MouseAdapter {
	public final Sandbox sandbox;
	protected boolean panning;
	protected int screenX, screenY;
	protected int worldX, worldY;
	protected int panStartX, panStartY;
	
	public Mouse(Sandbox sandbox) {
		this.sandbox = sandbox;
	}
	
	public int getWorldX() {
		return worldX;
	}
	
	public int getWorldY() {
		return worldY;
	}
	
	protected void update(MouseEvent e) {
		screenX = e.getX();
		screenY = e.getY();
		worldX = sandbox.getPanX() + screenX - Sandbox.WIDTH / 2;
		worldY = sandbox.getPanY() + screenY - Sandbox.HEIGHT / 2;
	}
	
	protected void startPanning() {
		panStartX = screenX;
		panStartY = screenY;
		panning = true;
		sandbox.redraw();
	}
	
	protected void pan() {
		int dx = panStartX - screenX;
		int dy = panStartY - screenY;
		sandbox.pan(dx, dy);
		sandbox.redraw();
		
		panStartX = screenX;
		panStartY = screenY;
	}
	
	protected void finishPanning() {
		panning = false;
		sandbox.redraw();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		update(e);
		
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			startPanning();
			break;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		update(e);
		
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			finishPanning();
			break;
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		update(e);
		
		if (panning) {
			pan();
		}
	}
}
