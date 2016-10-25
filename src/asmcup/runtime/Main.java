package asmcup.runtime;

import java.io.*;
import java.util.Base64;

import asmcup.vm.VM;

public class Main implements Recorder {
	protected World world;
	protected int frames;
	protected boolean recording;
	
	public static void main(String[] args) throws IOException {
		Main main = new Main();
		
		if (args.length > 1) {
			System.err.printf("USAGE: asmcup-runtime [file]\n");
			System.exit(1);
			return;
		}
		
		InputStream input;
		
		if (args.length > 0) {
			input = new FileInputStream(new File(args[0]));
		} else {
			input = System.in;
		}
		
		main.configure(input);
		main.run();
	}
	
	public void configure(InputStream input) throws IOException {
		InputStreamReader streamReader = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(streamReader);
		String line = "";
		
		while ((line = reader.readLine()) != null) {
			configure(line);
		}
		
		reader.close();
	}
	
	public void configure(String line) {
		String command = "";
		String[] parts = {};
		
		line = line.trim();
		
		if (line.isEmpty()) {
			return;
		}
		
		parts = line.split("[\\s\\t]+");
		
		if (parts.length <= 0) {
			return;
		}
		
		switch (command = parts[0].toLowerCase()) {
		case "frames":
			setFrames(parts[1]);
			break;
		case "seed":
			setSeed(parts[1]);
			break;
		case "bot":
			addBot(parts[1], parts[2]);
			break;
		case "record":
			setRecording(parts[1]);
			break;
		default:
			throw new IllegalArgumentException("Unknown command " + command);
		}
	}
	
	public void setFrames(String count) {
		frames = Integer.parseInt(count);
	}
	
	public void setSeed(String seed) {
		world = new World(Integer.parseInt(seed));
	}
	
	public void setRecording(String enabled) {
		recording = Integer.parseInt(enabled) > 0;
	}
	
	public void addBot(String id, String encoded) {
		addBot(Integer.parseInt(id), encoded);
	}
	
	public void addBot(int id, String encoded) {
		byte[] rom = Base64.getDecoder().decode(encoded.trim());
		addBot(id, rom);
	}
	
	public void addBot(int id, byte[] rom) {
		Robot robot;
		
		if (recording) {
			robot = new RecordedRobot(this, id, rom);
		} else {
			robot = new Robot(id, new VM(rom));
		}
		
		world.addRobot(robot);
	}
	
	public void run() {
		for (int i=0; i < frames; i++) {
			world.tick();
		}
	}
	
	public void record(RecordedRobot robot, byte[] data) {
		String encoded = Base64.getEncoder().encodeToString(data);
		System.out.printf("io %d %d %s\n", world.getFrame(), robot.id, encoded);
	}
}
