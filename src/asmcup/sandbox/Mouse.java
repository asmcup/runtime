package asmcup.sandbox;

import java.awt.Cursor;
import java.awt.event.*;

public class Mouse extends MouseAdapter {
	protected final Sandbox sandbox;
	protected boolean panning;
	protected int panStartX, panStartY;
	protected boolean teleport;
	protected int teleX, teleY;
	
	public Mouse(Sandbox sandbox) {
		this.sandbox = sandbox;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			if (teleport) {
				finishTeleport(e);
				break;
			}
			
			panStartX = e.getX();
			panStartY = e.getY();
			panning = true;
			sandbox.redraw();
			break;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			panning = false;
			sandbox.redraw();
			break;
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (panning) {
			int dx = panStartX - e.getX();
			int dy = panStartY - e.getY();
			sandbox.pan(dx, dy);
			sandbox.redraw();
			
			panStartX = e.getX();
			panStartY = e.getY();
		}
	}
	
	public void startTeleport() {
		sandbox.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		teleport = true;
	}
	
	public void finishTeleport(MouseEvent e) {
		sandbox.getFrame().setCursor(Cursor.getDefaultCursor());
		sandbox.teleport(e.getX(), e.getY());
		teleport = false;
	}
}
