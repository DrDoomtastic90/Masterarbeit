package serviceImplementation;



import java.sql.SQLException;
import java.util.LinkedHashMap;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.PreparationDAO;

public class DataAggregator {
	public static Map<String, String> getSortenPKBezMapping(PreparationDAO daoAnalysis) throws SQLException {
		Map<String, String> sortePKBezMap = daoAnalysis.getSortePKBezMapping();
		return sortePKBezMap;

	}

	public static JSONObject aggregateSorteDataDaily(String kNo, JSONObject configurations, PreparationDAO daoAnalysis) throws JSONException, SQLException {
		Map<String, String> sortePKBezMap = getSortenPKBezMapping(daoAnalysis);
		JSONObject dailyDataAllPKBEZ = daoAnalysis.getDataDaily(kNo, configurations);
		JSONObject dailyDataAllSorte = new JSONObject();
		
		for (String pKBez : dailyDataAllPKBEZ.keySet()) {
			if (pKBez == null) {
				//System.out.println("WTF");
			} else {

				String sorte = sortePKBezMap.get(pKBez);
				JSONObject dailyDataSinglePKBez = dailyDataAllPKBEZ.getJSONObject(pKBez);
				JSONObject dailyDataSingleSorte = new JSONObject();
				if (dailyDataAllSorte.has(sorte)) {
					dailyDataSingleSorte = dailyDataAllSorte.getJSONObject(sorte);
				}
				if (dailyDataSinglePKBez == null) {
					//System.out.println("WTF2");
				} else {
					for (String dateEntry : dailyDataSinglePKBez.keySet()) {
						if (dailyDataSingleSorte.has(dateEntry)) {
							double mengeAlt = dailyDataSingleSorte.getDouble(dateEntry);
							double mengeNeu = dailyDataSinglePKBez.getDouble(dateEntry);
							dailyDataSingleSorte.put(dateEntry,(mengeNeu + mengeAlt));
						} else {
							dailyDataSingleSorte.put(dateEntry, dailyDataSinglePKBez.getDouble(dateEntry));
						}
					}
					dailyDataAllSorte.put(sorte, dailyDataSingleSorte);
				}
			}
		}
		return dailyDataAllSorte;
	}
}
