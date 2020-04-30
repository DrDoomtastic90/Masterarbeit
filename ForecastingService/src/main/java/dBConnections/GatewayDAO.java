package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import dBConnections.GatewayServiceDBConnection;
import dBConnections.DBConnection;

public class GatewayDAO {
	DBConnection dbConnection = null;
	
	public GatewayDAO() {
		dbConnection = GatewayServiceDBConnection.getInstance();
	}
	
	public void writeWeightsToDB(JSONObject weights, String serviceNames, String forecastDate, String username){
		String sql = "INSERT OR REPLACE INTO ServiceWeights(ForecastDate, Weights, Services, Username) VALUES(?,?,?,?)"; 
		try (Connection connection = dbConnection.checkConnectivity(); 
	        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, forecastDate);
	        pstmt.setString(2, weights.toString());
	        pstmt.setString(3, serviceNames);
	        pstmt.setString(4, username);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
	    }
	}
	
	public void writeWeightCalculationValuesToDB(String username, String forecastDate, String serviceNames, JSONObject calculationValues){
		String sql = "INSERT OR REPLACE INTO CalculationValues(ForecastDate, CalculationValuesJSON, Services, Username) VALUES(?,?,?,?)"; 
		try (Connection connection = dbConnection.checkConnectivity(); 
	        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, forecastDate);
	        pstmt.setString(2, calculationValues.toString());
	        pstmt.setString(3, serviceNames);
	        pstmt.setString(4, username);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
	    }
	}
	
	public JSONObject getAveragedWeights(String toDateString, String serviceNames, String username) throws SQLException, ParseException {	
		JSONObject averagedWeights = new JSONObject();
		JSONObject weights;
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date toDate = dateFormat.parse(toDateString);
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(toDate);
		calendar.add(Calendar.DAY_OF_MONTH, - 14);
		Date fromDate = calendar.getTime();
		String fromDateString = dateFormat.format(fromDate);	
		try {
			statement = connection.createStatement();
			String sql = "SELECT CalculationValuesJSON from CalculationValues where ForecastDate >= '" + fromDateString + "' and ForecastDate <= '" + toDateString + "' and Username = '" + username + "' and Services = '" + serviceNames + "'";
			resultSet = statement.executeQuery(sql);
			boolean first = true;
			while (resultSet.next()) {
				weights = new JSONObject(resultSet.getString(1));
				for(String procedureName : weights.keySet()) {
					if(first) {
						averagedWeights.put(procedureName, weights.getDouble(procedureName));
					}else {
						double averagedWeight = (averagedWeights.getDouble(procedureName) + weights.getDouble(procedureName))/2;
						averagedWeights.put(procedureName, averagedWeight);
					}
				}
				first = false;
			}	
			return averagedWeights;
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
	
	
	public JSONObject getWeightCalculationValues(String toDateString, String serviceNames, String username) throws SQLException, ParseException {	
		JSONObject weightCalculationValues = new JSONObject();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date toDate = dateFormat.parse(toDateString);
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(toDate);
		calendar.add(Calendar.DAY_OF_MONTH, - 14);
		Date fromDate = calendar.getTime();
		String fromDateString = dateFormat.format(fromDate);	
		try {
			statement = connection.createStatement();
			String sql = "SELECT ForecastDate, CalculationValuesJSON from CalculationValues where ForecastDate >= '" + fromDateString + "' and ForecastDate <= '" + toDateString + "' and Username = '" + username + "' and Services = '" + serviceNames + "'";
			resultSet = statement.executeQuery(sql);
			boolean first = true;
			while (resultSet.next()) {
				String forecastDate = resultSet.getString(1);
				JSONObject singleValues = new JSONObject(resultSet.getString(2));
				weightCalculationValues.put(forecastDate, singleValues);
			}	
			return weightCalculationValues;
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
