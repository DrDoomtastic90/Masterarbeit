package serviceImplementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import webClient.RestClient;

public class AnalysisService {
	KieSession kieSession = null;
	URLClassLoader classLoader = null;
	String dynamicDirectoryLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\ForecastingTool\\Services\\ForecastingServices\\RuleBasedService\\DynamicClasses";

	private KieSession instantiateKnowledgebase(
			File drlFile /* , JSONObject json *//* , List<Object> dataObjects, ClassLoader cloader */) {
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kfs = kieServices.newKieFileSystem();
		Resource resource = kieServices.getResources().newFileSystemResource(drlFile).setResourceType(ResourceType.DRL);
		kfs.write(resource);
		// never used but might get important some day
		// KnowledgeBuilderConfiguration kBuilderConfiguration =
		// KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(null, cl);
		KieBuilder kiebuilder = kieServices.newKieBuilder(kfs, classLoader);

		kiebuilder.buildAll();
		if (kiebuilder.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + kiebuilder.getResults().toString());
		}
		KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
		KieSession kieSession = kieContainer.newKieSession();
		return kieSession;
	}

	
	private void instantiateDynamicClasses(JSONObject worldFacts) {
		File dynamicClassDirectory = new File(dynamicDirectoryLocation);
		try {
			classLoader = DynamicClassFileCreator.getDynamicClassLoader(dynamicClassDirectory);
			for (String className : worldFacts.keySet()) {
				JSONObject fields = worldFacts.getJSONObject(className).getJSONObject("fields");
				try {
					DynamicClassFileCreator.createDynamicJavaClass(className, dynamicDirectoryLocation, fields);
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}


	public String getPreparedData(JSONObject ruleBasedConfigurations) throws JSONException, IOException {
		URL url = new URL(ruleBasedConfigurations.getJSONObject("data").getString("provisioningServiceURL"));
		String contentType = "application/json";
		String requestBody = ruleBasedConfigurations.toString();
		RestClient restClient = new RestClient();
		restClient.setHttpsConnection(url, contentType);
		return restClient.postRequest(requestBody);
	}
	
	
	public void prepareForecasting(File drlFile, JSONObject worldFacts) {
		instantiateDynamicClasses(worldFacts);
		this.kieSession = instantiateKnowledgebase(drlFile);
	}


	public JSONObject analyseWorld(JSONObject worldFacts, int forecastPeriods) {
		RuleBasedResult.setForecastPeriods(forecastPeriods);
		ObjectMapper objectMapper = new ObjectMapper();
		JSONObject objectMap = new JSONObject();
		for (String factors : worldFacts.keySet()) {
			for (String objectType : worldFacts.getJSONObject(factors).keySet()) {
				JSONObject dataObjects = worldFacts.getJSONObject(factors).getJSONObject(objectType);
				for (String dataObjectName : dataObjects.keySet()) {
					JSONObject dataObject = dataObjects.getJSONObject(dataObjectName);
					dataObject.put("name", dataObjectName);
					try {
						Class<?> dynamicClass = DynamicClassFileCreator.loadDynamicJavaClass(classLoader, objectType);
						objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
						Object dynamicObject = objectMapper.readValue(dataObject.toString(), dynamicClass);
						objectMap.put(dataObjectName, dynamicObject);
						// System.out.println(dynamicObject.getClass().toString());
						kieSession.insert(dynamicObject);

					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		kieSession.fireAllRules();
		return RuleBasedResult.getResult();
	}
}
