package asmcup.sandbox;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import asmcup.runtime.Robot;

public class Debugger extends JFrame {
	protected final Sandbox sandbox;
	protected MemoryPane memPane;
	protected JScrollPane scrollPane;
	protected JSlider motorSlider, steerSlider;
	protected JProgressBar batteryIndicator, overclockIndicator;
	protected JLabel goldLabel;
	
	public Debugger(Sandbox sandbox) {
		this.sandbox = sandbox;
		
		memPane = new MemoryPane();
		scrollPane = new JScrollPane(memPane);
		motorSlider = new JSlider(-100, 100, 0);
		steerSlider = new JSlider(-100, 100, 0);
		batteryIndicator = new JProgressBar(0, Robot.BATTERY_MAX);
		batteryIndicator.setStringPainted(true);
		overclockIndicator = new JProgressBar(0, 255);
		overclockIndicator.setStringPainted(true);
		goldLabel = new JLabel("0");
		
		ChangeListener listener = (e) -> { updateControls(); };
		motorSlider.addChangeListener(listener);
		steerSlider.addChangeListener(listener);
		
		JPanel panel = new JPanel(new BorderLayout());
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));
		bottomPane.add(hitem("Motor:", motorSlider));
		bottomPane.add(hitem("Steer:", steerSlider));
		bottomPane.add(hitem("Battery:", batteryIndicator));
		bottomPane.add(hitem("Clock:", overclockIndicator));
		bottomPane.add(hitem("Gold:", goldLabel));
		
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(bottomPane, BorderLayout.SOUTH);
		
		setTitle("Debugger");
		setResizable(false);
		setContentPane(panel);
		pack();
	}
	
	public void updateDebugger() {
		Robot robot = sandbox.getRobot();
		motorSlider.setValue((int)(robot.getMotor() * 100));
		steerSlider.setValue((int)(robot.getSteer() * 100));
		batteryIndicator.setValue(robot.getBattery());
		overclockIndicator.setValue(robot.getOverclock());
		overclockIndicator.setString(String.valueOf(robot.getOverclock()));
		goldLabel.setText(String.valueOf(robot.getGold()));
		repaint();
	}
	
	public void updateControls() {
		synchronized (sandbox.getWorld()) {
			Robot robot = sandbox.getRobot();
			robot.setMotor(motorSlider.getValue() / 100.0f);
			robot.setSteer(steerSlider.getValue() / 100.0f);
		}
	}
	
	protected static JPanel hitem(String label, JComponent c) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(c, BorderLayout.CENTER);
		p.add(new JLabel(label), BorderLayout.WEST);
		return p;
	}
	
	protected class MemoryPane extends JComponent {
		protected Font font;
		protected int start, end;
		protected Mouse mouse = new Mouse();
		
		public MemoryPane() {
			font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			setPreferredSize(new Dimension(17 * 16, 16 * 16));
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
		}
		
		public int transform(int x, int y) {
			int col = Math.max(0, (x - 16) / 16);
			int row = Math.max(0, y / 16);
			return Math.min(255, row * 16 + col);
		}
		
		public int transform(MouseEvent e) {
			return transform(e.getX(), e.getY());
		}
		
		public void select(int a, int b) {
			start = Math.min(a,  b);
			end = Math.max(a, b);
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
				
				for (int col=0; col < 16; col++) {
					int x = 16 + col * 16;
					int addr = row * 16 + col;
					int value = robot.getVM().read8(addr);
					
					if (addr >= start && addr <= end) {
						g.setColor(Color.BLACK);
						g.fillRect(x, y - 12, 16, 16);
						g.setColor(Color.WHITE);
					} else {
						g.setColor(Color.BLACK);
					}
					
					if (addr == robot.getVM().getProgramCounter()) {
						g.setColor(Color.RED);
					}
					
					g.drawString(String.format("%02x", value), x + 1, y);
					
					if (addr == robot.getVM().getStackPointer()) {
						g.setColor(Color.RED);
						g.drawRect(x, y - 12, 16, 16);
					}
				}
			}
		}
		
		protected class Mouse extends MouseAdapter {
			protected boolean dragging = false;
			protected int dragStart;
			
			@Override
			public void mousePressed(MouseEvent e) {
				switch (e.getButton()) {
				case MouseEvent.BUTTON1:
					dragStart = transform(e);
					dragging = true;
					repaint();
					break;
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				switch (e.getButton()) {
				case MouseEvent.BUTTON1:
					mouseDragged(e);
					dragging = false;
					break;
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragging) {
					select(dragStart, transform(e));
					repaint();
				}
			}
		}
	}
}
