package serviceImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import outputHandler.CustomFileWriter;
import webClient.RestClient;


public class ARIMAAnalysis {
	Rengine rEngine = null;
	
	
	public ARIMAAnalysis() {
		rEngine = Rengine.getMainEngine();
		if(rEngine == null) {
			rEngine = new Rengine(new String[] { "–no-save" }, false, null);
		}
	}
	private void setUpArimaConfiguration() {
		// Installs the defined list of packages if not already installed
		rEngine.eval("list.of.packages <- c('rJava','ggplot2','forecast','tseries','futile.logger','jdx')");
		rEngine.eval("new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,'Package'])]");
		rEngine.eval("if(length(new.packages)) install.packages(new.packages, repos='https://cran.wu.ac.at/')");
		rEngine.eval("library(ggplot2)");
		rEngine.eval("library(forecast)");
		rEngine.eval("library(tseries)");
		rEngine.eval("library(futile.logger)");
		//rEngine.eval("library(jdx)");
		rEngine.eval("attach( javaImport('java.util'), pos = 2 , name = 'java:java.util')");
		}
	
	private JSONObject dataProcessingTargetFormat(String selectedFrequency, int forecastPeriods, boolean seasonal, String filePath) {
		int end = 0;
		int exp = rEngine.eval("freq").asIntArray()[0];
		double outPutFrequency = exp;
		double intervall = 0;
		int intervallInteger = 0;
		JSONObject result = new JSONObject();
		switch (selectedFrequency.toLowerCase()) {
		case "daily":
			rEngine.eval("cnt_maW = ma(cnt_cln, order=" + outPutFrequency + ")");
			// consideration of na values and outliers + frequency calculated above
			rEngine.eval("ts_omitma = ts(na.omit(cnt_maW), frequency=" + outPutFrequency + ")");
			rEngine.eval("	adf.test(ts_omitma, alternative = 'stationary')");
			getForecast(forecastPeriods, filePath);
			end = (int) (forecastPeriods);
			/* verlagert hinter case abfrage
			for (int beg = 1; beg <= end; beg++) {
				//prints result to console
				//System.out.println(rEngine.eval("fcast[[4]]"));
				//System.out.println(rEngine.eval("fcast[[4]][" + beg + "]"));
				resultList.add(rEngine.eval("fcast[[4]][" + beg + "]").asDoubleArray()[0]);
			}*/
			//rEngine.eval("flog.trace('fitDay	')");
			rEngine.eval("fit <- auto.arima(ts_omitma, seasonal=" + Boolean.toString(seasonal).toUpperCase() + ")");
			break;
		case "weekly":
			// ma is smoothing MA, however, alters information? artificially increases x
			// values

			rEngine.eval("cnt_maW = cnt_cln"); // ma(cnt_cln, order=round(" + outPutFrequency + "))");
			// consideration of na values and outliers + frequency calculated above
			// omit on 2 weekly basis?
			// omit NA für keine Sales beeinflusst Daten -> enfternen?
			rEngine.eval("ts_omitma = ts(na.omit(cnt_maW), frequency=" + outPutFrequency + "*2)");

			//System.out.println("OMITMA: " + rEngine.eval("ts_omitma"));
			rEngine.eval("	adf.test(ts_omitma, alternative = 'stationary')");
			// rEngine.eval("Acf(ts_omitma, main='')");
			// rEngine.eval("Pacf(ts_omitma, main='')");
			end = (int) (forecastPeriods * outPutFrequency);
			
			/* verlagert hinter case abfrage
			 getForecast(seasonal, end, filePath);
			for (int beg = 1; beg <= end; beg++) {
				intervall = (intervall + outPutFrequency);
				intervallInteger = (int) intervall;
				//System.out.println(rEngine.eval("print(sum(fcast[[4]][" + beg + ": " + intervallInteger + "]))"));
				resultList
						.add(rEngine.eval("sum(fcast[[4]][" + beg + ": " + intervallInteger + "])").asDoubleArray()[0]);
				beg = (int) (intervall);
			}*/
			//rEngine.eval("flog.trace('OMIT')");
			//rEngine.eval("flog.trace(ts_omitma)");
			rEngine.eval("fit <- auto.arima(ts_omitma, seasonal=" + Boolean.toString(seasonal).toUpperCase() + ")");
			//rEngine.eval("flog.trace('fitWeek')");
			//rEngine.eval("flog.trace(fit)");
			//System.out.println("FIT: " + rEngine.eval("fit"));
			break;
		case "monthly":
			rEngine.eval("cnt_maW = ma(cnt_cln, order=round(" + outPutFrequency + "))");
			// consideration of na values and outliers + frequency calculated above
			rEngine.eval("ts_omitma = ts(na.omit(cnt_maW), frequency=" + outPutFrequency + ")");
			rEngine.eval("	adf.test(ts_omitma, alternative = 'stationary')");
			// rEngine.eval("Acf(ts_omitma, main='')");
			// rEngine.eval("Pacf(ts_omitma, main='')");
			end = (int)(forecastPeriods + (Math.round(outPutFrequency * 4.3)));
			/* verlagert hinter case abfrage
			getForecast(seasonal, end, filePath);
			
			for (int beg = 1; beg <= end; beg++) {
				intervall = (intervall + outPutFrequency * 4.3);
				intervallInteger = (int) intervall;
				//System.out.println(rEngine.eval("print(sum(fcast[[4]][" + beg + ": " + intervallInteger + "]))"));
				resultList
						.add(rEngine.eval("sum(fcast[[4]][" + beg + ": " + intervallInteger + "])").asDoubleArray()[0]);
				beg = (int) (intervall);
			}*/
			//rEngine.eval("flog.trace('fitMonth')");
			rEngine.eval("fit <- auto.arima(ts_omitma, seasonal=" + Boolean.toString(seasonal).toUpperCase() + ")");
			//System.out.println("FIT: " + rEngine.eval("fit"));
			break;
		default:
			System.out.println("Error MEssage");

		}
		//end = 5;
		getForecast(end, filePath);
		int counter = 1;
			for (int beg = 1; beg <= end; beg++) {
				outPutFrequency = 5;
				intervall = (intervall + outPutFrequency);
				intervallInteger = (int) intervall;
				//intervallInteger = 5;
				//System.out.println(rEngine.eval("print(sum(fcast[[4]][" + beg + ": " + intervallInteger + "]))"));
				double ergebnis = rEngine.eval("sum(fcast[[4]][" + beg + ": " + intervallInteger + "])").asDoubleArray()[0];
				result.put(Integer.toString(counter),ergebnis);
				beg = (int) (intervall);
				counter = counter + 1;
			}

		return result;
	}
	
