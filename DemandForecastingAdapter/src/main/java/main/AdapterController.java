package main;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONObject;

import outputHandler.CustomFileWriter;
import webClient.RestClient;

public class AdapterController {

	public static void main(String[] args) throws Exception {
		String passphrase = args[1];
		String result = startCombinedForecasting(passphrase);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		String strDate = dateFormat.format(calendar.getTime());
		CustomFileWriter.createJSON("/home/matthiasb90/Masterarbeit/Bantel/Daten/ForecastingResults/Combined" + strDate, result);
	}
	
	private static String startCombinedForecasting(String passphrase) throws IOException {
		URL url = new URL("http://localhost:8080/ForecastingServices");
		String contentType = "application/json";
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", "BantelGmbH");
		requestBody.put("password", "bantel");
		requestBody.put("passPhrase", passphrase);
		RestClient restClient = new RestClient(url, contentType);
		return restClient.postRequest(requestBody.toString());
	}
}
