package serviceImplementation;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;

@Path("/Daten")
public class ConfigFileController {
	
	@POST
	@Path("/ConfigFile")
	@Produces(MediaType.APPLICATION_JSON)
	public void getConfigFile(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
        	File configFile = getConfigurations("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\Bantel_config.xml");
			JSONObject jsonConfigurations = WebInputHandler.convertXMLToJSON(configFile).getJSONObject("configuration");
			response.setContentType("application/json");
			response.setStatus(202);
			response.getWriter().write(jsonConfigurations.toString());
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private File getConfigurations(String locationConfigFile) {
	    return WebInputHandler.getLocalFile(locationConfigFile);

	}
}