package webClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestClient {
	private HttpURLConnection connection;
	
	
	public RestClient(URL url, String contentType) throws IOException {
		setConnection(url, contentType);
	}
	
	public void setConnection(URL url, String contentType) throws IOException {
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", contentType);
	}
	
	public String postRequest(String requestBody) throws IOException {
		return createRequest(requestBody, "POST");
	}
	
	public String getRequest(String requestBody) throws IOException {
		return createRequest(requestBody, "GET");
	}
	
	public String createRequest(String requestBody, String requestType) throws IOException {
		if(connection == null) {
			connection.connect();
		}
		connection.setRequestMethod(requestType);	
		OutputStream os = connection.getOutputStream();
		os.write(requestBody.getBytes());
		os.flush();
		if (connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
			throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
		StringBuilder sb = new StringBuilder();
	    String outputLine;
	    while ((outputLine = br.readLine()) != null) {
	        sb.append(outputLine);
	    }
	    String responseString = sb.toString();
	    connection.disconnect();
	    return responseString;
	}
}
