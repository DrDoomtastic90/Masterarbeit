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


public class KalmanAnalysis {
	Rengine rEngine = null;
	
	
	public KalmanAnalysis() {
		rEngine = Rengine.getMainEngine();
		if(rEngine == null) {
			rEngine = new Rengine(new String[] { "–no-save" }, false, null);
		}
	}
		//from https://mkyong.com/java/how-to-execute-shell-command-from-java/
		public JSONObject executeAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws InterruptedException, IOException {
			JSONObject resultValues = new JSONObject();
			String kalmanPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Kalman\\";
			String filePath = kalmanPath+"temp\\inputValues.tmp";
			int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData");
			String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData");
			for(String sorte : preparedData.keySet()) {
					if(sorte.equals("S1")) {
						System.out.println("Stop");
					}
					JSONObject executionResult = new JSONObject();
					//Input Daily OutputWeekly
					CustomFileWriter.createFile(filePath, preparedData.getString(sorte));
					String execString="";
					switch(inputAggr) {
					  case "daily":
						  switch(outputAggr) {
						  case "daily":
							execString   = "RScript " + kalmanPath + "Kalman_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
						    break;
						  case "weekly":
							execString = "RScript " + kalmanPath + "Kalman_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
						    break;
						  default:
							  throw new RuntimeException("Aggregation Invalid");
						}
					    break;
					  default:
						  throw new RuntimeException("Aggregation Invalid");
					}
					Process process = Runtime.getRuntime().exec(execString);
					StringBuilder output = new StringBuilder();
					StringBuilder error = new StringBuilder();
					BufferedReader outputStream = new BufferedReader( new InputStreamReader(process.getInputStream()));
					BufferedReader errorStream = new BufferedReader( new InputStreamReader(process.getErrorStream()));
					String resultString;
					int counter = 1;
					while ((resultString = outputStream.readLine()) != null) {
						output.append(resultString + "\n");
						executionResult.put(Integer.toString(counter), resultString);
						counter = counter + 1;
					}
					while ((resultString = errorStream.readLine()) != null) {
						error.append(resultString + "\n");
					}
					System.out.println(output);
					System.out.println(error);
					resultValues.put(sorte, executionResult);
			}		
			return resultValues;
		}
		public JSONObject getPreparedData(JSONObject aRIMAconfigurations) throws JSONException, IOException {
			URL url = new URL(aRIMAconfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			String requestBody = aRIMAconfigurations.toString();
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
