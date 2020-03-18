package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import dBConnections.KalmanDBConnection;
import dBConnections.DBConnection;

public class KalmanDAO {
	DBConnection dbConnection = null;
	
	public KalmanDAO() {
		dbConnection = KalmanDBConnection.getInstance();
	}
	public JSONObject getModel(String username, String inputAggr, String forecastAggr) throws SQLException {	
		
		JSONObject model = new JSONObject();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		try {
			statement = connection.createStatement();
			String sql = "SELECT model from Models where Username='" + username+ "' and InputAggregation='" + inputAggr + "' and ForecastAggregation='" + forecastAggr + "'";
			resultSet = statement.executeQuery(sql);
			
			while (resultSet.next()) {
				model= new JSONObject(resultSet.getString(1));

			}	
			return model;
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
	}
	
	public void storeModel(JSONObject model, String username, String inputAggr, String forecastAggr, String modelID) throws SQLException {		
		 String sql = "INSERT OR REPLACE INTO Models(model, username, inputAggregation, ForecastAggregation, ModelID) VALUES(?,?,?,?,?)"; 
	        try (Connection connection = dbConnection.checkConnectivity(); 
	        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setString(1, model.toString());
	            pstmt.setString(2, username);
	            pstmt.setString(3, inputAggr);
	            pstmt.setString(4, forecastAggr);
	            pstmt.setString(5, modelID);
	            pstmt.executeUpdate();
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }
}
