package serviceImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import outputHandler.CustomFileWriter;


public class OutlierHandler {
	
	
	public OutlierHandler() {}	
		
	public JSONObject limitHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\outlierHandler\\";
				

		
		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "OutlierHandling_ARIMAAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
			String resultString = executeProcessCMD(execString);
			resultValues.put(targetVariable, resultString);
		}
		return resultValues;
	}
	
	public JSONObject aRIMAHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
	
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\outlierHandler\\";
				

		
		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "OutlierHandling_ARIMAAnalysis_" + inputAggr + "_" + inputAggr + "_" + inputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
			String resultString = executeProcessCMD(execString);
			resultValues.put(targetVariable, resultString);
		}
		return resultValues;
	}
	
	public JSONObject exponentialSmoothingHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("outliers").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\outlierHandler\\";
				

		
		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "OutlierHandling_ExpSmoothingAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
			String resultString = executeProcessCMD(execString);
			resultValues.put(targetVariable, resultString);
			//JSONObject executionResult = new JSONObject(executeProcessCMD(execString));
			//resultValues.put(targetVariable, executionResult);
		}
		return resultValues;
	}
	
	private String executeProcessCMD(String execString) throws IOException {
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
		return output.toString();
	}

}
