package serviceImplementation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.PreparationDAO;
import errorHandler.UniqueConstraintException;
import outputHandler.CustomFileWriter;



public class ANNPreparation extends Analysis {
	
	public static JSONObject prepareDailyDatasetWeeklyOutput(JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		return prepareDailyDataSetWeeklyOutput("", configurations);
	}

	public static JSONObject prepareDailyDataSetWeeklyOutput(String kNo, JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		PreparationDAO daoAnalysis = new PreparationDAO(configurations.getString("passPhrase"));
		//boolean campaigns = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
		Map<String,Map<String,Double>> dailyAggregatedData = DataAggregator.aggregateSorteDataDaily(kNo, configurations, daoAnalysis);
		//Consideration of Last YEar values not implemented yet
		//double valueLastYear =0;
		//double valueLastYearMinus1Week=0;

		Map<String, ArrayList<double[]>> preparedDataAllSorte = new LinkedHashMap<String, ArrayList<double[]>>();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		double mondayCurrent=0;
		double tuesdayCurrent=0;
		double wednessdayCurrent=0;
		double thursdayCurrent=0;
		double fridayCurrent=0;
		double saturdayCurrent=0;
		double sundayCurrent=0;
		int dayCounter = 0;
		int weekCounter = 0;
		for (String sorte : dailyAggregatedData.keySet()) {
			ArrayList<double[]> preparedDataSingleSorte = new ArrayList<double[]>();
			double[] monthClassifier = new double[12];
			for (String dateEntry : dailyAggregatedData.get(sorte).keySet()) {
				dayCounter=dayCounter+1;
				weekCounter=weekCounter+1;
				cal.setTime(dateformat.parse(dateEntry));
				int day = cal.get(Calendar.DAY_OF_WEEK);
				switch (day) {
					case Calendar.MONDAY:
						mondayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.TUESDAY:
						tuesdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.WEDNESDAY:
						wednessdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.THURSDAY:
						thursdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.FRIDAY:
						fridayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.SATURDAY:
						saturdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.SUNDAY:
						sundayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;	
					default:
						throw new RuntimeException("Not a day");
				}
				
				
				String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
				switch (month) {
					case "January":
						monthClassifier[0]=monthClassifier[0]+1;
						break;
					case "February":
						monthClassifier[1]=monthClassifier[1]+1;
						break;
					case "March":
						monthClassifier[2]=monthClassifier[2]+1;
						break;
					case "April":
						monthClassifier[3]=monthClassifier[3]+1;
						break;
					case "May":
						monthClassifier[4]=monthClassifier[4]+1;
						break;
					case "June":
						monthClassifier[5]=monthClassifier[5]+1;
						break;
					case "July":
						monthClassifier[6]=monthClassifier[6]+1;
						break;
					case "August":
						monthClassifier[7]=monthClassifier[7]+1;
						break;
					case "September":
						monthClassifier[8]=monthClassifier[8]+1;
						break;
					case "October":
						monthClassifier[9]=monthClassifier[9]+1;
						break;
					case "November":
						monthClassifier[10]=monthClassifier[10]+1;
						break;
					case "December":
						monthClassifier[11]=monthClassifier[11]+1;
						break;
					default:
						throw new RuntimeException("Not a month");	
				}
				
				if(dayCounter>=7) {
					double[] weekValues = {
							mondayCurrent, 
							tuesdayCurrent,
							wednessdayCurrent,
							thursdayCurrent,
							fridayCurrent,
							saturdayCurrent,
							sundayCurrent
					};
					List<Double> monthInputList = Arrays.asList(ArrayUtils.toObject(monthClassifier));
			        double max = Collections.max(monthInputList);
			        int index  = monthInputList.indexOf(max);
			        double[] monthInputArray = new double[12];
			        monthInputArray[index] = 1;
			        
			        double[] sum = {mondayCurrent+tuesdayCurrent+wednessdayCurrent+thursdayCurrent+fridayCurrent+saturdayCurrent+sundayCurrent};
					double[] preparedDataWeek = ArrayUtils.addAll(sum, monthInputArray);
			        preparedDataWeek = ArrayUtils.addAll(preparedDataWeek, weekValues);
			        preparedDataSingleSorte.add(preparedDataWeek);
					dayCounter = 0;
					monthClassifier = new double[12];
				}
			}
			preparedDataAllSorte.put(sorte, preparedDataSingleSorte);
		}
		JSONObject resultJSON = new JSONObject();
		for(String sorte : preparedDataAllSorte.keySet()) {
			ArrayList<double[]> preparedDataSingleSorte = preparedDataAllSorte.get(sorte);
			JSONArray resultWeekly = new JSONArray();
			for(int i = 2; i<preparedDataSingleSorte.size(); i++) {
				double[] pastWeek = preparedDataSingleSorte.get(i-2);
				double[] currentWeek = preparedDataSingleSorte.get(i-1);
				double[] futureWeek = preparedDataSingleSorte.get(i);

				double[] output = {futureWeek[0]};
				double[] input = ArrayUtils.addAll(Arrays.copyOfRange(pastWeek, 13, pastWeek.length), Arrays.copyOfRange(currentWeek, 13, pastWeek.length));
				input = ArrayUtils.addAll(input, Arrays.copyOfRange(futureWeek, 1,13));
				JSONObject inputOuputMap = new JSONObject();
				inputOuputMap.put("input", input);
				inputOuputMap.put("output",output);
				resultWeekly.put(inputOuputMap);
			}
			resultJSON.put(sorte, resultWeekly);
		}
			
		
		return resultJSON;
	}

