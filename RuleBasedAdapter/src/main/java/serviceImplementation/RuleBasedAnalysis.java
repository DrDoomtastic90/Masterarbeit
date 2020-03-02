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
import org.sqlite.SQLiteConfig;

import dBConnections.MilkDAO;
import dBConnections.SorteDAO;





public class RuleBasedAnalysis extends Analysis {

	public static JSONObject prepareDataProductionPlanning(JSONObject configurations) throws FileNotFoundException, JSONException, ParseException, ClassNotFoundException, SQLException {
		return prepareDataDemandForecasting(configurations);
	}
	private static JSONObject prepareDataDemandForecasting(JSONObject configurations) throws JSONException, FileNotFoundException, ParseException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		JSONObject ruleObjects = getRuleObjects(configurations);
		responseContent.put("factors", ruleObjects);
		return responseContent;
	}
	
	private static JSONObject prepareDataProductionAssignment(JSONObject configurations) throws JSONException, FileNotFoundException {
		JSONObject responseContent = new JSONObject();
		return responseContent;
	}

	private static JSONObject getRuleObjects(JSONObject configurations ) throws JSONException, ParseException, ClassNotFoundException, SQLException {
		JSONObject ruleObjects = new JSONObject();
		//ruleObjects.put("Sorte", getSorteObjects(configurations));
		String inputAggregation = configurations.getJSONObject("parameters").getString("aggregationInputData").toUpperCase();
		switch(inputAggregation) {
		  case "DAILY":
			  ruleObjects.put("Sorte", getSorteDataDaily(configurations));
		    break;
		  case "WEEKLY":
			  ruleObjects.put("Sorte", getSorteDataWeekly(configurations));
		    break;
		  case "MONTHLY":
			  //ruleObjects.put("Sorte", getSorteDataWeekly(configurations));
		    break;
		  case "YEARLY":
			  //ruleObjects.put("Sorte", getSorteDataWeekly(configurations));
		    break;
		  default:
		    throw new JSONException("Not a valid aggregation");
		}

		
		
		ruleObjects.put("Milk", getMilkObjects(configurations));
		return ruleObjects;
	}
	
		
	private static JSONObject getSorteDataWeekly(JSONObject configurations) throws JSONException, ParseException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		//SorteDAO sorteDAO = new SorteDAO(configurations.getJSONObject("data").getString("sourcepath"), configurations.getJSONObject("data").getString("passPhrase"));
		SorteDAO sorteDAO = new SorteDAO("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Datenbanken", configurations.getJSONObject("data").getString("passPhrase"));
		Map<String, Sorte> sorteMap = sorteDAO.getSorteMasterData();
		Map<String, Map<String, Double>> currentSalesWeeklySorte = sorteDAO.getCurrentSalesAmountsWeekly(configurations);
		Map<String, Map<String, Double>> pastSalesWeeklySorte = sorteDAO.getPastSalesAmountsWeekly(configurations);
		Map<String, Double> inventoryMap = sorteDAO.getInventory(sorteMap, configurations);
		Map<String, Double> campaignMap = sorteDAO.getCampaigns(configurations);
		Map<String, Double> directSalesMap = sorteDAO.getDirectSales(configurations);
		Map<String, Double> unpackedMap = sorteDAO.getMengenUnverpackt(configurations);
		for(String skbez: sorteMap.keySet()) {
			Sorte sorte = sorteMap.get(skbez);
			if(inventoryMap.containsKey(skbez)) {
				sorte.setInventory(inventoryMap.get(skbez));
			}
			JSONObject jsonSorte = sorte.SorteTOJSON();
			jsonSorte.put("unpacked",unpackedMap.get(skbez));
			Map<String, Double> demands = new LinkedHashMap<String, Double>();
			for(String strDate : currentSalesWeeklySorte.keySet()) {
				demands.put(strDate,currentSalesWeeklySorte.get(strDate).get(skbez));
			}
			for(String strDate : pastSalesWeeklySorte.keySet()) {
				demands.put(strDate,pastSalesWeeklySorte.get(strDate).get(skbez));
			}
			jsonSorte.put("demand",demands);
			jsonSorte.put("demandWeek0",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek0").getString("content"));
			jsonSorte.put("demandWeek1",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek1").getString("content"));
			jsonSorte.put("demandWeek2",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek2").getString("content"));
			jsonSorte.put("demandWeekLY0",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeekLY0").getString("content"));
			jsonSorte.put("demandWeekLY1",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeekLY1").getString("content"));
			/*if(currentSalesWeeklySorte.containsKey("demandWeekM0")){
				if (currentSalesWeeklySorte.get("demandWeekM0").containsKey(skbez)){
					jsonSorte.put("demandWeek0",currentSalesWeeklySorte.get("demandWeekM0").get(skbez));
				}
			}
			if(currentSalesWeeklySorte.containsKey("demandWeekM1")){
				if (currentSalesWeeklySorte.get("demandWeekM1").containsKey(skbez)){
					jsonSorte.put("demandWeek1",currentSalesWeeklySorte.get("demandWeekM1").get(skbez));
				}
			}
			if(currentSalesWeeklySorte.containsKey("demandWeekM2")){
				if (currentSalesWeeklySorte.get("demandWeekM2").containsKey(skbez)){
					jsonSorte.put("demandWeek2",currentSalesWeeklySorte.get("demandWeekM2").get(skbez));
				}
			}
			if(pastSalesWeeklySorte.containsKey("demandWeekP0")){
				if (pastSalesWeeklySorte.get("demandWeekP0").containsKey(skbez)){
					jsonSorte.put("demandWeekLY0",pastSalesWeeklySorte.get("demandWeekP0").get(skbez));
				}
			}
			if(pastSalesWeeklySorte.containsKey("demandWeekP1")){
				if (pastSalesWeeklySorte.get("demandWeekP1").containsKey(skbez)){
					jsonSorte.put("demandWeekLY1",pastSalesWeeklySorte.get("demandWeekP1").get(skbez));
				}
			}*/
			if(campaignMap.containsKey(skbez)) {
				jsonSorte.put("campaignAmounts", campaignMap.get(skbez));
			}
			if(directSalesMap.containsKey(skbez)) {
				jsonSorte.put("directSale", directSalesMap.get(skbez));
			}
			responseContent.put(sorte.getSorteBez(), jsonSorte);
		}
		return responseContent;
	}
	
	private static JSONObject getSorteDataDaily(JSONObject configurations) throws JSONException, ParseException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		SorteDAO sorteDAO = new SorteDAO(configurations.getJSONObject("data").getString("source"), configurations.getJSONObject("data").getString("passPhrase"));

		Map<String, Sorte> sorteMap = sorteDAO.getSorteMasterData();
		Map<String, Map<String, Double>> currentSalesDailySorte = sorteDAO.getCurrentSalesAmountsDaily(configurations);
		Map<String, Map<String, Double>> pastSalesDailySorte = sorteDAO.getPastSalesAmountsDaily(configurations);
		Map<String, Double> inventoryMap = sorteDAO.getInventory(sorteMap, configurations);
		Map<String, Double> campaignMap = sorteDAO.getCampaigns(configurations);
		Map<String, Double> directSalesMap = sorteDAO.getDirectSales(configurations);
		Map<String, Double> unpackedMap = sorteDAO.getMengenUnverpackt(configurations);
		for(String skbez: sorteMap.keySet()) {
			Sorte sorte = sorteMap.get(skbez);
			if(inventoryMap.containsKey(skbez)) {
				sorte.setInventory(inventoryMap.get(skbez));
			}
			JSONObject jsonSorte = sorte.SorteTOJSON();
			Map<String, Double> demands = new LinkedHashMap<String, Double>();
			jsonSorte.put("unpacked",unpackedMap.get(skbez));
			for(String dateStr : currentSalesDailySorte.keySet()) {
				demands.put(dateStr,currentSalesDailySorte.get(dateStr).get(skbez));
			}
			for(String dateStr : pastSalesDailySorte.keySet()) {
				demands.put(dateStr, pastSalesDailySorte.get(dateStr).get(skbez));
			}

			jsonSorte.put("demand", demands);
			jsonSorte.put("demandWeek0",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek0").getString("content"));
			jsonSorte.put("demandWeek1",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek1").getString("content"));
			jsonSorte.put("demandWeek2",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeek2").getString("content"));
			jsonSorte.put("demandWeekLY0",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeekLY0").getString("content"));
			jsonSorte.put("demandWeekLY1",configurations.getJSONObject("factors").getJSONObject("Sorte").getJSONObject("fields").getJSONObject("demandWeekLY1").getString("content"));

			if(campaignMap.containsKey(skbez)) {
				jsonSorte.put("campaignAmounts", campaignMap.get(skbez));
			}
			if(directSalesMap.containsKey(skbez)) {
				jsonSorte.put("directSale", directSalesMap.get(skbez));
			}
			responseContent.put(sorte.getSorteBez(), jsonSorte);
		}
		return responseContent;
	}
	
	/*private static JSONObject getSorteObjects(JSONObject configurations) throws JSONException, ParseException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		SorteDAO sorteDAO = new SorteDAO(configurations.getJSONObject("data").getString("sourcepath"), configurations.getJSONObject("data").getString("passPhrase"));
		
		int considerPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		
		Map<String, Sorte> sorteMap = sorteDAO.getSorteMasterData();
		Map<String, Map<String, Datastruct>> productMapCurrent = sorteDAO.getProductDataDaily(configurations);		
		Map<String, Map<String, Datastruct>> productMapPast = sorteDAO.getProductDataDailyPast(configurations);

		Map<String, String> sortePKBezMap = sorteDAO.getSortePKBezMapping();
		Map<String, Map<String, Datastruct>> sorteMapDailyCurrent = DataAggregator.aggregatePKBezToSorte(productMapCurrent, sortePKBezMap);
		Map<String, Map<String, Datastruct>> sorteMapDailyPast = DataAggregator.aggregatePKBezToSorte(productMapPast, sortePKBezMap);
		Map<String, Map<String, Datastruct>> sorteMapWeeklyCurrent = DataAggregator.aggregateDataWeekly(sorteMapDailyCurrent);
		Map<String, Map<String, Datastruct>> sorteMapWeeklyPast = DataAggregator.aggregateDataWeekly(sorteMapDailyPast);
		Map<String, Double> inventoryMap = sorteDAO.getInventory(configurations);
		Map<String, Double> campaignMap = sorteDAO.getCampaigns(configurations);
		//Map<String, Double> saisonalityMap = sorteDAO.getSaisonality(configurations);
		Map<String, Double> campaignMapAggregated = DataAggregator.aggregateAktionenToSorte(campaignMap, sortePKBezMap);
		//TODO Implement Single Access to get all needed Data with one DB Access

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);

		for (String skbez : sorteMap.keySet()) {
			Sorte sorte = sorteMap.get(skbez);
			JSONObject jsonSorte = sorte.SorteTOJSON();
			
			
			if(sorteMapWeeklyCurrent.containsKey(skbez)) {
				if(inventoryMap.containsKey(skbez)) {
					sorte.setInventory(inventoryMap.get(skbez));
				}
				
				Map<String,Datastruct> sorteDataWeekly = sorteMapWeeklyCurrent.get(skbez);
				String strDatum = configurations.getJSONObject("data").getString("toDate");
				Date datum = dateFormat.parse(strDatum);
				calendar.setTime(datum); 
				String demandWeek0 = DataAggregator.createKWDBFormat(strDatum);
				if(sorte!=null) {
					if(sorteDataWeekly != null) {
						//TODO KW hardcoded implement calculation in combination with configfile
						jsonSorte.put("demandWeek0", sorteDataWeekly.get(demandWeek0).getMenge());
						for(int i = 1; i < considerPeriods; i++) {
							calendar.add(Calendar.DAY_OF_MONTH, -7);
							datum = calendar.getTime();
							strDatum = dateFormat.format(datum); 
							//System.out.println("STrDate: " + strDatum);
							//System.out.println("DemandWeekM" + i + ": " + DataAggregator.createKWDBFormat(strDatum));
							jsonSorte.put("demandWeekM" + i,  sorteDataWeekly.get(DataAggregator.createKWDBFormat(strDatum)).getMenge());
							//jsonSorte.put("demandWeekM2",  sorteDataWeekly.get(demandWeekM2).getMenge());
						}
	
							
						if (sorteMapWeeklyPast.containsKey(skbez)){
							Map<String,Datastruct> sorteDataWeeklyPast = sorteMapWeeklyPast.get(skbez);
							Date datumPast = dateFormat.parse(configurations.getJSONObject("data").getString("toDate"));
							calendar.setTime(datumPast); 
							calendar.add(Calendar.YEAR, -1);
							int kw = calendar.get(Calendar.WEEK_OF_YEAR);
							calendar.set(Calendar.WEEK_OF_YEAR, kw); 
							calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
							datumPast = calendar.getTime();
							String strDatumPast = dateFormat.format(datumPast);
							jsonSorte.put("demandLastYear",  sorteDataWeeklyPast.get(DataAggregator.createKWDBFormat(strDatumPast)).getMenge());
							calendar.set(Calendar.WEEK_OF_YEAR, kw + 1); 
							datumPast = calendar.getTime();
							strDatumPast = dateFormat.format(datumPast);
							jsonSorte.put("demandLastYearP1",  sorteDataWeeklyPast.get(DataAggregator.createKWDBFormat(strDatumPast)).getMenge());						
							
						}
					
		
						if(campaignMapAggregated.containsKey(skbez)) {
							jsonSorte.put("campaignAmounts", campaignMapAggregated.get(skbez));
						}
						
						//System.out.println(jsonSorte.toString());
						//responseContent.put(sorte.getSorteBez(), jsonSorte);
				//}else {
	//				System.out.println("Sorte ist NULL");
				//}
					}
				}
			//TODO inventory changed from double to map
			}
			
			
			responseContent.put(sorte.getSorteBez(), jsonSorte);
			//responseContent.put(sorteBez, jsonSorte);
		}
		
		return responseContent;
	}*/
	
	private static JSONObject getMilkObjects(JSONObject configurations) throws JSONException, ParseException, ClassNotFoundException, SQLException {
		JSONObject responseContent = new JSONObject();
		//MilkDAO milkDAO = new MilkDAO(configurations.getJSONObject("data").getString("sourcepath"), configurations.getJSONObject("data").getString("passPhrase"));
		MilkDAO milkDAO = new MilkDAO(configurations.getJSONObject("data").getString("source"), configurations.getJSONObject("data").getString("passPhrase"));
		Map<String, Milk> milkMap = milkDAO.getMilkData(configurations);
		for (String key : milkMap.keySet()) {
			Milk milk = milkMap.get(key);
			JSONObject jsonMilk = milk.toJSON();
			responseContent.put(key, jsonMilk);

		}
		//}
		return responseContent;
	}
	
	private static Map<String, Sorte> getDynamicSorteDataDaily(Map<String,Sorte> sorteMap, SorteDAO sorteDAO){
		//aufteilen von getMasterDAta?
		
		return sorteMap;
	}
	
	public static String createSorteCSV(Sorte sorte, String path) throws JSONException {
		String content = sorte.toJSONString();
		//needed?
		//CSVWriter.createCSV(path, content);
		return path;
	}

}