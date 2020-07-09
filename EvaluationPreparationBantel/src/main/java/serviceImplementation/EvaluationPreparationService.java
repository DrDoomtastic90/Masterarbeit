package serviceImplementation;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnection.BantelDAO;
import dBConnection.CallbackDAO;
import dBConnection.CallbackDBConnection;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class EvaluationPreparationService {

	
	
	public static JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
/*
	public static JSONObject evaluationMAE(JSONObject procedureResults) throws SQLException, ParseException, ClassNotFoundException, IOException {	
		JSONObject totalDeviationTargetVariable = new JSONObject();
		totalDeviationTargetVariable.put("ME", 0);
		totalDeviationTargetVariable.put("MEPercentage", 0);
		totalDeviationTargetVariable.put("MAE", 0);
		totalDeviationTargetVariable.put("MAEPercentage", 0);
		for(String targetVariableName : procedureResults.keySet()) {
			JSONObject totalDeviationObservationPeriod = new JSONObject();
			JSONObject targetVariableResults = procedureResults.getJSONObject(targetVariableName);
			totalDeviationObservationPeriod.put("ME", 0);
			totalDeviationObservationPeriod.put("MEPercentage", 0);
			totalDeviationObservationPeriod.put("MAE", 0);
			totalDeviationObservationPeriod.put("MAEPercentage", 0);
			for(String forecastDate : targetVariableResults.keySet()) {	
				JSONObject forecastDateResults = targetVariableResults.getJSONObject(forecastDate);
				JSONObject totalDeviationForecastPeriods = new JSONObject();
				totalDeviationForecastPeriods.put("ME", 0);
				totalDeviationForecastPeriods.put("MEPercentage", 0);
				totalDeviationForecastPeriods.put("MAE", 0);
				totalDeviationForecastPeriods.put("MAEPercentage", 0);
				for(String period : forecastDateResults.keySet()) {
					JSONObject periodResults = forecastDateResults.getJSONObject(period);
					double forecastResult = periodResults.getDouble("forecastResult");
					double actualDemand = periodResults.getDouble("demand");
					if(actualDemand == 0) {
						//hilfsmethode => Implementierung get all demands set to lowest non 0 demand
						actualDemand = 1;
					}
					double deviation = calculateMeanError(forecastResult, actualDemand);
					double ABSDeviation = calculateMeanAbsoluteError(forecastResult, actualDemand);
					periodResults.put("ME", deviation);
					periodResults.put("MEPercentage", deviation/actualDemand);
					periodResults.put("MAE", ABSDeviation);
					periodResults.put("MAEPercentage", ABSDeviation/actualDemand);
					periodResults.put("forecastResult", forecastResult);
					periodResults.put("actualDemand", actualDemand);
					totalDeviationForecastPeriods.put("ME", (totalDeviationForecastPeriods.getDouble("ME") + periodResults.getDouble("ME"))/2);
					totalDeviationForecastPeriods.put("MEPercentage", (totalDeviationForecastPeriods.getDouble("MEPercentage") + periodResults.getDouble("MEPercentage"))/2);
					totalDeviationForecastPeriods.put("MAE", (totalDeviationForecastPeriods.getDouble("MAE") + periodResults.getDouble("MAE"))/2);
					totalDeviationForecastPeriods.put("MAEPercentage", (totalDeviationForecastPeriods.getDouble("MAEPercentage") + periodResults.getDouble("MAEPercentage"))/2);
				}
				
				totalDeviationObservationPeriod.put("ME", (totalDeviationObservationPeriod.getDouble("ME") + totalDeviationForecastPeriods.getDouble("ME"))/2);
				totalDeviationObservationPeriod.put("MEPercentage", (totalDeviationObservationPeriod.getDouble("MEPercentage") + totalDeviationForecastPeriods.getDouble("MEPercentage"))/2);
				totalDeviationObservationPeriod.put("MAE", (totalDeviationObservationPeriod.getDouble("MAE") + totalDeviationForecastPeriods.getDouble("MAE"))/2);
				totalDeviationObservationPeriod.put("MAEPercentage", (totalDeviationObservationPeriod.getDouble("MAEPercentage") + totalDeviationForecastPeriods.getDouble("MAEPercentage"))/2);
			}
			totalDeviationTargetVariable.put("ME", (totalDeviationTargetVariable.getDouble("ME") + totalDeviationObservationPeriod.getDouble("ME"))/2);
			totalDeviationTargetVariable.put("MEPercentage", (totalDeviationTargetVariable.getDouble("MEPercentage") + totalDeviationObservationPeriod.getDouble("MEPercentage"))/2);
			totalDeviationTargetVariable.put("MAE", (totalDeviationTargetVariable.getDouble("MAE") + totalDeviationObservationPeriod.getDouble("MAE"))/2);
			totalDeviationTargetVariable.put("MAEPercentage", (totalDeviationTargetVariable.getDouble("MAEPercentage") + totalDeviationObservationPeriod.getDouble("MAEPercentage"))/2);
		}
		return procedureResults;
	}
		
	*/

	
	public static JSONObject getWeightCalculationValues(JSONObject configurations, String fromDateString, String toDateString) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject weightCalculationValues = new JSONObject();
		JSONObject consideratedConfigurations = new JSONObject();
		consideratedConfigurations.put("outlierHandling", "true");
		consideratedConfigurations.put("limitOutlier", "true");
		consideratedConfigurations.put("outlierProcedure", "true");
		consideratedConfigurations.put("campaignHandling", "true");
		consideratedConfigurations.put("limitCampaign", "true");
		consideratedConfigurations.put("campaignProcedure", "true");

    	JSONObject combinedConfigurations = configurations.getJSONObject("forecasting").getJSONObject("Combined");
		int forecastPeriods = combinedConfigurations.getInt("forecastPeriods");
		
		CallbackDBConnection.getInstance("CallbackDB");
		CallbackDAO callbackDAO = new CallbackDAO();
		ArrayList<String> forecastProcedureNames = new ArrayList<String>();
		JSONObject forecastProcedures = configurations.getJSONObject("forecasting");
		for(String forecastProcedureName : forecastProcedures.keySet()) {
			if(!forecastProcedureName.equals("Combined")) {
				boolean execution = forecastProcedures.getJSONObject(forecastProcedureName).getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(execution) {
			    	//overwrite forecasting specific configurations with shared combined parameters
					JSONObject procedureConfiguration = forecastProcedures.getJSONObject(forecastProcedureName);
					procedureConfiguration.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					weightCalculationValues.put(forecastProcedureName, callbackDAO.getForecastResults(procedureConfiguration, consideratedConfigurations, forecastProcedureName, fromDateString, toDateString));
					
					forecastProcedureNames.add(forecastProcedureName);
				}
			}
		}
		
		return weightCalculationValues;
		//weightCalculationValues = prepareAverageWeightCalculation(weightCalculationValues);
		//weights = calculateAverageWeights(weightCalculationValues, forecastDate, serviceNames, username);
	}
	
	public static JSONObject getActualDemandDaily(String fromDateString, String toDateString, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject actualResults = new JSONObject();
		BantelDAO evaluationDAO = new BantelDAO(passPhrase);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDateString));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		Date endDate = calendar.getTime();

		calendar.setTime(dateFormat.parse(fromDateString));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		Date weekBeginDate = calendar.getTime();
		String weekBeginDateString = dateFormat.format(weekBeginDate);	
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		Date weekEndDate = calendar.getTime();
		String weekEndDateString = dateFormat.format(weekEndDate);
		while(!weekBeginDate.after(endDate)) {
			JSONObject demand = evaluationDAO.getSalesAmounts(weekBeginDateString, weekEndDateString);
			JSONObject campaigns = evaluationDAO.getCampaignAmounts(weekBeginDateString, weekEndDateString);
			for(String skbez : demand.keySet()) {
				double totalDemand = demand.getDouble(skbez);
				double knownDemand = 0.0;
				double unknownDemand = totalDemand;
				if(campaigns.has(skbez)) {
					knownDemand = campaigns.getDouble(skbez);
					unknownDemand = totalDemand - knownDemand;
					
					if(unknownDemand < 0) {
						//ToDo: Should not be possible. Implement error message if occurs!!
						unknownDemand = totalDemand;
					}
				}
				demand.put(skbez, new JSONObject());
				demand.getJSONObject(skbez).put("totalDemand",totalDemand);
				demand.getJSONObject(skbez).put("knownDemand", knownDemand);
				demand.getJSONObject(skbez).put("unknownDemand", unknownDemand);	
			}
			JSONObject periodResult = new JSONObject();
			periodResult.put("1", demand);
			actualResults.put(weekBeginDateString, periodResult);
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			weekBeginDate = calendar.getTime();
			weekBeginDateString = dateFormat.format(weekBeginDate);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			weekEndDate = calendar.getTime();
			weekEndDateString = dateFormat.format(weekEndDate);
		}
		
		return actualResults;
	}
	
	public static JSONObject getActualResultsWeekly(String toDate, int forecastPeriods, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject actualResults = new JSONObject();
		BantelDAO evaluationDAO = new BantelDAO(passPhrase);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		int counter = 0;
		while(counter<forecastPeriods) {
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String fromDate = dateFormat.format(calendar.getTime());
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			toDate = dateFormat.format(calendar.getTime());	
			counter = counter + 1;
			
			//uncomment if campaigns are not considered		
			/**/
			JSONObject demand = evaluationDAO.getSalesAmounts(fromDate, toDate);
			JSONObject campaigns = evaluationDAO.getCampaignAmounts(fromDate, toDate);
			for(String skbez : demand.keySet()) {
				double totalDemand = demand.getDouble(skbez);
				double knownDemand = 0.0;
				double unknownDemand = totalDemand;
				if(campaigns.has(skbez)) {
					knownDemand = campaigns.getDouble(skbez);
					unknownDemand = totalDemand - knownDemand;
					if(unknownDemand < 0) {
						//ToDo: Should not be possible. Implement error message if occurs!!
						unknownDemand = totalDemand;
					}
				}
				demand.put(skbez, new JSONObject());
				demand.getJSONObject(skbez).put("totalDemand",totalDemand);
				demand.getJSONObject(skbez).put("knownDemand", knownDemand);
				demand.getJSONObject(skbez).put("unknownDemand", unknownDemand);	
			}
			
			/**/
			actualResults.put(Integer.toString(counter), demand);
		}
		return actualResults;
	}
	
	public static JSONObject getForecastDemandDaily(String toDate, int forecastPeriods, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject actualResults = new JSONObject();
		BantelDAO evaluationDAO = new BantelDAO(passPhrase);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		int counter = 0;
		while(counter<forecastPeriods) {
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			String fromDate = dateFormat.format(calendar.getTime());
			toDate = fromDate;
			counter = counter + 1;

			//uncomment if campaigns are not considered
			/**/
			JSONObject demand = evaluationDAO.getSalesAmounts(fromDate, toDate);
			JSONObject campaigns = evaluationDAO.getCampaignAmounts(fromDate, toDate);
			for(String skbez : demand.keySet()) {
				double totalDemand = demand.getDouble(skbez);
				double knownDemand = 0.0;
				double unknownDemand = totalDemand;
				if(campaigns.has(skbez)) {
					knownDemand = campaigns.getDouble(skbez);
					unknownDemand = totalDemand - knownDemand;
					if(unknownDemand < 0) {
						//ToDo: Should not be possible. Implement error message if occurs!!
						unknownDemand = totalDemand;
					}
				}
				demand.put(skbez, new JSONObject());
				demand.getJSONObject(skbez).put("totalDemand",totalDemand);
				demand.getJSONObject(skbez).put("knownDemand", knownDemand);
				demand.getJSONObject(skbez).put("unknownDemand", unknownDemand);	
			}
			/**/
			actualResults.put(Integer.toString(counter), demand);
			
		}
		return actualResults;
	}
	
	
	
	//Function of Bantel GmbH To get all ARIMA ForecastResults
	public static JSONObject getForecastResultsMulti(JSONObject configurations, JSONObject consideratedConfigurations, JSONArray executionRuns, String procedureName) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject forecastResults = new JSONObject();
		CallbackDBConnection.getInstance("CallbackDB");
		CallbackDAO callbackDAO = new CallbackDAO();
    	for(int i = 0; i<executionRuns.length();i++) {
    		String to = executionRuns.getJSONObject(i). getString("to");
    		String from = executionRuns.getJSONObject(i).getString("from");
    		JSONObject forecastResult = callbackDAO.getSingleForecastResult(configurations, consideratedConfigurations, procedureName, from, to);
    		if(forecastResult!=null) {
    			forecastResults.put(to, forecastResult);
    		}
    	}
    		
		return forecastResults;
	}
	
	
	public static JSONObject getCorrespondingDemandForForecastingValues(JSONArray executionRuns, JSONObject configurations, JSONObject loginCredentials) throws JSONException, ClassNotFoundException, SQLException, ParseException {
		JSONObject actualDemand = new JSONObject();
		String passPhrase = loginCredentials.getString("passPhrase");
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		for(int i = 0; i<executionRuns.length();i++) {
    		String forecastDate = executionRuns.getJSONObject(i). getString("to");
			//get actual demand for specific date if not already retrieved
			if(!actualDemand.has(forecastDate)) {
				if(aggregationOutputData.equals("DAILY")) {
					actualDemand.put(forecastDate, getForecastDemandDaily(forecastDate, forecastPeriods, passPhrase));
		    	}else {
		    		actualDemand.put(forecastDate, getActualResultsWeekly(forecastDate, forecastPeriods, passPhrase));
		    	}
			}
		}
		return actualDemand;
	}	
	
	
	public static JSONObject prepareEvaluationBantel(JSONObject forecastResult, JSONObject configurations, JSONObject loginCredentials) throws JSONException, ClassNotFoundException, SQLException, ParseException {
		JSONObject actualDemand = new JSONObject();
		String passPhrase = loginCredentials.getString("passPhrase");
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		JSONObject evaluationStructuredResults = new JSONObject();
		
		//for(String procedureName : forecastResult.keySet()) {
			//get procedure Results for specific date
			//JSONObject procedureResults = forecastResult.getJSONObject(procedureName);
			JSONObject procedureResults = forecastResult;
			
			for(String dateString : procedureResults.keySet()) {
				JSONObject forecastingConfigurations = procedureResults.getJSONObject(dateString);
				
				//get actual demand for specific date if not already retrieved
				if(!actualDemand.has(dateString)) {
					if(aggregationOutputData.equals("DAILY")) {
						actualDemand.put(dateString, getForecastDemandDaily(dateString, forecastPeriods, passPhrase));
			    	}else {
			    		actualDemand.put(dateString, getActualResultsWeekly(dateString, forecastPeriods, passPhrase));
			    	}
				}
				for(String configuration: forecastingConfigurations.keySet()) {
					JSONObject targetVariableResults = forecastingConfigurations.getJSONObject(configuration);
					for(String targetVariableName : targetVariableResults.keySet()) {
	        			JSONObject periodResults = targetVariableResults.getJSONObject(targetVariableName);
	        			JSONObject structuredConfigurations = new JSONObject();
	        			JSONObject structuredPeriod;
	        			for(String period : periodResults.keySet()) {
	        				JSONObject evaluationAttributes = new JSONObject();
		        			double result = periodResults.getDouble(period);
		        			
		        			//switch comment if campaings are not considered
		        			//double demand = 0;
		        			JSONObject demand = null;
		        			if(actualDemand.getJSONObject(dateString).getJSONObject(period).has(targetVariableName)) {
		        				//switch comment if campaigns are not considered
		        				//demand = actualDemand.getJSONObject(dateString).getJSONObject(period).getDouble(targetVariableName);
		        				demand = actualDemand.getJSONObject(dateString).getJSONObject(period).getJSONObject(targetVariableName);
		        			}
		        			periodResults.put(period, evaluationAttributes);
		        			evaluationAttributes.put("forecastResult", result);
		        			evaluationAttributes.put("demand", demand);
		        			
		        			/*JSONObject evaluationStructuredPeriodResult = new JSONObject();
		        			evaluationStructuredPeriodResult.put("forecastResult", result);
		        			evaluationStructuredPeriodResult.put("demand", actualDemand.getJSONObject(dateString).getDouble(period));
		        			evaluationStructuredVariableResult.put(targetVariableName, evaluationStructuredPeriodResult);
		        			evaluationStructuredResults.put(period, evaluationStructuredVariableResult);*/
		        			if(evaluationStructuredResults.has(targetVariableName)) {
		        				structuredConfigurations = evaluationStructuredResults.getJSONObject(targetVariableName);
		        			}else {
		        				structuredConfigurations = new JSONObject();
		        				evaluationStructuredResults.put(targetVariableName, structuredConfigurations);
		        			}
		        			if(structuredConfigurations.has(configuration)) {
		        				structuredPeriod = structuredConfigurations.getJSONObject(configuration);
		        			}else {
		        				structuredPeriod = new JSONObject();
		        				structuredConfigurations.put(configuration, structuredPeriod);
		        			}
		        			structuredPeriod.put(dateString, periodResults);
		        			
		        			//structuredConfigurations.getJSONObject(configuration).put(dateString, periodResults);	
		        		}
	        		/*	if(evaluationStructuredResults.has(targetVariableName)) {
	        				evaluationStructuredResults.get(targetVariableName, new JSONObject());
	        			}*//*
	        			//evaluationStructuredResults.getJSONObject(targetVariableName).put(configuration, structuredConfigurations);
	        			evaluationStructuredResults.get(targetVariableName).put(structuredConfigurations); */
	        		}
				}
    			
    			
			}
		//}
		return evaluationStructuredResults;
	}	
	
	//DELETE
	/*
	public static JSONObject prepareEvaluationBantelv1(JSONObject combinedAnalysisResults, JSONObject configurations, JSONObject loginCredentials) throws JSONException, ClassNotFoundException, SQLException, ParseException {
		JSONObject actualDemand = new JSONObject();
		String passPhrase = loginCredentials.getString("passPhrase");
		int forecastPeriods = configurations.getInt("forecastPeriods");
		JSONObject evaluationStructuredResults = new JSONObject();
		
		for(String dateString : combinedAnalysisResults.keySet()) {
			//get procedure Results for specific date
			JSONObject procedureResults = combinedAnalysisResults.getJSONObject(dateString);
			
			//get actual demand for specific date if not already retrieved
			if(!actualDemand.has(dateString)) {
				if(configurations.getString("aggregationOutputData").toUpperCase().equals("DAILY")) {
					actualDemand.put(dateString, getActualResultsDaily(dateString, forecastPeriods, passPhrase));
		    	}else {
		    		actualDemand.put(dateString, getActualResultsWeekly(dateString, forecastPeriods, passPhrase));
		    	}
			}
			for(String procedureName : procedureResults.keySet()) {
				JSONObject targetVariableResults = procedureResults.getJSONObject(procedureName);

    			for(String targetVariableName : targetVariableResults.keySet()) {
        			JSONObject periodResults = targetVariableResults.getJSONObject(targetVariableName);
        			for(String period : periodResults.keySet()) {
        				JSONObject evaluationAttributes = new JSONObject();
	        			double result = periodResults.getDouble(period);
	        			periodResults.put(period, evaluationAttributes);
	        			evaluationAttributes.put("forecastResult", result);
	        			evaluationAttributes.put("demand", actualDemand.getJSONObject(dateString).getJSONObject(period).getDouble(procedureName));
	        			

	        			
	        		}
        		}
    			evaluationStructuredResults.put(procedureName, new JSONObject());
    			if(!evaluationStructuredResults.has(procedureName)) {
    				evaluationStructuredResults.put(procedureName, new JSONObject());
    			}
    			evaluationStructuredResults.getJSONObject(procedureName).put(dateString, targetVariableResults);
			}
		}
		return evaluationStructuredResults;
	}	
	*/
}
