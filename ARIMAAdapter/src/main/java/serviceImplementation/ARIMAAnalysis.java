package serviceImplementation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.AnalysisDAO;
import errorHandler.UniqueConstraintException;



public class ARIMAAnalysis extends Analysis {
	
	public static JSONObject sorteAnalysisDaily(JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		return sorteAnalysisDaily("", configurations);
	}

	public static JSONObject sorteAnalysisDaily(String kNo, JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
		
		Map<String, Map<String, Datastruct>> dailyAggregatedData = DataAggregator.aggregateSorteDataDaily(kNo, configurations, daoAnalysis);
		for (String sorte : dailyAggregatedData.keySet()) {
			//System.out.println(sorte);
			List<Datastruct> datastructList = new ArrayList<Datastruct>();
			for (String dateEntry : dailyAggregatedData.get(sorte).keySet()) {
				datastructList.add(dailyAggregatedData.get(sorte).get(dateEntry));
				//System.out.println(dailyAggregatedData.get(sorte).get(dateEntry).getDatum() + " "
				//		+ dailyAggregatedData.get(sorte).get(dateEntry).getMenge());
			}
			String path = createSorteCSVDaily(sorte, datastructList, configurations);
			responseContent.put(sorte, path);
		}
		return responseContent;
	}

	public static void sorteAnalysisWeekly(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		sorteAnalysisWeekly("", configurations);
	}

	public static void sorteAnalysisWeekly(String kNo, /*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, ClassNotFoundException, SQLException {
		//String fromDate = configurations.get("data").get("fromDate");
		//String toDate = configurations.get("data").get("toDate");
		AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
		Map<String, Map<String, Datastruct>> sorteAnalysisDaily = DataAggregator.aggregateSorteDataDaily(kNo, /*fromDate, toDate*/ configurations, daoAnalysis);
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

	public void SorteAnalysisAllCustomers(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
		try {
			List<String> kNoList = daoAnalysis.getAllKNo();
			for (String kNo : kNoList) {
				// Map<String,Map<String, Datastruct>> sorteAnalysis =
				// DailyAggregation.getSorteDaten(kNo);
				sorteAnalysisDaily(kNo, configurations);
			}
		} catch (UniqueConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String createSorteCSVDaily(String sorte, List<Datastruct> dataSet, JSONObject configurations) throws JSONException {
		boolean campaigns = configurations.getJSONObject("parameters").getBoolean("campaigns");
		boolean nA = configurations.getJSONObject("parameters").getBoolean("navalues");

		
		String content = AnalysisDAO.dataToCSVString(dataSet, false, campaigns, nA);
		//String analysis = "";
		/*if (campaigns) {
			analysis = "SorteAnalysisDailyWCampaigns";
		} else {
			analysis = "SorteAnalysisDaily";
		}
		if (kNo.length() > 0) {
			analysis = "/" + analysis + "/" + kNo;
		} else {
			analysis = "/" + analysis + "/all";
		}
		String path = configurations.get("data").get("sourcepath") + analysis + "/" + sorte +  ".csv";
		*/
		//String path = configurations.get("data").get("sourcepath") + "/" + sorte + ".csv";
		String path = configurations.getJSONObject("data").getString("preprocessed") + "/" + sorte + ".csv";
		return path;
	}

	public static void createSorteCSVWeekly(String sorte, List<Datastruct> dataSet, boolean campaigns, String kNo,
			boolean nA) {
		String content = AnalysisDAO.dataToCSVString(dataSet, false, campaigns, nA);
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
		AnalysisDAO daoAnalysis = new AnalysisDAO(configurations.getString("passPhrase"));
		Map<String, Map<String, Datastruct>> sorteAnalysisDaily = DataAggregator.aggregateSorteDataDaily(kNo, configurations /*fromDate, toDate*/, daoAnalysis);
		Map<String, Map<String, Datastruct>> sorteAnalysisWeekly = WeeklyAnalysis
				.aggregateDataWeekly(sorteAnalysisDaily);
		return sorteAnalysisWeekly;
	}
}