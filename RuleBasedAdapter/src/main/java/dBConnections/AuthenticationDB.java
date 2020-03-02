package dBConnections;

import java.sql.SQLException;

public class AuthenticationDB extends SQLiteConnection{
	private  static AuthenticationDB singleton;
	//private static String dbLocation = "I:/DataManager/Database/Authentication.db";
	private static String dbLocation = "D:/Arbeit/Bantel/Masterarbeit/Datenbank/Sqlite/Authentication.db";
	
	private AuthenticationDB(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static AuthenticationDB getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new AuthenticationDB(passPhrase);
		}
		return singleton;
	}
	public static AuthenticationDB getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
