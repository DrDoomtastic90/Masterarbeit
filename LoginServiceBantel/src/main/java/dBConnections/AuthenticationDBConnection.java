package dBConnections;

import java.sql.SQLException;

public class AuthenticationDBConnection extends SQLiteConnection{
	private  static AuthenticationDBConnection singleton;
	//private static String dbLocation = "I:/DataManager/Database/Authentication.db";
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Datenbanken\\Login.db";
	
	private AuthenticationDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static AuthenticationDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new AuthenticationDBConnection(passPhrase);
		}
		return singleton;
	}
	public static AuthenticationDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
