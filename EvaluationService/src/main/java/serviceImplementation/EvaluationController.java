package serviceImplementation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnection.EvaluationDAO;
import dBConnection.EvaluationDBConnection;
import inputHandler.RestRequestHandler;
import webClient.RestClient;

@Path("/EvaluationService")
public class EvaluationController {
	
	/*
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void evaluateCombinedResults(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			EvaluationService.evaluationCombined(requestBody);
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write("");
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	/*
	//EVALUATION CUSTOM BANTEL SIDE
	@POST
	@Path("/BantelGmbH/ARIMA")
	@Produces(MediaType.APPLICATION_JSON)
	public void evaluateResultsCustom(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject forecastResults = new JSONObject();
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials");
			JSONArray executionRuns = requestBody.getJSONArray("executionRuns");
			String serviceURL = "https://localhost:9100/Daten/ConfigFile/Bantel_config";        		     
        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentials);  
        	
			loginCredentials = EvaluationService.invokeLoginService(loginCredentials);
			
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				//forecastResults.put("ARIMA", EvaluationService.getForecastResultsMulti(configurations, executionRuns, "ARIMA"));
				JSONObject preparedResults = EvaluationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
				EvaluationService.evaluationMAE(preparedResults);
			}
			
			
			//JSONObject preparedResults = EvaluationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
			
			
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write("");
			response.flushBuffer();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	@POST
	@Path("/Combined")
	@Produces(MediaType.APPLICATION_JSON)
	//Simulates evaluation of ForecastResults for BAntel GmbH
	public void evaluateResultsCombined(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject evaluationResult = new JSONObject();
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials");
			/*JSONArray executionRuns = requestBody.getJSONArray("executionRuns");
			String serviceURL = "https://localhost:9100/Daten/ConfigFile/Bantel_config";        		     
        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentials);  
        	*/
			JSONObject configurations = requestBody.getJSONObject("configurations");
			JSONObject evaluationResults = requestBody.getJSONObject("evaluationResults");
			
			
        	//overwrite forecasting specific configurations with shared combined parameters
        	//JSONObject combinedConfigurations = configurations.getJSONObject("forecasting").getJSONObject("Combined");
			
        	
			loginCredentials = EvaluationService.invokeLoginService(loginCredentials);
			
			if(configurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				//JSONObject aRIMAConfigurations = configurations.getJSONObject("forecasting").getJSONObject("ARIMA");
				
				
				/*configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				forecastResults.put("ARIMA", EvaluationService.getForecastResultsMulti(configurations, executionRuns, "ARIMA"));
				JSONObject preparedResults = EvaluationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
				*/
				
				//Evaluation MAE evaluation service from forecasting tool => Outsource to separate ForecastingTool service
				evaluationResult.put("ARIMA", EvaluationService.evaluationMAE(evaluationResults.getJSONObject("ARIMA")));
				
				//Store result
				/*EvaluationDBConnection.getInstance("EvaluationDB");
				EvaluationDAO evaluationDAO = new EvaluationDAO();
				evaluationDAO.writeEvaluationResultsToDB(evaluationResult.getJSONObject("ARIMA"), aRIMAConfigurations, "ARIMA", "MAE");
				*/
			}
			if(configurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				evaluationResult.put("ExponentialSmoothing", EvaluationService.evaluationMAE(evaluationResults.getJSONObject("ExponentialSmoothing")));
			}
			
			
			
			//JSONObject preparedResults = EvaluationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
			
			
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write(evaluationResult.toString());
			response.flushBuffer();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/Combined/Excel")
	@Produces(MediaType.APPLICATION_JSON)
	//Simulates evaluation of ForecastResults for BAntel GmbH
	public Response evaluateResultsCombinedExcel(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		File file = null;
		String fileContentString = null;
		String fileName = null;
		JSONObject evaluationResults = new JSONObject();
		try {
			
			
			JSONObject requestBody;

			requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);

			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials");
			JSONObject configurations = requestBody.getJSONObject("configurations");
			JSONObject forecastResults = requestBody.getJSONObject("forecastResults");

			loginCredentials = EvaluationService.invokeLoginService(loginCredentials);
			
			if(configurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject aRIMAEvaluation = new JSONObject();
				JSONObject aRIMAEvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("ARIMA"));
				
				file = EvaluationService.writeEvaluationResultsToExcelFile(aRIMAEvaluationMAE, "ARIMA");
				fileName = file.getName();
				byte[] bytes = Files.readAllBytes(file.toPath());   
				fileContentString = new String(Base64.getEncoder().encode(bytes));
				
				aRIMAEvaluation.put("MAE", aRIMAEvaluationMAE);
				aRIMAEvaluation.put("fileName",fileName);
				aRIMAEvaluation.put("fileContentString",fileContentString);
				evaluationResults.put("ARIMA", aRIMAEvaluation);
			}
			if(configurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject expSmoothingEvaluation = new JSONObject();
				JSONObject expSmoothingEvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("ExponentialSmoothing"));			
				
