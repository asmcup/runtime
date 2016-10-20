package asmcup.sandbox;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import asmcup.runtime.Cell;
import asmcup.runtime.Item;
import asmcup.runtime.Robot;
import asmcup.runtime.World;

public class Sandbox {
	protected Mouse mouse;
	protected Menu menu;
	protected Canvas canvas;
	protected Frame frame;
	protected CodeEditor codeEditor;
	protected Debugger debugger;
	protected Image backBuffer;
	protected World world;
	protected Robot robot;
	protected int panX, panY;
	protected boolean paused;
	protected Image[] ground, wall, obstacles, hazards, floor;
	protected Image[] coins, batteryImg;
	protected Image bot;
	protected boolean showGrid;
	
	public Sandbox() throws IOException {
		reseed();
		
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
		floor = loadImage("/floor.png");
		batteryImg = loadImage("/battery.png");
		
		frame.setIconImage(bot);
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
		world = new World();
		
		do {
			panX = (int)(Math.random() * World.SIZE);
			panY = (int)(Math.random() * World.SIZE);
		} while (world.isSolid(panX, panY, 32));
		
		if (robot == null) {
			robot = new Robot(1);
		}
		
		world.addRobot(robot);
		robot.position(panX, panY);
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
		
		if (paused) {
			g.setColor(Color.WHITE);
			g.drawString("PAUSED", 25, 50);
		}
	}
	
	public int screenX(float x) {
		return (int)(WIDTH/2 + x - panX);
	}
	
	public int screenY(float y) {
		return (int)(HEIGHT/2 + y - panY);
	}
	
	public int screenX(int x) {
		return (WIDTH/2 + x - panX);
	}
	
	public int screenY(int y) {
		return (HEIGHT/2 + y - panY);
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
		int variant = (tile >> 3) & 0b11;
		
		switch (tile & 0b111) {
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
		case Cell.TILE_FLOOR:
			drawVariant(g, floor, x, y, variant);
			break;
		}
	}
	
	protected void drawRobot(Graphics lg, Robot robot) {
		Graphics2D g = (Graphics2D)lg;
		int sx = screenX(robot.getX());
		int sy = screenY(robot.getY());
		AffineTransform t = g.getTransform();
		
		g.rotate(robot.getFacing(), sx, sy);
		g.drawImage(bot, sx - World.TILE_HALF, sy - World.TILE_HALF, null);
		
		if (robot.getLazer() > 0) {
			int w = (int)(robot.getLazer() * Robot.LAZER_RANGE);
			g.setColor(Color.RED);
			g.drawLine(sx, sy, sx + w, sy);
		}
		
		if (robot.getSensor() > 0) {
			int w = (int)(robot.getSensor());
			g.setColor(Color.BLUE);
			g.drawLine(sx, sy, sx + w, sy);
		}
		
		g.setTransform(t);
		
		if (showGrid) {
			g.setColor(Color.RED);
			g.fillRect(sx - 1, sy - 1, 3, 3);
		}
	}
	
	protected void drawItem(Graphics g, Item item) {
		if (item instanceof Item.Battery) {
			drawItemBattery(g, (Item.Battery)item);
		} else if (item instanceof Item.Gold) {
			drawItemGold(g, (Item.Gold)item);
		}
		
		if (showGrid) {
			g.setColor(Color.RED);
			g.fillRect(screenX(item.getX()), screenY(item.getY()), 2, 2);
		}
	}
	
	protected void drawItemBattery(Graphics g, Item.Battery battery) {
		int x = screenX(battery.getX());
		int y = screenY(battery.getY());
		drawVariant(g, batteryImg, x - 16, y - 16, battery.getVariant());
	}
	
	protected void drawItemGold(Graphics g, Item.Gold gold) {
		int x = screenX(gold.getX());
		int y = screenY(gold.getY());
		drawVariant(g, coins, x - 16, y - 16, gold.getVariant());
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
