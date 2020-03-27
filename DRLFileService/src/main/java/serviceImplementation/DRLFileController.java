package serviceImplementation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import inputHandler.RestRequestHandler;
import inputHandler.WebInputHandler;
import webClient.RestClient;

@Path("/Daten")
public class DRLFileController {

	
	@POST
	@Path("/DRLFile")
	@Produces(MediaType.APPLICATION_JSON)
	public void getConfigFile(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject configurations = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = invokeLoginService(configurations);
			if(loginCredentials.getBoolean("isAuthorized")) {
	        	File drlFile = getFile("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\ForecastingRulesBantel.drl");
	        	String content = FileUtils.readFileToString(drlFile, StandardCharsets.UTF_8);
	            JSONObject drlJSON = new JSONObject();
	            drlJSON.put("drlFile", content);
				response.setContentType("application/json");
				response.setStatus(202);
				response.getWriter().write(drlJSON.toString());
				response.flushBuffer();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private File getFile(String locationConfigFile) {
	    return WebInputHandler.getLocalFile(locationConfigFile);

	}
	
	private JSONObject invokeLoginService(JSONObject requestBody) throws IOException {
		URL url = new URL("http://localhost:" + 9110 + "/LoginServices/CustomLoginService");
		String contentType = "application/json";
		RestClient restClient = new RestClient();
		restClient.setHttpConnection(url, contentType);
		return new JSONObject(restClient.postRequest(requestBody.toString()));
	}
}
