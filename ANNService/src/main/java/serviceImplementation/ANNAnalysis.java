package serviceImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import dBConnections.ANNDAO;
import dBConnections.ANNDBConnection;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class ANNAnalysis {

		
	private String executeProcessCMD(String execString) throws IOException {
		//JSONObject executionResult = new JSONObject();
		Process process = Runtime.getRuntime().exec(execString);
		StringBuilder output = new StringBuilder();
		StringBuilder error = new StringBuilder();
		BufferedReader outputStream = new BufferedReader( new InputStreamReader(process.getInputStream()));
		BufferedReader errorStream = new BufferedReader( new InputStreamReader(process.getErrorStream()));
		String resultString;
		while ((resultString = outputStream.readLine()) != null) {
			output.append(resultString + "\n");
		}
		while ((resultString = errorStream.readLine()) != null) {
			error.append(resultString + "\n");
		}
		System.out.println(output);
		System.out.println(error);
		return output.toString();
	}
	
	
	private JSONObject trainModel(String inputAggr, String outputAggr, String scriptPath, String sourcePath, String sorte, int forecastPeriods, JSONObject network) throws IOException {
		/*String execString ="";
		switch(inputAggr) {
		  case "daily":
			  switch(outputAggr) {
			  case "daily":
				execString   = "python " + scriptPath + "Train_FeedForwardNetwork_Day_Week.py " + sourcePath + " " + sorte + " " + forecastPeriods;
				break;
			  case "weekly": 
				execString = "python " + scriptPath + "Train_FeedForwardNetwork_Week_Week.py " + sourcePath + " " + sorte + " " + forecastPeriods;
				break;
			  default:
				  throw new RuntimeException("Aggregation Invalid");
			}
		    break;
		  default:
			  throw new RuntimeException("Aggregation Invalid");
		}*/
		String networktype = network.getString("networkType");
		int amountInputNodes = network.getInt("amountInputNodes");
		int amountOutputNodes = network.getInt("amountOutputNodes");
		int amountHiddenNodes = network.getJSONObject("hiddenLayers").getJSONObject("hiddenLayer").getInt("amountNodes");
		String transformationFunction = network.getJSONObject("hiddenLayers").getJSONObject("hiddenLayer").getString("transformationFunction");
		String execString = "python " + scriptPath + "Train_FeedForwardNetwork_" + inputAggr + "_" + outputAggr+".py " + sourcePath + " " + sorte + " " + forecastPeriods + " " + transformationFunction + " " + networktype +  " " + amountInputNodes + " " + amountHiddenNodes + " " + amountOutputNodes;
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));	
	}
	
	private JSONObject forecastModel(String inputAggr, String outputAggr, String scriptPath, String sourcePath, String sorte, int forecastPeriods, String modelPath) throws IOException {
		/*String execString ="";
		System.out.println("SORTE: " + sorte);
		switch(inputAggr) {
		  case "daily":
			  switch(outputAggr) {
			  case "daily":
				execString   = "python " + scriptPath + "Exec_FeedForwardNetwork_Day_Week.py " + sourcePath + " " + modelPath + " " + sorte + " " + forecastPeriods;
				break;
			  case "weekly": 
				execString = "python " + scriptPath + "Exec_FeedForwardNetwork_Week_Week.py " + sourcePath + " " + modelPath + " " + sorte + " " + forecastPeriods;
				break;
			  default:
				  throw new RuntimeException("Aggregation Invalid");
			}
		    break;
		  default:
			  throw new RuntimeException("Aggregation Invalid");
		}*/
		String execString = "python " + scriptPath + "Exec_FeedForwardNetwork_" + inputAggr + "_" + outputAggr + ".py " + sourcePath + " " + modelPath + " " + sorte + " " + forecastPeriods;
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));
	}
	
	
	public ANNAnalysis() {}

	public JSONObject executeAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws SQLException, ClassNotFoundException {
		JSONObject resultValues = new JSONObject();
		String scriptPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ANN\\";
		
		JSONObject network = configurations.getJSONObject("parameters").getJSONObject("network");
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		boolean train = configurations.getJSONObject("parameters").getJSONObject("execution").getBoolean("train");
		String username = configurations.getString("username");
		
		ANNDBConnection.getInstance("ANNDB");
		ANNDAO aNNDAO = new ANNDAO();
		for(String targetVariable : preparedData.keySet()) {
			JSONObject model = new JSONObject();
			JSONObject executionResult = new JSONObject();
			//Input Daily OutputWeekly
			String sourcePath = scriptPath+"temp\\" + targetVariable + ".tmp";
			String modelPath = scriptPath+"temp\\network.xml";
			String targetPath = scriptPath+"temp\\" + targetVariable + "_result.tmp";
			JSONArray dataset = preparedData.getJSONArray(targetVariable);
			CustomFileWriter.createFile(sourcePath, dataset.toString());
			
			try {
				if(train) {
					model = trainModel(inputAggr, outputAggr, scriptPath, sourcePath, targetVariable, forecastPeriods, network);
					aNNDAO.storeModel(configurations, "ANN", username, model, targetVariable);
					//aNNDAO.storeModel(model, username, inputAggr, outputAggr, sorte);
				}else {
					//model = aNNDAO.getModel(username, inputAggr, outputAggr, sorte);
					model = aNNDAO.getModel(configurations, "ANN", username, targetVariable);
				}
				CustomFileWriter.createFile(modelPath, model.getString("model"));
				executionResult = forecastModel(inputAggr, outputAggr, scriptPath, sourcePath, targetVariable, forecastPeriods, modelPath);
				CustomFileWriter.createFile(targetPath, executionResult.toString());
				resultValues.put(targetVariable, executionResult);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return resultValues;
	}

		public JSONObject getPreparedData(JSONObject aNNConfigurations) throws JSONException, IOException {
			URL url = new URL(aNNConfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			JSONObject requestBody = new JSONObject(aNNConfigurations.toString());
			requestBody.put("username", "ForecastingTool");
			requestBody.put("password", "forecasting");
			//String requestBody = kalmanConfigurations.toString();
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
