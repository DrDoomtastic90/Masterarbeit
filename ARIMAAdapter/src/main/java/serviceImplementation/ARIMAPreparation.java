package serviceImplementation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.BantelDBConnection;
import dBConnections.PreparationDAO;
import errorHandler.UniqueConstraintException;
import outputHandler.CustomFileWriter;



public class ARIMAPreparation extends Analysis {
	
	public static JSONObject getSorteDataDaily(JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, UniqueConstraintException, ParseException {
		return getSorteDataDaily("", configurations);
	}

	public static JSONObject getSorteDataDaily(String kNo, JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, UniqueConstraintException, ParseException {
		JSONObject responseContent = new JSONObject();
		
		//setup DB Connection for data access
		String passPhrase = configurations.getString("passPhrase");
		PreparationDAO preparationDAO = new PreparationDAO(BantelDBConnection.getInstance(passPhrase));
		
		//get start and end date of retrieval period
		String fromDate = configurations.getJSONObject("data").getString("from");
		String toDate = configurations.getJSONObject("data").getString("to");
		
		//initialize Map used to provide prepared data
		Map<String, Map<String, Datastruct>> dataDailySorteAll = preparationDAO.initializePreparedDataMap(fromDate, toDate);
		
		//get sales amounts within retrieval period
		Map<String, Map<String, Double>> salesDataDailySorteAll = preparationDAO.getSalesDataDailySorte(fromDate, toDate);
		
		//get campaigns amounts within retrieval period
		Map<String, Map<String, Double>> campaignsDataDailySorteAll = preparationDAO.getCampaignsDataDailySorte(fromDate, toDate);
		
		//prepare data
		for(String sorte : dataDailySorteAll.keySet()) {
			//should always be true due to left join used in query. If not a mapping error exists
			if(salesDataDailySorteAll.containsKey(sorte)) {
				for(String dateEntry: dataDailySorteAll.get(sorte).keySet()) {
					double salesAmount = 0;
					double campaignsAmount = 0;
					//get sales amount if available for a specific date.
					if(salesDataDailySorteAll.get(sorte).containsKey(dateEntry)) {
						salesAmount = salesDataDailySorteAll.get(sorte).get(dateEntry);	
					}
					//get campaign amount if available for specific date
					if(campaignsDataDailySorteAll.containsKey(sorte) && campaignsDataDailySorteAll.get(sorte).containsKey(dateEntry)) {
						campaignsAmount = campaignsDataDailySorteAll.get(sorte).get(dateEntry);	
					}
					//reduce sales amount by campaign amount
					double forecastAmount = salesAmount - campaignsAmount;
					
					//add relevant forecasting quantity to prepared data
					dataDailySorteAll.get(sorte).get(dateEntry).setMenge(forecastAmount);
					
					//add weekday dummy to prepared data
					dataDailySorteAll.get(sorte).get(dateEntry).setWeekDayDummies(calculateWeekDay(dateEntry));
					
					//add month dummy to prepared data
					dataDailySorteAll.get(sorte).get(dateEntry).setMonthDummies(calculateMonth(dateEntry));
					
					//add easter dummy
					dataDailySorteAll.get(sorte).get(dateEntry).setEasterDummy(calculateEasterPeriod(dateEntry));
				}
				String content = createCSVStringSorte(dataDailySorteAll.get(sorte));
				responseContent.put(sorte, content);
			}
		}	
		return responseContent;
	}
	
	
	public static JSONObject getSalesDataWeekly (JSONObject configurations) throws ClassNotFoundException, SQLException, ParseException {
		JSONObject actualResults = new JSONObject();
		
		//setup DB Connection for data access
		String passPhrase = configurations.getString("passPhrase");
		PreparationDAO preparationDAO = new PreparationDAO(BantelDBConnection.getInstance(passPhrase));
		
		//get start and end date of retrieval period
		String toDate = configurations.getJSONObject("data").getString("to");
		int forecastPeriods = configurations.getInt("forecastPeriods");
		
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
			actualResults.put(Integer.toString(counter), preparationDAO.getSalesAmountsDaily(fromDate, toDate));
		}
		return actualResults;
	}
		
		

	
	private static String calculateWeekDay(String dateString) {
    	StringBuilder weekDayDummies = new StringBuilder();
		LocalDate localDate = LocalDate.parse(dateString);
    	int weekday = localDate.getDayOfWeek().getValue();
    	boolean first = true;
    	for(int i = 1; i<8;i++) {
    		if(i == weekday) {
    			if(first) {
    				weekDayDummies.append("1");
    				first = false;
    			}else {
    				weekDayDummies.append(",1");
    			}	
    		} else {
    			if(first) {
    				weekDayDummies.append("0");
    				first = false;
    			}else {
    				weekDayDummies.append(",0");
    			}	
    		}
    	}
		return weekDayDummies.toString();
	}
	
	private static String calculateMonth(String dateString) {
		StringBuilder monthDummies = new StringBuilder();
		LocalDate localDate = LocalDate.parse(dateString);
    	int month = localDate.getMonthValue();
    	boolean first = true;
    	for(int i = 1; i<13;i++) {
    		if(i == month) {
    			if(first) {
    				monthDummies.append("1");
    				first = false;
    			}else {
    				monthDummies.append(",1");
    			}			
    		}
    		else {
    			if(first) {
    				monthDummies.append("0");
    				first = false;
    			}else {
    				monthDummies.append(",0");
    			}	
    		}
    	}
		return monthDummies.toString();
	}
	
	//calculate Easter
	//from https://dzone.com/articles/algorithm-calculating-date
	private static String calculateEasterPeriod(String dateString) throws ParseException {
		int year =  Integer.parseInt(dateString.split("-")[0]);
	    int Y = year;
	    int a = Y % 19;
	    int b = Y / 100;
	    int c = Y % 100;
	    int d = b / 4;
	    int e = b % 4;
	    int f = (b + 8) / 25;
	    int g = (b - f + 1) / 3;
	    int h = (19 * a + b - d - g + 15) % 30;
	    int i = c / 4;
	    int k = c % 4;
	    int L = (32 + 2 * e + 2 * i - h - k) % 7;
	    int m = (a + 11 * h + 22 * L) / 451;
	    int month = (h + L - 7 * m + 114) / 31;
	    int day = ((h + L - 7 * m + 114) % 31) + 1;
	    String easterSunday = year+"-"+month+"-"+day;
	    Calendar calendar = new GregorianCalendar(Locale.GERMAN);
	    DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(easterSunday)); 
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.DAY_OF_MONTH, - 1);
		Date beginDate = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		Date endDate = calendar.getTime();
		Date currentDate = dateFormat.parse(dateString);
		if(currentDate.after(beginDate) && currentDate.before(endDate)) {
			return "1";
		}else {
			return "0";
		}
	}

	public static String createCSVStringSorte(Map<String, Datastruct> contentMap){
		StringBuilder csvStringBuilder = new StringBuilder();
		csvStringBuilder.append("Datum");
		csvStringBuilder.append(",");
		csvStringBuilder.append(Datastruct.writeAttributeNamesAsCSVString());
		for(String dateEntry : contentMap.keySet()) {
			csvStringBuilder.append(dateEntry);
			csvStringBuilder.append(",");
			csvStringBuilder.append(contentMap.get(dateEntry).writeAttributeValuesAsCSVString());
		}
		return csvStringBuilder.toString();
	}

	public static void sorteAnalysisWeekly(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		sorteAnalysisWeekly("", configurations);
	}

	public static void sorteAnalysisWeekly(String kNo, /*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		//String fromDate = configurations.get("data").get("fromDate");
		//String toDate = configurations.get("data").get("toDate");
		//setup DB Connection for data access
		String passPhrase = configurations.getString("passPhrase");
		PreparationDAO preparationDAO = new PreparationDAO(BantelDBConnection.getInstance(passPhrase));
		Map<String, Map<String, Datastruct>> sorteAnalysisDaily = DataAggregator.aggregateSorteDataDaily(kNo, /*fromDate, toDate*/ configurations, preparationDAO);
		Map<String, Map<String, Datastruct>> sorteAnalysisWeekly = WeeklyAnalysis
				.aggregateDataWeekly(sorteAnalysisDaily);
		for (String sorte : sorteAnalysisWeekly.keySet()) {
			//System.out.println(sorte);
			List<Datastruct> datastructList = new ArrayList<Datastruct>();
			for (String dateEntry : sorteAnalysisWeekly.get(sorte).keySet()) {
				datastructList.add(sorteAnalysisWeekly.get(sorte).get(dateEntry));
				//System.out.println(sorteAnalysisWeekly.get(sorte).get(dateEntry).getDatum() + " "
				//		+ sorteAnalysisWeekly.get(sorte).get(dateEntry).getMenge());
			}
			boolean campaigns = Boolean.parseBoolean(configurations.getJSONObject("parameters").getString("campaigns")); //configurations.get("parameters").get("campaigns"));
			boolean nA = Boolean.parseBoolean(configurations.getJSONObject("parameters").getString("navalues")); //configurations.get("parameters").get("navalues"));
			createSorteCSVWeekly(sorte, datastructList, campaigns, kNo, nA);
		}
	}

	public void SorteAnalysisAllCustomers(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		//setup DB Connection for data access
		String passPhrase = configurations.getString("passPhrase");
		PreparationDAO preparationDAO = new PreparationDAO(BantelDBConnection.getInstance(passPhrase));
		try {
			List<String> kNoList = preparationDAO.getAllKNo();
			for (String kNo : kNoList) {
				// Map<String,Map<String, Datastruct>> sorteAnalysis =
				// DailyAggregation.getSorteDaten(kNo);
				getSorteDataDaily(kNo, configurations);
			}
		} catch (UniqueConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String createSorteCSVDaily(String sorte, List<Datastruct> dataSet, JSONObject configurations) throws JSONException {
		boolean campaigns = Boolean.parseBoolean(configurations.getJSONObject("parameters").getJSONObject("campaigns").getString("contained"));
		String content = PreparationDAO.dataToCSVString(dataSet, false, campaigns );
		String path = configurations.getJSONObject("data").getString("preprocessed") + "/" + sorte + ".csv";
		return path;
	}

	public static void createSorteCSVWeekly(String sorte, List<Datastruct> dataSet, boolean campaigns, String kNo,
			boolean nA) {
		String content = PreparationDAO.dataToCSVString(dataSet, false, campaigns);
		String analysis = "";
		if (campaigns) {
			analysis = "SorteAnalysisWeeklyWCampaigns";
		} else {
			analysis = "SorteAnalysisWeekly";
		}
		if (kNo.length() > 0) {
			analysis = analysis + "\\" + kNo;
		} else {
			analysis = analysis + "\\all";
		}
		String path = "D:\\Arbeit\\Bantel\\Masterarbeit\\Daten\\" + analysis + "\\" + sorte
				+ "_SorteAnalysisWeekly.csv";
	}

	private static Map<String, Map<String, Datastruct>> getDataWeeklyAggregated(String kNo, /*String fromDate, String toDate */ JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		String passPhrase = configurations.getString("passPhrase");
		PreparationDAO preparationDAO = new PreparationDAO(BantelDBConnection.getInstance(passPhrase));
		Map<String, Map<String, Datastruct>> sorteAnalysisDaily = DataAggregator.aggregateSorteDataDaily(kNo, configurations /*fromDate, toDate*/, preparationDAO);
		Map<String, Map<String, Datastruct>> sorteAnalysisWeekly = WeeklyAnalysis
				.aggregateDataWeekly(sorteAnalysisDaily);
		return sorteAnalysisWeekly;
	}
}