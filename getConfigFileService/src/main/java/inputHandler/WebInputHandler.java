package inputHandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class WebInputHandler {

	public static File getWebFile(String uRLString, String fileType) {
		UrlValidator urlValidator = new UrlValidator();
		//String configURL = (String) request.getAttribute("responseString");
		File webFile = null;
		//URL is valid get File else try to get local file
		if (urlValidator.isValid(uRLString)) {
			try {
				URL url = new URL(uRLString);
				webFile = File.createTempFile("webFile", fileType);
				// https://stackoverflow.com/questions/8324862/how-to-create-file-object-from-url-object
				FileUtils.copyURLToFile(url, webFile);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		return webFile;
	}
	
	
	public static File getLocalFile(String uRLString) {
		return new File(uRLString);
	}
	
	 public static JSONObject convertXMLToJSON(File configFile) {
	    	//Thesis adaption: create class from configFile instead of ruleBased response 
	    	JSONObject json = null;
	    	try {
	        	String content = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
	            json = XML.toJSONObject(content);
	            System.out.println(json.toString());
	        } catch (JSONException je) {
	            System.out.println(je.toString());
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return json;
	    }
}