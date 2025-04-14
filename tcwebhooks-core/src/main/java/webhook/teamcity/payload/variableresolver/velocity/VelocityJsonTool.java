package webhook.teamcity.payload.variableresolver.velocity;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.intellij.openapi.diagnostic.Logger;

public class VelocityJsonTool {
	private static final Logger LOG = Logger.getInstance(VelocityJsonTool.class.getName());

	private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

	public Map<String,String> jsonToStringMap(String jsonString) {
		try {
			return gson.fromJson(jsonString, new TypeToken<Map<String,String>>(){}.getType());
		} catch (JsonParseException ex) {
			LOG.warn("WebHooks VelocityJsonTool :: Unable to parse string into JSON. Returning empty map from String: '" +
									jsonString + "'");
			return new HashMap<>();
		}
	}
	
	public Map<String,String> jsonToMap(String jsonString) {
		return jsonToStringMap(jsonString);
	}

}
