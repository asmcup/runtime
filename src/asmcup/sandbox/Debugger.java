package asmcup.sandbox;

import java.awt.*;

import javax.swing.*;

import asmcup.runtime.Robot;

public class Debugger extends JFrame {
	protected final Sandbox sandbox;
	protected MemoryPane memPane;
	protected JScrollPane scrollPane;
	
	public Debugger(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		memPane = new MemoryPane();
		scrollPane = new JScrollPane(memPane);
		
		setTitle("Debugger");
		setContentPane(scrollPane);
		pack();
	}
	
	protected class MemoryPane extends JComponent {
		protected Font font;
		
		public MemoryPane() {
			font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			setPreferredSize(new Dimension(17 * 16, 16 * 16));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Rectangle c = g.getClipBounds();
			Robot robot = sandbox.getRobot();
			
			g.setColor(Color.WHITE);
			g.fillRect(c.x, c.y, c.width, c.height);
			
			g.setFont(font);
			
			for (int row=0; row < 16; row++) {
				int y = 12 + row * 16;
				
				g.setColor(Color.DARK_GRAY);
				g.fillRect(0, row * 16, 16, 16);
				g.setColor(Color.WHITE);
				g.drawString(String.format("%02x", row * 16), 0, y);
				
				g.setColor(Color.BLACK);
				
				for (int col=0; col < 16; col++) {
					int x = 16 + col * 16;
					int value = robot.getVM().read8(row * 16 + col);
					g.drawString(String.format("%02x", value), x, y);
				}
			}
		}
	}
}
