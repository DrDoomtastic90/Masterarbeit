package serviceImplementation;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;





public class DataAggregator {


	public static Map<String, Map<String, Datastruct>> aggregatePKBezToSorte(Map<String, Map<String, Datastruct>> productMap, Map<String, String> sortePKBezMap) throws JSONException {
		Map<String, Map<String, Datastruct>> sorteDataDaily = new LinkedHashMap<String, Map<String, Datastruct>>();
		for (String pKBez : productMap.keySet()) {
			if (pKBez == null) {
				//System.out.println("WTF");
			} else {
				String sorte = sortePKBezMap.get(pKBez);
				Map<String, Datastruct> dailyDataSinglePKBez = productMap.get(pKBez);
				Map<String, Datastruct> aggregatedDataSingleSorte = new LinkedHashMap<String, Datastruct>();
				if (sorteDataDaily.containsKey(sorte)) {
					aggregatedDataSingleSorte = sorteDataDaily.get(sorte);
				}
				if (dailyDataSinglePKBez == null) {
					//System.out.println("WTF2");
				} else {
					for (String dateEntry : dailyDataSinglePKBez.keySet()) {
						if (aggregatedDataSingleSorte.containsKey(dateEntry)) {
							double mengeAlt = aggregatedDataSingleSorte.get(dateEntry).getMenge();
							double mengeNeu = dailyDataSinglePKBez.get(dateEntry).getMenge();
							aggregatedDataSingleSorte.get(dateEntry).setMenge(mengeNeu + mengeAlt);
						} else {
							aggregatedDataSingleSorte.put(dateEntry, dailyDataSinglePKBez.get(dateEntry));
						}
						sorteDataDaily.put(sorte, aggregatedDataSingleSorte);
					}
				}
			}
		}
		return sorteDataDaily;
	}
	
	public static Map<String, Double> aggregateAktionenToSorte(Map<String, Double> productMap, Map<String, String> sortePKBezMap) throws JSONException {
		Map<String, Double> aggregatedDataSorte = new LinkedHashMap<String, Double>();
		for (String pKBez : productMap.keySet()) {
			if (pKBez == null) {
				//System.out.println("WTF");
			} else {
				String sorte = sortePKBezMap.get(pKBez);
				double menge = productMap.get(pKBez);
				if (productMap == null) {
					//System.out.println("WTF2");
				} else {
						if (aggregatedDataSorte.containsKey(sorte)) {
							double mengeAlt = aggregatedDataSorte.get(sorte);
							double mengeNeu = productMap.get(pKBez);
							aggregatedDataSorte.put(sorte, mengeNeu + mengeAlt);
						} else {
							aggregatedDataSorte.put(sorte, menge);
						}
				}
			}
		}
		return aggregatedDataSorte;
	}

	
	
