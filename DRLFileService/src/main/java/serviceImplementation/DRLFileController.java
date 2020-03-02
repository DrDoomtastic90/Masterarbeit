package serviceImplementation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import inputHandler.WebInputHandler;

@Path("/Daten")
public class DRLFileController {

	
	@GET
	@Path("/DRLFile")
	@Produces(MediaType.APPLICATION_JSON)
	public void getConfigFile(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
        	File drlFile = getFile("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\ForecastingRulesBantel.drl");
        	String content = FileUtils.readFileToString(drlFile, StandardCharsets.UTF_8);
            JSONObject drlJSON = new JSONObject();
            drlJSON.put("drlFile", content);
			response.setContentType("application/json");
			response.setStatus(202);
			response.getWriter().write(drlJSON.toString());
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private File getFile(String locationConfigFile) {
	    return WebInputHandler.getLocalFile(locationConfigFile);

	}
}
