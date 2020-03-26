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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import dBConnection.EvaluationDAO;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class EvaluationService {

	private static JSONObject evaluationMAE(JSONObject actualResults, JSONObject forecastingResults) {
		JSONObject diffSKBez = new JSONObject();
		for(String skbez : forecastingResults.keySet()) {
			JSONObject skbezResults = forecastingResults.getJSONObject(skbez);
			JSONObject diffKW = new JSONObject();
			int counter = 0;
			double maeSum = 0;
			for(String kw : skbezResults.keySet()) {
				if(actualResults.getJSONObject(kw).has(skbez)){
					double diff = skbezResults.getDouble(kw) - actualResults.getJSONObject(kw).getDouble(skbez);
					diffKW.put(kw, diff);
					counter = counter + 1;
					maeSum = maeSum + Math.abs(diff);
				}else {
					diffKW.put(kw, 0);
				}
			}
			//To not devide by 0
			if(counter <= 0) {
				counter = 1;
			}
			double mae = maeSum / counter;
			diffKW.put("MAE", mae);
			diffSKBez.put(skbez, diffKW);
		}
		return diffSKBez;
	}
	
	private static JSONObject evaluationSMAE(JSONObject alternativeForecastingResult, JSONObject benchmarkResult) {
		JSONObject maseResults = new JSONObject();
		int counter = 0;
		double maseSum = 0;
		for(String skbez : alternativeForecastingResult.keySet()) {
			double altMAE = alternativeForecastingResult.getJSONObject(skbez).getDouble("MAE");
			double benchMAE = Math.abs(benchmarkResult.getJSONObject(skbez).getDouble("1"));
			double mase = 0;
			if(benchMAE<=0) {
				System.out.println("STOP");			
				counter=counter - 1;
			}else {
				mase = Math.abs(altMAE/benchMAE);
			}
			maseResults.put(skbez, mase);
			maseSum = maseSum + mase;
			counter = counter + 1;
		}
		//To not devide by 0
		if(counter <= 0) {
			counter = 1;
		}
		
		double smaeTot = maseSum / counter;
		maseResults.put("SMAETot",smaeTot);
		return maseResults;
	}
	
	
	private static JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	public static void evaluationCombined(JSONObject configAndResults) throws SQLException, ParseException, ClassNotFoundException, IOException {
		JSONObject diffResults = new JSONObject();
		JSONObject configurations = configAndResults.getJSONObject("forecasting").getJSONObject("Combined");
		JSONObject loginCredentials = invokeLoginService(configurations);
    	String passPhrase = loginCredentials.getString("passPhrase");
		configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
		String fromDate = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("from");
    	String toDate = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
    	int forecastPeriods = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastPeriods");
    	JSONObject actualResults = getActualResults(fromDate, toDate, forecastPeriods, passPhrase);
    	CustomFileWriter.createJSON("D:/Arbeit/Bantel/Masterarbeit/Implementierung/Bantel/Daten/Actual_RESULT.json", actualResults.toString());
    	
		JSONObject aRIMAResults = configAndResults.getJSONObject("results").getJSONObject("ARIMAResult");
		JSONObject ruleBasedResults = configAndResults.getJSONObject("results").getJSONObject("RuleBasedResult");
		JSONObject aNNResult = configAndResults.getJSONObject("results").getJSONObject("ANNResult");
		JSONObject kalmanResult = configAndResults.getJSONObject("results").getJSONObject("KalmanResult");
		JSONObject smoothedCombResult = configAndResults.getJSONObject("results").getJSONObject("ExpSmoothingResult");
		JSONObject combResult = configAndResults.getJSONObject("results").getJSONObject("CombinedResult");
    	diffResults.put("ARIMADiff", evaluationMAE(actualResults, aRIMAResults));
    	diffResults.put("RuleBasedDiff", evaluationMAE(actualResults, ruleBasedResults));
    	diffResults.put("ANNDiff", evaluationMAE(actualResults, aNNResult));
    	diffResults.put("KalmanDiff", evaluationMAE(actualResults, kalmanResult));
    	diffResults.put("ExpSmoothingbDiff", evaluationMAE(actualResults, smoothedCombResult));
    	diffResults.put("CombDiff", evaluationMAE(actualResults, combResult));
    	diffResults.put("MASE_ARIMA", evaluationSMAE(diffResults.getJSONObject("ExpSmoothingbDiff"),diffResults.getJSONObject("ARIMADiff")));
    	diffResults.put("MASE_RuleBased", evaluationSMAE(diffResults.getJSONObject("ExpSmoothingbDiff"),diffResults.getJSONObject("RuleBasedDiff")));
    	diffResults.put("MASE_ANNs", evaluationSMAE(diffResults.getJSONObject("ExpSmoothingbDiff"),diffResults.getJSONObject("ANNDiff")));
    	diffResults.put("MASE_Kalman", evaluationSMAE(diffResults.getJSONObject("ExpSmoothingbDiff"),diffResults.getJSONObject("KalmanDiff")));
    	diffResults.put("MASE_Comb", evaluationSMAE(diffResults.getJSONObject("ExpSmoothingbDiff"),diffResults.getJSONObject("CombDiff")));
    	
    	System.out.println(diffResults);
    	CustomFileWriter.createJSON("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Evaluation\\EvaluationResults.json", diffResults.toString());
	}
	
	public static JSONObject getActualResults(String fromDate, String toDate, int forecastPeriods, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
		JSONObject actualResults = new JSONObject();
		EvaluationDAO evaluationDAO = new EvaluationDAO(passPhrase);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		int counter = 0;
		while(counter<forecastPeriods) {
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			int kw = calendar.get(Calendar.WEEK_OF_YEAR);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			fromDate = dateFormat.format(calendar.getTime());
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			toDate = dateFormat.format(calendar.getTime());	
			counter = counter + 1;
			actualResults.put(Integer.toString(counter), evaluationDAO.getSalesAmounts(fromDate, toDate));
		}
		return actualResults;
	}
		
}
