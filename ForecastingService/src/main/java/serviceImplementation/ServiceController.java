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
	        	
				//login credentials to access customer system and dB passphrase is provided
				JSONObject loginCredentialsCustomerSystem = new JSONObject();
				String passPhrase = requestBody.getString("passPhrase");
				loginCredentialsCustomerSystem.put("username", "ForecastingTool");
				loginCredentialsCustomerSystem.put("password", "forecasting");
				loginCredentialsCustomerSystem.put("passPhrase", passPhrase);
	        	
				//Get Configuration file and set initial execution parameters
				String serviceURL = loginCredentials.getString("apiURL");
	        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentialsCustomerSystem); 
	        	
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
	        		JSONObject forecastResult = executeRuleBasedForeasting(ruleBasedConfigurations, loginCredentialsCustomerSystem, username);
					
					//prepare Callback Request
					JSONObject callBackRequestBody = new JSONObject();
					callBackRequestBody.put("ruleBasedResult", forecastResult);
					callBackRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);	
					String callbackServiceURL = ruleBasedConfigurations.getJSONObject("data").getString("callbackServiceURL");
					
					//return result
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
	        	
				//login credentials to access customer system and dB passphrase is provided
				JSONObject loginCredentialsCustomerSystem = new JSONObject();
	        	String passPhrase = requestBody.getString("passPhrase");
				loginCredentialsCustomerSystem.put("username", "ForecastingTool");
				loginCredentialsCustomerSystem.put("password", "forecasting");
				loginCredentialsCustomerSystem.put("passPhrase", passPhrase);
				
				
				//Get Configuration file and set initial execution parameters
				String serviceURL = loginCredentials.getString("apiURL");
	        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentialsCustomerSystem);       	
	        	String to = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getJSONObject("data").getString("to");
	        	int forecastPeriods = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getInt("forecastPeriods");
	        	String callbackServiceURL = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined").getString("callbackServiceURL");
	        	String username = jsonConfigurations.getJSONObject("user").getString("name");
	        	asyncResponse.resume("Request Successfully Received. Result will be returned as soon as possible!");
	        	
	        	//Execute Forecasting Procedures
	        	if(loginCredentials.getBoolean("isEnabledRuleBased") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {	
	        		//get relevant rulebased Configurations
	        		JSONObject ruleBasedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");
	        		
	        		//overwrite forecasting specific configurations with shared combined parameters
	        		ruleBasedConfigurations.getJSONObject("data").put("to", to);
	        		ruleBasedConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
	        		JSONObject forecastResult = executeRuleBasedForeasting(ruleBasedConfigurations, loginCredentialsCustomerSystem, username);
	        		combinedAnalysisResult.put("ruleBasedResult", forecastResult);
				} 	
				if(loginCredentials.getBoolean("isEnabledARIMA") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {	
					//get relevant ARIMA Configurations
					JSONObject aRIMAConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");
					
					//overwrite forecasting specific configurations with shared combined parameters
					aRIMAConfigurations.getJSONObject("data").put("to", to);
					aRIMAConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);								
					JSONObject forecastResult = executeARIMAForecasting(aRIMAConfigurations, loginCredentialsCustomerSystem, username);	        		
					combinedAnalysisResult.put("ARIMAResult", forecastResult);
				}			
				if(loginCredentials.getBoolean("isEnabledKalman") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					//get relevant Kalman Configurations
					JSONObject kalmanConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman");				
					
					//overwrite forecasting specific configurations with shared combined parameters
					kalmanConfigurations.getJSONObject("data").put("to", to);
					kalmanConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);		
					JSONObject forecatsResult = executeKalmanForecasting(kalmanConfigurations, loginCredentialsCustomerSystem, username);
					combinedAnalysisResult.put("kalmanResult", forecatsResult);
				}	
				if(loginCredentials.getBoolean("isEnabledExpSmoothing") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					//get relevant expSmoothing Configurations
					JSONObject expSmoothingConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");				
					
					//overwrite forecasting specific configurations with shared combined parameters
					expSmoothingConfigurations.getJSONObject("data").put("to", to);
					expSmoothingConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);	
					JSONObject forecatsResult = executeExpSmoothingForecasting(expSmoothingConfigurations, loginCredentialsCustomerSystem, username);
					combinedAnalysisResult.put("ExpSmoothingResult", forecatsResult);
				}		
				if(loginCredentials.getBoolean("isEnabledANN") && jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
					//get relevant ANN Configurations
					JSONObject aNNConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN");				
					
					//overwrite forecasting specific configurations with shared combined parameters
					aNNConfigurations.getJSONObject("data").put("to", to);
					aNNConfigurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);	
					JSONObject forecatsResult = executeANNForecasting(aNNConfigurations, loginCredentialsCustomerSystem, username);
					combinedAnalysisResult.put("ANNResult", forecatsResult);
				}
				
				//calculate Combined Result
				combinedAnalysisResult.put("CombinedResult", calculateCombinedResult(combinedAnalysisResult));
				
				//prepare Callback Reqwuest
				JSONObject callBackRequestBody = new JSONObject();
				callBackRequestBody.put("results", combinedAnalysisResult);
				callBackRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);		
				
				//return result
				invokeHTTPSService(callbackServiceURL, callBackRequestBody);
				
				//perform Evaluation
				JSONObject evaluationRequestBody = new JSONObject();
				evaluationRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
				evaluationRequestBody.put("results", combinedAnalysisResult);
				evaluationRequestBody.put("configurations", jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined"));
				invokeEvaluationService(evaluationRequestBody);
			}
		} catch (IOException e) {
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
	
	private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 8110 + "/LoginServices/CustomLoginService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
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
	
	private JSONObject executeRuleBasedForeasting(JSONObject ruleBasedConfigurations, JSONObject loginCredentialsCustomerSystem, String username) throws IOException {
		
		//initialize request body for service call
		JSONObject ruleBasedRequestBody = new JSONObject();
		ruleBasedRequestBody.put("configurations", ruleBasedConfigurations);				
		
		//set login credentials to access customer system and dB passphrase is provided
		ruleBasedRequestBody.put("loginCredentials", loginCredentialsCustomerSystem);
		String serviceURL = ruleBasedConfigurations.getJSONObject("data").getString("provisioningServiceURL");
		
		//get prepared data
		JSONObject preparedData = invokeHTTPSService(serviceURL, ruleBasedRequestBody);
		ruleBasedRequestBody.put("dataset", preparedData);
		String drlFileService =ruleBasedConfigurations.getJSONObject("data").getString("drlFilePath");
		JSONObject drlJSON = invokeHTTPSService(drlFileService, loginCredentialsCustomerSystem);
		ruleBasedRequestBody.put("drlFile", drlJSON);
				
		//Outlier Handling - not applicable for rulebased forecasting
		//prepare request body for forecasting service call
		
		//customer username used to link result to corresponding service caller
		ruleBasedConfigurations.put("username", username);
		serviceURL = "http://localhost:" + 8110 + "/RuleBasedService";
		JSONObject analysisResult = invokeHTTPService(serviceURL, ruleBasedRequestBody);
		return analysisResult;
	}
	
	private JSONObject executeARIMAForecasting(JSONObject aRIMAConfigurations, JSONObject loginCredentialsCustomerSystem, String username) throws IOException {

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
		JSONObject analysisResult = invokeHTTPService(serviceURL, aRIMARequestBody);
		return analysisResult;
		
	}
	
	private JSONObject executeKalmanForecasting(JSONObject kalmanConfigurations, JSONObject loginCredentialsCustomerSystem, String username) throws IOException {
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
		JSONObject analysisResult = invokeHTTPService(serviceURL, kalmanRequestBody);
		return analysisResult;
		
	}
	
	private JSONObject executeExpSmoothingForecasting(JSONObject expSmoothingConfigurations, JSONObject loginCredentialsCustomerSystem, String username) throws IOException {
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
		JSONObject analysisResult = invokeHTTPService(serviceURL, expSmoothingRequestBody);		
		return analysisResult;
		
	}
	
	private JSONObject executeANNForecasting(JSONObject aNNConfigurations, JSONObject loginCredentialsCustomerSystem, String username) throws IOException {
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
		//Not needed in case of neural networks
		
		//prepare request body for forecasting service call
		//customer username used to link result to corresponding service caller
		aNNConfigurations.put("username", username);
		serviceURL = "http://localhost:" + 8110 + "/ANNService";		
		JSONObject analysisResult = invokeHTTPService(serviceURL, aNNRequestBody);	
		return analysisResult;
		
	}

}
