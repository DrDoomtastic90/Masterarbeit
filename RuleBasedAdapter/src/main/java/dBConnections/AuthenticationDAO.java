package dBConnections;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;


public class AuthenticationDAO {
	DBConnection authenticationConnection = null;
	
	public AuthenticationDAO() {
		authenticationConnection = AuthenticationDB.getInstance();
	}
	public Map<String, String> authenticate(String username, String password) throws SQLException {	
		Map<String, String> credentials = new LinkedHashMap<String, String>();
		Statement statement = null;
		ResultSet resultSet = null;
		String usernameDB = null;
		String passwordDB = null;
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement
					.executeQuery("SELECT Username, Password, API_URL, isEnabledARIMA, isEnabledAI, isEnabledRuleBased from LoginCredentials where Username='" + username
							+ "' and password='" + password + "'");
			while (resultSet.next()) {
				usernameDB = resultSet.getString(1);
				passwordDB = resultSet.getString(2);
				//credentials.put("username", resultSet.getString(1));
				//credentials.put("password", resultSet.getString(2));
				credentials.put("apiURL", resultSet.getString(3));
				credentials.put("isEnabledARIMA", resultSet.getString(4));
				credentials.put("isEnabledAI", resultSet.getString(5));
				credentials.put("isEnabledRuleBased", resultSet.getString(6));
			}
			if(usernameDB != null && passwordDB != null && usernameDB.equals(username) && passwordDB.equals(password)) {
				statement.executeQuery("SELECT SecretContent from Secrets where SecretBez = 'PassPhrase'");
				while (resultSet.next()) {
					credentials.put("passPhrase", resultSet.getString(1));
				}		
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	
		return credentials;
	}
}
