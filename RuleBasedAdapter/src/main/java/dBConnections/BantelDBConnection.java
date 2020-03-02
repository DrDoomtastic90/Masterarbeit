package dBConnections;


import java.sql.SQLException;


public class BantelDBConnection extends SQLiteConnection{
	private  static BantelDBConnection singleton;
	
	private BantelDBConnection(String passPhrase, String dbLocation) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static BantelDBConnection getInstance(String dbLocation, String passPhrase) throws ClassNotFoundException {
		if (singleton==null) {
			singleton = new BantelDBConnection(passPhrase, dbLocation);
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

/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;





public class ConnectionBantelDB {
	private static ConnectionBantelDB singleton = null;
	private Connection connection = null;
	private String sourcePath = null;
	private String pw = null;
	
	private ConnectionBantelDB(String sourcePath, String pw) {	
		try {	 
			Class.forName("org.sqlite.JDBC");
			this.sourcePath = sourcePath;
			this.pw = pw;
		}
		catch(ClassNotFoundException cnfex) {
			System.out.println("Problem in loading or registering SQLITE driver");
			cnfex.printStackTrace();
		}
	}
	
	public Connection checkConnectivity(){
		if(connection==null) {
			connect();
		}
		return connection;
	}

	
	private void connect() {
	    try {
	    	 //String sQLiteDB = "/home/matthiasb90/public_html/Masterarbeit/Daten/Bantel/ARIMA/AdapterImplementation/Bantel.db";
	    	//String connUrl = "jdbc:sqlite:"+sourcePath;
	    	String connUrl = "jdbc:sqlite:file:" + sourcePath +"?cipher=sqlcipher&key=" + pw;
	    	//file:D:/Arbeit/Bantel/Masterarbeit/Datenbank/Authentication.db?cipher=sqlcipher&legacy=1&kdf_iter=4000&key=mykey
	    	//connection = DriverManager.getConnection(connUrl,"Bantel","hallo");
	    	this.connection = DriverManager.getConnection(connUrl);
	    	
	    	
				//statement.execute("PRAGMA rekey ='YOLO'");
			
				//resultSet = statement.executeQuery("SELECT * from person;");
				//int colCount = resultSet.getMetaData().getColumnCount();
				//for (int i = 1; i <= colCount; ++i) {
				  //  System.out.println("ENTRY: " + resultSet.getString(i)); // Or even rs.getObject()
				//}
	    	//System.out.println("Connection to SQLite has been established.");
	    } 
	    catch (SQLException e) {
	            System.out.println(e.getMessage());
	    }
	}
	
	
	
	public void close() {
		if(connection!=null)
			try {
				connection.close();
				connection=null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		singleton = null;
	}

	public static ConnectionBantelDB getInstance(String sourcePath, String pw) {
		if (singleton==null) {
			singleton = new ConnectionBantelDB(sourcePath, pw);
		}
		return singleton;
	}
}
*/