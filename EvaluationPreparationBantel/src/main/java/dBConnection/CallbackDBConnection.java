package dBConnection;

import java.sql.SQLException;

public class CallbackDBConnection extends SQLiteConnection{
	private  static CallbackDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Services\\CallbackService\\Callback.db";
	
	private CallbackDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static CallbackDBConnection getInstance(String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new CallbackDBConnection(passPhrase);
		}
		return singleton;
	}
	public static CallbackDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
