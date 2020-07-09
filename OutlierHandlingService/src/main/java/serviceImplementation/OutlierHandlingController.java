package serviceImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import inputHandler.RestRequestHandler;
import serverImplementation.HttpServerOutlierHandling;

@Path("/OutlierHandlingService")
public class OutlierHandlingController {
	
	@POST
	@Path("/LimitHandler")
	@Produces(MediaType.APPLICATION_JSON)
	public void performLimitHandling(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject dataWithOutliers = requestBody.getJSONObject("dataset");
			JSONObject configurations = requestBody.getJSONObject("configurations");
			
			OutlierHandler outlierHandler = new OutlierHandler();
			JSONObject dataWithoutOutliers = outlierHandler.limitHandler(configurations, dataWithOutliers);
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write(dataWithoutOutliers.toString());
			response.flushBuffer();
			if(HttpServerOutlierHandling.isAutomaticShutdown()) {
				HttpServerOutlierHandling.attemptShutdown();
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/ARIMAHandler")
	@Produces(MediaType.APPLICATION_JSON)
	public void performARIMAAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject dataWithOutliers = requestBody.getJSONObject("dataset");
			JSONObject configurations = requestBody.getJSONObject("configurations");
			
			OutlierHandler outlierHandler = new OutlierHandler();
			JSONObject dataWithoutOutliers = outlierHandler.aRIMAHandler(configurations, dataWithOutliers);
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write(dataWithoutOutliers.toString());
			response.flushBuffer();
			if(HttpServerOutlierHandling.isAutomaticShutdown()) {
				HttpServerOutlierHandling.attemptShutdown();
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/ExpSmoothingHandler")
	@Produces(MediaType.APPLICATION_JSON)
	public void performExpSmoothingAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject dataWithOutliers = requestBody.getJSONObject("dataset");
			JSONObject configurations = requestBody.getJSONObject("configurations");
			
			OutlierHandler outlierHandler = new OutlierHandler();
			JSONObject dataWithoutOutliers = outlierHandler.exponentialSmoothingHandler(configurations, dataWithOutliers);
			response.setStatus(202);
			response.setContentType("application/json");
			response.getWriter().write(dataWithoutOutliers.toString());
			response.flushBuffer();
			if(HttpServerOutlierHandling.isAutomaticShutdown()) {
				HttpServerOutlierHandling.attemptShutdown();
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
    }
}
