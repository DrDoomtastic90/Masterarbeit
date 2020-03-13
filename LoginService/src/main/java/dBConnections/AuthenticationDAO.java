package dBConnections;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import dBConnections.AuthenticationDBConnection;
import dBConnections.DBConnection;

public class AuthenticationDAO {
	DBConnection authenticationConnection = null;
	
	public AuthenticationDAO() {
		authenticationConnection = AuthenticationDBConnection.getInstance();
	}
	public JSONObject authenticate(String username, String password) throws SQLException {	
		
		JSONObject credentials = new JSONObject();
		credentials.put("isAuthorized", false);
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = authenticationConnection.checkConnectivity();
		
		try {
			statement = connection.createStatement();
			String sql = "SELECT Username, Password, API_URL, isEnabledARIMA, isEnabledAI, isEnabledRuleBased, isEnabledKalman from LoginCredentials where Username='" + username+ "' and password='" + password + "'";
			resultSet = statement.executeQuery(sql);
			
			while (resultSet.next()) {
				credentials.put("username", resultSet.getString(1));
				credentials.put("password", resultSet.getString(2));
				credentials.put("apiURL", resultSet.getString(3));
				credentials.put("isEnabledARIMA", resultSet.getString(4));
				credentials.put("isEnabledAI", resultSet.getString(5));
				credentials.put("isEnabledRuleBased", resultSet.getString(6));
				credentials.put("isEnabledKalman", resultSet.getString(7));
				//credentials.put("erfassungProduktion", Boolean.toString(resultSet.getBoolean(7)));
				//credentials.put("erfassungVerpackung", Boolean.toString(resultSet.getBoolean(8)));
				//credentials.put("erfassungMilch", Boolean.toString(resultSet.getBoolean(9)));
				//credentials.put("stammdatenKonfiguration", Boolean.toString(resultSet.getBoolean(10)));
				//credentials.put("lesen", Boolean.toString(resultSet.getBoolean(11)));
				credentials.put("isAuthorized", true);
				
			}		
			if(credentials.has("username") && credentials.get("username").equals(username)) {
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