	private void getForecast(int forecastPeriods, String filePath) {
		//der methode vorgelagert damit nicht immer bei abfrage neu trainiert werden muss und case reduziert werden kann
		//rEngine.eval("fit <- auto.arima(ts_omitma, seasonal=" + seasonal + ")");
		//rEngine.eval("flog.trace(fit)");
		rEngine.eval("fcast <- forecast(fit, h=" + forecastPeriods + ")");
		//rEngine.eval("flog.trace('DAVOR')");
		//System.out.println("FCAST: " + rEngine.eval("fcast"));
		//rEngine.eval("flog.trace(fcast)");
		//rEngine.eval("flog.trace('DANACH)");
		//rEngine.eval("jpeg('" + filePath + "/rplot_fcast.jpg')");
		//rEngine.eval("plot(fcast)");
		//rEngine.eval("dev.off()");
		
		
	}
	
	
	public JSONObject executeArimaAnalysis(JSONObject configurations, JSONObject preparedData) {
		JSONObject resultValues = new JSONObject();
		

		String targetPath = "D:/Arbeit/Bantel/Masterarbeit/Implementierung/ForecastingTool/Daten/Bantel";
		//String preprocessedData = configurations.get("data").get("preprocessed");
		//String filetype =configurations.getJSONObject("data").getString("filetype");
		String aggregation = configurations.getJSONObject("parameters").getString("aggregationOutputData");
		boolean seasonality = configurations.getJSONObject("parameters").getBoolean("seasonality");
		int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
		
		//Applicable for NON-URL File access!!
		// source of code https://www.java2novice.com/java-file-io-operations/file-list-by-file-filter/
		/*String sourcepath = configurations.getJSONObject("data").getString("sourcepreprocessed");
		File folder = new File(sourcepath + "/");
		File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
            	System.out.println(name);
                if(name.toLowerCase().endsWith(filetype)){
                    return true;
                } else {
                    return false;
                }
            }
        });*/
		
        
		setUpArimaConfiguration();
		
		
		
		//rEngine.eval("dateString <- " + dateString + "");
		//System.out.println(rEngine.eval("dateString"));
		//rEngine.eval("daily_data <- data.frame(Date=as.Date(dateString), stringsAsFactors=FALSE)");
		//System.out.println(rEngine.eval("daily_data"));
		
		
		for(String sorte : preparedData.keySet()) {
			try {
				if(sorte.equals("S6")) {
					System.out.println(sorte + ": " + preparedData.getString(sorte));
				}
				String input = "raw_data = read.csv(text='" + preparedData.getString(sorte) +"')";
				rEngine.eval(input);
				//rEngine.eval("write.csv(dataset, '" + targetPath + "/raw_data.csv')");
		//For non URL-File Location (File-List)
		//for(File file:files){
			//new File((targetPath + "/" + file.getName()).split("\\.")[0]).mkdirs();
			//String filepath = file.getAbsolutePath().replaceAll("\\\\", "/");
			//try {
			
		//FOR URL-File Location
		/* URL sourcepath = null;
			for(String sorte: configurations.get("Path").keySet()) {
			try {
				//System.out.println("Sorte: " + sorte);
				try {
					sourcepath = new URL(configurations.get("data").get("sourcepreprocessed") + "/" + sorte + ".csv");
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/				
		//Only for URL Files not local files
		//File file = File.createTempFile("Sorte_" + sorte, ".csv");
		//FileUtils.copyURLToFile(sourcepath, file);
		//String filepath = file.getAbsolutePath().replace("\\", "/");
		//rEngine.eval("raw_data = read.csv('" + filepath + "', header=TRUE, stringsAsFactors=FALSE)");
		
		
		//rEngine.eval("raw_data = read.csv('D:\\Arbeit\\Bantel\\Masterarbeit\\Daten\\Bantel\\ARIMA\\SorteAnalysisDaily\\all\\F01.csv', header=TRUE, stringsAsFactors=FALSE)");
		//System.out.println("Raw Data: " + rEngine.eval("raw_data"));
		//System.out.println("Raw Data: " + rEngine.eval("print(raw_data)"));
		//REXP result= rEngine.eval("raw_data");
		//System.out.println(result.asString());
		//rEngine.eval("daily_data$Date = as.Date(daily_data)");
		rEngine.eval("daily_data<- data.frame(Date=as.Date(raw_data$Datum), stringsAsFactors=FALSE)");
		rEngine.eval("daily_data$Menge <- raw_data$Menge");
		//System.out.println(rEngine.eval("print(daily_data)"));
		//System.out.println("Daily Data: " + rEngine.eval("daily_data"));
		//rEngine.eval("flog.threshold(TRACE)");
		//rEngine.eval("flog.appender(appender.file('D:/Arbeit/Bantel/Masterarbeit/Server/Logs/rlog.txt'))");
		//rEngine.eval("flog.trace(daily_data)");
		// rEngine.eval("jpeg('D:/Arbeit/Bantel/Masterarbeit/rplot.jpg')");
		//rEngine.eval("plt<-ggplot() + geom_line(data = daily_data, aes(x = Date, y = daily_data$Menge, colour = 'Red')) + ylab('DailyData')");
		
		//rEngine.eval("jpeg('" + targetPath + "/rplot_initialDataStructure.jpg')");
		//rEngine.eval(" pl <- (ggplot() + geom_line(data = daily_data, aes(x = Date, y = daily_data$Menge, colour = 'Counts')) + ylab('DailyData'))");
		//rEngine.eval("print(plt)");
		//rEngine.eval("ggsave('" + targetPath + "/rplot_initialDataStructure.jpg',plt)");
		
		//rEngine.eval("dev.off()");
		// order dates
		rEngine.eval("daily_data <- daily_data[order(daily_data$Date),]");
		// count days in data set
		rEngine.eval("daycount <- table(daily_data$Date)");
		rEngine.eval("lastindex<-sum(daycount)");

		// get first date
		rEngine.eval("firstday<-daily_data$Date[[1]]");
		rEngine.eval("lastday<-daily_data$Date[[lastindex]]");
		rEngine.eval("day_diff<-lastday-firstday+1");
		// create vector of all days
		rEngine.eval("completeDayList <- seq(daily_data$Date[1],length=day_diff,by='+1 day')");
		// create Data Frame including all days
		rEngine.eval("daily_data_complete <- data.frame(Date=as.Date(completeDayList), stringsAsFactors=FALSE)");
		rEngine.eval("daily_data_complete$Menge <- daily_data$Menge[match(daily_data_complete$Date, daily_data$Date)]");
		rEngine.eval("daily_data_complete");
		//System.out.println("Menge Complete: " + rEngine.eval("daily_data_complete$Menge"));
		// get weekday
		rEngine.eval("daily_data_complete$week_day <- weekdays(as.Date(daily_data_complete$Date))");
		// remove days that are not relevant/NA (e.g. remove Saturday and Sunday if not
		// relevant)
		rEngine.eval("sumPerDays<-aggregate(Menge~week_day, sum, data=daily_data_complete)");
		rEngine.eval("sumAllDays<-sum(sumPerDays$Menge)");
		rEngine.eval("index=1");
		rEngine.eval("sapply(sumPerDays$Menge, function(x){  \n" + "if((x/sumAllDays)<0.001){ \n"
				+ "print(sumPerDays$week_day[index])  \n"
				+ "daily_data_complete<<-daily_data_complete[!(daily_data_complete$week_day==sumPerDays$week_day[index]),]  \n"
				+ "} \n" + "index<<-index+1 \n" + "})");
		//System.out.println("WeekDay: " + rEngine.eval("daily_data_complete$week_day"));
		//rEngine.eval("flog.trace(\n\n\n daily_data_complete)");
		// define weekday frequency for analysis
		rEngine.eval("freq <- sum(lengths(table(daily_data_complete$week_day)))");
		rEngine.eval("count_ts = ts(daily_data_complete$Menge)");

		// if few outliers then weekly frequency on daily basis:
		// handles outliers and missing data
		rEngine.eval("daily_data_complete$clean_cnt = tsclean(count_ts)");
		//System.out.println("Daily Data Complete: " + rEngine.eval("daily_data_complete"));
		rEngine.eval("cnt_cln = daily_data_complete$clean_cnt");
		// daily_data_complete$clean_cnt2 = count_ts
		//System.out.println("Count: " + rEngine.eval("cnt_cln"));
		//rEngine.eval("flog.trace(cnt_cln)");
		// PREDICT mit omit und ma
		// ma is used to smooth moving averages (should that be done?)

		// TODO: Define ROUTINE to determin suitable sesonality (weekly, monthly,...
		// currently only weekly
		
		
		//resultValues.put(file.getName(), aggregation(aggregation, forecastPeriods, seasonality, targetPath));
		
		//Only for URL
		//resultValues.put(sorte, dataProcessingTargetFormat(aggregation, forecastPeriods, seasonality, targetPath));
		
		//Only for NON URL
		resultValues.put(sorte, dataProcessingTargetFormat(aggregation, forecastPeriods, seasonality, targetPath));
		
			}catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		}
		//}
		//rEngine.end();
		return resultValues;
		
		}
		
		//from https://mkyong.com/java/how-to-execute-shell-command-from-java/
		public JSONObject executeARIMAAnalysisCMD(JSONObject configurations, JSONObject preparedData) throws InterruptedException, IOException {
			JSONObject resultValues = new JSONObject();
			String aRIMAPath = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\ARIMA\\";
			String filePath = aRIMAPath+"temp\\inputValues.tmp";
			int forecastPeriods = configurations.getJSONObject("parameters").getInt("forecastPeriods");
			String inputAggr = configurations.getJSONObject("parameters").getString("aggregationInputData");
			String outputAggr = configurations.getJSONObject("parameters").getString("aggregationOutputData");
			for(String sorte : preparedData.keySet()) {
					if(sorte.equals("S1")) {
						System.out.println("Stop");
					}
					JSONObject executionResult = new JSONObject();
					//Input Daily OutputWeekly
					CustomFileWriter.createFile(filePath, preparedData.getString(sorte));
					String execString="";
					switch(inputAggr) {
					  case "daily":
						  switch(outputAggr) {
						  case "daily":
							execString   = "RScript " + aRIMAPath + "ARIMAAnalysis_Day_Day_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
						    break;
						  case "weekly":
							execString = "RScript " + aRIMAPath + "ARIMAAnalysis_Day_Week_Week.txt " + filePath + " " + sorte + " " + forecastPeriods;
						    break;
						  default:
							  throw new RuntimeException("Aggregation Invalid");
						}
					    break;
					  default:
						  throw new RuntimeException("Aggregation Invalid");
					}
					Process process = Runtime.getRuntime().exec(execString);
					StringBuilder output = new StringBuilder();
					StringBuilder error = new StringBuilder();
					BufferedReader outputStream = new BufferedReader( new InputStreamReader(process.getInputStream()));
					BufferedReader errorStream = new BufferedReader( new InputStreamReader(process.getErrorStream()));
					String resultString;
					int counter = 1;
					while ((resultString = outputStream.readLine()) != null) {
						output.append(resultString + "\n");
						//executionResult.put(Integer.toString(counter), resultString);
						//counter = counter + 1;
					}
					while ((resultString = errorStream.readLine()) != null) {
						error.append(resultString + "\n");
					}
					//System.out.println(output);
					//System.out.println(error);
					//resultValues.put(sorte, executionResult);
					resultValues.put(sorte, output.toString());
			}		
			return resultValues;
		}
		public JSONObject getPreparedData(JSONObject aRIMAconfigurations) throws JSONException, IOException {
			URL url = new URL(aRIMAconfigurations.getJSONObject("data").getString("provisioningServiceURL"));
			String contentType = "application/json";
			String requestBody = aRIMAconfigurations.toString();
			RestClient restClient = new RestClient();
			restClient.setHttpsConnection(url, contentType);
			return new JSONObject(restClient.postRequest(requestBody.toString()));
		}

}
