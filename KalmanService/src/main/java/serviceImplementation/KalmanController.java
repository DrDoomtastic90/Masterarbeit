package serviceImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
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
import outputHandler.CustomFileWriter;
import serverImplementation.HttpServerKalman;

@Path("/KalmanService")
public class KalmanController {
	
	@POST
	@Path("/ARIMA")
	@Produces(MediaType.APPLICATION_JSON)
	public void performKalmanAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject kalmanConfigurations = requestBody.getJSONObject("configurations");
			JSONObject preparedData = requestBody.getJSONObject("dataset");
					
			//JSONObject kalmanConfigurations = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			KalmanAnalysis kalmanAnalysis = new KalmanAnalysis();
			//JSONObject preparedData = kalmanAnalysis.getPreparedData(kalmanConfigurations);
			JSONObject analysisResult = kalmanAnalysis.executeAnalysisCMDNeu(kalmanConfigurations, preparedData);
			
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write(analysisResult.toString());
			response.flushBuffer();
			if(HttpServerKalman.isAutomaticShutdown()) {
				HttpServerKalman.attemptShutdown();
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
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
}
