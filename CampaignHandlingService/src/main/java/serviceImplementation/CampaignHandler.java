package serviceImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import outputHandler.CustomFileWriter;


public class CampaignHandler {
	
	
	public CampaignHandler() {}	
	
	public JSONObject limitHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\campaignHandler\\";

		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "CampaignHandling_Identification_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
			String resultString = executeProcessCMD(execString);
			resultValues.put(targetVariable, resultString);
		}
		return resultValues;
	}
	
	public JSONObject identificationHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\campaignHandler\\";

		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "CampaignHandling_Identification_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
			String resultString = executeProcessCMD(execString);
			resultValues.put(targetVariable, resultString);
		}
		return resultValues;
	}
	
	public JSONObject aRIMAHandler(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
	
		//Instantiate result object 
		JSONObject resultValues = new JSONObject();
		
		//Get Factors
		String factorsString = "";
		if(configurations.getJSONObject("factors").has("independentVariable")){
			StringBuilder factorStringBuilder = new StringBuilder();
			boolean first = true;
			JSONArray factors = configurations.getJSONObject("factors").getJSONArray("independentVariable");
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
			factorsString = factorStringBuilder.toString();
		}
		//Get Execution Parameters
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\campaignHandler\\";

		//Execute Analysis for each target variable (only target variables are handled. Independent variable are assumed as given)
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "CampaignHandling_ARIMAAnalysis_" + inputAggr + "_" + inputAggr + "_" + inputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers + " " + factorsString;
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
		double upperLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("upperLimit");
		double lowerLimitOutliers = configurations.getJSONObject("parameters").getJSONObject("campaigns").getDouble("lowerLimit");
		String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		String processingAggr = configurations.getJSONObject("parameters").getString("aggregationProcessing").toUpperCase();
		
		//Initialize Path Variables
		String handlerPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\DataProvisioningServices\\campaignHandler\\";

		//Execute Analysis for each target variable
		for(String targetVariable : dataWithOutliers.keySet()) {
			
			//create temporary input file (loaded by rscript to handle outliers)
			String resourcePath = handlerPath+"temp\\inputValues_" + targetVariable + ".tmp";
			CustomFileWriter.createFile(resourcePath, dataWithOutliers.getString(targetVariable));
			String execString = "RScript " + handlerPath + "CampaignHandling_ExpSmoothingAnalysis_" + inputAggr + "_" + processingAggr + "_" + outputAggr + ".txt " + resourcePath + " " + targetVariable + " " + forecastPeriods + " " + lowerLimitOutliers + " " + upperLimitOutliers;
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
