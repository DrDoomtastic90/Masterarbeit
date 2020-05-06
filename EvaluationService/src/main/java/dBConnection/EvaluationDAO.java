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
		
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String aggregationInputData = configurations.getJSONObject("parameters").getString("aggregationInputData");
		String aggregationProcessing = configurations.getJSONObject("parameters").getString("aggregationProcessing");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		double campaignLowerLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		double campaignUpperLimit = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		String campaignProcedure = configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("procedure");
		boolean campaignEnabled = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
		double outlierLowerLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		double outlierUpperLimit = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		String outlierProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		boolean outlierEnabled = configurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle");
		
		
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