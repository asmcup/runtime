package asmcup.sandbox;

import java.awt.*;

import javax.swing.JComponent;

public class Canvas extends JComponent {
	public final Sandbox sandbox;
	public final CanvasMenu menu;
	
	public Canvas(Sandbox sandbox) {
		this.sandbox = sandbox;
		this.menu = new CanvasMenu(this);
		
		setPreferredSize(new Dimension(Sandbox.WIDTH, Sandbox.HEIGHT));
		setComponentPopupMenu(menu);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Image backBuffer = sandbox.getBackBuffer();
		
		if (backBuffer == null) {
			return;
		}
		
		synchronized (backBuffer) {
			g.drawImage(backBuffer, 0, 0, null);
		}
	}
}