package asmcup.runtime;

import java.io.*;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err.printf("USAGE: asmcup-runtime <snapshot> <events> <frames> <output>\n");
			System.exit(1);
			return;
		}
		
		DataInputStream snapshotInput = read(args[0]);
		DataInputStream eventInput = read(args[1]);
		int frames = Integer.parseInt(args[2]);
		FileOutputStream fileOutput = new FileOutputStream(args[3]);
		DataOutputStream dataOutput = new DataOutputStream(fileOutput);
		World world = new World(snapshotInput);
		
		while (frames > 0) {
			world.tick();
			frames++;
		}
		
		world.save(dataOutput);
	}
	
	protected static DataInputStream read(String path) throws IOException {
		return new DataInputStream(new FileInputStream(path));
	}
	
	protected static DataOutputStream write(String path) throws IOException {
		return new DataOutputStream(new FileOutputStream(path));
	}
}
