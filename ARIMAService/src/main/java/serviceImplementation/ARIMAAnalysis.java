package serviceImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class ARIMAAnalysis {
	
	
	public ARIMAAnalysis() {}	
	
		//from https://mkyong.com/java/how-to-execute-shell-command-from-java/
		public JSONObject executeARIMAAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws InterruptedException, IOException {
			JSONObject resultValues = new JSONObject();
			String aRIMAPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ARIMA\\";
			String filePath = aRIMAPath+"temp\\inputValues.tmp";
			int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
			String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
			String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
			for(String sorte : preparedData.keySet()) {
					if(sorte.equals("S1")) {
						System.out.println("Stop");
					}
					//Input Daily OutputWeekly
					CustomFileWriter.createFile(filePath, preparedData.getString(sorte));
					String execString = "RScript " + aRIMAPath + "ARIMAAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + filePath + " " + sorte + " " + forecastPeriods;
					/*String execString="";
					switch(inputAggr) {
					  case "daily":
						  switch(processingAggr) {
						  	case "daily":
								  switch(outputAggr) {
								  	case "daily":
								  		execString   = "RScript " + aRIMAPath + "ARIMAAnalysis_Daily_Daily_Daily.txt " + filePath + " " + sorte + " " + forecastPeriods;
								  		break;
								  	case "weekly":
								  		execString = "RScript " + aRIMAPath + "ARIMAAnalysis_Daily_Daily_Weekly.txt " + filePath + " " + sorte + " " + forecastPeriods;
								  		break;
								  	default:
									  throw new RuntimeException("Aggregation Invalid");
								  }
								  break;
						  	case "weekly":
						  		switch(outputAggr) {
								  	case "weekly":
								  		execString = "RScript " + aRIMAPath + "ARIMAAnalysis_Daily_Weekly_Weekly.txt " + filePath + " " + sorte + " " + forecastPeriods;
								  		break;
								  	default:
									  throw new RuntimeException("Aggregation Invalid");
						  		}
						  		break;
						  	default:
							  throw new RuntimeException("Aggregation Invalid");
						  }
						  break;
					  case "weekly":
						  switch(processingAggr) {
						  	case "weekly":
						  		switch(outputAggr) {
								  	case "weekly":
								  		execString = "RScript " + aRIMAPath + "ARIMAAnalysis_Weekly_Weekly_Weekly.txt " + filePath + " " + sorte + " " + forecastPeriods;
								  		break;
								  	default:
									  throw new RuntimeException("Aggregation Invalid");
						  		}
						  		break;
						  	default:
							  throw new RuntimeException("Aggregation Invalid");
						  }
						  break;
					  default:
						  throw new RuntimeException("Aggregation Invalid");
					}*/
					Process process = Runtime.getRuntime().exec(execString);
					StringBuilder output = new StringBuilder();
					StringBuilder error = new StringBuilder();
					BufferedReader outputStream = new BufferedReader( new InputStreamReader(process.getInputStream()));
					BufferedReader errorStream = new BufferedReader( new InputStreamReader(process.getErrorStream()));
					String resultString;
					int counter = 1;
					while ((resultString = outputStream.readLine()) != null) {
						output.append(resultString + "\n");
						//executionResult.put(Integer.toString(counter), resultString);
						//counter = counter + 1;
					}
					while ((resultString = errorStream.readLine()) != null) {
						error.append(resultString + "\n");
					}
					//System.out.println(output);
					//System.out.println(error);
					//resultValues.put(sorte, executionResult);
					JSONObject executionResult = new JSONObject(output.toString());
					resultValues.put(sorte, executionResult);
			}		
			return resultValues;
		}
		public JSONObject getPreparedData(JSONObject aRIMAconfigurations) throws JSONException, IOException {
			URL url = new URL(aRIMAconfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			JSONObject requestBody = new JSONObject(aRIMAconfigurations.toString());
			requestBody.put("username", "ForecastingTool");
			requestBody.put("password", "forecasting");
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
