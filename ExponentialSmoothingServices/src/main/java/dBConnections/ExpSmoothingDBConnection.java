package dBConnections;

import java.sql.SQLException;

public class ExpSmoothingDBConnection extends SQLiteConnection{
	private  static ExpSmoothingDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ExpSmoothing\\ExpSmoothing.db";
	
	private ExpSmoothingDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static ExpSmoothingDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new ExpSmoothingDBConnection(passPhrase);
		}
		return singleton;
	}
	public static ExpSmoothingDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
