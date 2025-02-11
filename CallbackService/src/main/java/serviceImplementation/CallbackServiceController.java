package serviceImplementation;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.CallbackDAO;
import dBConnections.CallbackDBConnection;
import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;
import outputHandler.CustomFileWriter;
import serverImplementation.HttpServerCallback;
import webClient.RestClient;


@Path("/Callback")
public class CallbackServiceController {

		
		@POST
		@Produces(MediaType.APPLICATION_JSON)
		public void performCallback(@Context HttpServletRequest request, @Context HttpServletResponse response) {

			try {
				JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials"); 
				JSONObject configurations = requestBody.getJSONObject("configurations"); 
				JSONObject forecastResults = requestBody.getJSONObject("results");
				loginCredentials = invokeLoginService(loginCredentials);
				//JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				//JSONObject loginCredentials = invokeLoginService(requestBody.getJSONObject("forecasting").getJSONObject("Combined"));
				if(loginCredentials.getBoolean("isAuthorized")) {
					
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date toDate = new Date();
					String dateString = dateFormat.format(toDate);
			        UUID uuid = UUID.randomUUID();
			        String randomUUIDString = uuid.toString();
					String groupIdentifier = dateString + "_" + randomUUIDString;
					
					CallbackDBConnection.getInstance("CallbackDB");
					CallbackDAO callbackDAO = new CallbackDAO();
							
					if(forecastResults.has("ARIMAResult")) {
						JSONObject aRIMAConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ARIMA");
						JSONObject aRIMAResult = forecastResults.getJSONObject("ARIMAResult");
						callbackDAO.writeForecastResultsToDB(aRIMAConfigurations, "ARIMA", aRIMAResult, groupIdentifier);
					}
					if(forecastResults.has("ExponentialSmoothingResult")) {
						JSONObject expSmoothingConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");
						JSONObject expSmoothingResult = forecastResults.getJSONObject("ExponentialSmoothingResult");
						callbackDAO.writeForecastResultsToDB(expSmoothingConfigurations, "ExponentialSmoothing", expSmoothingResult, groupIdentifier);
					}
					if(forecastResults.has("kalmanResult")) {
						JSONObject kalmanConfigurations = configurations.getJSONObject("forecasting").getJSONObject("Kalman");
						JSONObject kalmanResult = forecastResults.getJSONObject("kalmanResult");
						callbackDAO.writeForecastResultsToDB(kalmanConfigurations, "Kalman", kalmanResult, groupIdentifier);
					}
					if(forecastResults.has("ruleBasedResult")) {
						JSONObject ruleBasedConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ruleBased");
						JSONObject ruleBasedResult = forecastResults.getJSONObject("ruleBasedResult");
						callbackDAO.writeForecastResultsToDB(ruleBasedConfigurations, "ruleBased", ruleBasedResult, groupIdentifier);
					}
					if(forecastResults.has("ANNResult")) {
						JSONObject aNNConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ANN");
						JSONObject aNNResult = forecastResults.getJSONObject("ANNResult");
						callbackDAO.writeForecastResultsToDB(aNNConfigurations, "ANN", aNNResult, groupIdentifier);
					}
					if(forecastResults.has("CombinedResult")) {
						JSONObject combinedConfigurations = configurations.getJSONObject("forecasting").getJSONObject("Combined");
						JSONObject combinedResult = forecastResults.getJSONObject("CombinedResult");
						callbackDAO.writeForecastResultsToDB(combinedConfigurations, "Combined", combinedResult, groupIdentifier);
					}
					if(forecastResults.has("bruteForceResult")) {
						//JSONObject aNNConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ANN");
						JSONObject aNNBruteResult = forecastResults.getJSONObject("bruteForceResult");
						String targetPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ANN\\";
						CustomFileWriter.createJSON(targetPath + "bruteForceResult.json", aNNBruteResult.toString());
						//callbackDAO.writeForecastResultsToDB(combinedConfigurations, "Combined", combinedResult, groupIdentifier);
					}
					
					
					String filePath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\Result.json";
					CustomFileWriter.createFile(filePath, requestBody.getJSONObject("results").toString());
					response.setStatus(202);
					response.setContentType("application/json");
					response.getWriter().write("{\"Result\": \"Execution Successful\"}");
					response.flushBuffer();
				}else {
					response.setStatus(400);
					response.setContentType("application/json");
					response.getWriter().write("Error");
					response.flushBuffer();
				}
				if(HttpServerCallback.isAutomaticShutdown()) {
					HttpServerCallback.attemptShutdown();
				}
			} catch (JSONException | IOException e) {
				e.printStackTrace();
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void startServer() throws IOException {
	    	// Run a java app in a separate system process
	    	Process proc = Runtime.getRuntime().exec("java -jar D:\\Arbeit\\Bantel\\Masterarbeit\\Programme\\JavaAdapters\\runnable\\RuleBasedMicroservice.jar");
	    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	    	BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	    	String result = stdInput.lines().collect(Collectors.joining());
	    	String error = stdError.lines().collect(Collectors.joining());
	    	//TODO Error HAndling if error is returned
	    }
		
		
		private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
			URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
			String contentType = "application/json";
			RestClient restClient = new RestClient();
			restClient.setHttpConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}


}
