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
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import dBConnections.ExpSmoothingDAO;
import dBConnections.ExpSmoothingDBConnection;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class ExponentialSmoothingAnalysis {
	
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
		//System.out.println(output);
		//System.out.println(error);
		return output.toString();
	}
	
	
	private JSONObject trainModel(String inputAggr, String outputAggr, String processingAggr, String expSmoothingPath, String filePath, String sorte, int forecastPeriods) throws IOException {
		String execString = "RScript " + expSmoothingPath + "Train_ExpSmoothing_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + filePath + " " + sorte + " " + forecastPeriods;
		
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));	
	}
	
	private JSONObject forecastModel(String inputAggr, String outputAggr, String processingAggr, String expSmoothingPath, String filePath, String sorte, int forecastPeriods) throws IOException {
		/*String execString ="";
		System.out.println("SORTE: " + sorte);
		switch(inputAggr) {
		  case "daily":
			  switch(outputAggr) {
			  case "daily":
				execString   = "RScript " + expSmoothingPath + "ExpSmoothing_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
				break;
			  case "weekly": 
				execString = "RScript " + expSmoothingPath + "ExpSmoothing_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
				break;
			  default:
				  throw new RuntimeException("Aggregation Invalid");
			}
		    break;
		  default:
			  throw new RuntimeException("Aggregation Invalid");
		}*/
		String execString = "RScript " + expSmoothingPath + "ExpSmoothing_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + filePath + " " + sorte + " " + forecastPeriods;
		
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));
	}
	
	
	public ExponentialSmoothingAnalysis() {	}
	
	public JSONObject executeAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws SQLException, ClassNotFoundException {
		JSONObject resultValues = new JSONObject();
		String expPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ExpSmoothing\\";
		
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData");
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing");
		//boolean train = configurations.getJSONObject("parameters").getJSONObject("execution").getBoolean("train");
		//String username = configurations.getString("username");
		
		//ExpSmoothingDBConnection.getInstance("ExpSmoothingDB");
		//ExpSmoothingDAO expSmoothingDAO = new ExpSmoothingDAO();
		for(String sorte : preparedData.keySet()) {
			String filePath = expPath+"temp\\" + sorte + ".tmp";
			if(sorte.equals("S11")) {
				System.out.println("STOP");
			}
			//JSONObject model = new JSONObject();
			JSONObject executionResult = new JSONObject();
			//Input Daily OutputWeekly
			CustomFileWriter.createFile(filePath, preparedData.getString(sorte));
			
			try {
				//if(train) {
				//model = trainModel(inputAggr, outputAggr, expPath, filePath, sorte, forecastPeriods);
					
				//expSmoothingDAO.storeModel(model, username, inputAggr, outputAggr, sorte);
				//}else {
					//model = expSmoothingDAO.getModel(username, inputAggr, outputAggr);
				//}			
				executionResult = forecastModel(inputAggr, outputAggr, processingAggr, expPath, filePath, sorte, forecastPeriods);
				resultValues.put(sorte, executionResult);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return resultValues;
	}

		
		public JSONObject getPreparedData(JSONObject expSmoothingConfigurations) throws JSONException, IOException {
			URL url = new URL(expSmoothingConfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			JSONObject requestBody = new JSONObject(expSmoothingConfigurations.toString());
			requestBody.put("username", "ForecastingTool");
			//String requestBody = kalmanConfigurations.toString();
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
