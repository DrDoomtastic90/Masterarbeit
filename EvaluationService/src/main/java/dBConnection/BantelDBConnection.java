package dBConnection;

import java.sql.SQLException;

public class BantelDBConnection extends SQLiteConnection{
	private  static BantelDBConnection singleton;
	//private static String dbLocation = "I:/DataManager/Database/Authentication.db";
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Datenbanken\\Bantel.db";
	
	private BantelDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static BantelDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new BantelDBConnection(passPhrase);
		}
		return singleton;
	}
	public static BantelDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
