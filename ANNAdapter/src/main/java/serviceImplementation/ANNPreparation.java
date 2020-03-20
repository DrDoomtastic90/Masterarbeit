package serviceImplementation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.PreparationDAO;
import errorHandler.UniqueConstraintException;
import outputHandler.CustomFileWriter;



public class ANNPreparation extends Analysis {
	
	public static JSONArray prepareDailyDataset(JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		return prepareDailyDataSet("", configurations);
	}

	public static JSONArray prepareDailyDataSet(String kNo, JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		PreparationDAO daoAnalysis = new PreparationDAO(configurations.getString("passPhrase"));
		boolean campaigns = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
		JSONObject dailyAggregatedData = DataAggregator.aggregateSorteDataDaily(kNo, configurations, daoAnalysis);
		//Consideration of Last YEar values not implemented yet
		//double valueLastYear =0;
		//double valueLastYearMinus1Week=0;
		JSONArray preparedDataDaily= new JSONArray();
		double currentValue=0;
		double currentValueMinus1Week=0;
		double currentValueMinus2Week=0;
		double futureValue=0;
		int counter = 0;
		for (String sorte : dailyAggregatedData.keySet()) {
			for (String dateEntry : dailyAggregatedData.getJSONObject(sorte).keySet()) {
				currentValueMinus2Week=currentValueMinus1Week;
				currentValueMinus1Week=currentValue;
				currentValue = futureValue;
				futureValue = dailyAggregatedData.getJSONObject(sorte).getDouble(dateEntry);
				counter = counter + 1;
				if(counter>=4) {
					double[] input = {currentValue, currentValueMinus1Week, currentValueMinus2Week};
					double[] output = {futureValue};
					JSONObject inputOuputMap = new JSONObject();
					inputOuputMap.put("input", input);
					inputOuputMap.put("output",output);
					preparedDataDaily.put(inputOuputMap);
				}
			}
			//String content = PreparationDAO.dataToCSVString(datastructList, false, campaigns );
			//CustomFileWriter.createCSV("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Daten\\Bantel\\ARIMA\\DAILY\\" + sorte + ".csv", content);
			//responseContent.put(sorte, content);
		}
		return preparedDataDaily;
	}

	public void SorteAnalysisAllCustomers(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException {
		PreparationDAO daoAnalysis = new PreparationDAO(configurations.getString("passPhrase"));
		try {
			List<String> kNoList = daoAnalysis.getAllKNo();
			for (String kNo : kNoList) {
				// Map<String,Map<String, Datastruct>> sorteAnalysis =
				// DailyAggregation.getSorteDaten(kNo);
				prepareDailyDataSet(kNo, configurations);
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
}