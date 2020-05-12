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

import org.json.JSONArray;
import org.json.JSONObject;

import dBConnections.CallbackDBConnection;
import dBConnections.DBConnection;

public class CallbackDAO {
	DBConnection dbConnection = null;
	
	public CallbackDAO() {
		dbConnection = CallbackDBConnection.getInstance();
	}
	
	public void writeForecastResultsToDB(JSONObject configurations, String serviceName, JSONObject forecastResult){
		String from = configurations.getJSONObject("data").getString("from") ;
		String to = configurations.getJSONObject("data").getString("to");
		String externalRegressors = "";
		ArrayList<String> independentVariableList = new ArrayList<String>();
		if(configurations.getJSONObject("factors").has("independentVariable")) {
			JSONArray independentVariables = configurations.getJSONObject("factors").getJSONArray("independentVariable");
			for(int i = 0; i<independentVariables.length();i++) {
				independentVariableList.add(independentVariables.getJSONObject(i).getString("content"));
			}
			externalRegressors = independentVariableList.toString();
		}
		
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String aggregationInputData = configurations.getJSONObject("parameters").getString("aggregationInputData");
		String aggregationProcessing = configurations.getJSONObject("parameters").getString("aggregationProcessing");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		
		double campaignLowerLimit = 0;
		double campaignUpperLimit = 0;
		String campaignProcedure = "None";
		boolean campaignEnabled = false;
		if(configurations.getJSONObject("parameters").has("campaigns")) {
			campaignLowerLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
			campaignUpperLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
			campaignProcedure = configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("procedure");
			campaignEnabled = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
		}
		
		double outlierLowerLimit = 0;
		double outlierUpperLimit = 0;
		String outlierProcedure = "None";
		boolean outlierEnabled = false;
		if(configurations.getJSONObject("parameters").has("outliers")) {
			outlierLowerLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
			outlierUpperLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
			outlierEnabled = configurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle");
			outlierProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		}
		
		/*String sql = "INSERT INTO ForecastResults('ForecastDate','ForecastProcedure', 'ForecastResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure','ExternalRegressors') "
				+ "VALUES('" + to + "','" + serviceName + "','" + forecastResult + "','" + forecastPeriods + "','" + aggregationInputData + "','" + aggregationProcessing + "','" + aggregationOutputData + "','" + from + "',"
						+ "'" + campaignLowerLimit + "','" + campaignUpperLimit + "','" + campaignProcedure + "','" + outlierLowerLimit + "','" + outlierUpperLimit + "','" + outlierProcedure + "','" + externalRegressors + "')"; 
		*/
		String sql = "INSERT INTO ForecastResults('ForecastDate','ForecastProcedure', 'ForecastResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure', 'CampaignEnabled','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure', 'OutlierEnabled','ExternalRegressors') "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (Connection connection = dbConnection.checkConnectivity();
	        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, to);
			pstmt.setString(2, serviceName);
			pstmt.setString(3, forecastResult.toString());
			pstmt.setInt(4, forecastPeriods);
			pstmt.setString(5, aggregationInputData);
			pstmt.setString(6, aggregationProcessing);
			pstmt.setString(7, aggregationOutputData);
			pstmt.setString(8, from);
			pstmt.setDouble(9, campaignLowerLimit);
			pstmt.setDouble(10, campaignUpperLimit);
			pstmt.setString(11, campaignProcedure);
			pstmt.setBoolean(12, campaignEnabled);
			pstmt.setDouble(13, outlierLowerLimit);
			pstmt.setDouble(14, outlierUpperLimit);
			pstmt.setString(15, outlierProcedure);
			pstmt.setBoolean(16, outlierEnabled);
	        pstmt.setString(17, externalRegressors);
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
