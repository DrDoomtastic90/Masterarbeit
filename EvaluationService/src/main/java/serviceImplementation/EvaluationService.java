package serviceImplementation;

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


public class EvaluationService {

	private static JSONObject evaluationMAE(JSONObject actualResults, JSONObject forecastingResults) {
		JSONObject diffSKBez = new JSONObject();
		for(String skbez : forecastingResults.keySet()) {
			JSONObject skbezResults = forecastingResults.getJSONObject(skbez);
			JSONObject diffKW = new JSONObject();
			for(String kw : skbezResults.keySet()) {
				double diff = skbezResults.getDouble(kw) - forecastingResults.getJSONObject(skbez).getDouble(kw);
				diffKW.put(kw, diff);
			}
			diffSKBez.put(skbez, diffKW);
		}
		return diffSKBez;
	}
	
	public static void evaluationCombined(JSONObject configAndResults) throws SQLException, ParseException {
		JSONObject diffResults = new JSONObject();
		JSONObject aRIMAResults = configAndResults.getJSONObject("results").getJSONObject("ARIMAResult");
		JSONObject ruleBasedResults = configAndResults.getJSONObject("results").getJSONObject("RuleBasedResult");
    	String fromDate = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("from");
    	String toDate = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
    	int forecastPeriods = configAndResults.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastingPeriods");
    	JSONObject actualResults = getActualResults(fromDate, toDate, forecastPeriods);
    	diffResults.put("ARIMADiff", evaluationMAE(actualResults, aRIMAResults));
    	diffResults.put("RuleBasedDiff", evaluationMAE(actualResults, ruleBasedResults));
    	System.out.println(diffResults);
	}
	
	public static JSONObject getActualResults(String fromDate, String toDate, int forecastPeriods) throws SQLException, ParseException {
		JSONObject actualResults = new JSONObject();
		EvaluationDAO evaluationDAO = new EvaluationDAO();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(fromDate)); 
		calendar.add(Calendar.DAY_OF_MONTH, - 7);
		int kwFrom = calendar.get(Calendar.WEEK_OF_YEAR);
		int weekCounter = 0;
		calendar.setTime(dateFormat.parse(toDate));
		dateFormat = new SimpleDateFormat("yy"); 
		String year = dateFormat.format(calendar.getTime());
		dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		int kwTo = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.set(Calendar.WEEK_OF_YEAR, kwTo); 
		int counter = 0;
		while(counter<forecastPeriods) {	
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			fromDate = dateFormat.format(calendar.getTime());
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			int kw = calendar.get(Calendar.WEEK_OF_YEAR);
			toDate = dateFormat.format(calendar.getTime());	
			actualResults.put(Integer.toString(kw), evaluationDAO.getSalesAmounts(fromDate, toDate));
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			counter = counter + 1;
		}
		return actualResults;
	}
		
}
