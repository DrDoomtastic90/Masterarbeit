package inputHandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;

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
}