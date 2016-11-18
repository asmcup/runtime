package asmcup.sandbox;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

import asmcup.evaluation.EvaluatorWindow;
import asmcup.evaluation.Spawns;
import asmcup.evaluation.SpawnsWindow;
import asmcup.genetics.*;
import asmcup.runtime.*;
import asmcup.runtime.Robot;
import asmcup.runtime.TILE;

public class Sandbox {
	public final Mouse mouse;
	public final Menu menu;
	public final Canvas canvas;
	public final JFrame frame;
	public final LoadWorldDialog loadWorld;
	public final CodeEditor codeEditor;
	public final Debugger debugger;
	public final EvaluatorWindow evaluator;
	public final Genetics genetics;
	public final Spawns spawns;
	public final SpawnsWindow spawnsWindow;
	protected Image backBuffer;
	protected World world;
	protected Robot robot;
	protected int panX, panY;
	protected boolean paused = false;
	protected float frameRate = DEFAULT_FRAMERATE;
	protected Image[] ground, wall, obstacles, hazards, floor;
	protected Image[] coins, batteryImg;
	protected Image bot;
	protected boolean showGrid;
	protected boolean lockCenter;
	protected byte[] rom = new byte[256];
	
	public Sandbox() throws IOException {
		reseed();
		
		// Core components
		mouse = new Mouse(this);
		canvas = new Canvas(this);
		frame = new JFrame("Sandbox");
		
		// Modules
		loadWorld = new LoadWorldDialog(this);
		codeEditor = new CodeEditor(this);
		debugger = new Debugger(this);
		spawns = new Spawns(this);
		spawnsWindow = new SpawnsWindow(this);
		
		evaluator = new EvaluatorWindow(this);
		genetics = new Genetics(this);
		
		// Menu gets built last
		menu = new Menu(this);
		
		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setJMenuBar(menu);
		frame.setContentPane(canvas);
		frame.pack();
		
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
	
	public World getWorld() {
		return world;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public byte[] getROM() {
		return rom;
	}
	
	public void setROM(byte[] rom) {
		if (rom.length != 256) {
			throw new IllegalArgumentException("ROM must be 256 bytes");
		}
		
		this.rom = rom;
	}
	
	public void flash() {
		synchronized (world) {
			Robot old = robot;
			world.removeRobot(old);
			
			robot = new Robot(1, rom);
			robot.position(old.getX(), old.getY());
			robot.setFacing(old.getFacing());
			world.addRobot(robot);
		}
	}
	
	public void loadROM(byte[] rom) {
		setROM(rom);
		flash();
	}
	
	public void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg);
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
			if (lockCenter) {
				centerView();
			}
			
			world.tick();
			debugger.updateDebugger();
			redraw();
		}
	}
	
	protected void tickWait(long lastTick) {
		long now = System.currentTimeMillis();
		long span = now - lastTick;
		int msPerFrame = Math.round(1000 / frameRate);
		int wait = (int)(msPerFrame - span);
		sleep(wait);
	}
	
	public void togglePaused()
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

	public void setFramerate(float f) {
		frameRate = f;
	}
	
	public void reseed() {
		world = new World();
		
		if (robot == null) {
			robot = new Robot(1);
		}
		
		world.randomizePosition(robot);
		centerView();
		world.addRobot(robot);
		redraw();
	}

	public void resetWorld() {
		resetWorld(world.getSeed());
	}
	
	public void resetWorld(int seed) {
		synchronized (this) {
			world = new World(seed);
			world.addRobot(robot);
		}
		
		redraw();
	}
	
	public void loadSpawn(Spawn spawn) {
		robot.position(spawn.x, spawn.y);
		robot.setFacing(spawn.facing);
		resetWorld(spawn.seed);
		centerView();
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
		
		g.setColor(Color.PINK);
		
		for (Spawn spawn : spawns.getIterable()) {
			int x = screenX(spawn.x);
			int y = screenY(spawn.y);
			
			g.drawLine(x - 4, y, x + 4, y);
			g.drawLine(x, y - 4, x, y + 4);
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
				int tile = world.getTile(col,  row);
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
		case TILE.GROUND:
			drawVariant(g, ground, x, y, variant);
			break;
		case TILE.OBSTACLE:
			drawVariant(g, ground, x, y, variant ^ 0b11);
			drawVariant(g, obstacles, x, y, variant);
			break;
		case TILE.WALL:
			drawVariant(g, wall, x, y, variant);
			break;
		case TILE.HAZARD:
			drawVariant(g, hazards, x, y, variant);
			break;
		case TILE.FLOOR:
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
		g.setTransform(t);
		
		g.rotate(robot.getBeamAngle(), sx, sy);
		
		if (robot.getLazerEnd() > 0) {
			int w = (int)(robot.getLazerEnd());
			g.setColor(Color.RED);
			g.drawLine(sx, sy, sx + w, sy);
		}
		
		if (world.getFrame() - robot.getSensorFrame() < 3) {
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
	
	public void redraw() {
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
		redraw();
	}

	public void toggleLockCenter() {
		lockCenter = !lockCenter;
	}

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int DEFAULT_FRAMERATE = 10;
}