	public static JSONObject prepareDailyDatasetDailyOutput(JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		return prepareDailyDataSetDailyOutput("", configurations);
	}

	public static JSONObject prepareDailyDataSetDailyOutput(String kNo, JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		PreparationDAO daoAnalysis = new PreparationDAO(configurations.getString("passPhrase"));
		//boolean campaigns = configurations.getJSONObject("parameters").getJSONObject("campaigns").getBoolean("contained");
		Map<String,Map<String,Double>> dailyAggregatedData = DataAggregator.aggregateSorteDataDaily(kNo, configurations, daoAnalysis);
		//Consideration of Last YEar values not implemented yet
		//double valueLastYear =0;
		//double valueLastYearMinus1Week=0;

		Map<String, ArrayList<double[]>> preparedDataAllSorte = new LinkedHashMap<String, ArrayList<double[]>>();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		double mondayCurrent=0;
		double tuesdayCurrent=0;
		double wednessdayCurrent=0;
		double thursdayCurrent=0;
		double fridayCurrent=0;
		double saturdayCurrent=0;
		double sundayCurrent=0;
		int dayCounter = 0;
		int weekCounter = 0;
		for (String sorte : dailyAggregatedData.keySet()) {
			ArrayList<double[]> preparedDataSingleSorte = new ArrayList<double[]>();
			double[] monthClassifier = new double[12];
			for (String dateEntry : dailyAggregatedData.get(sorte).keySet()) {
				dayCounter=dayCounter+1;
				weekCounter=weekCounter+1;
				cal.setTime(dateformat.parse(dateEntry));
				int day = cal.get(Calendar.DAY_OF_WEEK);
				switch (day) {
					case Calendar.MONDAY:
						mondayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.TUESDAY:
						tuesdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.WEDNESDAY:
						wednessdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.THURSDAY:
						thursdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.FRIDAY:
						fridayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.SATURDAY:
						saturdayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;
					case Calendar.SUNDAY:
						sundayCurrent = dailyAggregatedData.get(sorte).get(dateEntry);;
						break;	
					default:
						throw new RuntimeException("Not a day");
				}
				
				
				String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
				switch (month) {
					case "January":
						monthClassifier[0]=monthClassifier[0]+1;
						break;
					case "February":
						monthClassifier[1]=monthClassifier[1]+1;
						break;
					case "March":
						monthClassifier[2]=monthClassifier[2]+1;
						break;
					case "April":
						monthClassifier[3]=monthClassifier[3]+1;
						break;
					case "May":
						monthClassifier[4]=monthClassifier[4]+1;
						break;
					case "June":
						monthClassifier[5]=monthClassifier[5]+1;
						break;
					case "July":
						monthClassifier[6]=monthClassifier[6]+1;
						break;
					case "August":
						monthClassifier[7]=monthClassifier[7]+1;
						break;
					case "September":
						monthClassifier[8]=monthClassifier[8]+1;
						break;
					case "October":
						monthClassifier[9]=monthClassifier[9]+1;
						break;
					case "November":
						monthClassifier[10]=monthClassifier[10]+1;
						break;
					case "December":
						monthClassifier[11]=monthClassifier[11]+1;
						break;
					default:
						throw new RuntimeException("Not a month");	
				}
				
				if(dayCounter>=7) {
					double[] weekValues = {
							mondayCurrent, 
							tuesdayCurrent,
							wednessdayCurrent,
							thursdayCurrent,
							fridayCurrent,
							saturdayCurrent,
							sundayCurrent
					};
					List<Double> monthInputList = Arrays.asList(ArrayUtils.toObject(monthClassifier));
			        double max = Collections.max(monthInputList);
			        int index  = monthInputList.indexOf(max);
			        double[] monthInputArray = new double[12];
			        monthInputArray[index] = 1;
			        
			        double[] sum = {mondayCurrent+tuesdayCurrent+wednessdayCurrent+thursdayCurrent+fridayCurrent+saturdayCurrent+sundayCurrent};
					double[] preparedDataWeek = ArrayUtils.addAll(sum, monthInputArray);
			        preparedDataWeek = ArrayUtils.addAll(preparedDataWeek, weekValues);
			        preparedDataSingleSorte.add(preparedDataWeek);
					dayCounter = 0;
					monthClassifier = new double[12];
				}
			}
			preparedDataAllSorte.put(sorte, preparedDataSingleSorte);
		}
		JSONObject resultJSON = new JSONObject();
		for(String sorte : preparedDataAllSorte.keySet()) {
			ArrayList<double[]> preparedDataSingleSorte = preparedDataAllSorte.get(sorte);
			JSONArray resultWeekly = new JSONArray();
			for(int i = 2; i<preparedDataSingleSorte.size(); i++) {
				double[] pastWeek = preparedDataSingleSorte.get(i-2);
				double[] currentWeek = preparedDataSingleSorte.get(i-1);
				double[] futureWeek = preparedDataSingleSorte.get(i);

				double[] output = ArrayUtils.addAll(Arrays.copyOfRange(futureWeek, 13, futureWeek.length));
				double[] input = ArrayUtils.addAll(Arrays.copyOfRange(pastWeek, 13, pastWeek.length), Arrays.copyOfRange(currentWeek, 13, pastWeek.length));
				input = ArrayUtils.addAll(input, Arrays.copyOfRange(futureWeek, 1,13));
				JSONObject inputOuputMap = new JSONObject();
				inputOuputMap.put("input", input);
				inputOuputMap.put("output",output);
				resultWeekly.put(inputOuputMap);
			}
			resultJSON.put(sorte, resultWeekly);
		}
			
		
		return resultJSON;
	}

	public void SorteAnalysisAllCustomers(/*Map<String, Map<String, String>>*/ JSONObject configurations) throws JSONException, FileNotFoundException, ClassNotFoundException, SQLException, ParseException {
		PreparationDAO daoAnalysis = new PreparationDAO(configurations.getString("passPhrase"));
		try {
			List<String> kNoList = daoAnalysis.getAllKNo();
			for (String kNo : kNoList) {
				// Map<String,Map<String, Datastruct>> sorteAnalysis =
				// DailyAggregation.getSorteDaten(kNo);
				prepareDailyDataSetWeeklyOutput(kNo, configurations);
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