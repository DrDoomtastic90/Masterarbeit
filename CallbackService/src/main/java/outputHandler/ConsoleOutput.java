package outputHandler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.drools.core.spi.KnowledgeHelper;

public class ConsoleOutput {
	static PrintStream out = null;
	
	public static void setOutputStreamToFile(/*final String message*/) {
		if(out == null) {
			try {
				out = new PrintStream(new FileOutputStream("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\log.txt"));
				System.setOut(out);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println(message);
		}
	public static void helper(final KnowledgeHelper drools){
		      System.out.println("\nrule triggered: " + drools.getRule().getName());
	}
}
