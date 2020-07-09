package dBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnection {
	public Connection checkConnectivity() throws SQLException;
	public void close() throws SQLException;
}
