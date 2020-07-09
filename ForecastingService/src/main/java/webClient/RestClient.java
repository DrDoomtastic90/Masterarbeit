package webClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;




public class RestClient {
	private HttpURLConnection httpConnection;
	private HttpsURLConnection httpsConnection;
	
	
	public RestClient(){}
	
	public void setHttpConnection(URL url, String contentType, int milliseconds) throws IOException {
		setHttpConnection(url, contentType);
		httpConnection.setConnectTimeout(milliseconds);
	}
	
	public void setHttpConnection(URL url, String contentType) throws IOException {
		httpConnection = (HttpURLConnection)  url.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestProperty("Content-Type", contentType);
	}
	
	public void setHttpsConnection(URL url, String contentType, int milliseconds) throws IOException {
		setHttpsConnection(url, contentType);
		httpsConnection.setConnectTimeout(milliseconds);
	}
	
	public void setHttpsConnection(URL url, String contentType) throws IOException {
		HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
		setHttpConnection(url, contentType);
		httpsConnection = (HttpsURLConnection) httpConnection;
	}
	
	public String postRequest(String requestBody) throws IOException {
		return createRequest(requestBody, "POST");
	}
	
	public String getRequest(String requestBody) throws IOException {
		return createRequest(requestBody, "GET");
	}
	
	public String createRequest(String requestBody, String requestType) throws IOException {
		StringBuilder sb = new StringBuilder();
		if(httpsConnection != null) {
			httpsConnection.setRequestMethod(requestType);
			OutputStream os = httpsConnection.getOutputStream();
			os.write(requestBody.getBytes());
			os.flush();
			if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
				throw new RuntimeException("Failed : HTTPS error code : " + httpsConnection.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((httpsConnection.getInputStream())));
		    String outputLine;
		    while ((outputLine = br.readLine()) != null) {
		        sb.append(outputLine);
		    }
		    httpsConnection.disconnect();
		} else {
			httpConnection.setRequestMethod(requestType);
			OutputStream os = httpConnection.getOutputStream();
			os.write(requestBody.getBytes());
			os.flush();
			if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
				throw new RuntimeException("Failed : HTTP error code : " + httpConnection.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
		    String outputLine;
		    while ((outputLine = br.readLine()) != null) {
		        sb.append(outputLine);
		    }
		    httpConnection.disconnect();
		}
	    String responseString = sb.toString();
	    return responseString;
	}
}
