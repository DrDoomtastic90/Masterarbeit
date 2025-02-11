package serviceImplementation;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;
import outputHandler.CustomFileWriter;
import serverImplementation.HttpServerRuleBased;
import webClient.RestClient;


@Path("/RuleBasedService")
public class RuleBasedController {

		
		@POST
		@Produces(MediaType.APPLICATION_JSON)
		public void performRuleBasedAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
			try {
				
				//JSONObject ruleBasedConfigurations = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				JSONObject ruleBasedConfigurations = requestBody.getJSONObject("configurations");
				JSONObject worldFacts = requestBody.getJSONObject("dataset");
				JSONObject drlJSON = requestBody.getJSONObject("drlFile");
				//String drlFileLocation =ruleBasedConfigurations.getJSONObject("data").getString("drlFilePath");
				//String passPhrase = ruleBasedConfigurations.getString("passPhrase");
				int forecastPeriods = ruleBasedConfigurations.getJSONObject("parameters").getInt("forecastPeriods");
				//File drlFile = WebInputHandler.getWebFile(drlFilepath,".drl");
				//JSONObject drlJSON = invokeDRLFileService(drlFileLocation, passPhrase);
				
				File drlFile = CustomFileWriter.createTempFile(drlJSON.getString("drlFile"));
				JSONObject factors = ruleBasedConfigurations.getJSONObject("factors");
				AnalysisService analysisService = new AnalysisService();
				//String preparedData;
				//preparedData = analysisService.getPreparedData(ruleBasedConfigurations);	
				//JSONObject worldFacts = new JSONObject(preparedData);
				analysisService.prepareForecasting(drlFile, factors);
				JSONObject analysisResult = analysisService.analyseWorld(worldFacts, forecastPeriods);
				//Write JSON File to File System
				//JSONWritter.createJSON(ruleBasedConfigurations.getJSONObject("data").getString("target"),preparedData.getJSONObject("ruleBased").toString());
				response.setStatus(202);
				response.setContentType("application/json");
				response.getWriter().write(analysisResult.toString());
				response.flushBuffer();
				if(HttpServerRuleBased.isAutomaticShutdown()) {
					HttpServerRuleBased.attemptShutdown();
				}
			} catch (JSONException | IOException e) {
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
		
		private JSONObject invokeDRLFileService(String drlFileLocation, String passPhrase) throws IOException {
			//Internal Implementation
			URL url = new URL(drlFileLocation);	
			//public_html implementation Forecasting
			//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
			String contentType = "application/json";
			JSONObject requestBody = new JSONObject();
			requestBody.put("username", "ForecastingTool");
			requestBody.put("password", "forecasting");
			requestBody.put("passPhrase", passPhrase);
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}


}
