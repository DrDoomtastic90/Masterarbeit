package serviceImplementation;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.AnalysisDAO;
import errorHandler.UniqueConstraintException;





public class WeeklyAnalysis extends Analysis{

	/*public WeeklyAnalysis(String sourePath) {
		super(sourePath);
	}*/
	public  void weeklyAnalysis(JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		weeklyAnalysis("", configurations);
	}
	public void weeklyAnalysis(String kNo, JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		Map<String, Map<String, Datastruct>> weeklyAnalysis = getWeeklyData(kNo, configurations);
		for (String pKBez : weeklyAnalysis.keySet()) {
			List<Datastruct> datastructList = new ArrayList<Datastruct>();
			for (String dateEntry : weeklyAnalysis.get(pKBez).keySet()) {
				datastructList.add(weeklyAnalysis.get(pKBez).get(dateEntry));
			}
			boolean campaigns = Boolean.parseBoolean(configurations.getJSONObject("parameters").getString("campaigns"));
			 boolean nA =  Boolean.parseBoolean(configurations.getJSONObject("parameters").getString("navalues"));
			createWeeklyCSV(pKBez, datastructList, campaigns, kNo, nA);
		}
	}

	public void WeeklyAnalysisAllCustomers(JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		try {
			AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
			List<String> kNoList = daoAnalysis.getAllKNo();
			for (String kNo : kNoList) {
				weeklyAnalysis(kNo, configurations);
			}
		} catch (UniqueConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Datastruct>> getWeeklyData(String kNo, JSONObject configurations/* String fromDate, String toDate*/) throws JSONException, ClassNotFoundException, SQLException {
		AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
		Map<String, Map<String, Datastruct>> dailyAnalysis = daoAnalysis.getDataDaily(kNo, configurations /*fromDate, toDate*/);		
		Map<String, Map<String, Datastruct>>weeklyAnalysis= aggregateDataWeekly(dailyAnalysis);	
		return weeklyAnalysis;
	}

	private static int getWeekNumberFromDate(Date date) {
		Calendar cal = Calendar.getInstance(Locale.GERMAN);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	private static void createWeeklyCSV(String pKBez, List<Datastruct> dataSet, boolean campaigns, String kNo, boolean nA) {
		String content = AnalysisDAO.dataToCSVString(dataSet, false, campaigns, nA);
		String analysis = "";
		if (campaigns) {
			analysis = "WeeklyAnalysisCampaigns";
		} else {
			analysis = "WeeklyAnalysis";
		}
		if (kNo.length() > 0) {
			analysis = analysis + "\\" + kNo;
		} else {
			analysis = analysis + "\\all";
		}
		String path = "D:\\Arbeit\\Bantel\\Masterarbeit\\Daten\\" + analysis + "\\" + pKBez + "_WeeklyAnalysis.csv";
		System.out.println(path);
	}
	public static Map<String, Map<String, Datastruct>> aggregateDataWeekly(Map<String, Map<String, Datastruct>> dailyAnalysis){
		Map<String, Map<String, Datastruct>> weeklyAnalysis = new LinkedHashMap<String, Map<String, Datastruct>>();
		boolean isAktion = false;
		Datastruct datastruct = null;
		for (String pKBez : dailyAnalysis.keySet()) {
			Map<String, Datastruct> dailyValues = dailyAnalysis.get(pKBez);
			Map<String, Datastruct> weeklyData = new LinkedHashMap<String, Datastruct>();
			if (dailyAnalysis.get(pKBez) != null) {
				for (String dateEntry : dailyValues.keySet()) {
					try {
						Date datum = new SimpleDateFormat("yyyy-MM-dd").parse(dateEntry);

						String kW = Integer.toString(getWeekNumberFromDate(datum));
						String kWYearly = kW + "-" + dateEntry.substring(2, 4);
						isAktion = dailyValues.get(dateEntry).getAktion();
						double menge = dailyValues.get(dateEntry).getMenge();
						if (weeklyData.size() > 0 && (weeklyData.containsKey(kWYearly))) {
							weeklyData.get(kWYearly).setMenge(weeklyData.get(kWYearly).getMenge() + menge);
							if (isAktion) {
								datastruct.setAktion(isAktion);
							}
						} else {
							datastruct = new Datastruct(kWYearly, menge, isAktion);
							weeklyData.put(kWYearly, datastruct);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				weeklyAnalysis.put(pKBez, weeklyData);
			}
		}
		return weeklyAnalysis;
	}

}
