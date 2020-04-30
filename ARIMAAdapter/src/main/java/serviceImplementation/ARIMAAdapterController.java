package serviceImplementation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import errorHandler.UniqueConstraintException;
import inputHandler.RestRequestHandler;
import webClient.RestClient;

@Path("/Daten/Forecasting/ARIMA")
public class ARIMAAdapterController {
	
	@POST
	@Path("/DAILY")
	@Produces(MediaType.APPLICATION_JSON)
	public void prepareDataARIMA(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		JSONObject responseContent = new JSONObject();
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject configurations = requestBody.getJSONObject("configurations");
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials"); 
			
			//Verify user access permission
			loginCredentials = invokeLoginService(loginCredentials);
			if(loginCredentials.getBoolean("isAuthorized")) {
				//provide passphrase for encrypted DB
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				
				//prepare data for forecasting
				responseContent = ARIMAPreparation.getSorteDataDaily(configurations);
				System.out.println(responseContent.toString());
				
				//return prepared data to caller
				response.setContentType("application/json");
				response.setStatus(202);
				response.getWriter().write(responseContent.toString());
				response.flushBuffer();
			}
			System.out.println("STOP");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UniqueConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/DailySales")
	@Produces(MediaType.APPLICATION_JSON)
	public void getSalesDataDaily(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		JSONObject responseContent = new JSONObject();
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject configurations = requestBody.getJSONObject("configurations");
			JSONObject loginCredentials = requestBody.getJSONObject("loginCredentials"); 
			
			//Verify user access permission
			loginCredentials = invokeLoginService(loginCredentials);
			if(loginCredentials.getBoolean("isAuthorized")) {
				//provide passphrase for encrypted DB
				configurations.put("passPhrase", loginCredentials.getString("passPhrase"));
				
				//prepare data for forecasting
				responseContent = ARIMAPreparation.getSalesDataWeekly(configurations);
				System.out.println(responseContent.toString());
				
				//return prepared data to caller
				response.setContentType("application/json");
				response.setStatus(202);
				response.getWriter().write(responseContent.toString());
				response.flushBuffer();
			}
			System.out.println("STOP");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}


}
