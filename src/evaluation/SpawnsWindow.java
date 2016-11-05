package evaluation;

import javax.swing.JFrame;

import asmcup.sandbox.FrontPanel;

public class SpawnsWindow extends JFrame {
	protected final Spawns spawns;
	protected final FrontPanel panel = new FrontPanel();

	public SpawnsWindow(Spawns spawns) {
		this.spawns = spawns;
	}
}
