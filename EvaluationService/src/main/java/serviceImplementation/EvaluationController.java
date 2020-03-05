package serviceImplementation;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;

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
import webClient.RestClient;

@Path("/EvaluationService")
public class EvaluationController {
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void evaluateResults(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		JSONObject responseContent = new JSONObject();
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			EvaluationService.evaluationCombined(requestBody);
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
}
