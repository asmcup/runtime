package asmcup.sandbox;

import java.io.IOException;

import javax.swing.UIManager;

public class Main {
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Don't care if theme can't be set
		}
		
		Sandbox sandbox = new Sandbox();
		sandbox.run();
	}
}
