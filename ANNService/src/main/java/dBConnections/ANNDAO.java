package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dBConnections.ANNDBConnection;
import dBConnections.DBConnection;

public class ANNDAO {
	DBConnection dbConnection = null;
	
	public ANNDAO() {
		dbConnection = ANNDBConnection.getInstance();
	}
	public JSONObject getModel(String username, String inputAggr, String forecastAggr, String sorte) throws SQLException {	
		
		JSONObject model = new JSONObject();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dbConnection.checkConnectivity();
		
		try {
			statement = connection.createStatement();
			String sql = "SELECT model from Models where Username='" + username+ "' and InputAggregation='" + inputAggr + "' and ForecastAggregation='" + forecastAggr + "' and modelID = '" + sorte + "'";
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
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		
		String networkType = configurations.getJSONObject("parameters").getJSONObject("network").getString("networkType");
		int amountInputNodes = configurations.getJSONObject("parameters").getJSONObject("network").getInt("amountInputNodes");
		JSONArray hiddenLayers = null;
		if( configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").length()>1){
			hiddenLayers =  configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONArray("hiddenLayer");
			//String execString = "python " + scriptPath + "Train_FeedForwardNetwork_" + inputAggr + "_" + outputAggr+".py " + sourcePath + " " + sorte + " " + forecastPeriods + " " + transformationFunction + " " + networktype +  " " + amountInputNodes + " " + amountHiddenNodes + " " + amountOutputNodes;
		}else {
			hiddenLayers = new JSONArray();
			hiddenLayers.put(configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONObject("hiddenLayer"));
		}
		int amountHiddenNodes = 0;
		ArrayList<String> transformationFunction = new ArrayList<String>();
		for (int i = 0; i<hiddenLayers.length();i++) {
			amountHiddenNodes = amountHiddenNodes + hiddenLayers.getJSONObject(i).getInt("amountNodes");
			transformationFunction.add(hiddenLayers.getJSONObject(i).getString("transformationFunction"));
		}
		int amountOutputNodes = configurations.getJSONObject("parameters").getJSONObject("network").getInt("amountOutputNodes");
		//String transformationFunctioxn = configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONObject("hiddenLayer").getString("transformationFunction");
		//String transformationFunction = hiddenLayers.getJSONObject(0).getString("transformationFunction");
		String sql = "INSERT INTO Models('ForecastDate','ForecastProcedure', 'TargetVariable', 'Model','AggregationInputData','AggregationOutputData','DataConsideratedFromDate','NetworkType','AmountInputNodes', 'AmountHiddenNodes', 'AmountOutputNodes', 'TransformationFunction', 'ExternalRegressors','Username') "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try (Connection connection = dbConnection.checkConnectivity();
        		PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, to);
			pstmt.setString(2, serviceName);
			pstmt.setString(3, targetVariable);
			pstmt.setString(4, model.toString());
			pstmt.setString(5, aggregationInputData);
			pstmt.setString(6, aggregationOutputData);
			pstmt.setString(7, from);
			pstmt.setString(8, networkType);
			pstmt.setInt(9, amountInputNodes);
			pstmt.setInt(10, amountHiddenNodes);
			pstmt.setInt(11, amountOutputNodes);
			pstmt.setString(12, transformationFunction.toString());
	        pstmt.setString(13, externalRegressors);
	        pstmt.setString(14, username);
	        pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
	    }
	}
	
	public JSONObject getModel(JSONObject configurations, String serviceName, String username, String targetVariable) throws SQLException {	
		String to = configurations.getJSONObject("data").getString("to");
		String from = configurations.getJSONObject("data").getString("from");
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
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		
		String networkType = configurations.getJSONObject("parameters").getJSONObject("network").getString("networkType");
		int amountInputNodes = configurations.getJSONObject("parameters").getJSONObject("network").getInt("amountInputNodes");
		//int amountHiddenNodes = configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONObject("hiddenLayer").getInt("amountNodes");
		//JSONArray hiddenLayers = configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONArray("hiddenLayer");
		JSONArray hiddenLayers = null;
		if( configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").length()>1){
			hiddenLayers =  configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONArray("hiddenLayer");
			//String execString = "python " + scriptPath + "Train_FeedForwardNetwork_" + inputAggr + "_" + outputAggr+".py " + sourcePath + " " + sorte + " " + forecastPeriods + " " + transformationFunction + " " + networktype +  " " + amountInputNodes + " " + amountHiddenNodes + " " + amountOutputNodes;
		}else {
			hiddenLayers = new JSONArray();
			hiddenLayers.put(configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONObject("hiddenLayer"));
		}
		int amountHiddenNodes = 0;
		ArrayList<String> transformationFunction = new ArrayList<String>();
		for (int i = 0; i<hiddenLayers.length();i++) {
			amountHiddenNodes = amountHiddenNodes + hiddenLayers.getJSONObject(i).getInt("amountNodes");
			transformationFunction.add(hiddenLayers.getJSONObject(i).getString("transformationFunction"));
		}

		int amountOutputNodes = configurations.getJSONObject("parameters").getJSONObject("network").getInt("amountOutputNodes");
//		String transformationFunction = configurations.getJSONObject("parameters").getJSONObject("network").getJSONObject("hiddenLayers").getJSONArray("hiddenLayer").getJSONObject(0).getString("transformationFunction");
		
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
					" and AggregationOutputData = '" + aggregationOutputData + "'" +
					" and NetworkType = '" + networkType + "'" +
					" and AmountInputNodes = '" + amountInputNodes + "'" +
					" and AmountHiddenNodes = '" + amountHiddenNodes + "'" +
					" and AmountOutputNodes = '" + amountOutputNodes + "'" +
					" and TransformationFunction = '" + transformationFunction.toString() + "'" +
					" and ExternalRegressors = '" + externalRegressors + "'" +
					" and DataConsideratedFromDate = '"+ from + "'" +
					" and Username = '" + username + "'" +
					" and ForecastDate <= '" + to + "'" +
					" Order by ForecastDate desc, modelID desc" +
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
	    }*/
	
	
}
