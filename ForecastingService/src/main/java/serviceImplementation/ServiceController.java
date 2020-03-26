package serviceImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;


import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;
import webClient.RestClient;


@Path("/ForecastingServices")
public class ServiceController {

	private JSONObject calculateCombinedResult(JSONObject results) {
		JSONObject combinedResult = new JSONObject();
		JSONObject arimaResult=results.getJSONObject("ARIMAResult");
		JSONObject ruleBasedResult=results.getJSONObject("RuleBasedResult");
		JSONObject kalmanResult=results.getJSONObject("KalmanResult");
		JSONObject expSmoothingResult=results.getJSONObject("ExpSmoothingResult");
		JSONObject aNNResult=results.getJSONObject("ANNResult");
		for(String sorte : arimaResult.keySet()) {
			JSONObject resultSorte = new JSONObject();
			for(String forecastPeriod : arimaResult.getJSONObject(sorte).keySet()){
				double arimaValue=arimaResult.getJSONObject(sorte).getDouble(forecastPeriod);
				double ruleBasedValue=ruleBasedResult.getJSONObject(sorte).getDouble(forecastPeriod);
				double kalmanValue=kalmanResult.getJSONObject(sorte).getDouble(forecastPeriod);
				double expSmoothingValue=expSmoothingResult.getJSONObject(sorte).getDouble(forecastPeriod);
				double aNNValue=aNNResult.getJSONObject(sorte).getDouble(forecastPeriod);
				double combinedValue = (arimaValue+ruleBasedValue+kalmanValue+expSmoothingValue+aNNValue)/5;
				resultSorte.put(forecastPeriod, combinedValue);
			}
			combinedResult.put(sorte, resultSorte);
		}
		
		
		return combinedResult;
	}
	
