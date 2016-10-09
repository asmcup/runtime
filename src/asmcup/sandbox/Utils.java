package asmcup.sandbox;

import java.io.*;
import java.nio.file.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Utils {
	public static String readAsString(String path) throws IOException {
		return readAsString(new File(path));
	}
	
	public static String readAsString(File file) throws IOException {
		return readAsString(file.toPath());
	}
	
	public static String readAsString(Path path) throws IOException {
		return new String(Files.readAllBytes(path));
	}
	
	public static String readAsString(JFrame frame, String ext, String desc) throws IOException {
		File file = findFileOpen(frame, ext, desc);
		
		if (file == null) {
			return null;
		}
		
		return readAsString(file);
	}
	
	public static void write(Path path, String text) throws IOException {
		Files.write(path, text.getBytes("ASCII"));
	}
	
	public static void write(File file, String text) throws IOException {
		write(file.toPath(), text);
	}
	
	public static void write(JFrame frame, String ext, String desc, String text) throws IOException {
		File file = findFileSave(frame, ext, desc);
		
		if (file == null) {
			return;
		}
		
		write(file, text);
	}
	
	public static File findFileOpen(JFrame frame, String ext, String desc) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));
		
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		
		return chooser.getSelectedFile();
	}
	
	public static File findFileSave(JFrame frame, String ext, String desc) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));
		
		if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		
		return chooser.getSelectedFile();
	}
}
