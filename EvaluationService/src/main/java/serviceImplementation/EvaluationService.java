package serviceImplementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.text.html.parser.TagElement;

import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import dBConnection.EvaluationDAO;
import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class EvaluationService {
	static List<String> skipList = Arrays.asList("MAE", "ME", "MAEPercentage", "MEPercentage");
	
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
				benchMAE=1;
			}	
			mase = Math.abs(altMAE/benchMAE);
		
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
	
	
	public static JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	private static double calculateMeanError(double forecastResult, double actualDemand) {
		return actualDemand-forecastResult;
	}
	
	private static double calculateMeanAbsoluteError(double forecastResult, double actualDemand) {
		return Math.abs(actualDemand-forecastResult);
	}
	public static JSONObject evaluationMAE(JSONObject procedureResults) throws SQLException, ParseException, ClassNotFoundException, IOException {	
		JSONObject totalDeviationTargetVariable = new JSONObject();
		totalDeviationTargetVariable.put("ME", 0);
		totalDeviationTargetVariable.put("MEPercentage", 0);
		totalDeviationTargetVariable.put("MAE", 0);
		totalDeviationTargetVariable.put("MAEPercentage", 0);
		for(String targetVariableName : procedureResults.keySet()) {
			JSONObject totalDeviationConfiguration = new JSONObject();
			totalDeviationConfiguration.put("ME", 0);
			totalDeviationConfiguration.put("MEPercentage", 0);
			totalDeviationConfiguration.put("MAE", 0);
			totalDeviationConfiguration.put("MAEPercentage", 0);
			JSONObject targetVariableResults = procedureResults.getJSONObject(targetVariableName);

			for(String forecastDate : targetVariableResults.keySet()) {	
				JSONObject totalDeviationObservationPeriod = new JSONObject();
				totalDeviationObservationPeriod.put("ME", 0);
				totalDeviationObservationPeriod.put("MEPercentage", 0);
				totalDeviationObservationPeriod.put("MAE", 0);
				totalDeviationObservationPeriod.put("MAEPercentage", 0);
				JSONObject forecastingConfigurations = targetVariableResults.getJSONObject(forecastDate);
				
				for(String configuration : forecastingConfigurations.keySet()) {	
					JSONObject forecastDateResults = forecastingConfigurations.getJSONObject(configuration);
					JSONObject totalDeviationForecastPeriods = new JSONObject();
					totalDeviationForecastPeriods.put("ME", 0);
					totalDeviationForecastPeriods.put("MEPercentage", 0);
					totalDeviationForecastPeriods.put("MAE", 0);
					totalDeviationForecastPeriods.put("MAEPercentage", 0);
					for(String period : forecastDateResults.keySet()) {
						JSONObject periodResults = forecastDateResults.getJSONObject(period);
						
						double forecastResult = periodResults.getDouble("forecastResult");
						double actualDemand = 0;
						//switch comment if campaigns are not considered
						
						//without campaign handling
						//actualDemand = periodResults.getDouble("demand");
						
						/**/ //with campaign handling
						//handles case that no demand occured during time period
						double totalDemand = 0;
						double knownDemand = 0;
						if(periodResults.has("demand")){
								actualDemand = periodResults.getJSONObject("demand").getDouble("unknownDemand");
								totalDemand = periodResults.getJSONObject("demand").getDouble("totalDemand");
								knownDemand = periodResults.getJSONObject("demand").getDouble("knownDemand");
						}else {
							actualDemand = 0;
						}
						/**/
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
						//uncomment if campaigns are not considered
						/**/
						periodResults.put("totalDemand", totalDemand);
						periodResults.put("knownDemand", knownDemand);
						/**/
						totalDeviationForecastPeriods.put(period, periodResults);
						totalDeviationForecastPeriods.put("ME", (totalDeviationForecastPeriods.getDouble("ME") + periodResults.getDouble("ME"))/2);
						totalDeviationForecastPeriods.put("MEPercentage", (totalDeviationForecastPeriods.getDouble("MEPercentage") + periodResults.getDouble("MEPercentage"))/2);
						totalDeviationForecastPeriods.put("MAE", (totalDeviationForecastPeriods.getDouble("MAE") + periodResults.getDouble("MAE"))/2);
						totalDeviationForecastPeriods.put("MAEPercentage", (totalDeviationForecastPeriods.getDouble("MAEPercentage") + periodResults.getDouble("MAEPercentage"))/2);
					}
					totalDeviationObservationPeriod.put(configuration, totalDeviationForecastPeriods);
					totalDeviationObservationPeriod.put("ME", (totalDeviationObservationPeriod.getDouble("ME") + totalDeviationForecastPeriods.getDouble("ME"))/2);
					totalDeviationObservationPeriod.put("MEPercentage", (totalDeviationObservationPeriod.getDouble("MEPercentage") + totalDeviationForecastPeriods.getDouble("MEPercentage"))/2);
					totalDeviationObservationPeriod.put("MAE", (totalDeviationObservationPeriod.getDouble("MAE") + totalDeviationForecastPeriods.getDouble("MAE"))/2);
					totalDeviationObservationPeriod.put("MAEPercentage", (totalDeviationObservationPeriod.getDouble("MAEPercentage") + totalDeviationForecastPeriods.getDouble("MAEPercentage"))/2);
				}
				totalDeviationConfiguration.put(forecastDate, totalDeviationObservationPeriod);
				totalDeviationConfiguration.put("ME", (totalDeviationConfiguration.getDouble("ME") + totalDeviationObservationPeriod.getDouble("ME"))/2);
				totalDeviationConfiguration.put("MEPercentage", (totalDeviationConfiguration.getDouble("MEPercentage") + totalDeviationObservationPeriod.getDouble("MEPercentage"))/2);
				totalDeviationConfiguration.put("MAE", (totalDeviationConfiguration.getDouble("MAE") + totalDeviationObservationPeriod.getDouble("MAE"))/2);
				totalDeviationConfiguration.put("MAEPercentage", (totalDeviationConfiguration.getDouble("MAEPercentage") + totalDeviationObservationPeriod.getDouble("MAEPercentage"))/2);
			}
			totalDeviationTargetVariable.put(targetVariableName, totalDeviationConfiguration);
			totalDeviationTargetVariable.put("ME", (totalDeviationTargetVariable.getDouble("ME") + totalDeviationConfiguration.getDouble("ME"))/2);
			totalDeviationTargetVariable.put("MEPercentage", (totalDeviationTargetVariable.getDouble("MEPercentage") + totalDeviationConfiguration.getDouble("MEPercentage"))/2);
			totalDeviationTargetVariable.put("MAE", (totalDeviationTargetVariable.getDouble("MAE") + totalDeviationConfiguration.getDouble("MAE"))/2);
			totalDeviationTargetVariable.put("MAEPercentage", (totalDeviationTargetVariable.getDouble("MAEPercentage") + totalDeviationConfiguration.getDouble("MAEPercentage"))/2);
		}
		return totalDeviationTargetVariable;
	}
	
	
	private static XSSFRow retrieveRow(XSSFSheet sheet, int rowIndex) {
		if(sheet.getRow(rowIndex) != null){
			return sheet.getRow(rowIndex);
		}else {
			return sheet.createRow(rowIndex);
		}
	}
	
	private static Cell writeValueToCell(XSSFSheet sheet, int rowIndex, int colIndex, double value) {
		XSSFRow row = retrieveRow(sheet, rowIndex);
		Cell cell =row.createCell(colIndex);
		cell.setCellValue(value);
		return cell;
	}
	private static void writeValueToCell(XSSFSheet sheet, int rowIndex, int colIndex, String value) {
		XSSFRow row = retrieveRow(sheet, rowIndex);
		Cell cell =row.createCell(colIndex);
		//System.out.println(colIndex + ","+rowIndex+":"+value);
		cell.setCellValue(value);
	}
	
	private static JSONObject initializeHeaders(XSSFSheet sheet, ArrayList<String> configurations, ArrayList<String> dates, int forecastPeriods/*JSONObject targetVariableResult*/) {
		//XSSFSheet sheet = wb.getSheet("Produktübersicht");
		JSONObject colRowMapper = new JSONObject();
		colRowMapper.put("Dates", new JSONObject());
		colRowMapper.put("Configurations", new JSONObject());
		
		int baseRowIndex = 3;
		int baseColIndex = CellReference.convertColStringToIndex("B");		
		//int numberOfConfigurations = targetVariableResult.length();
		int numberOfConfigurations = configurations.size();
		int rowIndex = baseRowIndex;
		int colIndex = baseColIndex;
		writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Period");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Total Demand");
		
		//uncomment if campaigns are not considered
		/**/
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Known Demand");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Unknown Demand");
		/**/
		rowIndex+=(2+numberOfConfigurations);
		writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Period");
		rowIndex+=(2+numberOfConfigurations);
		writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Period");
		rowIndex+=(2+numberOfConfigurations);
		writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Period");
		rowIndex+=(2+numberOfConfigurations);
		writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
		rowIndex+=1;
		writeValueToCell(sheet, rowIndex, colIndex, "Period");
		sheet.autoSizeColumn(colIndex);
		rowIndex = baseRowIndex;
		int i = 0;
		for(String configuration : configurations) {
		//for(String configuration : targetVariableResult.keySet()) {
			if(!skipList.contains(configuration)) {
				//switch comment if campaigns are not considered
				//rowIndex += 3;
				//camapgins considered
				rowIndex += 5;
				writeValueToCell(sheet, rowIndex, colIndex, configuration);
				colRowMapper.getJSONObject("Configurations").put(configuration, rowIndex);
				rowIndex+=(3+numberOfConfigurations);
				writeValueToCell(sheet, rowIndex, colIndex, configuration);
				rowIndex+=(3+numberOfConfigurations);
				writeValueToCell(sheet, rowIndex, colIndex, configuration);
				rowIndex+=(3+numberOfConfigurations);
				writeValueToCell(sheet, rowIndex, colIndex, configuration);
				rowIndex+=(3+numberOfConfigurations);
				writeValueToCell(sheet, rowIndex, colIndex, configuration);
				i += 1;
				rowIndex = baseRowIndex + i;
			}
		}
		rowIndex = baseRowIndex;
		colIndex = baseColIndex;
		for(String date : dates) {
			//for(String configuration : targetVariableResult.keySet()) {
			if(!skipList.contains(date)) {
				for(int period = 1; period<=forecastPeriods;period++) {
					colIndex  +=1;
					writeValueToCell(sheet, rowIndex, colIndex, date);
					rowIndex +=1;
					writeValueToCell(sheet, rowIndex, colIndex, period);
					colRowMapper.getJSONObject("Dates").put(date+period, colIndex);
					rowIndex = baseRowIndex;
				}
			}
		}
		return colRowMapper;
	}
	
	private static JSONObject sortJSONObject(JSONObject evaluationResults){
		JSONObject unsortedResult = new JSONObject();
		JSONObject sortedTargetVariable;
		JSONObject sortedDateResult;
		JSONObject sortedConfiguration;
		JSONObject sortedPeriod;
		ArrayList<String> dateList = new ArrayList<String>();
		boolean first = true;
		for(String targetVariableName : evaluationResults.keySet()) {
			JSONObject targetVariableResult = evaluationResults.getJSONObject(targetVariableName);
			for(String configuration : targetVariableResult.keySet()) {
				JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);
				for(String dateString : configurationResult.keySet()) {
					if(first) {
						dateList.add(dateString);
					}
					JSONObject dateResult = configurationResult.getJSONObject(dateString);
					for(String period : dateResult.keySet()) {
						JSONObject periodResult = dateResult.getJSONObject(period);
						if(!unsortedResult.has(targetVariableName)) {
							sortedTargetVariable = new JSONObject();
							unsortedResult.put(targetVariableName, sortedTargetVariable);
						}else {
							sortedTargetVariable = unsortedResult.getJSONObject(targetVariableName);
						}
						if(!sortedTargetVariable.has(dateString)) {
							sortedDateResult = new JSONObject();
							sortedTargetVariable.put(dateString, sortedDateResult);
						}else {
							sortedDateResult = sortedTargetVariable.getJSONObject(dateString);
						}
						if(!sortedDateResult.has(configuration)) {
							sortedConfiguration = new JSONObject();
							sortedDateResult.put(configuration, sortedConfiguration);
						}else {
							sortedConfiguration = sortedDateResult.getJSONObject(configuration);
						}
						
						if(!sortedConfiguration.has(period)) {
							sortedConfiguration.put(period, periodResult);
						}
					}
				}
				first = false;
			}
		}
		return unsortedResult;
	}
	
	
	public static File writeEvaluationResultsToExcelFile(JSONObject evaluationResults, String procedure) throws FileNotFoundException, IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();        
		String targetPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Evaluation\\temp\\";
		String filename = procedure + "_Evaluation.xlsx";
		 File file = new File(targetPath+filename);
		List<String> skipList = Arrays.asList("MAE", "ME", "MAEPercentage", "MEPercentage");
		//JSONObject sortedResult = sortJSONObject(evaluationResults);
		
		ArrayList<String> dateList = new ArrayList<String>();
		ArrayList<String> configList = new ArrayList<String>();
		int forecastPeriods = 1;
		for(String targetVariableName : evaluationResults.keySet()) {
			if(!skipList.contains(targetVariableName)) {
				JSONObject targetVariableResult = evaluationResults.getJSONObject(targetVariableName);
				for(String configuration : targetVariableResult.keySet()) {
					if(!skipList.contains(configuration)) {
						if(!configList.contains(configuration)) {
							configList.add(configuration);
						}
						JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);
						for(String dateString : configurationResult.keySet()) {
							if(!skipList.contains(dateString) && !dateList.contains(dateString)) {
								dateList.add(dateString);
								forecastPeriods = configurationResult.getJSONObject(dateString).length()-skipList.size();
							}
						}	
					}
				}
			}
		}

		Collections.sort(dateList);		
		for(String targetVariableName : evaluationResults.keySet()) {
			if(!skipList.contains(targetVariableName)) {
				XSSFSheet sheet = workbook.createSheet(targetVariableName);
				JSONObject targetVariableResult = evaluationResults.getJSONObject(targetVariableName);
				JSONObject colRowMapper = initializeHeaders(sheet, configList, dateList, forecastPeriods/*targetVariableResult*/);
				
				
				//XSSFSheet sheet = wb.getSheet("Produktübersicht");
				int baseRowIndex = 3;
				int baseColIndex = CellReference.convertColStringToIndex("C");		
				int numberOfConfigurations = targetVariableResult.length()-4;
				int rowIndex = baseRowIndex;
				int colIndex = baseColIndex;
				Cell cell = null;
				
				CellStyle stylePercentage = workbook.createCellStyle();
				stylePercentage.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 )));
				
				
				//boolean first = true;
				for(String configuration : targetVariableResult.keySet()) {
					if(!skipList.contains(configuration)) {
						JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);		
						colIndex = baseColIndex;
						
							for(String dateString : dateList) {
								
							//for(String dateString : configurationResult.keySet()) {
								if(!skipList.contains(dateString) && configurationResult.has(dateString)) {
									int dateRowIndex = baseRowIndex;
									JSONObject dateResult = configurationResult.getJSONObject(dateString);
									for(String period : dateResult.keySet()) {
										if(!skipList.contains(period)) {
											JSONObject periodResult = dateResult.getJSONObject(period);								
											double actualDemand = periodResult.getDouble("actualDemand");
											
											//uncomment if campaigns not handled
											/**/
											double knownDemand = periodResult.getDouble("knownDemand");
											double totalDemand = periodResult.getDouble("totalDemand");
											/**/
											double forecastResult = periodResult.getDouble("forecastResult");
											double mE = periodResult.getDouble("ME");
											double mAE = periodResult.getDouble("MAE");
											double mEPercentage = periodResult.getDouble("MEPercentage");
											double mAEPercentage = periodResult.getDouble("MAEPercentage");
											
												
											colIndex = colRowMapper.getJSONObject("Dates").getInt(dateString+period);
											rowIndex = baseRowIndex + 2;
											writeValueToCell(sheet, rowIndex, colIndex, totalDemand);
											//uncomment if campaigns not handled
											/**/
											rowIndex +=1;
											cell = writeValueToCell(sheet, rowIndex, colIndex, knownDemand);
											if(knownDemand>totalDemand) {
												CellStyle style = workbook.createCellStyle();  
									            style.setFillForegroundColor(IndexedColors.RED.getIndex());  
									            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
									            cell.setCellStyle(style);
											}
											rowIndex +=1;
											writeValueToCell(sheet, rowIndex, colIndex, actualDemand);
											/**/
											rowIndex = colRowMapper.getJSONObject("Configurations").getInt(configuration);
											writeValueToCell(sheet, rowIndex, colIndex, forecastResult);
											rowIndex+=(numberOfConfigurations+3);
											writeValueToCell(sheet, rowIndex, colIndex, mE);
											rowIndex+=(numberOfConfigurations+3);
											writeValueToCell(sheet, rowIndex, colIndex, mAE);
											rowIndex+=(numberOfConfigurations+3);
											cell = writeValueToCell(sheet, rowIndex, colIndex, mEPercentage);
											cell.setCellStyle(stylePercentage);
											rowIndex+=(numberOfConfigurations+3);
											cell = writeValueToCell(sheet, rowIndex, colIndex, mAEPercentage);	
											cell.setCellStyle(stylePercentage);
											sheet.autoSizeColumn(colIndex);
											
											
											/*
											if(first) {
												writeValueToCell(sheet, rowIndex, colIndex, dateString);
												rowIndex+=1;
												writeValueToCell(sheet, rowIndex, colIndex, period);
												rowIndex+=1;
												writeValueToCell(sheet, rowIndex, colIndex, actualDemand);
												rowIndex+=(1+numberOfConfigurations+1);
												for(int i = 0; i<4;i++) {	
													writeValueToCell(sheet, rowIndex, colIndex, dateString);
													rowIndex+=1;
													writeValueToCell(sheet, rowIndex, colIndex, period);
													rowIndex+=(1+numberOfConfigurations+1);
												}
											}
											rowIndex=baseRowIndex+3;
											writeValueToCell(sheet, rowIndex, colIndex, forecastResult);
											rowIndex+=(numberOfConfigurations+3);
											writeValueToCell(sheet, rowIndex, colIndex, mE);
											rowIndex+=(numberOfConfigurations+3);
											writeValueToCell(sheet, rowIndex, colIndex, mAE);
											rowIndex+=(numberOfConfigurations+3);
											cell = writeValueToCell(sheet, rowIndex, colIndex, mEPercentage);
											cell.setCellStyle(stylePercentage);
											rowIndex+=(numberOfConfigurations+3);
											cell = writeValueToCell(sheet, rowIndex, colIndex, mAEPercentage);	
											cell.setCellStyle(stylePercentage);
											sheet.autoSizeColumn(colIndex);
											colIndex = colIndex + 1;
											*/
										}
									}
								}				
							}
							//first = false;
							
						
						//colIndex = baseColIndex;
						//baseRowIndex = baseRowIndex + 1;
					}
				}
			}
		}
		
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
		}
		workbook.close();
		return file;
	}
		
	
	/*public static void evaluationCombined(JSONObject configAndResults) throws SQLException, ParseException, ClassNotFoundException, IOException {
		JSONObject diffResults = new JSONObject();
    	diffResults.put("Difference", new JSONObject());
    	diffResults.put("MASE", new JSONObject());
		
		JSONObject configurations = configAndResults.getJSONObject("configurations");
		JSONObject results = configAndResults.getJSONObject("results");
		JSONObject loginCredentials = configAndResults.getJSONObject("loginCredentials");
		loginCredentials = invokeLoginService(loginCredentials);
    	String passPhrase = loginCredentials.getString("passPhrase");
		configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
    	int forecastPeriods = configurations.getInt("forecastPeriods");
    	String startDate = configurations.getJSONObject("data").getString("to"); 
		JSONObject actualResults = new JSONObject();
    	if(configurations.getString("aggregationOutputData").toUpperCase().equals("DAILY")) {
    		actualResults= getActualResultsDaily(startDate, forecastPeriods, passPhrase);
    	}else {
    		actualResults = getActualResultsWeekly(startDate, forecastPeriods, passPhrase);
    	}
    	CustomFileWriter.createJSON("D:/Arbeit/Bantel/Masterarbeit/Implementierung/Bantel/Daten/Actual_RESULT.json", actualResults.toString());
    	
    	for(String procedureName : results.keySet()) {
    		JSONObject procedureResult = results.getJSONObject(procedureName);
    		diffResults.getJSONObject("Difference").put(procedureName, evaluationMAE(actualResults, procedureResult));
    	}
    	if(diffResults.getJSONObject("Difference").has("ExpSmoothingResult")) {
    		JSONObject benchmarkResult = diffResults.getJSONObject("Difference").getJSONObject("ExpSmoothingResult");
    		for(String procedureName : diffResults.getJSONObject("Difference").keySet()) {
        		JSONObject alternativeResult = diffResults.getJSONObject("Difference").getJSONObject(procedureName);
        		diffResults.getJSONObject("MASE").put("MASE_"+ procedureName, evaluationSMAE(benchmarkResult, alternativeResult));
        	}
    	}
    	
    	System.out.println(diffResults);
    	CustomFileWriter.createJSON("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Evaluation\\EvaluationResults.json", diffResults.toString());
	}*/
	
	/*
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
	}*/
	
	/*public static JSONObject getActualResultsDaily(String toDate, int forecastPeriods, String passPhrase) throws SQLException, ParseException, ClassNotFoundException {
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
	}*/
	
	//Function of Bantel GmbH To get all ARIMA ForecastResults
	/*public static JSONObject getForecastResultsMulti(JSONObject configurations, JSONArray executionRuns, String procedureName) throws SQLException, ParseException, ClassNotFoundException {
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
	*/
	
	/*
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
	*/
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
	
	
	public static File writeComparedEvaluationMAEToExcelFile(JSONObject comparedEvaluationMAE, String procedure) throws FileNotFoundException, IOException {
		JSONObject comparedResult = new JSONObject();

			XSSFWorkbook workbook = new XSSFWorkbook();        
			String targetPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Evaluation\\temp\\";
			String filename = procedure + "_Evaluation.xlsx";
			 File file = new File(targetPath+filename);
			//JSONObject sortedResult = sortJSONObject(evaluationResults);
			
			ArrayList<String> dateList = new ArrayList<String>();
			ArrayList<String> configList = new ArrayList<String>();
			ArrayList<String> procedureList = new ArrayList<String>();
			int forecastPeriods = 1;
			for(String targetVariableName : comparedEvaluationMAE.keySet()) {
				if(!skipList.contains(targetVariableName)) {
					JSONObject targetVariableResult = comparedEvaluationMAE.getJSONObject(targetVariableName);
					for(String configuration : targetVariableResult.keySet()) {
						if(!skipList.contains(configuration)) {
							if(!configList.contains(configuration)) {
								configList.add(configuration);
							}
							JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);
							for(String dateString : configurationResult.keySet()) {
								if(!skipList.contains(dateString)) {
									JSONObject dateResult = configurationResult.getJSONObject(dateString);
									if(!dateList.contains(dateString)) {
										dateList.add(dateString);
										forecastPeriods = dateResult.length();
									}
									for(String period : dateResult.keySet()) {
										if(!skipList.contains(period)) {
											JSONObject periodResult = dateResult.getJSONObject(period);
											for(String procedureName : periodResult.keySet()) {
												if(!procedureList.contains(procedureName)) {
													procedureList.add(procedureName);
												}
											}
										}
									}
								}
							}	
						}
					}
				}
			}
			Collections.sort(dateList);		
			for(String targetVariableName : comparedEvaluationMAE.keySet()) {
				if(!skipList.contains(targetVariableName)) {
					XSSFSheet sheet = workbook.createSheet(targetVariableName);
					JSONObject targetVariableResult = comparedEvaluationMAE.getJSONObject(targetVariableName);
					JSONObject colRowMapper = initializeHeadersCompared(sheet, configList, dateList, procedureList, forecastPeriods);
					
					
					//XSSFSheet sheet = wb.getSheet("Produktübersicht");
					int baseRowIndex = 3;
					int baseColIndex = CellReference.convertColStringToIndex("C");		
					int numberOfConfigurations = targetVariableResult.length()-4;
					int rowIndex = baseRowIndex;
					int colIndex = baseColIndex;
					Cell cell = null;
					
					CellStyle stylePercentage = workbook.createCellStyle();
					stylePercentage.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 )));
					
					
					//boolean first = true;
					for(String configuration : targetVariableResult.keySet()) {
						if(!skipList.contains(configuration)) {
							JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);		
							colIndex = baseColIndex;
							
								for(String dateString : dateList) {
									
								//for(String dateString : configurationResult.keySet()) {
									if(!skipList.contains(dateString) && configurationResult.has(dateString)) {
										int dateRowIndex = baseRowIndex;
										JSONObject dateResult = configurationResult.getJSONObject(dateString);
										for(String period : dateResult.keySet()) {
											if(!skipList.contains(period)) {
												JSONObject periodResult = dateResult.getJSONObject(period);
												for(String procedureName : periodResult.keySet()) {
													JSONObject procedureResult = periodResult.getJSONObject(procedureName);								
													double mAEPercentage = procedureResult.getDouble("MAEPercentage");	
													colIndex = colRowMapper.getJSONObject("Dates").getInt(dateString+period);
													rowIndex = colRowMapper.getJSONObject("Configurations").getJSONObject(configuration).getInt(procedureName);
													cell = writeValueToCell(sheet, rowIndex, colIndex, mAEPercentage);	
													cell.setCellStyle(stylePercentage);
													sheet.autoSizeColumn(colIndex);
												}
									
											}
										}
									}				
								}
							}
						}
					}
				}
			
			
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
	            workbook.write(outputStream);
			}
			workbook.close();
			return file;
		}
			
			private static JSONObject initializeHeadersCompared(XSSFSheet sheet, ArrayList<String> configurations, ArrayList<String> dates, ArrayList<String> procedures, int forecastPeriods/*JSONObject targetVariableResult*/) {
				//XSSFSheet sheet = wb.getSheet("Produktübersicht");
				JSONObject colRowMapper = new JSONObject();
				colRowMapper.put("Dates", new JSONObject());
				colRowMapper.put("Configurations", new JSONObject());
				
				int baseRowIndex = 3;
				int baseColIndex = CellReference.convertColStringToIndex("B");		
				//int numberOfConfigurations = targetVariableResult.length();
				int numberOfConfigurations = configurations.size();
				int rowIndex = baseRowIndex;
				int colIndex = baseColIndex;
				writeValueToCell(sheet, rowIndex, colIndex, "ForecastDate");
				rowIndex+=1;
				writeValueToCell(sheet, rowIndex, colIndex, "Period");
				sheet.autoSizeColumn(colIndex);
				rowIndex = baseRowIndex;
				int i = 0;
				rowIndex+=3;
				for(String configuration : configurations) {
				//for(String configuration : targetVariableResult.keySet()) {
					if(!skipList.contains(configuration)) {
						writeValueToCell(sheet, rowIndex, colIndex, configuration);
						colRowMapper.getJSONObject("Configurations").put(configuration, new JSONObject());
						for(String procedureName : procedures) {
							writeValueToCell(sheet, rowIndex, colIndex + 1, procedureName);
							colRowMapper.getJSONObject("Configurations").getJSONObject(configuration).put(procedureName, rowIndex);
							rowIndex+=1;
						}
						i += 1;
					}
				}
				rowIndex = baseRowIndex;
				colIndex = baseColIndex;
				colIndex += 1;
				for(String date : dates) {
					//for(String configuration : targetVariableResult.keySet()) {
					if(!skipList.contains(date)) {
						for(int period = 1; period<=forecastPeriods;period++) {
							colIndex  +=1;
							writeValueToCell(sheet, rowIndex, colIndex, date);
							rowIndex +=1;
							writeValueToCell(sheet, rowIndex, colIndex, period);
							colRowMapper.getJSONObject("Dates").put(date+period, colIndex);
							rowIndex = baseRowIndex;
						}
					}
				}
				return colRowMapper;
			}
		
	public static JSONObject comparedEvaluationMAE(JSONObject evaluationResults){
		JSONObject comparedResult = new JSONObject();
		JSONObject structuredTargetVariableResult;
		JSONObject structuredConfigurationResult;
		JSONObject structuredDateResult;
		JSONObject structuredPeriodResult;
		JSONObject structuredProcedureResult;
		for(String procedureName : evaluationResults.keySet()) {
			if(!skipList.contains(procedureName)) {
				JSONObject procedureResult = evaluationResults.getJSONObject(procedureName).getJSONObject("MAE");
				for(String targetVariableName : procedureResult.keySet()) {
					if(!skipList.contains(targetVariableName)) {
						JSONObject targetVariableResult = procedureResult.getJSONObject(targetVariableName);
						for(String configuration : targetVariableResult.keySet()) {
							if(!skipList.contains(configuration)) {
								JSONObject configurationResult = targetVariableResult.getJSONObject(configuration);		
								for(String dateString : configurationResult.keySet()) {	
								//for(String dateString : configurationResult.keySet()) {
									if(!skipList.contains(dateString)) {
										JSONObject dateResult = configurationResult.getJSONObject(dateString);		
										for(String period : dateResult.keySet()) {	
										//for(String dateString : configurationResult.keySet()) {
											if(!skipList.contains(period)) {
												double mAEPercentage = dateResult.getJSONObject(period).getDouble("MAEPercentage");
												
												if(!comparedResult.has(targetVariableName)) {
													structuredTargetVariableResult = new JSONObject();
													comparedResult.put(targetVariableName, structuredTargetVariableResult);
												}else {
													structuredTargetVariableResult = comparedResult.getJSONObject(targetVariableName);
												}
												if(!structuredTargetVariableResult.has(configuration)) {
													structuredConfigurationResult = new JSONObject();
													structuredTargetVariableResult.put(configuration, structuredConfigurationResult);
												}else {
													structuredConfigurationResult = structuredTargetVariableResult.getJSONObject(configuration);
												}
												if(!structuredConfigurationResult.has(dateString)) {
													structuredDateResult = new JSONObject();
													structuredConfigurationResult.put(dateString, structuredDateResult);
												}else {
													structuredDateResult = structuredConfigurationResult.getJSONObject(dateString);
												}
												if(!structuredDateResult.has(period)) {
													structuredPeriodResult = new JSONObject();
													structuredDateResult.put(period, structuredPeriodResult);
												}else {
													structuredPeriodResult = structuredDateResult.getJSONObject(period);
												}
												if(!structuredPeriodResult.has(procedureName)) {
													structuredProcedureResult = new JSONObject();
													structuredPeriodResult.put(procedureName, structuredProcedureResult);
												}else {
													structuredProcedureResult = structuredPeriodResult.getJSONObject(procedureName);
												}
												structuredProcedureResult.put("MAEPercentage", mAEPercentage);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return comparedResult;
	}
}
