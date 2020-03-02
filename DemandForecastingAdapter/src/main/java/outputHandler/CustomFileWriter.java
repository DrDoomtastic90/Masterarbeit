package outputHandler;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomFileWriter {
	public static void createJSON(String targetString, String jsonString) {
		// from https://stackoverflow.com/questions/2833853/create-whole-path-automatically-when-writing-to-a-new-file
		if (!targetString.contains(".json")) {
			targetString = targetString + ".json";
		}
		try {
			Path targetPath = Paths.get(targetString);
			Files.deleteIfExists(targetPath);
			Path targetFile = Files.createFile(targetPath);
			targetFile.toFile().getParentFile().mkdirs();
			try (PrintWriter writer = new PrintWriter(targetFile.toFile())) {
				writer.write(jsonString);
			} catch (IOException writteExc) {
				writteExc.printStackTrace();
			}
		} catch (IOException permissionException) {
			permissionException.printStackTrace();
		}
	}
	
	public static void createCSV(String targetString, String csvString) {
		// from https://stackoverflow.com/questions/2833853/create-whole-path-automatically-when-writing-to-a-new-file
		if (!targetString.contains(".csv")) {
			targetString = targetString + ".csv";
		}
		try {
			Path targetPath = Paths.get(targetString);
			Files.deleteIfExists(targetPath);
			Path targetFile = Files.createFile(targetPath);
			targetFile.toFile().getParentFile().mkdirs();
			try (PrintWriter writer = new PrintWriter(targetFile.toFile())) {
				writer.write(csvString);
			} catch (IOException writteExc) {
				writteExc.printStackTrace();
			}
		} catch (IOException permissionException) {
			permissionException.printStackTrace();
		}
	}
}
