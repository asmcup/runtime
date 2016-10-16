package asmcup.sandbox;

import java.awt.*;

import javax.swing.JComponent;

public class Canvas extends JComponent {
	protected final Sandbox sandbox;
	
	public Canvas(Sandbox sandbox) {
		this.sandbox = sandbox;
		setPreferredSize(new Dimension(Sandbox.WIDTH, Sandbox.HEIGHT));
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