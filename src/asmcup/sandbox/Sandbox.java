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
	protected Debugger debugger;
	
	protected Image backBuffer;
	protected SandboxWorld world;
	protected Robot robot;
	protected int panX, panY;
	protected boolean paused;
	protected Image[] ground, wall, obstacles, hazards, coins;
	protected Image bot;
	protected boolean showGrid;
	
	public Sandbox() throws IOException {
		panX = (int)(Math.random() * World.SIZE);
		panY = (int)(Math.random() * World.SIZE);
		world = new SandboxWorld();
		robot = new Robot(1);
		robot.position(panX, panY);
		world.addRobot(robot);
		paused = false;
		
		mouse = new Mouse(this);
		menu = new Menu(this);
		canvas = new Canvas(this);
		frame = new Frame(this);
		codeEditor = new CodeEditor(this);
		debugger = new Debugger(this);
		
		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);
		
		ground = loadImage("/ground.png");
		wall = loadImage("/wall.png");
		obstacles = loadImage("/obstacles.png");
		hazards = loadImage("/hazards.png");
		bot = ImageIO.read(getClass().getResource("/robot.png"));
		coins = loadImage("/gold.png");
	}
	
	public int getPanX() {
		return panX;
	}
	
	public int getPanY() {
		return panY;
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
	
	public Debugger getDebugger() {
		return debugger;
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
	
	public void teleport(float canvasX, float canvasY) {
		float x = panX + canvasX - WIDTH/2;
		float y = panY + canvasY - HEIGHT/2;
		robot.position(x, y);
	}
	
	protected Image[] loadImage(String path) throws IOException {
		URL url = getClass().getResource(path);
		Image sheet = ImageIO.read(url);
		Image[] variants = new Image[4];
		
		for (int i=0; i < 4; i++) {
			Image img = new BufferedImage(World.TILE_SIZE, World.TILE_SIZE,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.drawImage(sheet, -i * World.TILE_SIZE, 0, null);
			variants[i] = img;
		}
		
		return variants;
	}
	
	public void run() {
		long lastTick;
		
		frame.setVisible(true);
		
		while (frame.isVisible()) {
			lastTick = System.currentTimeMillis();
			
			if (!paused) {
				tick();
			}
			
			tickWait(lastTick);
		}
	}
	
	protected void tick() {
		synchronized (world) {
			world.tick();
			debugger.updateDebugger();
			redraw();
		}
	}
	
	protected void tickWait(long lastTick) {
		long now = System.currentTimeMillis();
		long span = now - lastTick;
		int msPerFrame = 1000 / FRAMERATE;
		int wait = (int)(msPerFrame - span);
		sleep(wait);
	}
	
	public void pauseResume()
	{
		paused = !paused;
		redraw();
	}
	
	public void singleTick()
	{
		if (paused) {
			tick();
		}
	}
	
	public void reseed() {
		world = new SandboxWorld();
		world.addRobot(robot);
		redraw();
	}
	
	public void centerView() {
		panX = (int)robot.getX();
		panY = (int)robot.getY();
		redraw();
	}
	
	public void draw() {
		if (backBuffer == null) {
			return;
		}
		
		Graphics g = backBuffer.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		int left = (int)Math.floor((panX - WIDTH/2.0) / World.CELL_SIZE);
		int right = (int)Math.ceil((panX + WIDTH/2.0) / World.CELL_SIZE);
		int top = (int)Math.floor((panY - HEIGHT/2.0) / World.CELL_SIZE);
		int bottom = (int)Math.ceil((panY + HEIGHT/2.0) / World.CELL_SIZE);
		
		left = Math.max(0, left);
		right = Math.max(0, right);
		top = Math.max(0, top);
		bottom = Math.max(0, bottom);
		
		for (int cellY=top; cellY < bottom; cellY++) {
			for (int cellX=left; cellX < right; cellX++) {
				drawCell(g, world.getCell(cellX, cellY));
			}
		}
		
		for (Robot robot : world.getRobots()) {
			drawRobot(g, robot);
		}
		
		g.setColor(Color.BLUE);
		
		for (SandboxWorld.Ray ray : world.getRays()) {
			drawRay(g, ray);
		}
		
		if (paused) {
			g.setColor(Color.WHITE);
			g.drawString("PAUSED", 25, 50);
		}
	}
	
	protected void drawRay(Graphics g, SandboxWorld.Ray ray) {
		int x1 = screenX(ray.x);
		int y1 = screenY(ray.y);
		int x2 = screenX(ray.x + (float)Math.cos(ray.theta) * ray.d);
		int y2 = screenY(ray.y + (float)Math.sin(ray.theta) * ray.d);
		g.drawLine(x1, y1, x2, y2);
	}
	
	public int screenX(float x) {
		return (int)(WIDTH/2 + x - panX);
	}
	
	public int screenY(float y) {
		return (int)(HEIGHT/2 + y - panY);
	}
	
	protected void drawCell(Graphics g, Cell cell) {
		int left = cell.getX() * World.TILES_PER_CELL;
		int right = left + World.TILES_PER_CELL;
		int top = cell.getY() * World.TILES_PER_CELL;
		int bottom = top + World.TILES_PER_CELL;
		
		for (int row=top; row < bottom; row++) {
			for (int col=left; col < right; col++) {
				int tile = cell.getTile(col - left, row - top);
				drawTile(g, col, row, tile);
			}
		}
		
		for (Item item : cell.getItems()) {
			drawItem(g, item);
		}
		
		if (showGrid) {
			g.setColor(Color.WHITE);
			int x = WIDTH/2 + left * World.TILE_SIZE - panX;
			int y = HEIGHT/2 + top * World.TILE_SIZE - panY;
			g.drawRect(x, y, World.CELL_SIZE, World.CELL_SIZE);
			
			String msg = String.format("%d, %d", cell.getX(), cell.getY());
			g.drawString(msg, x + 100, y + 100);
			
			msg = String.format("%x", cell.getKey());
			g.drawString(msg, x + 100, y + 150);
		}
	}
	
	protected void drawTile(Graphics g, int col, int row, int tile) {
		int x = WIDTH/2 + col * World.TILE_SIZE - panX;
		int y = HEIGHT/2 + row * World.TILE_SIZE - panY;
		int variant = (tile >> 2) & 0b11;
		
		switch (tile & 0b11) {
		case Cell.TILE_GROUND:
			drawVariant(g, ground, x, y, variant);
			break;
		case Cell.TILE_OBSTACLE:
			drawVariant(g, ground, x, y, variant ^ 0b11);
			drawVariant(g, obstacles, x, y, variant);
			break;
		case Cell.TILE_WALL:
			drawVariant(g, wall, x, y, variant);
			break;
		case Cell.TILE_HAZARD:
			drawVariant(g, hazards, x, y, variant);
			break;
		}
	}
	
	protected void drawRobot(Graphics lg, Robot robot) {
		Graphics2D g = (Graphics2D)lg;
		int x = (int)robot.getX();
		int y = (int)robot.getY();
		int sx = WIDTH/2 + x - panX;
		int sy = HEIGHT/2 + y - panY;
		
		AffineTransform t = g.getTransform();
		g.rotate(robot.getFacing(), sx, sy);
		g.drawImage(bot, sx - World.TILE_SIZE/2, sy - World.TILE_SIZE/2, null);
		
		g.setTransform(t);
		
		g.setColor(Color.RED);
		g.fillRect(sx-1, sy-1, 3, 3);
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
		int x = WIDTH/2 + (int)gold.getX() - panX;
		int y = HEIGHT/2 + (int)gold.getY() - panY;
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
		Image img = frame.createVolatileImage(WIDTH, HEIGHT);
		
		if (img == null) {
			return null;
		}
		
		Graphics g = img.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
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
	
	public void toggleGrid() {
		showGrid = !showGrid;
	}

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int FRAMERATE = 10;
}
