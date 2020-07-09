package serviceImplementation;



import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedHashMap;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.research.ws.wadl.Link;

import dBConnections.PreparationDAO;

public class DataAggregator {
	public static Map<String, String> getSortenPKBezMapping(PreparationDAO daoAnalysis) throws SQLException {
		Map<String, String> sortePKBezMap = daoAnalysis.getSortePKBezMapping();
		return sortePKBezMap;

	}

	public static Map<String, Map<String,Double>> aggregateSorteDataDaily(String kNo, JSONObject configurations, PreparationDAO daoAnalysis) throws JSONException, SQLException, ParseException {
		Map<String, String> sortePKBezMap = getSortenPKBezMapping(daoAnalysis);
		Map<String,Map<String,Double>> dailyDataAllPKBEZ = daoAnalysis.getDataDailyPerWeek(kNo, configurations);
		Map<String,Map<String,Double>> dailyDataAllSorte = new LinkedHashMap<String, Map<String,Double>>();
		
		for (String pKBez : dailyDataAllPKBEZ.keySet()) {
			if (pKBez == null) {
				//System.out.println("WTF");
			} else {

				String sorte = sortePKBezMap.get(pKBez);
				Map<String,Double> dailyDataSinglePKBez = dailyDataAllPKBEZ.get(pKBez);
				Map<String, Double> dailyDataSingleSorte = new LinkedHashMap<String, Double>();
				if (dailyDataAllSorte.containsKey(sorte)) {
					dailyDataSingleSorte = dailyDataAllSorte.get(sorte);
				}
				if (dailyDataSinglePKBez == null) {
					//System.out.println("WTF2");
				} else {
					for (String dateEntry : dailyDataSinglePKBez.keySet()) {
						if (dailyDataSingleSorte.containsKey(dateEntry)) {
							double mengeAlt = dailyDataSingleSorte.get(dateEntry);
							double mengeNeu = dailyDataSinglePKBez.get(dateEntry);
							dailyDataSingleSorte.put(dateEntry,(mengeNeu + mengeAlt));
						} else {
							dailyDataSingleSorte.put(dateEntry, dailyDataSinglePKBez.get(dateEntry));
						}
					}
					dailyDataAllSorte.put(sorte, dailyDataSingleSorte);
				}
			}
		}
		return dailyDataAllSorte;
	}
}
