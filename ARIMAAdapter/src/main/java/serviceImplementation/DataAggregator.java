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

	public static Map<String, Map<String, Datastruct>> aggregateSorteDataDaily(String kNo, JSONObject configurations, PreparationDAO daoAnalysis) throws JSONException, SQLException {
		Map<String, String> sortePKBezMap = getSortenPKBezMapping(daoAnalysis);
		Map<String, Map<String, Datastruct>> dailyDataAllPKBEZ = daoAnalysis.getDataDaily(kNo, configurations);
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
}
