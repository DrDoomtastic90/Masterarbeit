package inputHandler;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class RestRequestHandler {
	//Question: Does only work if not redirected via Request Dispatcher. Sending data from form or client will need to use this function
	//to get inputstream data (attributes). However, using redirect the inputstream is empty but the attributes are available via getAttribute
	public static JSONObject readJSONEncodedHTTPRequestParameters(HttpServletRequest request) throws IOException{
		ServletInputStream inputStream = request.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "utf-8");
		String responseString = writer.toString();
		JSONObject requestBody = new JSONObject(responseString);
		return requestBody;
	}
	
	public static HttpServletRequest readURLEncodedHTTPRequestParameters(HttpServletRequest request) throws IOException{
		ServletInputStream inputStream = request.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "utf-8");
		String theString = writer.toString();
		if(theString.length()>0) {
			String[] attributeStrings = theString.split("&");
			for (String attributeString : attributeStrings) {
				request.setAttribute(attributeString.split("=")[0], attributeString.split("=")[1]);
			}
		}
		return request;
	}
	public static HttpSession setSessionParameters(HttpServletRequest request) throws IOException{
		 HttpSession session = request.getSession();
		ServletInputStream inputStream = request.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "utf-8");
		String theString = writer.toString();
		String[] attributeStrings = theString.split("&");
		for (String attributeString : attributeStrings) {
			session.setAttribute(attributeString.split("=")[0], attributeString.split("=")[1]);
		}
		return session;
	}
}