	public static Map<String, Map<String, Datastruct>> aggregateDataWeekly(Map<String, Map<String, Datastruct>> dailyAnalysis){
		Map<String, Map<String, Datastruct>> weeklyAnalysis = new LinkedHashMap<String, Map<String, Datastruct>>();
		boolean isAktion = false;
		Datastruct datastruct = null;
		for (String sKBez : dailyAnalysis.keySet()) {
			Map<String, Datastruct> dailyValues = dailyAnalysis.get(sKBez);
			Map<String, Datastruct> weeklyData = new LinkedHashMap<String, Datastruct>();
			if (dailyAnalysis.get(sKBez) != null) {
				for (String dateEntry : dailyValues.keySet()) {
					try {
						String kWYearly = createKWDBFormat(dateEntry);
						isAktion = dailyValues.get(dateEntry).getAktion();
						double menge = dailyValues.get(dateEntry).getMenge();
						if (weeklyData.size() > 0 && (weeklyData.containsKey(kWYearly))) {
							menge = weeklyData.get(kWYearly).getMenge() + menge;
							weeklyData.get(kWYearly).setMenge(menge);
							if (isAktion) {
								datastruct.setAktion(isAktion);
							}
						} else {
							datastruct = new Datastruct(kWYearly, menge, isAktion);
							weeklyData.put(kWYearly, datastruct);
						}
						//System.out.println("PKBez: " + pKBez + "KW: " + kWYearly + "Menge aggr: " + menge);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				weeklyAnalysis.put(sKBez, weeklyData);
			}
		}
		return weeklyAnalysis;
	}
	
	public static JSONObject aggregateDataSorteWeekly(Map<String, Datastruct> dailyData){
		Map<String, Datastruct> weeklyData = new LinkedHashMap<String, Datastruct>();
		boolean isAktion = false;
		Datastruct datastruct = null;
			if (dailyData != null) {
				for (String dateEntry : dailyData.keySet()) {
					try {
						String kWYearly = createKWDBFormat(dateEntry);
						isAktion = dailyData.get(dateEntry).getAktion();
						double menge = dailyData.get(dateEntry).getMenge();
						if (weeklyData.size() > 0 && (weeklyData.containsKey(kWYearly))) {
							menge = weeklyData.get(kWYearly).getMenge() + menge;
							weeklyData.get(kWYearly).setMenge(menge);
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
		}
		return new JSONObject(weeklyData);
	}
	
	/*public static Map<String, Map<String, Datastruct>> aggregateMilkDataDaily(JSONObject configurations, sorteDAO daoAnalysis) throws JSONException {
		Map<String, String> sortePKBezMap = getSortenPKBezMapping(daoAnalysis);
		Map<String, Map<String, Datastruct>> dailyDataAllPKBEZ = daoAnalysis.getProductDataDaily(configurations);
		Map<String, Map<String, Datastruct>> aggregatedDataAllSorte = new LinkedHashMap<String, Map<String, Datastruct>>();
		
		for (String pKBez : dailyDataAllPKBEZ.keySet()) {
			if (pKBez == null) {
				//System.out.println("WTF");
			} else {

				String sorte = sortePKBezMap.get(pKBez);
				Map<String, Datastruct> dailyDataSinglePKBez = dailyDataAllPKBEZ.get(pKBez);
				Map<String, Datastruct> aggregatedDataSingleSorte = new LinkedHashMap<String, Datastruct>();
				if (aggregatedDataAllSorte.containsKey(sorte)) {
					aggregatedDataSingleSorte = aggregatedDataAllSorte.get(sorte);
				}
				if (dailyDataSinglePKBez == null) {
					//System.out.println("WTF2");
				} else {
					for (String dateEntry : dailyDataSinglePKBez.keySet()) {
						if (aggregatedDataSingleSorte.containsKey(dateEntry)) {
							double mengeAlt = aggregatedDataSingleSorte.get(dateEntry).getMenge();
							double mengeNeu = dailyDataSinglePKBez.get(dateEntry).getMenge();
							aggregatedDataSingleSorte.get(dateEntry).setMenge(mengeNeu + mengeAlt);
						} else {
							aggregatedDataSingleSorte.put(dateEntry, dailyDataSinglePKBez.get(dateEntry));
						}
						aggregatedDataAllSorte.put(sorte, aggregatedDataSingleSorte);
					}
				}
			}
		}
		return aggregatedDataAllSorte;
	}
	*/

	public static int getWeekNumberFromDate(Date date) {
		Calendar cal = Calendar.getInstance(Locale.GERMAN);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static int getCurrentCalendarWeek() {
		Calendar cal = Calendar.getInstance(Locale.GERMAN);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static String createKWDBFormat(String dateEntry) throws ParseException {
		Date datum = new SimpleDateFormat("yyyy-MM-dd").parse(dateEntry);
		String kW = Integer.toString(getWeekNumberFromDate(datum));
		String kWYearly = kW + "-" + dateEntry.substring(2, 4);
		return kWYearly;
	}
}
