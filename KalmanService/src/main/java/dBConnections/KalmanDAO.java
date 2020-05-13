package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dBConnections.KalmanDBConnection;
import dBConnections.DBConnection;

public class KalmanDAO {
	DBConnection dbConnection = null;
	
	public KalmanDAO() {
		dbConnection = KalmanDBConnection.getInstance();
	}
	public JSONObject getModel(JSONObject configurations, String serviceName, String username, String targetVariable) throws SQLException {	
		
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
		
		String aggregationInputData = configurations.getJSONObject("parameters").getString("aggregationInputData");
		String aggregationProcessing = configurations.getJSONObject("parameters").getString("aggregationProcessing");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		
		double campaignLowerLimit = 0;
		double campaignUpperLimit = 0;
		String campaignProcedure = "None";
		int campaignEnabled = 0;
		if(configurations.getJSONObject("parameters").has("campaigns")) {
			campaignLowerLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
			campaignUpperLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
			campaignProcedure = configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("procedure");
			campaignEnabled = (configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained")) ? 1 : 0;
			 
		}
		
		double outlierLowerLimit = 0;
		double outlierUpperLimit = 0;
		String outlierProcedure = "None";
		int outlierEnabled = 0;
		if(configurations.getJSONObject("parameters").has("outliers")) {
			outlierLowerLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
			outlierUpperLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
			outlierEnabled = (configurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) ? 1 : 0;
			outlierProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		}
		
		JSONObject model = new JSONObject();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		try {
			statement = connection.createStatement();
			String sql = "SELECT model from Models where " +
					"ForecastProcedure = '" + serviceName + "'" +
					" and TargetVariable = '" + targetVariable + "'" +
					" and AggregationInputData = '" + aggregationInputData + "'" +
					" and AggregationProcessing = '" + aggregationProcessing + "'" +
					" and AggregationOutputData = '" + aggregationOutputData + "'" +
					" and CampaignLowerLimit = '" + campaignLowerLimit + "'" +
					" and CampaignUpperLimit = '" + campaignUpperLimit + "'" +
					" and CampaignProcedure = '" + campaignProcedure + "'" +
					" and CampaignEnabled = '" + campaignEnabled + "'" +
					" and OutlierLowerLimit = '" + outlierLowerLimit + "'" +
					" and OutlierUpperLimit = '" + outlierUpperLimit + "'" +
					" and OutlierProcedure = '" + outlierProcedure + "'" +
					" and OutlierEnabled = '" + outlierEnabled + "'" +
					" and ExternalRegressors = '" + externalRegressors + "'" +
					" and Username = '" + username + "'" +
					" and ForecastDate <= '" + to + "'" +
					" Order by ForecastDate desc" +
					" limit 1";
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
	
	/*public void storeModel(JSONObject model, String username, String inputAggr, String forecastAggr, String modelID) throws SQLException {		
		 String sql = "INSERT OR REPLACE INTO Models(model, username, inputAggregation, ForecastAggregation,  ModelID) VALUES(?,?,?,?,?)"; 
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
	    }*/
	public void storeModel(JSONObject configurations, String serviceName, String username, JSONObject model, String targetVariable){
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
		
		String sql = "INSERT INTO Models('ForecastDate','ForecastProcedure', 'TargetVariable', 'Model','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure', 'CampaignEnabled','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure', 'OutlierEnabled','ExternalRegressors','Username') "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try (Connection connection = dbConnection.checkConnectivity();
        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, to);
			pstmt.setString(2, serviceName);
			pstmt.setString(3, targetVariable);
			pstmt.setString(4, model.toString());
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
	        pstmt.setString(18, username);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
	    }
	}
}
