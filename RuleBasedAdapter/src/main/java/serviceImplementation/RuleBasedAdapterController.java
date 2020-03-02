package serviceImplementation;

import java.io.IOException;
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

@Path("/Daten/Forecasting")
public class RuleBasedAdapterController {
	
	@GET
	@Path("/RuleBasedData")
	@Produces(MediaType.APPLICATION_JSON)
	public void prepareDataRuleBased(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		JSONObject responseContent = new JSONObject();
		try {
			JSONObject configurations = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			responseContent = RuleBasedAnalysis.prepareDataProductionPlanning(configurations);
			System.out.println(responseContent.toString());
			response.setContentType("application/json");
			response.setStatus(202);
			response.getWriter().write(responseContent.toString());
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
