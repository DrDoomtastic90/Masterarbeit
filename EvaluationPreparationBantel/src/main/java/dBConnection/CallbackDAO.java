package dBConnection;

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

import outputHandler.CustomFileWriter;


public class CallbackDAO {
	DBConnection dbConnection = null;
	
	public CallbackDAO() {
		dbConnection = CallbackDBConnection.getInstance();
	}
	
	public JSONObject getSingleForecastResult(JSONObject configurations, JSONObject consideratedConfigurations, String procedureName, String from, String to) throws SQLException {
		JSONObject forecastResults= null;
		String externalRegressors = "";
		ArrayList<String> independentVariableList = new ArrayList<String>();
		if(configurations.has("factors") && configurations.getJSONObject("factors").has("independentVariable")) {
			JSONArray independentVariables = configurations.getJSONObject("factors").getJSONArray("independentVariable");
			for(int i = 0; i<independentVariables.length();i++) {
				independentVariableList.add(independentVariables.getJSONObject(i).getString("content"));
			}
			externalRegressors = independentVariableList.toString();
		}
		
		int forecastPeriods = 1;
		
		String aggregationInputData = null;
		String aggregationProcessing = null;
		String aggregationOutputData = null;
		
		double campaignLowerLimit = 0;
		double campaignUpperLimit = 0;
		String campaignProcedure = "None";
		boolean campaignEnabled = false;
		
		double outlierLowerLimit = 0;
		double outlierUpperLimit = 0;
		String outlierProcedure = "None";
		boolean outlierEnabled = false;
		
		if(configurations.has("parameters")) {
			if(configurations.getJSONObject("parameters").has("forecastPeriods")) {
				forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			}
			
			
			if(configurations.getJSONObject("parameters").has("aggregationInputData")) {
				aggregationInputData = configurations.getJSONObject("parameters").getString("aggregationInputData");
			}
			if(configurations.getJSONObject("parameters").has("aggregationProcessing")) {
				aggregationProcessing = configurations.getJSONObject("parameters").getString("aggregationProcessing");
			}
			if(configurations.getJSONObject("parameters").has("aggregationOutputData")) {
				aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
			}
			
	
			if(configurations.getJSONObject("parameters").has("campaigns")) {
				campaignLowerLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
				campaignUpperLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
				campaignProcedure = configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("procedure");
				campaignEnabled = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
			}
			
	
			if(configurations.getJSONObject("parameters").has("outliers")) {
				outlierLowerLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
				outlierUpperLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
				outlierEnabled = configurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle");
				outlierProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
			}
			
	}
		String sqlSelect = "select ForecastID, ForecastResult, CampaignEnabled,  CampaignProcedure, CampaignLowerLimit, CampaignUpperLimit, OutlierEnabled, OutlierProcedure, OutlierLowerLimit, OutlierUpperLimit ";
		String sqlFrom = "from ForecastResults ";
		String sqlWhereClause = "where ForecastID in (select max(ForecastID) from ForecastResults " +
				"where ForecastDate = '" + to + "'" + 
				" AND ForecastProcedure = '"+ procedureName + "'" + 
				" AND ExecutionPeriods='" + forecastPeriods + "'" +  
				" AND DataConsideratedFromDate = '"+ from + "' " +
				" AND ExternalRegressors = '"+ externalRegressors + "' ";
		String sqlGroupByClause = " Group By CampaignEnabled, CampaignProcedure, CampaignLowerLimit, CampaignUpperLimit, OutlierEnabled, OutlierProcedure, OutlierLowerLimit, OutlierUpperLimit";
		
		if(aggregationProcessing != null) {
			sqlWhereClause = sqlWhereClause + " AND AggregationProcessing='"+ aggregationProcessing + "'";
		}else {
			sqlWhereClause = sqlWhereClause + " AND AggregationProcessing is NULL";
		}
		
		if(aggregationInputData != null) {
			sqlWhereClause = sqlWhereClause + " AND AggregationInputData = '"+ aggregationInputData + "'";
		}else {
			sqlWhereClause = sqlWhereClause + " AND AggregationInputData is NULL";
		}
		
		if(aggregationOutputData != null) {
			sqlWhereClause = sqlWhereClause + " AND AggregationOutputData = '"+ aggregationOutputData + "'";
		}else {
			sqlWhereClause = sqlWhereClause + " AND AggregationOutputData is NULL";
		}
		
		if(consideratedConfigurations.has("campaignHandling")) {
			sqlWhereClause = sqlWhereClause + " AND CampaignEnabled = '"+ (campaignEnabled ? 1 : 0) + "'";
			if(consideratedConfigurations.has("limitCampaign")) {
				if(consideratedConfigurations.getBoolean("campaignHandling") && consideratedConfigurations.getBoolean("limitCampaign")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignLowerLimit = '"+ campaignLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND CampaignUpperLimit = '"+ campaignUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("campaignProcedure")) {
				if(consideratedConfigurations.getBoolean("campaignProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignProcedure = '"+ campaignProcedure + "'";
				}
			}
		} else {
			if(consideratedConfigurations.has("limitCampaign")) {
				if(consideratedConfigurations.getBoolean("limitCampaign")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignLowerLimit = '"+ campaignLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND CampaignUpperLimit = '"+ campaignUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("campaignProcedure")) {
				if(consideratedConfigurations.getBoolean("campaignProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignProcedure = '"+ campaignProcedure + "'";
				}
			}
		}

		if(consideratedConfigurations.has("outlierHandling")) {
			sqlWhereClause = sqlWhereClause + " AND OutlierEnabled = '"+ (outlierEnabled ? 1 : 0) + "'";
			if(consideratedConfigurations.has("limitOutlier")) {
				if(consideratedConfigurations.getBoolean("outlierHandling") && consideratedConfigurations.getBoolean("limitOutlier")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierLowerLimit = '"+ outlierLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND OutlierUpperLimit = '"+ outlierUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("outlierProcedure")) {
				if(consideratedConfigurations.getBoolean("outlierProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierProcedure = '"+ outlierProcedure + "'";
					
				}
			}
		}else {
			if(consideratedConfigurations.has("limitOutlier")) {
				if(consideratedConfigurations.getBoolean("limitOutlier")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierLowerLimit = '"+ outlierLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND OutlierUpperLimit = '"+ outlierUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("outlierProcedure")) {
				if(consideratedConfigurations.getBoolean("outlierProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierProcedure = '"+ outlierProcedure + "'";
					
				}
			}
		}
	
		
		sqlWhereClause = sqlWhereClause + sqlGroupByClause + ")";
		String sql = sqlSelect + sqlFrom + sqlWhereClause + " Order By ForecastID desc";
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			boolean first = true;
			while (resultSet.next()) {
				if(first) {
					forecastResults = new JSONObject();
					first = false;
				}
				JSONObject singleForecastResult =  new JSONObject(resultSet.getString(2));
				String configuration = "P";
				if(resultSet.getBoolean(3)) {
					configuration = configuration + "_" + resultSet.getString(4);
					configuration = configuration + "_" + resultSet.getString(5);
					configuration = configuration + "_" + resultSet.getString(6);
				}else {
					configuration = configuration + "_NONE_X_X";
				}
				if(resultSet.getBoolean(7)) {
					configuration = configuration + "_" + resultSet.getString(8);
					configuration = configuration + "_" + resultSet.getString(9);
					configuration = configuration + "_" + resultSet.getString(10);
				}else {
					configuration = configuration + "_NONE_X_X";
				}
				configuration = configuration + "_" + externalRegressors;
				forecastResults.put(configuration,singleForecastResult);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return forecastResults;	
		
	}
	
	public JSONObject getForecastResults(JSONObject configurations, JSONObject consideratedConfigurations, String procedureName, String from, String to) throws SQLException, ParseException {
		JSONObject forecastResults= null;
		String externalRegressors = "";
		ArrayList<String> independentVariableList = new ArrayList<String>();
		/*if(configurations.getJSONObject("factors").has("independentVariable")) {
			JSONArray independentVariables = configurations.getJSONObject("factors").getJSONArray("independentVariable");
			for(int i = 0; i<independentVariables.length();i++) {
				independentVariableList.add(independentVariables.getJSONObject(i).getString("content"));
			}
			externalRegressors = independentVariableList.toString();
		}*/
		
		int forecastPeriods = 1;
		if(configurations.getJSONObject("parameters").has("forecastPeriods")) {
			forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		}
		String aggregationInputData = null;
		String aggregationProcessing = null;
		String aggregationOutputData = null;
		
		if(configurations.getJSONObject("parameters").has("aggregationInputData")) {
			aggregationInputData = configurations.getJSONObject("parameters").getString("aggregationInputData");
		}
		if(configurations.getJSONObject("parameters").has("aggregationProcessing")) {
			aggregationProcessing = configurations.getJSONObject("parameters").getString("aggregationProcessing");
		}
		if(configurations.getJSONObject("parameters").has("aggregationOutputData")) {
			aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		}
		
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
		
		
		String sqlSelect = "select ForecastDate, ForecastResult";
		String sqlFrom = " from ForecastResults";
		String sqlWhereClause = " where ForecastID in (select ForecastID from ForecastResults" +
				" where ForecastDate <= '" + to + "'" +
				" AND ForecastDate >= '"+ from + "'" +
				" AND ForecastProcedure = '"+ procedureName + "'" + 
				" AND ExecutionPeriods='" + forecastPeriods + "'" + 
				" AND AggregationInputData = '"+ aggregationInputData + "'" +  
				" AND AggregationOutputData = '"+ aggregationOutputData + "'";
				//" AND ExternalRegressors = '"+ externalRegressors + "' ";
	
		if(consideratedConfigurations.has("AggregationProcessing") && aggregationProcessing != null) {
			sqlWhereClause = sqlWhereClause + " AND AggregationProcessing='"+ aggregationProcessing + "'";
		}
		
		if(consideratedConfigurations.has("campaignHandling")) {
			sqlWhereClause = sqlWhereClause + " AND CampaignEnabled = '"+ (campaignEnabled ? 1 : 0) + "'";
			if(consideratedConfigurations.has("limitCampaign")) {
				if(consideratedConfigurations.getBoolean("campaignHandling") && consideratedConfigurations.getBoolean("limitCampaign")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignLowerLimit = '"+ campaignLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND CampaignUpperLimit = '"+ campaignUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("campaignProcedure")) {
				if(consideratedConfigurations.getBoolean("campaignProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignProcedure = '"+ campaignProcedure + "'";
				}
			}
		} else {
			if(consideratedConfigurations.has("limitCampaign")) {
				if(consideratedConfigurations.getBoolean("limitCampaign")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignLowerLimit = '"+ campaignLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND CampaignUpperLimit = '"+ campaignUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("campaignProcedure")) {
				if(consideratedConfigurations.getBoolean("campaignProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND CampaignProcedure = '"+ campaignProcedure + "'";
				}
			}
		}

		if(consideratedConfigurations.has("outlierHandling")) {
			sqlWhereClause = sqlWhereClause + " AND OutlierEnabled = '"+ (outlierEnabled ? 1 : 0) + "'";
			if(consideratedConfigurations.has("limitOutlier")) {
				if(consideratedConfigurations.getBoolean("outlierHandling") && consideratedConfigurations.getBoolean("limitOutlier")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierLowerLimit = '"+ outlierLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND OutlierUpperLimit = '"+ outlierUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("outlierProcedure")) {
				if(consideratedConfigurations.getBoolean("outlierProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierProcedure = '"+ outlierProcedure + "'";
					
				}
			}
		}else {
			if(consideratedConfigurations.has("limitOutlier")) {
				if(consideratedConfigurations.getBoolean("limitOutlier")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierLowerLimit = '"+ outlierLowerLimit + "' ";
					sqlWhereClause = sqlWhereClause + " AND OutlierUpperLimit = '"+ outlierUpperLimit + "' " ;
				}
			}
			if(consideratedConfigurations.has("outlierProcedure")) {
				if(consideratedConfigurations.getBoolean("outlierProcedure")) {
					sqlWhereClause = sqlWhereClause + " AND OutlierProcedure = '"+ outlierProcedure + "'";
					
				}
			}
		}	
		sqlWhereClause = sqlWhereClause +  ")";
		String sql = sqlSelect + sqlFrom + sqlWhereClause + " Order By ForecastID desc";
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			forecastResults = new JSONObject();
			//boolean first = true;
			while (resultSet.next()) {
				/*if(first) {
					forecastResults = new JSONObject();
					first = false;
				}*/
				/*
				JSONObject singleForecastResult =  new JSONObject();
				String configuration = "P";
				if(resultSet.getBoolean(3)) {
					configuration = configuration + "_" + resultSet.getString(4);
					configuration = configuration + "_" + resultSet.getString(5);
					configuration = configuration + "_" + resultSet.getString(6);
				}else {
					configuration = configuration + "_NONE_X_X";
				}
				if(resultSet.getBoolean(7)) {
					configuration = configuration + "_" + resultSet.getString(8);
					configuration = configuration + "_" + resultSet.getString(9);
					configuration = configuration + "_" + resultSet.getString(10);
				}else {
					configuration = configuration + "_NONE_X_X";
				}
				singleForecastResult.put(configuration, new JSONObject(resultSet.getString(2)));
				
				*/
				JSONObject singleForecastResult =  new JSONObject();
				String configuration = "Placeholder";
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
				Calendar calendar = new GregorianCalendar(Locale.GERMAN);
				calendar.setFirstDayOfWeek(Calendar.MONDAY);
				String dateString = resultSet.getString(1);
				calendar.setTime(dateFormat.parse(dateString));
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				calendar.add(Calendar.DAY_OF_MONTH, + 1);
				Date forecastDate = calendar.getTime();
				String forecastDateString = dateFormat.format(forecastDate);
				
				if(!forecastResults.has(forecastDateString)) {
					singleForecastResult.put(configuration, new JSONObject(resultSet.getString(2)));
					CustomFileWriter.createJSON("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Evaluation\\temp\\test_"+procedureName+"_"+ forecastDateString + ".json", singleForecastResult.toString());
					
					//forecastResults.put(resultSet.getString(1), new JSONObject(resultSet.getString(2)));
					forecastResults.put(forecastDateString, singleForecastResult);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return forecastResults;	
		
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
		double campaignLowerLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		double campaignUpperLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		String campaignProcedure = configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("procedure");
		double outlierLowerLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		double outlierUpperLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		String outlierProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		
		
		/*String sql = "INSERT INTO ForecastResults('ForecastDate','ForecastProcedure', 'ForecastResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure','ExternalRegressors') "
				+ "VALUES('" + to + "','" + serviceName + "','" + forecastResult + "','" + forecastPeriods + "','" + aggregationInputData + "','" + aggregationProcessing + "','" + aggregationOutputData + "','" + from + "',"
						+ "'" + campaignLowerLimit + "','" + campaignUpperLimit + "','" + campaignProcedure + "','" + outlierLowerLimit + "','" + outlierUpperLimit + "','" + outlierProcedure + "','" + externalRegressors + "')"; 
		*/
		String sql = "INSERT INTO ForecastResults('ForecastDate','ForecastProcedure', 'ForecastResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure','ExternalRegressors') "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
			pstmt.setDouble(12, outlierLowerLimit);
			pstmt.setDouble(13, outlierUpperLimit);
			pstmt.setString(14, outlierProcedure);
	        pstmt.setString(15, externalRegressors);
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
	
	
	/*public JSONObject getWeightCalculationValues(String toDateString, String fromDate, String serviceNames, String username) throws SQLException, ParseException {	
		JSONObject weightCalculationValues = new JSONObject();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date toDate = dateFormat.parse(toDateString);
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(toDate);
		//calendar.add(Calendar.DAY_OF_MONTH, - 14);
		//Date fromDate = calendar.getTime();
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
	}*/
	
	
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
