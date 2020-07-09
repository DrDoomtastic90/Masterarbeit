package dBConnections;

import java.sql.SQLException;

public class KalmanDBConnection extends SQLiteConnection{
	private  static KalmanDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Kalman\\Kalman.db";
	
	private KalmanDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static KalmanDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new KalmanDBConnection(passPhrase);
		}
		return singleton;
	}
	public static KalmanDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
