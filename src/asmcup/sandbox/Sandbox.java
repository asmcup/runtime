package asmcup.sandbox;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javax.imageio.ImageIO;

import asmcup.runtime.*;
import asmcup.runtime.Robot;

public class Sandbox {
	protected Mouse mouse;
	protected Menu menu;
	protected Canvas canvas;
	protected Frame frame;
	protected CodeEditor codeEditor;
	
	protected Image backBuffer;
	protected World world;
	protected Robot robot;
	protected int panX, panY;
	protected Image[] ground, wall, obstacles, hazards, coins;
	protected Image bot;
	
	public Sandbox() throws IOException {
		mouse = new Mouse(this);
		menu = new Menu(this);
		canvas = new Canvas(this);
		frame = new Frame(this);
		codeEditor = new CodeEditor(this);
		
		world = new World();
		ground = loadImage("/ground.png");
		wall = loadImage("/wall.png");
		obstacles = loadImage("/obstacles.png");
		hazards = loadImage("/hazards.png");
		bot = ImageIO.read(getClass().getResource("/robot.png"));
		coins = loadImage("/gold.png");
	}
	
	public Mouse getMouse() {
		return mouse;
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public Frame getFrame() {
		return frame;
	}
	
	public CodeEditor getCodeEditor() {
		return codeEditor;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public void pan(int dx, int dy) {
		panX += dx;
		panY += dy;
	}
	
	protected Image[] loadImage(String path) throws IOException {
		URL url = getClass().getResource(path);
		Image sheet = ImageIO.read(url);
		Image[] variants = new Image[4];
		
		for (int i=0; i < 4; i++) {
			Image img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.drawImage(sheet, -i * 32, 0, null);
			variants[i] = img;
		}
		
		return variants;
	}
	
	public void run() {
		robot = new Robot(1);
		world.addRobot(robot);
		frame.setVisible(true);
		
		long lastTick = System.currentTimeMillis();
		
		while (frame.isVisible()) {
			lastTick = System.currentTimeMillis();
			
			synchronized (world) {
				world.tick();
			}
			
			if (backBuffer != null) {
				synchronized (backBuffer) {
					draw();
				}
			}
			
			frame.repaint();
			
			long now = System.currentTimeMillis();
			long span = now - lastTick;
			int wait = (int)(1000 - span);
			sleep(wait);
		}
	}
	
	public void draw() {
		if (backBuffer == null) {
			return;
		}
		
		Graphics g = backBuffer.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 600);
		
		int left = (int)Math.floor((panX - 400.0) / (16.0 * 32.0));
		int right = (int)Math.ceil((panX + 400.0) / (16.0 * 32.0));
		int top = (int)Math.floor((panY - 300.0) / (16.0 * 32.0));
		int bottom = (int)Math.ceil((panY + 300.0) / (16.0 * 32.0));
		
		for (int cellY=top; cellY < bottom; cellY++) {
			for (int cellX=left; cellX < right; cellX++) {
				drawCell(g, world.getCell(cellX, cellY));
			}
		}
	}
	
	protected void drawCell(Graphics g, Cell cell) {
		int left = cell.getX() * 16;
		int right = left + 16;
		int top = cell.getY() * 16;
		int bottom = top + 16;
		
		for (int row=top; row < bottom; row++) {
			for (int col=left; col < right; col++) {
				int tile = cell.getTile(col - left, row - top);
				drawTile(g, col, row, tile);
			}
		}
		
		for (Item item : cell.getItems()) {
			drawItem(g, item);
		}
		
		for (Robot robot : cell.getRobots()) {
			drawRobot(g, robot);
		}
		
		g.setColor(Color.WHITE);
		int x = 400 + left * 32 - panX;
		int y = 300 + top * 32 - panY;
		g.drawRect(x, y, 16 * 32, 16 * 32);
		
		String msg = String.format("%d, %d", cell.getX(), cell.getY());
		g.drawString(msg, x + 100, y + 100);
		
		msg = String.format("%x", cell.getKey());
		g.drawString(msg, x + 100, y + 150);
	}
	
	protected void drawTile(Graphics g, int col, int row, int tile) {
		int x = 400 + col * 32 - panX;
		int y = 300 + row * 32 - panY;
		int variant = (tile >> 2) & 0b11;
		
		switch (tile & 0b11) {
		case 0:
			drawVariant(g, ground, x, y, variant);
			break;
		case 1:
			drawVariant(g, ground, x, y, variant ^ 0b11);
			drawVariant(g, obstacles, x, y, variant);
			break;
		case 2:
			drawVariant(g, wall, x, y, variant);
			break;
		case 3:
			drawVariant(g, hazards, x, y, variant);
			break;
		}
	}
	
	protected void drawRobot(Graphics lg, Robot robot) {
		Graphics2D g = (Graphics2D)lg;
		int x = (int)robot.getX();
		int y = (int)robot.getY();
		int sx = 400 + x - panX;
		int sy = 300 + y - panY;
		
		AffineTransform t = g.getTransform();
		g.rotate(robot.getFacing(), sx + 16, sy + 16);
		g.drawImage(bot, sx, sy, null);
		
		g.setTransform(t);
	}
	
	protected void drawItem(Graphics g, Item item) {
		if (item instanceof Item.Battery) {
			drawItemBattery(g, (Item.Battery)item);
		} else if (item instanceof Item.Gold) {
			drawItemGold(g, (Item.Gold)item);
		}
	}
	
	protected void drawItemBattery(Graphics g, Item.Battery battery) {
		
	}
	
	protected void drawItemGold(Graphics g, Item.Gold gold) {
		int x = 400 + (int)gold.getX() - panX;
		int y = 300 + (int)gold.getY() - panY;
		drawVariant(g, coins, x, y, gold.getVariant());
	}
	
	protected void drawVariant(Graphics g, Image[] imgs, int x, int y, int variant) {
		g.drawImage(imgs[variant], x, y, null);
	}

	public static void sleep(int ms) {
		if (ms <= 0) {
			return;
		}
		
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			
		}
	}
	
	protected Image createBackBuffer() {
		Image img = frame.createVolatileImage(800, 600);
		
		if (img == null) {
			return null;
		}
		
		Graphics g = img.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 600);
		
		return img;
	}
	
	protected void redraw() {
		if (backBuffer != null) {
			synchronized (backBuffer) {
				draw();
			}
			
			frame.repaint();
		}
	}
	
	public Image getBackBuffer() {
		if (backBuffer == null) {
			backBuffer = createBackBuffer();
		}
		
		return backBuffer;
	}
	
	public void quit() {
		System.exit(0);
	}
}
