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
import serverImplementation.HttpServerCallback;
import webClient.RestClient;


@Path("/Callback")
public class CallbackServiceController {

		
		@POST
		@Produces(MediaType.APPLICATION_JSON)
		public void performRuleBasedAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {

			try {
				JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials"); 
				loginCredentials = invokeLoginService(loginCredentials);
				//JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
				//JSONObject loginCredentials = invokeLoginService(requestBody.getJSONObject("forecasting").getJSONObject("Combined"));
				if(loginCredentials.getBoolean("isAuthorized")) {
				
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
