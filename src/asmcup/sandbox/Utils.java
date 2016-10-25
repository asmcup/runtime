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
	
	public static String readAsString(InputStream input) throws IOException {
		String line;
		StringBuilder builder = new StringBuilder();
		InputStreamReader streamReader = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(streamReader);
		
		while ((line = reader.readLine()) != null) {
			builder.append(line + "\n");
		}
		
		return builder.toString();
	}
	
	public static byte[] readAsBytes(JFrame frame, String ext, String desc) throws IOException {
		File file = findFileOpen(frame, ext, desc);
		
		if (file == null) {
			return null;
		}
		
		return Files.readAllBytes(file.toPath());
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
	
	public static void write(JFrame frame, String ext, String desc, byte[] data) throws IOException {
		File file = findFileSave(frame, ext, desc);
		
		if (file == null) {
			return;
		}
		
		Files.write(file.toPath(), data);
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
		
		return getSelectedFileWithExtension(chooser);
	}
	
	/**
	 * Returns the selected file from a JFileChooser, including the extension from
	 * the file filter.
	 * From: http://stackoverflow.com/a/18984561
	 */
	public static File getSelectedFileWithExtension(JFileChooser c) {
	    File file = c.getSelectedFile();
	    if (c.getFileFilter() instanceof FileNameExtensionFilter) {
	        String[] exts = ((FileNameExtensionFilter)c.getFileFilter()).getExtensions();
	        String nameLower = file.getName().toLowerCase();
	        for (String ext : exts) { // check if it already has a valid extension
	            if (nameLower.endsWith('.' + ext.toLowerCase())) {
	                return file; // if yes, return as-is
	            }
	        }
	        // if not, append the first extension from the selected filter
	        file = new File(file.toString() + '.' + exts[0]);
	    }
	    return file;
	}
	
}
