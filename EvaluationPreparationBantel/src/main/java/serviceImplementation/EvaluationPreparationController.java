package serviceImplementation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dBConnection.EvaluationDAO;
import dBConnection.EvaluationDBConnection;
import inputHandler.RestRequestHandler;
import webClient.RestClient;

@Path("/EvaluationPreparationService/BantelGmbH")
public class EvaluationPreparationController {
	
/*
	//EVALUATION CUSTOM BANTEL SIDE
	@POST
	@Path("/ARIMA")
	@Produces(MediaType.APPLICATION_JSON)
	public void evaluateResultsCustom(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject forecastResults = new JSONObject();
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials");
			JSONArray executionRuns = requestBody.getJSONArray("executionRuns");
			String serviceURL = "https://localhost:9100/Daten/ConfigFile/Bantel_config";        		     
        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentials);  
        	
			loginCredentials = EvaluationPreparationService.invokeLoginService(loginCredentials);
			
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				forecastResults.put("ARIMA", EvaluationPreparationService.getForecastResultsMulti(configurations, executionRuns, "ARIMA"));
				JSONObject preparedResults = EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
				//EvaluationPreparationService.evaluationMAE(preparedResults);
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
	public void evaluateResultsBantelCombined(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject preparedData = new JSONObject();
			JSONObject forecastResults = new JSONObject();
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials");
			JSONObject consideratedConfigurations = requestBody.getJSONObject("consideratedConfigurations");
			
			JSONArray executionRuns = requestBody.getJSONArray("executionRuns");
			String serviceURL = "https://localhost:9100/Daten/ConfigFile/Bantel_config";        		     
        	JSONObject jsonConfigurations =  invokeHTTPSService(serviceURL, loginCredentials);  
        	
        	//overwrite forecasting specific configurations with shared combined parameters
        	JSONObject combinedConfigurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Combined");
        	int forecastPeriods =combinedConfigurations.getInt("forecastPeriods");
			
        	
			loginCredentials = EvaluationPreparationService.invokeLoginService(loginCredentials);
			
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ARIMA");	
				configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				//forecastResults.put("ARIMA", EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ARIMA"));
				forecastResults = EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ARIMA");
				preparedData.put("ARIMA", EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials));
			}
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ExponentialSmoothing");		
				configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				//forecastResults.put("ExpSmoothing", EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ExpSmoothing"));
				forecastResults = EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ExpSmoothing");
				preparedData.put("ExpSmoothing", EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials));
			}
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("Kalman");		
				configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				forecastResults = EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "Kalman");
				preparedData.put("Kalman", EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials));
			}
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ANN");		
				configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				forecastResults = EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ANN");
				preparedData.put("ANN", EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials));
			}
			if(jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased").getJSONObject("parameters").getJSONObject("execution").getBoolean("execute")) {
				JSONObject configurations = jsonConfigurations.getJSONObject("forecasting").getJSONObject("ruleBased");		
				configurations.getJSONObject("parameters").put("forecastPeriods", forecastPeriods);
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				forecastResults = EvaluationPreparationService.getForecastResultsMulti(configurations, consideratedConfigurations, executionRuns, "ruleBased");
				preparedData.put("ruleBased", EvaluationPreparationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials));
			}
			
			//Update LoginCredentials to Call ForecastingTool Service
			loginCredentials.put("username", "BantelGmbH");
			loginCredentials.put("password", "bantel");		
			
			JSONObject evaluationRequestBody = new JSONObject();
			serviceURL = "https://localhost:" + 443 + "/ForecastingTool/EvaluationService/Combined/Excel";
			evaluationRequestBody.put("forecastResults", preparedData);
			evaluationRequestBody.put("loginCredentials", loginCredentials);		
			evaluationRequestBody.put("configurations", jsonConfigurations);	
			
			//return result
			JSONObject evaluationResponse = invokeHTTPSService(serviceURL, evaluationRequestBody);
			JSONObject evaluationResults = evaluationResponse.getJSONObject("evaluationResults");
			//Store results
			EvaluationDBConnection.getInstance("EvaluationDB");
			EvaluationDAO evaluationDAO = new EvaluationDAO();
			
			System.out.println(evaluationResponse.get("result").toString());
			
			for(String procedureName : evaluationResults.keySet()) {
				JSONObject procedureResult = evaluationResults.getJSONObject(procedureName);
				JSONObject mAE = procedureResult.getJSONObject("MAE");
				evaluationDAO.writeEvaluationResultsToDB(mAE, jsonConfigurations.getJSONObject("forecasting").getJSONObject(procedureName), procedureName, "MAE");
				
				String fileName = procedureResult.getString("fileName");
				String fileContentString = procedureResult.getString("fileContentString");
				convertByteToFile(fileContentString, fileName);
				//JSONObject preparedResults = EvaluationService.prepareEvaluationBantel(forecastResults, configurations, loginCredentials);
				
			}
			
			response.setStatus(202);
			response.setContentType("application/json");
			JSONObject responseContent = new JSONObject();
			responseContent.put("Result", "DONE");
			response.getWriter().write(responseContent.toString());
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
	
	//from https://stackoverflow.com/questions/18599985/send-file-inside-jsonobject-to-rest-webservice
	 //Convert a Base64 string and create a file
	private static final void convertByteToFile(String fileString, String filename) throws IOException{
		byte[] bytes = Base64.getDecoder().decode(fileString);
		String targetPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\Evaluation\\temp\\";
		File file = new File(targetPath+"BANTEL_"+filename);
		FileOutputStream fop = new FileOutputStream(file);
		fop.write(bytes);
		fop.flush();
		fop.close();
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
}
