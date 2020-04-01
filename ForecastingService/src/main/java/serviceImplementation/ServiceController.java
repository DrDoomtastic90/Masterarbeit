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
import org.json.JSONException;
import org.json.JSONObject;


import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;
import webClient.RestClient;


@Path("/ForecastingServices")
public class ServiceController {
	
	@GET
	@Path("/RuleBasedService")
	@Produces(MediaType.APPLICATION_JSON)
	public void performRuleBasedAnalysis(@Context HttpServletRequest request, @Suspended final AsyncResponse asyncResponse) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = invokeLoginService(requestBody);
			if(loginCredentials.getBoolean("isAuthorized")) {
	        	String passPhrase = requestBody.getString("passPhrase");
	        	JSONObject jsonConfigurations =  invokeConfigFileService(loginCredentials.getString("apiURL"), passPhrase);
	        	
	        	
	        	//String from = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("from");
	        	String to = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
	        	int forecastPeriods = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastPeriods");
	        	String username = jsonConfigurations.getJSONObject("user").getString("name");
	        	asyncResponse.resume("Request Successfully Received. Result will be returned as soon as possible!");
	        	
	        	if(loginCredentials.getBoolean("isEnabledRuleBased") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
	        		//get relevant rulebased Configurations
	        		JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
					
					//overwrite forecasting specific configurations with shared combined parameters
	        		ruleBasedConfigurations.getJSONObject("data").put("to", to);
	        		ruleBasedConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
		        	
					//initialize request body for service call
					JSONObject ruleBasedRequestBody = new JSONObject();
					ruleBasedRequestBody.put("configurations", ruleBasedConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					JSONObject loginCredentialsCustomerSystem = new JSONObject();
					loginCredentialsCustomerSystem.put("username", "ForecastingTool");
					loginCredentialsCustomerSystem.put("password", "forecasting");
					loginCredentialsCustomerSystem.put("passPhrase", requestBody.get("passPhrase"));
					ruleBasedRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = ruleBasedConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, ruleBasedRequestBody);
					ruleBasedRequestBody.put("dataset", preparedData);
					
					String drlFileService =ruleBasedConfigurations.getJSONObject("data").getString("drlFilePath");
					JSONObject drlJSON = invokeHTTPSService(drlFileService, loginCredentialsCustomerSystem);
					ruleBasedRequestBody.put("drlFile", drlJSON);
	
	
					//customer username used to link result to corresponding service caller
					ruleBasedConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/RuleBasedService";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					JSONObject analysisResult = invokeHTTPService(serviceURL, ruleBasedRequestBody);
					
					
					//prepare Callback Reqwuest
					JSONObject callBackRequestBody = new JSONObject();
					callBackRequestBody.put("ruleBasedResult", analysisResult);
					callBackRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);	
					String callbackServiceURL = ruleBasedConfigurations.getJSONObject("data").getString("callbackServiceURL");
					
					System.out.println(jsonConfigurations);
					//invokeCallbackService(jsonConfigurations);
					invokeHTTPSService(callbackServiceURL, callBackRequestBody);
	        	}
			}
		} catch (IOException e) {
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
	        	String passPhrase = requestBody.getString("passPhrase");
	        	JSONObject jsonConfigurations =  invokeConfigFileService(loginCredentials.getString("apiURL"), passPhrase);
	        	
	        	
	        	//String from = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("from");
	        	String to = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
	        	int forecastPeriods = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastPeriods");
	        	String callbackServiceURL = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getString("callbackServiceURL");
	        	String username = jsonConfigurations.getJSONObject("user").getString("name");
	        	asyncResponse.resume("Request Successfully Received. Result will be returned as soon as possible!");
	        	
	        	
				//login credentials to access customer system and dB passphrase is provided
				JSONObject loginCredentialsCustomerSystem = new JSONObject();
				loginCredentialsCustomerSystem.put("username", "ForecastingTool");
				loginCredentialsCustomerSystem.put("password", "forecasting");
				loginCredentialsCustomerSystem.put("passPhrase", requestBody.get("passPhrase"));
				
				
	        	if(loginCredentials.getBoolean("isEnabledRuleBased") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					/*JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
					//ruleBasedConfigurations.getJSONObject("data").put("from", from);
					ruleBasedConfigurations.getJSONObject("data").put("to", to);
					ruleBasedConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					ruleBasedConfigurations.put("username", username);
					ruleBasedConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeRuleBasedService(ruleBasedConfigurations);
					combinedAnalysisResult.put("RuleBasedResult", analysisResult);
				}*/
	        		//get relevant rulebased Configurations
	        		JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
					
					//overwrite forecasting specific configurations with shared combined parameters
	        		ruleBasedConfigurations.getJSONObject("data").put("to", to);
	        		ruleBasedConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					
					//initialize request body for service call
					JSONObject ruleBasedRequestBody = new JSONObject();
					ruleBasedRequestBody.put("configurations", ruleBasedConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					ruleBasedRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = ruleBasedConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, ruleBasedRequestBody);
					ruleBasedRequestBody.put("dataset", preparedData);
					
					String drlFileService =ruleBasedConfigurations.getJSONObject("data").getString("drlFilePath");
					JSONObject drlJSON = invokeHTTPSService(drlFileService, loginCredentialsCustomerSystem);
					ruleBasedRequestBody.put("drlFile", drlJSON);
					
					
					//not needed - executable for rulebased analysis?
					/*
					//perform outlier handling
					if(ruleBasedConfigurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) {
						String outlierHandlingProcedure = ruleBasedConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
						serviceURL = "http://localhost:" + 8110 + "/OutlierHandlingService/" + outlierHandlingProcedure + "Handler";
						preparedData = invokeHTTPService(serviceURL, ruleBasedRequestBody);
						ruleBasedRequestBody.put("dataset", preparedData);
						//preparedData =  handleOutliers(aRIMAConfigurations, preparedData);
					}
					*/
					//prepare request body for forecasting service call
					//customer username used to link result to corresponding service caller
					ruleBasedConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/RuleBasedService";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					analysisResult = invokeHTTPService(serviceURL, ruleBasedRequestBody);
					combinedAnalysisResult.put("ruleBasedResult", analysisResult);
				}
	        		
	        		
				if(loginCredentials.getBoolean("isEnabledARIMA") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					
					//get relevant ARIMA Configurations
					JSONObject aRIMAConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");
					
					//overwrite forecasting specific configurations with shared combined parameters
					aRIMAConfigurations.getJSONObject("data").put("to", to);
					aRIMAConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					
					//initialize request body for service call
					JSONObject aRIMARequestBody = new JSONObject();
					aRIMARequestBody.put("configurations", aRIMAConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					aRIMARequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = aRIMAConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, aRIMARequestBody);
					aRIMARequestBody.put("dataset", preparedData);
					//JSONObject preparedData = getPreparedData(aRIMAConfigurations);
					
					//perform outlier handling
					if(aRIMAConfigurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) {
						String outlierHandlingProcedure = aRIMAConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
						serviceURL = "http://localhost:" + 8110 + "/OutlierHandlingService/" + outlierHandlingProcedure + "Handler";
						preparedData = invokeHTTPService(serviceURL, aRIMARequestBody);
						aRIMARequestBody.put("dataset", preparedData);
						//preparedData =  handleOutliers(aRIMAConfigurations, preparedData);
					}
					
					//prepare request body for forecasting service call
					//customer username used to link result to corresponding service caller
					aRIMAConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/ARIMAService";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					analysisResult = invokeHTTPService(serviceURL, aRIMARequestBody);
					combinedAnalysisResult.put("ARIMAResult", analysisResult);
				}
				boolean execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledKalman") && execute) {
					
					//get relevant Kalman Configurations
					JSONObject kalmanConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman");
					
					//overwrite forecasting specific configurations with shared combined parameters
					kalmanConfigurations.getJSONObject("data").put("to", to);
					kalmanConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					
					//initialize request body for service call
					JSONObject kalmanRequestBody = new JSONObject();
					kalmanRequestBody.put("configurations", kalmanConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					kalmanRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = kalmanConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, kalmanRequestBody);
					kalmanRequestBody.put("dataset", preparedData);
					//JSONObject preparedData = getPreparedData(aRIMAConfigurations);
					
					//perform outlier handling
					if(kalmanConfigurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) {
						String outlierHandlingProcedure = kalmanConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
						serviceURL = "http://localhost:" + 8110 + "/OutlierHandlingService/" + outlierHandlingProcedure + "Handler";
						preparedData = invokeHTTPService(serviceURL, kalmanRequestBody);
						kalmanRequestBody.put("dataset", preparedData);
						//preparedData =  handleOutliers(aRIMAConfigurations, preparedData);
					}
					
					//prepare request body for forecasting service call
					//customer username used to link result to corresponding service caller
					kalmanConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/KalmanService/ARIMA";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					analysisResult = invokeHTTPService(serviceURL, kalmanRequestBody);
					combinedAnalysisResult.put("kalmanResult", analysisResult);
				}
					
					
					
			/*		JSONObject kalmanConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman");
					kalmanConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					kalmanConfigurations.put("username", username);
					kalmanConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeKalmanService(kalmanConfigurations);
					combinedAnalysisResult.put("KalmanResult", analysisResult);
				}*/
				execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledExpSmoothing") && execute) {
					/*JSONObject expSmoothingConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");
					expSmoothingConfigurations.getJSONObject("data").put("to", to);
					expSmoothingConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					expSmoothingConfigurations.put("username", username);
					expSmoothingConfigurations.put("password", "forecasting");
					expSmoothingConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeExpSmoothingService(expSmoothingConfigurations);
					combinedAnalysisResult.put("ExpSmoothingResult", analysisResult);
				}*/
				
					//get relevant expSmoothing Configurations
					JSONObject expSmoothingConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");
					
					//overwrite forecasting specific configurations with shared combined parameters
					expSmoothingConfigurations.getJSONObject("data").put("to", to);
					expSmoothingConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					
					//initialize request body for service call
					JSONObject expSmoothingRequestBody = new JSONObject();
					expSmoothingRequestBody.put("configurations", expSmoothingConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					expSmoothingRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = expSmoothingConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, expSmoothingRequestBody);
					expSmoothingRequestBody.put("dataset", preparedData);
					//JSONObject preparedData = getPreparedData(aRIMAConfigurations);
					
					//perform outlier handling
					if(expSmoothingConfigurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) {
						String outlierHandlingProcedure = expSmoothingConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
						serviceURL = "http://localhost:" + 8110 + "/OutlierHandlingService/" + outlierHandlingProcedure + "Handler";
						preparedData = invokeHTTPService(serviceURL, expSmoothingRequestBody);
						expSmoothingRequestBody.put("dataset", preparedData);
						//preparedData =  handleOutliers(aRIMAConfigurations, preparedData);
					}
				
					//prepare request body for forecasting service call
					//customer username used to link result to corresponding service caller
					expSmoothingConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/SmoothingService";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					analysisResult = invokeHTTPService(serviceURL, expSmoothingRequestBody);
					combinedAnalysisResult.put("ExpSmoothingResult", analysisResult);
				}
				execute = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute");
				if(loginCredentials.getBoolean("isEnabledANN") && execute) {
				
					//get relevant ANN Configurations
					JSONObject aNNConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN");
					
					//overwrite forecasting specific configurations with shared combined parameters
					aNNConfigurations.getJSONObject("data").put("to", to);
					aNNConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					
					//initialize request body for service call
					JSONObject aNNRequestBody = new JSONObject();
					aNNRequestBody.put("configurations", aNNConfigurations);				
					
					//get prepared data
					//login credentials to access customer system and dB passphrase is provided
					aNNRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
					String serviceURL = aNNConfigurations.getJSONObject("data").getString("provisioningServiceURL");
					JSONObject preparedData = invokeHTTPSService(serviceURL, aNNRequestBody);
					aNNRequestBody.put("dataset", preparedData);
					//JSONObject preparedData = getPreparedData(aRIMAConfigurations);
					
					//perform outlier handling
					/* IMPLEMENTIERUNG MUSS NOCH UEBERPRUEFT WERDEN.
					
					if(aNNConfigurations.getJSONObject("parameters").getJSONObject("outliers").getBoolean("handle")) {
						String outlierHandlingProcedure = aNNConfigurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
						serviceURL = "http://localhost:" + 8110 + "/OutlierHandlingService/" + outlierHandlingProcedure + "Handler";
						preparedData = invokeHTTPService(serviceURL, aNNRequestBody);
						aNNRequestBody.put("dataset", preparedData);
						//preparedData =  handleOutliers(aRIMAConfigurations, preparedData);
					}
					*/
					//prepare request body for forecasting service call
					//customer username used to link result to corresponding service caller
					aNNConfigurations.put("username", username);
					serviceURL = "http://localhost:" + 8110 + "/ANNService";
					//analysisResult =  invokeARIMAService(aRIMAConfigurations, preparedData);
					analysisResult = invokeHTTPService(serviceURL, aNNRequestBody);
					combinedAnalysisResult.put("ANNResult", analysisResult);
				}
					
					/*JSONObject aNNConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN");
					//aNNConfigurations.getJSONObject("data").put("from", from);
					aNNConfigurations.getJSONObject("data").put("to", to);
					aNNConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
					aNNConfigurations.put("username", username);
					aNNConfigurations.put("passPhrase", requestBody.get("passPhrase"));
					analysisResult =  invokeANNFeedForwardService(aNNConfigurations);
					combinedAnalysisResult.put("ANNResult", analysisResult);
				}*/
				combinedAnalysisResult.put("CombinedResult", calculateCombinedResult(combinedAnalysisResult));
				
				//prepare Callback Reqwuest
				JSONObject callBackRequestBody = new JSONObject();
				callBackRequestBody.put("results", combinedAnalysisResult);
				callBackRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);	
				//invokeCallbackService(jsonConfigurations);
				invokeHTTPSService(callbackServiceURL, callBackRequestBody);
				
				//jsonConfigurations.put("results", combinedAnalysisResult);
				//jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").put("username", username);
				//jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").put("passPhrase", requestBody.get("passPhrase"));
				//System.out.println(jsonConfigurations);
				//invokeCallbackService(jsonConfigurations);
				JSONObject evaluationRequestBody = new JSONObject();
				evaluationRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
				evaluationRequestBody.put("results", combinedAnalysisResult);
				evaluationRequestBody.put("configurations", jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined"));
				invokeEvaluationService(evaluationRequestBody);
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
	
	private double getSingleResultFromProcedure(JSONObject procedure) {
		double result=0;
		for(String sorte : procedure.keySet()) {
			JSONObject resultSorte = new JSONObject();
			for(String forecastPeriod : procedure.getJSONObject(sorte).keySet()){
				result=procedure.getJSONObject("ARIMAResult").getJSONObject(sorte).getDouble(forecastPeriod);
			}
		}
		return result;
	}

	private JSONObject calculateCombinedResult(JSONObject results) {
		JSONObject combinedResult = new JSONObject();
		for(String procedureName : results.keySet()) {
			JSONObject procedure = results.getJSONObject(procedureName);
			for(String targetVariableName : procedure.keySet()) {
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				for(String forecastPeriod : targetVariable.keySet()){
					if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod, (combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod) + targetVariable.getDouble(forecastPeriod)));
					}else {
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod, targetVariable.getDouble(forecastPeriod));
					}
				}
			}
		}
		return combinedResult;
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
	
	private JSONObject invokeARIMAService(JSONObject aRIMAConfigurations, JSONObject preparedData) throws IOException {
		JSONObject requestBody = new JSONObject();
		
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/ARIMAService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		requestBody.put("configurations", aRIMAConfigurations);
		requestBody.put("dataset", preparedData);
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/Daten/Bantel/ruleBased/Adapter/Adapter.php");
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType, 12000);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
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
	
	private JSONObject invokeConfigFileService(String configFileLocation, String passPhrase) throws IOException {
		//Internal Implementation
		URL url = new URL(configFileLocation);	
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
	
	private void invokeEvaluationService(JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/EvaluationService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		restClient.postRequest(requestBody.toString());
	}
	
	private JSONObject invokeCallbackService(JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL("https://localhost:" + 9100 + "/Callback");
		requestBody.getJSONObject("forecasting").getJSONObject("Combined").put("username", "ForecastingTool");
		requestBody.getJSONObject("forecasting").getJSONObject("Combined").put("password", "forecasting");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	
	public JSONObject getPreparedData(JSONObject configurations) throws JSONException, IOException {
		URL url = new URL(configurations.getJSONObject("data").getString("provisioningServiceURL"));
		String contentType = "application/json";
		
		//Create request body and add login credentials
		JSONObject requestBody = new JSONObject(configurations.toString());
		requestBody.put("username", "ForecastingTool");
		requestBody.put("password", "forecasting");
		RestClient restClient = new RestClient();
		
		//configure endpoint and connection type
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	
	public JSONObject handleOutliers(JSONObject configurations, JSONObject dataWithOutliers) throws JSONException, IOException {
		String handlingProcedure = configurations.getJSONObject("parameters").getJSONObject("outliers").getString("procedure");
		
		//Internal Implementation
		URL url = new URL("http://localhost:" + 8110 + "/OutlierHandlingService/" + handlingProcedure + "Handler");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/ForecastingServices/RuleBasedService");
		String contentType = "application/json";
		
		//Create request body and add login credentials
		JSONObject requestBody = new JSONObject();
		requestBody.put("configurations", configurations);
		requestBody.put("dataset", dataWithOutliers);
		requestBody.put("username", "ForecastingTool");
		requestBody.put("password", "forecasting");
		RestClient restClient = new RestClient();
		
		//configure endpoint and connection type
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString())); 
	}
	
	private JSONObject invokeHTTPSService(String serviceURL, JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL(serviceURL);	
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpsConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	
	private JSONObject invokeHTTPService(String serviceURL, JSONObject requestBody) throws IOException {
		//Internal Implementation
		URL url = new URL(serviceURL);	
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
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
