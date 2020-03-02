package serviceImplementation;

import org.json.JSONObject;

public class RuleBasedResult {
	private static JSONObject result = new JSONObject();
	
	public static JSONObject getResult() {
		return result;
	}

	public static void setForecastResult(String entryKey, double productionAmount) {
		JSONObject forecastResult;
		if(result.has("forecastResult")){
			forecastResult = result.getJSONObject("forecastResult");
		}else {
			forecastResult  = new JSONObject();
		}
		forecastResult.put(entryKey, productionAmount);
		result.put("forecastResult", forecastResult);
	}
	public static void setProdPlanResult(String entryKey, double productionAmount) {
		JSONObject prodPlanResult;
		if(result.has("prodPlanResult")){
			prodPlanResult =  result.getJSONObject("prodPlanResult");
		}else {
			prodPlanResult  = new JSONObject();
		}
		prodPlanResult.put(entryKey, productionAmount);
		result.put("prodPlanResult", prodPlanResult);
	}
	public static void setInitialForecastResult(String entryKey, double productionAmount) {
		JSONObject initForecastResult;
		if(result.has("initForecastResult")){
			initForecastResult =  result.getJSONObject("initForecastResult");
		}else {
			initForecastResult  = new JSONObject();
		}
		initForecastResult.put(entryKey, productionAmount);
		result.put("initForecastResult", initForecastResult);
	}
}
