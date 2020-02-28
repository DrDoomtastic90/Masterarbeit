package serviceImplementation;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import outputHandler.CustomFileWriter;


@Path("/ForecastingServices")
public class ServiceController {

	
	@GET
	//@Path("{parameter: |CombinedServices}")
	@Produces(MediaType.APPLICATION_JSON)
	public void performCombinedAnalysis(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			String jsonString = "{\"Result\":\"Successful\"}";
			CustomFileWriter.createJSON("/home/matthiasb90/Masterarbeit/ForecastingTool/Microservices/result.json", jsonString);
			System.out.println(jsonString);
			response.setContentType("application/json");
			response.getWriter().write("{\"Result\":\"Successful\"}");
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
