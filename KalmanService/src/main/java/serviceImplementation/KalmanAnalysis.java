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

import dBConnections.KalmanDAO;
import dBConnections.KalmanDBConnection;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class KalmanAnalysis {
	Rengine rEngine = null;
	
	
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
	
	
	private JSONObject trainModel(String inputAggr, String outputAggr, String processingAggr, String kalmanPath, String filePath, String sorte, int forecastPeriods) throws IOException {
		/*String execString ="";
		/*switch(inputAggr) {
		  case "daily":
			  switch(outputAggr) {
			  case "daily":
				execString   = "RScript " + kalmanPath + "Train_Kalman_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
				break;
			  case "weekly": 
				execString = "RScript " + kalmanPath + "Train_Kalman_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
				break;
			  default:
				  throw new RuntimeException("Aggregation Invalid");
			}
		    break;
		  default:
			  throw new RuntimeException("Aggregation Invalid");
		}
		*/
		String execString = "RScript " + kalmanPath + "Train_Kalman_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + filePath + " " + sorte + " " + forecastPeriods;
		
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));	
	}
	
	private JSONObject forecastModel(String inputAggr, String outputAggr, String processingAggr, String kalmanPath, String filePath, String sorte, int forecastPeriods, String model) throws IOException {
		/*String execString ="";
		System.out.println("SORTE: " + sorte);
		switch(inputAggr) {
		  case "daily":
			  switch(outputAggr) {
			  case "daily":
				execString   = "RScript " + kalmanPath + "Exec_Kalman_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods + " \"" + JSONObject.valueToString(model) + "\"";
				break;
			  case "weekly": 
				execString = "RScript " + kalmanPath + "Exec_Kalman_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods + " \"" + JSONObject.valueToString(model) + "\"";
				break;
			  default:
				  throw new RuntimeException("Aggregation Invalid");
			}
		    break;
		  default:
			  throw new RuntimeException("Aggregation Invalid");
		}*/
		String execString = "RScript " + kalmanPath + "Exec_Kalman_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + filePath + " " + sorte + " " + forecastPeriods + " \"" + JSONObject.valueToString(model) + "\"";
		
		System.out.println(execString);
		return new JSONObject(executeProcessCMD(execString));
	}
	
	
	public KalmanAnalysis() {
		rEngine = Rengine.getMainEngine();
		if(rEngine == null) {
			rEngine = new Rengine(new String[] { "–no-save" }, false, null);
		}
	}
	
	public JSONObject executeAnalysisCMDNeu(JSONObject configurations, JSONObject preparedData) throws SQLException, ClassNotFoundException {
		JSONObject resultValues = new JSONObject();
		String kalmanPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Kalman\\";
		
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		boolean train = configurations.getJSONObject("parameters").getJSONObject("execution").getBoolean("train");
		String username = configurations.getString("username");
		
		KalmanDBConnection.getInstance("KalmanFilterDB");
		KalmanDAO kalmanDAO = new KalmanDAO();
		for(String sorte : preparedData.keySet()) {
			String filePath = kalmanPath+"temp\\" + sorte + ".tmp";
			if(sorte.equals("S11")) {
				System.out.println("STOP");
			}
			JSONObject model = new JSONObject();
			JSONObject executionResult = new JSONObject();
			//Input Daily OutputWeekly
			CustomFileWriter.createFile(filePath, preparedData.getString(sorte));
			
			try {
				if(train) {
				model = trainModel(inputAggr, outputAggr, processingAggr, kalmanPath, filePath, sorte, forecastPeriods);
					
					kalmanDAO.storeModel(model, username, inputAggr, outputAggr, sorte);
				}else {
					model = kalmanDAO.getModel(username, inputAggr, outputAggr, sorte);
				}			
				executionResult = forecastModel(inputAggr, outputAggr, processingAggr, kalmanPath, filePath, sorte, forecastPeriods, model.toString());
				resultValues.put(sorte, executionResult);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return resultValues;
	}

		//from https://mkyong.com/java/how-to-execute-shell-command-from-java/
		public JSONObject executeAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws InterruptedException, IOException {
			JSONObject resultValues = new JSONObject();
			String kalmanPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Kalman\\";
			//String filePath = kalmanPath+"temp\\inputValues.tmp";
			int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData");
			String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData");
			boolean train = configurations.getJSONObject("parameters").getJSONObject("execution").getBoolean("train");

			for(String sorte : preparedData.keySet()) {
					String filePath = kalmanPath+"temp\\" + sorte + ".tmp";
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
							  if(train) {
								  execString   = "RScript " + kalmanPath + "Train_Kalman_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
							  }
							  execString   = "RScript " + kalmanPath + "Kalman_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;  
							break;
						  case "weekly":
							  if(train) {
								  execString = "RScript " + kalmanPath + "Train_Kalman_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
							  }else {
								  execString = "RScript " + kalmanPath + "Kalman_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
							  }
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
		
		
		public JSONObject getPreparedData(JSONObject kalmanConfigurations) throws JSONException, IOException {
			URL url = new URL(kalmanConfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			JSONObject requestBody = new JSONObject(kalmanConfigurations.toString());
			requestBody.put("username", "ForecastingTool");
			requestBody.put("password", "forecasting");
			//String requestBody = kalmanConfigurations.toString();
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
