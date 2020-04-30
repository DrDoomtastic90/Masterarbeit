package serviceImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class ARIMAAnalysis {
	
	
	public ARIMAAnalysis() {}	
		
	
	/*public JSONObject getPreparedData(JSONObject aRIMAconfigurations) throws JSONException, IOException {
		URL url = new URL(aRIMAconfigurations.getJSONObject("data").getString("provisioningServiceURL"));
		String contentType = "application/json";
		
		//Create request body and add login credentials
		JSONObject requestBody = new JSONObject(aRIMAconfigurations.toString());
		requestBody.put("username", "ForecastingTool");
		requestBody.put("password", "forecasting");
		RestClient restClient = new RestClient();
		
		//configure endpoint and connection type
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	
	public JSONObject handleOutliers(JSONObject aRIMAConfigurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		String handlingProcedure = aRIMAConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/OutlierHandlingService/" + handlingProcedure + "Handler");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		
		//Create request body and add login credentials
		JSONObject requestBody = new JSONObject();
		requestBody.put("configurations", aRIMAConfigurations);
		requestBody.put("dataset", dataWithOutliers);
		requestBody.put("username", "ForecastingTool");
		requestBody.put("password", "forecasting");
		RestClient restClient = new RestClient();
		
		//configure endpoint and connection type
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString())); 
	}*/
	
	
	
	//from https://mkyong.com/java/how-to-execute-shell-command-from-java/
	public JSONObject executeARIMAAnalysis(JSONObject configurations, JSONObject preparedData) throws InterruptedException, IOException {
	
		//Initialize Path Variables
		String aRIMAPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ARIMA\\";
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		//double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		//double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		JSONArray factors = new JSONArray();
		if(configurations.getJSONObject("factors").has("independentVariable")) {
			factors = configurations.getJSONObject("factors").getJSONArray("independentVariable");
		}
		//return executeARIMAAnalysisCMD(aRIMAPath, inputAggr, processingAggr, outputAggr, forecastPeriods, lowerLimitOutliers, upperLimitOutliers, preparedData);
		return executeARIMAAnalysisCMD(aRIMAPath, inputAggr, processingAggr, outputAggr, forecastPeriods, preparedData, factors);
	}
	
	
	//private JSONObject executeARIMAAnalysisCMD(String aRIMAPath, String inputAggr, String processingAggr, String outputAggr, int forecastPeriods, double lowerLimitOutliers, double upperLimitOutliers, JSONObject preparedData) throws IOException {
	private JSONObject executeARIMAAnalysisCMD(String aRIMAPath, String inputAggr, String processingAggr, String outputAggr, int forecastPeriods, JSONObject preparedData, JSONArray factors) throws IOException {
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Factors
		StringBuilder factorStringBuilder = new StringBuilder();
		boolean first = true;
		
		for(int i = 0; i < factors.length(); ++i) {
			JSONObject factor = factors.getJSONObject(i);
			String content = factor.getString("content");
			if(first) {
				factorStringBuilder.append(content);
				first=false;
			}else {
				factorStringBuilder.append(",");
				factorStringBuilder.append(content);
			}
		}
		String factorsString = factorStringBuilder.toString();
				
		//Execute Analysis for each target variable
		for(String targetVariable : preparedData.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = aRIMAPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, preparedData.getString(targetVariable));
			//handleOutliers(aRIMAPath, resourcePath, targetVariable, inputAggr, processingAggr, outputAggr, forecastPeriods, lowerLimitOutliers, upperLimitOutliers);
			//resourcePath = aRIMAPath + "temp\\inputValuesWOOutlier_" + targetVariable + ".tmp";
			
			
			//create temporary input file (loaded by foreasting rscript)
			//CustomFileWriter.createFile(resourcePath, preparedData.getString(targetVariable));
			String execString = "RScript " + aRIMAPath + "Exec_ARIMAAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + factorsString;
			JSONObject executionResult = new JSONObject(executeProcessCMD(execString));
			resultValues.put(targetVariable, executionResult);
		}
		return resultValues;
	}
	
	
	/*private void handleOutliers(String aRIMAPath, String resourcePath, String targetVariable, String inputAggr, String processingAggr, String outputAggr, int forecastPeriods, double lowerLimitsOutliers, double upperLimitsOutliers) throws IOException {
		//Prepare execution String for CMD Execution
		String execString = "RScript " + aRIMAPath + "OutlierHandling_ARIMAAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitsOutliers + " " + upperLimitsOutliers;
		executeProcessCMD(execString);
	}*/
	
	
	private String executeProcessCMD(String execString) throws IOException {
		
		System.out.println(execString);
		//Execute RScript via CMD
		Process process = Runtime.getRuntime().exec(execString);
		
		//Read Outputstream from Process
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
		if(error.toString().length()>0) {
			//throw new RuntimeException(error.toString());
			System.out.println(error.toString());
		}
		
		return output.toString();
	}

}