				file = EvaluationService.writeEvaluationResultsToExcelFile(expSmoothingEvaluationMAE, "ExponentialSmoothing");
				fileName = file.getName();
				byte[] bytes = Files.readAllBytes(file.toPath());   
				fileContentString = new String(Base64.getEncoder().encode(bytes));
				
				expSmoothingEvaluation.put("MAE", expSmoothingEvaluationMAE);
				expSmoothingEvaluation.put("fileName",fileName);
				expSmoothingEvaluation.put("fileContentString",fileContentString);
				evaluationResults.put("ExponentialSmoothing", expSmoothingEvaluation);
			}
			if(configurations.getJSONObject("forecasting").getJSONObject("Kalman").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject kalmanEvaluation = new JSONObject();
				JSONObject kalmanEvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("Kalman"));			
				
				file = EvaluationService.writeEvaluationResultsToExcelFile(kalmanEvaluationMAE, "Kalman");
				fileName = file.getName();
				byte[] bytes = Files.readAllBytes(file.toPath());   
				fileContentString = new String(Base64.getEncoder().encode(bytes));
				
				kalmanEvaluation.put("MAE", kalmanEvaluationMAE);
				kalmanEvaluation.put("fileName",fileName);
				kalmanEvaluation.put("fileContentString",fileContentString);
				evaluationResults.put("Kalman", kalmanEvaluation);
			}
			if(configurations.getJSONObject("forecasting").getJSONObject("ANN").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject aNNEvaluation = new JSONObject();
				JSONObject aNNEvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("ANN"));			
				
				file = EvaluationService.writeEvaluationResultsToExcelFile(aNNEvaluationMAE, "ANN");
				fileName = file.getName();
				byte[] bytes = Files.readAllBytes(file.toPath());   
				fileContentString = new String(Base64.getEncoder().encode(bytes));
				
				aNNEvaluation.put("MAE", aNNEvaluationMAE);
				aNNEvaluation.put("fileName",fileName);
				aNNEvaluation.put("fileContentString",fileContentString);
				evaluationResults.put("ANN", aNNEvaluation);
			}

			if(configurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject ruleBasedEvaluation = new JSONObject();
				JSONObject ruleBasedEvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("ruleBased"));			
				
				file = EvaluationService.writeEvaluationResultsToExcelFile(ruleBasedEvaluationMAE, "ruleBased");
				fileName = file.getName();
				byte[] bytes = Files.readAllBytes(file.toPath());   
				fileContentString = new String(Base64.getEncoder().encode(bytes));
				
				ruleBasedEvaluation.put("MAE", ruleBasedEvaluationMAE);
				ruleBasedEvaluation.put("fileName",fileName);
				ruleBasedEvaluation.put("fileContentString",fileContentString);
				evaluationResults.put("ruleBased", ruleBasedEvaluation);
			}
			JSONObject combinedEvaluation = new JSONObject();
			JSONObject combinedvaluationMAE = EvaluationService.evaluationMAE(forecastResults.getJSONObject("Combined"));			
			
			file = EvaluationService.writeEvaluationResultsToExcelFile(combinedvaluationMAE, "Combined");
			fileName = file.getName();
			byte[] bytes = Files.readAllBytes(file.toPath());   
			fileContentString = new String(Base64.getEncoder().encode(bytes));
			
			combinedEvaluation.put("MAE", combinedvaluationMAE);
			combinedEvaluation.put("fileName",fileName);
			combinedEvaluation.put("fileContentString",fileContentString);
			evaluationResults.put("Combined", combinedEvaluation);
		
			
			JSONObject comparedEvaluation = new JSONObject();
			JSONObject comparedEvaluationMAE = EvaluationService.comparedEvaluationMAE(evaluationResults);
			file = EvaluationService.writeComparedEvaluationMAEToExcelFile(comparedEvaluationMAE, "comparedMAE");
			fileName = file.getName();
			bytes = Files.readAllBytes(file.toPath());   
			fileContentString = new String(Base64.getEncoder().encode(bytes));
			comparedEvaluation.put("MAE", comparedEvaluationMAE);
			comparedEvaluation.put("fileName",fileName);
			comparedEvaluation.put("fileContentString",fileContentString);
			evaluationResults.put("compared", comparedEvaluation);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rBuild.type(MediaType.APPLICATION_JSON);
		ResponseBuilder rBuild = null;
		JSONObject responseMessage = new JSONObject();
		if(file!=null) {
			rBuild = Response.status(202);
			responseMessage.put("result", "Request Successfully Received. Result will be returned as soon as possible!");
			responseMessage.put("evaluationResults", evaluationResults);
			rBuild.entity(responseMessage.toString());
		}else {
			rBuild = Response.status(400);
			responseMessage.put("result", "Request could not be handled!");
			rBuild.entity(responseMessage.toString());
		}
		return rBuild.build();
		
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
	
	private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 8110 + "/LoginServices/CustomLoginService");
		//public_html implementation Forecasting
		//URL url = new URL("http://wwwlab.cs.univie.ac.at/~matthiasb90/Masterarbeit/ForecastingTool/Services/LoginServices/LoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
	

}
