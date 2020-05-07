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
import java.util.ArrayList;
import java.util.Calendar;
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
			actualResults.put(Integer.toString(counter), evaluationDAO.getSalesAmounts(fromDate, toDate));
		}
		return actualResults;
	}
	
	public static JSONObject getActualResultsDaily(String toDate, int forecastPeriods, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
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
			actualResults.put(Integer.toString(counter), evaluationDAO.getSalesAmounts(fromDate, toDate));
		}
		return actualResults;
	}
	
	//Function of Bantel GmbH To get all ARIMA ForecastResults
	public static JSONObject getForecastResultsMulti(JSONObject configurations, JSONArray executionRuns, String procedureName) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject forecastResults = new JSONObject();
		CallbackDBConnection.getInstance("CallbackDB");
		CallbackDAO callbackDAO = new CallbackDAO();
    	for(int i = 0; i<executionRuns.length();i++) {
    		String to = executionRuns.getJSONObject(i). getString("to");
    		String from = executionRuns.getJSONObject(i).getString("from");
    		forecastResults.put(to, callbackDAO.getForecastResult(configurations, procedureName, from, to));
    	}
    		
		return forecastResults;
	}
	
	
	
	public static JSONObject prepareEvaluationBantel(JSONObject forecastResult, JSONObject configurations, JSONObject loginCredentials) throws JSONException, ClassNotFoundException, SQLException, ParseException {
		JSONObject actualDemand = new JSONObject();
		String passPhrase = loginCredentials.getString("passPhrase");
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		String aggregationOutputData = configurations.getJSONObject("parameters").getString("aggregationOutputData").toUpperCase();
		JSONObject evaluationStructuredResults = new JSONObject();
		
		for(String procedureName : forecastResult.keySet()) {
			//get procedure Results for specific date
			JSONObject procedureResults = forecastResult.getJSONObject(procedureName);
			
			
			for(String dateString : procedureResults.keySet()) {
				JSONObject targetVariableResults = procedureResults.getJSONObject(dateString);
				
				//get actual demand for specific date if not already retrieved
				if(!actualDemand.has(dateString)) {
					if(aggregationOutputData.equals("DAILY")) {
						actualDemand.put(dateString, getActualResultsDaily(dateString, forecastPeriods, passPhrase));
			    	}else {
			    		actualDemand.put(dateString, getActualResultsWeekly(dateString, forecastPeriods, passPhrase));
			    	}
				}

    			for(String targetVariableName : targetVariableResults.keySet()) {
        			JSONObject periodResults = targetVariableResults.getJSONObject(targetVariableName);
        			for(String period : periodResults.keySet()) {
        				JSONObject evaluationAttributes = new JSONObject();
	        			double result = periodResults.getDouble(period);
	        			double demand = 0;
	        			if(actualDemand.getJSONObject(dateString).getJSONObject(period).has(targetVariableName)) {
	        				demand = actualDemand.getJSONObject(dateString).getJSONObject(period).getDouble(targetVariableName);
	        			}
	        			periodResults.put(period, evaluationAttributes);
	        			evaluationAttributes.put("forecastResult", result);
	        			evaluationAttributes.put("demand", demand);
	        			
	        			/*JSONObject evaluationStructuredPeriodResult = new JSONObject();
	        			evaluationStructuredPeriodResult.put("forecastResult", result);
	        			evaluationStructuredPeriodResult.put("demand", actualDemand.getJSONObject(dateString).getDouble(period));
	        			evaluationStructuredVariableResult.put(targetVariableName, evaluationStructuredPeriodResult);
	        			evaluationStructuredResults.put(period, evaluationStructuredVariableResult);*/
	        			
	        		}
        			if(!evaluationStructuredResults.has(targetVariableName)) {
        				evaluationStructuredResults.put(targetVariableName, new JSONObject());
        			}
        			evaluationStructuredResults.getJSONObject(targetVariableName).put(dateString, periodResults);	
        		}
    			
			}
		}
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
