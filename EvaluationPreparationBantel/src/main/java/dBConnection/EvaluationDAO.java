package dBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class EvaluationDAO {
	DBConnection dBConnection = null;

	public EvaluationDAO(String passPhrase) throws ClassNotFoundException {
		dBConnection = EvaluationDBConnection.getInstance(passPhrase);
	}


	public EvaluationDAO() {
		dBConnection = EvaluationDBConnection.getInstance();
	}

	
	public void writeEvaluationResultsToDB(JSONObject evaluationResults,JSONObject configurations, String forecastProcedure, String evalutionProcedure){
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
		
		int forecastPeriods = 1;
		if(configurations.getJSONObject("parameters").has("forecastPeriods")) {
			forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			
		}
		 configurations.getJSONObject("parameters").getInt("forecastPeriods");

		
		
		/*String sql = "INSERT INTO ForecastResults('ForecastDate','ForecastProcedure', 'ForecastResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure','ExternalRegressors') "
				+ "VALUES('" + to + "','" + serviceName + "','" + forecastResult + "','" + forecastPeriods + "','" + aggregationInputData + "','" + aggregationProcessing + "','" + aggregationOutputData + "','" + from + "',"
						+ "'" + campaignLowerLimit + "','" + campaignUpperLimit + "','" + campaignProcedure + "','" + outlierLowerLimit + "','" + outlierUpperLimit + "','" + outlierProcedure + "','" + externalRegressors + "')"; 
		*/
		String sql = "INSERT INTO EvaluationResults('ForecastDate','ForecastProcedure', 'EvaluationResult','ExecutionPeriods','AggregationInputData','AggregationProcessing','AggregationOutputData','DataConsideratedFromDate','CampaignLowerLimit','CampaignUpperLimit','CampaignProcedure', 'CampaignEnabled','OutlierLowerLimit','OutlierUpperLimit','OutlierProcedure', 'OutlierEnabled','ExternalRegressors', 'EvaluationProcedure') "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (Connection connection = dBConnection.checkConnectivity();
	        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, to);
			pstmt.setString(2, forecastProcedure);
			pstmt.setString(3, evaluationResults.toString());
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
	        pstmt.setString(18, evalutionProcedure);
	        
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
	    }
	}
	
}