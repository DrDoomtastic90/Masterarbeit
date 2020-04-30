package dBConnections;

import java.sql.SQLException;

public class GatewayServiceDBConnection extends SQLiteConnection{
	private  static GatewayServiceDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\GatewayService\\GatewayService.db";
	
	private GatewayServiceDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static GatewayServiceDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new GatewayServiceDBConnection(passPhrase);
		}
		return singleton;
	}
	public static GatewayServiceDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
