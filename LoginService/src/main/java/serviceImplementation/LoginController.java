package serviceImplementation;
import java.io.IOException;
import java.sql.SQLException;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import dBConnections.AuthenticationDAO;
import dBConnections.AuthenticationDBConnection;
import errorHandler.AuthenticationRequestException;
import inputHandler.RestRequestHandler;

@Path("/LoginServices")
public class LoginController {
	
	@POST
	@Path("/CustomLoginService")
	@Produces(MediaType.APPLICATION_JSON)
	public void performLogin(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			JSONObject requestBody = RestRequestHandler.readJSONEncodedHTTPRequestParameters(request);
			JSONObject loginCredentials = authenticationProcedure(requestBody);
			response.setContentType("application/json");
			response.setStatus(202);
			response.getWriter().write(loginCredentials.toString());
			response.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static JSONObject  authenticationProcedure(JSONObject loginCredentials) {
		try {
			String username = loginCredentials.getString("username");
			String password = loginCredentials.getString("password");
			String passPhrase = loginCredentials.getString("passPhrase");
			AuthenticationDBConnection.getInstance(passPhrase);
			AuthenticationDAO authDao = new AuthenticationDAO();
			return authDao.authenticate(username, password);
		} catch (ClassNotFoundException | SQLException | ClassCastException e) {
			e.printStackTrace();
			throw new AuthenticationRequestException(e.getMessage());
		} 
	}
}



