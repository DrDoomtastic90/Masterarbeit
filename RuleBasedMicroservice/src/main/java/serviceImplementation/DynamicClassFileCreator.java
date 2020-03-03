package serviceImplementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.json.JSONObject;

public class DynamicClassFileCreator {

	private static String createClasstemplateWithAttributes(String classname, JSONObject fields) {
		StringBuilder constructor = new StringBuilder();
		StringBuilder classTemplate = new StringBuilder();

		classTemplate.append("package serviceImplementation;\n");

		classTemplate.append("import java.util.LinkedHashMap;\n");
		classTemplate.append("import java.util.Map;\n");
		classTemplate.append("import java.util.List;\n");

		classTemplate.append("public class " + classname + "{\n");
		classTemplate.append("private String name;\n");

		constructor.append("public " + classname + "(");
		constructor.append("String name");

		for (String fieldName : fields.keySet()) {
			String type = fields.getJSONObject(fieldName).getString("type");
			classTemplate.append("private " + type + " " + fieldName + ";\n");
			constructor.append(", " + type + " " + fieldName);
		}
		constructor.append("){\n\n");

		classTemplate.append("public " + classname + "(){}\n\n");
		classTemplate.append(constructor.toString());
		classTemplate.append("this.name = name;\n");
		for (String fieldName : fields.keySet()) {
			//String type = fields.getJSONObject(fieldName).getString("type");
			classTemplate.append("this." + fieldName + " = " + fieldName + ";\n");
		}
		classTemplate.append("}\n");

		classTemplate.append("public String getName() {\n");
		classTemplate.append("return name;\n");
		classTemplate.append("}\n");

		classTemplate.append("public void setName(String name) {\n");
		classTemplate.append("this.name = name;\n");
		classTemplate.append("}\n");

		for (String fieldName : fields.keySet()) {
			String type = fields.getJSONObject(fieldName).getString("type");
			// from https://stackoverflow.com/questions/3904579/how-to-capitalize-the-first-letter-of-a-string-in-java/47225370
			classTemplate.append("public " + type + " get" + fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1) + "() {\n");
			classTemplate.append("return " + fieldName + ";\n");
			classTemplate.append("}\n");
			
			// from https://stackoverflow.com/questions/3904579/how-to-capitalize-the-first-letter-of-a-string-in-java/47225370
			classTemplate.append("public void set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
					+ "(" + type + " " + fieldName + ") {\n");
			classTemplate.append("this." + fieldName + " = " + fieldName + ";\n");
			classTemplate.append("}\n");
		}

		classTemplate.append("}\n");
		return classTemplate.toString();
	}

	private static String createClasstemplate(String classname) {

		StringBuilder classTemplate = new StringBuilder();
		classTemplate.append("package analytics.ruleBased;\n");

		classTemplate.append("import java.util.LinkedHashMap;\n");
		classTemplate.append("import java.util.Map;\n");

		classTemplate.append("public class " + classname + "{\n");
		classTemplate.append("private Map<String, Object> fields;\n");

		classTemplate.append("public " + classname + "(Map<String, Object> fields){\n");
		classTemplate.append("this.fields = fields;\n");
		classTemplate.append("}\n");

		classTemplate.append("public Map<String, Object> getFields() {\n");
		classTemplate.append("return fields;\n");
		classTemplate.append("}\n");

		classTemplate.append("public void setFields(Map<String, Object> fields) {\n");
		classTemplate.append("this.fields = fields;\n");
		classTemplate.append("}\n");
		classTemplate.append("}\n");
		return classTemplate.toString();
	}

	private static File createClassFile(String classname, String pathString, JSONObject fields) {
		File sourceFile = null;
		try {
			sourceFile = new File(pathString, classname + ".java");
			sourceFile.deleteOnExit();
			FileWriter writer = new FileWriter(sourceFile);
			String sourceCode = createClasstemplateWithAttributes(classname, fields);
			writer.write(sourceCode);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourceFile;

	}

	private static void compileFile(File sourceFile) throws IOException {
		// compile the source file
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		File parentDirectory = sourceFile.getParentFile();
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(parentDirectory));
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
		compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
		fileManager.close();
	}

	/*
	 * public static Class<?> createDynamicJavaClass(String classname) throws
	 * IOException, ClassNotFoundException { //adapted from
	 * https://www.quora.com/How-can-I-dynamically-create-a-Java-class-and-execute-
	 * its-methods-at-runtime String pathString =
	 * "D:/Arbeit/Bantel/Masterarbeit/Daten/preProcessed/ruleBased/"; File
	 * sourceFile = createClassFile(classname, pathString); compileFile(sourceFile);
	 * //String packagePath =
	 * DynamicClassCreator.class.getPackageName().replace(".", "/");
	 * //System.out.println(packagePath); //File parentDirectory = new
	 * File(pathString + packagePath); File parentDirectory
	 * =sourceFile.getParentFile(); URLClassLoader classLoader =
	 * URLClassLoader.newInstance(new URL[] { parentDirectory.toURI().toURL() });
	 * String classPath = DynamicClassCreator.class.getPackageName()+"." +
	 * classname; System.out.println(classPath); Class<?> dynamicClass =
	 * classLoader.loadClass(classPath); return dynamicClass; }
	 */

	public static File createDynamicJavaClass(String classname, String pathString, JSONObject fields)
			throws IOException, ClassNotFoundException {
		// adapted from https://www.quora.com/How-can-I-dynamically-create-a-Java-class-and-execute-its-methods-at-runtime
		File sourceFile = createClassFile(classname, pathString, fields);
		compileFile(sourceFile);
		File parentDirectory = sourceFile.getParentFile();
		return parentDirectory;
	}

	public static URLClassLoader getDynamicClassLoader(File dynamicClassDirectory) throws MalformedURLException {
		URL[] urls = new URL[] { dynamicClassDirectory.toURI().toURL() };
		URLClassLoader classLoader = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
		return classLoader;
	}

	public static Class<?> loadDynamicJavaClass(URLClassLoader classLoader, String classname)
			throws ClassNotFoundException {
		String classPath = DynamicClassFileCreator.class.getPackageName() + "." + classname;
		Class<?> dynamicClass = classLoader.loadClass(classPath);
		return dynamicClass;
	}

}
