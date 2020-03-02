package dBConnections;

import java.sql.Connection;
import java.sql.SQLException;



public abstract class DAO {
	private Connection connection;

	public DAO(String sourcePath, String pw) throws ClassNotFoundException, SQLException {
		this.connection = BantelDBConnection.getInstance(sourcePath, pw).checkConnectivity();
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
