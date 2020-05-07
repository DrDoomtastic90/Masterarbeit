package outputHandler;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

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
	
	public static File createTempFile(String content) {
			File tempFile = null;
			BufferedWriter writer = null;
			try {
				tempFile = File.createTempFile("temporaryDRLFILE", ".tmp");
				writer = new BufferedWriter(new FileWriter(tempFile));
				writer.write(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try{
					if(writer != null) writer.close();
				}catch(Exception ex){}
			}
			return tempFile;
		}
}
