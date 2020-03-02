package dBConnections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


import org.sqlite.SQLiteConfig;


public class SQLiteConnection implements DBConnection{
	private  static SQLiteConnection singleton;
	private  Connection connection;
	private String passPhrase;
	private String dbLocation;
	
	protected SQLiteConnection(String passPhrase, String dbLocation) throws ClassNotFoundException {	 
			Class.forName("org.sqlite.JDBC");
				this.passPhrase = passPhrase;
				this.dbLocation = dbLocation;
	}
	
	private void connect() throws SQLException {
	    	String connURL;
	    	if(passPhrase != null && passPhrase.length() > 0) {
	    		connURL = "jdbc:sqlite:file:" + dbLocation +"?cipher=sqlcipher&legacy=4&kdf_iter=256000&key=" + passPhrase;
	    	}else {
	    		connURL = "jdbc:sqlite:file:" + dbLocation;
	    	}
	    	SQLiteConfig config = new SQLiteConfig();
			config.enforceForeignKeys(true);
	    	this.connection = DriverManager.getConnection(connURL, config.toProperties()); 
	}
	
	
	public static DBConnection getInstance(String passPhrase, String dbLocation) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new SQLiteConnection(passPhrase, dbLocation);
		}
		return singleton;
	}
	public static DBConnection getInstance() {
		return singleton;
	}
	
	public Connection checkConnectivity() throws SQLException{
		if(connection==null ||connection.isClosed()) {
			connect();
		}
		return this.connection;
	}
		
	public void close() throws SQLException {
		if(connection!=null)
			connection.close();
		connection=null;
		singleton = null;
	}
}
