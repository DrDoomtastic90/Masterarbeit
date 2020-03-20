package dBConnections;

import java.sql.SQLException;

public class ANNDBConnection extends SQLiteConnection{
	private  static ANNDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ANN\\ANN.db";
	
	private ANNDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static ANNDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new ANNDBConnection(passPhrase);
		}
		return singleton;
	}
	public static ANNDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