	@GET
	@Path("/RuleBasedService")
	@Produces(MediaType.APPLICATION_JSON)
	public void performRuleBasedAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = invokeLoginService(requestBody);
			if(loginCredentials.getBoolean("isAuthorized")) {
				if(loginCredentials.getBoolean("isEnabledRuleBased")) {
				JSONObject jsonConfigurations =  invokeConfigFileService(loginCredentials.getString("apiURL"));
				JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
				JSONObject analysisResult = invokeRuleBasedService(ruleBasedConfigurations);
				response.setContentType("application/json");
				response.setStatus(200);
				response.getWriter().write(analysisResult.toString());
			}else {
				response.setContentType("application/json");
				response.setStatus(401);
				response.getWriter().write("Permission Denied");
			}
			response.flushBuffer();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GET
	//@Path("{parameter: |CombinedServices}")
	@Produces(MediaType.APPLICATION_JSON)
	public void performCombinedAnalysis(@Context HttpServletRequest request, @Suspended final AsyncResponse asyncResponse) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = invokeLoginService(requestBody);
			if(loginCredentials.getBoolean("isAuthorized")) {
	        	JSONObject combinedAnalysisResult = new JSONObject();
	        	JSONObject analysisResult = null;
	        	JSONObject jsonConfigurations =  invokeConfigFileService(loginCredentials.getString("apiURL"));
	        	
	        	
	        	//String from = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("from");
	        	//String to = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
	        	int forecastPeriods = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastPeriods");
	        	String username = jsonConfigurations.getJSONObject("user").getString("name");
	        	asyncResponse.resume("Request Successfully Received. Result will be returned as soon as possible!");
	        	
	        	if(loginCredentials.getBoolean("isEnabledRuleBased") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
					//ruleBasedConfigurations.getJSONObject("data").put("from", from);
					//ruleBasedConfigurations.getJSONObject("data").put("to", to);
					ruleBasedConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					ruleBasedConfigurations.put("username", "ForecastingTool");
					ruleBasedConfigurations.put("password", "forecasting");
					ruleBasedConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeRuleBasedService(ruleBasedConfigurations);
					combinedAnalysisResult.put("RuleBasedResult", analysisResult);
				}
				if(loginCredentials.getBoolean("isEnabledARIMA") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					JSONObject aRIMAConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");
					aRIMAConfigurations.getJSONObject("data").put("from", from);
					aRIMAConfigurations.getJSONObject("data").put("to", to);
					aRIMAConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					aRIMAConfigurations.put("username", username);
					aRIMAConfigurations.put("password", "forecasting");
					aRIMAConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeARIMAService(aRIMAConfigurations);
					combinedAnalysisResult.put("ARIMAResult", analysisResult);
				}
				boolean execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledKalman") && execute) {
					JSONObject kalmanConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman");
					kalmanConfigurations.getJSONObject("data").put("from", from);
					kalmanConfigurations.getJSONObject("data").put("to", to);
					kalmanConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					kalmanConfigurations.put("username", username);
					kalmanConfigurations.put("password", "forecasting");
					kalmanConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeKalmanService(kalmanConfigurations);
					combinedAnalysisResult.put("KalmanResult", analysisResult);
				}
				execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledExpSmoothing") && execute) {
					JSONObject kalmanConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");
					kalmanConfigurations.getJSONObject("data").put("from", from);
					kalmanConfigurations.getJSONObject("data").put("to", to);
					kalmanConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					kalmanConfigurations.put("username", username);
					kalmanConfigurations.put("password", "forecasting");
					kalmanConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeExpSmoothingService(kalmanConfigurations);
					combinedAnalysisResult.put("ExpSmoothingResult", analysisResult);
				}
				execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledANN") && execute) {
					JSONObject aNNConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN");
					aNNConfigurations.getJSONObject("data").put("from", from);
					aNNConfigurations.getJSONObject("data").put("to", to);
					aNNConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					aNNConfigurations.put("username", username);
					aNNConfigurations.put("password", "forecasting");
					aNNConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeANNFeedForwardService(aNNConfigurations);
					combinedAnalysisResult.put("ANNResult", analysisResult);
				}
				combinedAnalysisResult.put("CombinedResult", calculateCombinedResult(combinedAnalysisResult));
				jsonConfigurations.put("results", combinedAnalysisResult);
				jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").put("username", username);
				jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").put("password", "forecasting");
				jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").put("passPhrase", requestBody.get("passPhrase"));
				invokeCallbackService(jsonConfigurations);
				invokeEvaluationService(jsonConfigurations);
			}
			/*}else {
				response.setContentType("application/json");
				response.setStatus(401);
				response.getWriter().write("Permission Denied");
				response.flushBuffer();
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@POST
	@Path("/{Service}/shutDownService/{Port}")
	private static void shutdownService(@Context HttpServletRequest request, @Context HttpServletResponse response, @PathParam("Service") String service, @PathParam("Port") String port) {
        try{
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
        	String passphrase = requestBody.getString("passphrase");
            URL url = new URL("http://localhost:" + port + "/shutdown?token=" + passphrase);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.getResponseCode();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
	
	
	private boolean isPortInUse(String host, int port) {
        boolean inUse = false;
        //error is caught to allow start of new Server on Free port
        try {
            (new Socket(host, port)).close();
            inUse = true;
        } catch (SocketException e) {
            // Could not connect.
        } catch (UnknownHostException e) {
            // Host not found
        } catch (IOException e) {
            // IO exception
        }    
        return inUse;
    }
	
	
	private JSONObject invokeRuleBasedService(JSONObject ruleBasedConfigurations) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/RuleBasedService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		String requestBody = ruleBasedConfigurations.toString();
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody));
	}
	
	private JSONObject invokeARIMAService(JSONObject aRIMAConfigurations) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/ARIMAService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		String requestBody = aRIMAConfigurations.toString();
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType, 12000);
		return new JSONObject(restClient.postRequest(requestBody));
	}
	
	private JSONObject invokeKalmanService(JSONObject kalmanConfigurations) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/KalmanService/ARIMA");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		String requestBody = kalmanConfigurations.toString();
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType, 12000);
		return new JSONObject(restClient.postRequest(requestBody));
	}
	
	private JSONObject invokeExpSmoothingService(JSONObject expSmoothingConfigurations) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/SmoothingService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		String requestBody = expSmoothingConfigurations.toString();
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType, 12000);
		return new JSONObject(restClient.postRequest(requestBody));
	}
	
	private JSONObject invokeANNFeedForwardService(JSONObject aNNConfigurations) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/ANNService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		String requestBody = aNNConfigurations.toString();
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType, 12000);
		return new JSONObject(restClient.postRequest(requestBody));
	}
	
	private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		//Internal Implementation
		//auslagern NGINX
		/*URL url = new URL("http://localhost:" + 8300 + "/LoginServices/CustomLoginService");
		if(!isPortInUse("localhost", 8300)) {
    		if(!isPortInUse("localhost", 8301)) {
    			throw new RuntimeException("Server not Running");
    		}else {
    			url = new URL("https://localhost:" + 8301 + "/LoginServices/CustomLoginService");
    		}
		}*/	
		URL url = new URL("http://localhost:" + 8110 + "/LoginServices/CustomLoginService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	private JSONObject invokeConfigFileService(String configFileLocation) throws IOException {
		//Internal Implementation
		URL url = new URL(configFileLocation);	
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(configFileLocation.toString()));
	}
	
	private JSONObject invokeEvaluationService(JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/EvaluationService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	private JSONObject invokeCallbackService(JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL("https://localhost:" + 9100 + "/Callback");
		requestBody.getJSONObject("forecasting").getJSONObject("Combined").put("username", "ForecastingTool");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	
	
	
	//Nur wichtig wenn kein Port erreichbar ist. Dann Server starten und wieder abdrehen. Fehlermeldung falls Exception passiert  
	private void startServer() throws IOException {
    	// Run a java app in a separate system process
    	Process proc = Runtime.getRuntime().exec("java -jar D:\\Arbeit\\Bantel\\Masterarbeit\\Programme\\JavaAdapters\\runnable\\RuleBasedMicroservice.jar");
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    	BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    	//TODO Error HAndling if error is returned
    }
	
	
}
