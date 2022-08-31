package webhook.teamcity.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.BuildStateEnum;

public class WebHookConfigurationGsonBuilder {
	
	private WebHookConfigurationGsonBuilder() {}

	public static Gson gsonBuilder() {
		return new GsonBuilder()
					.registerTypeAdapter(BuildStateEnum.class, new BuildStateEnumTypeAdaptor())
					.enableComplexMapKeySerialization()
					.setPrettyPrinting()
					.create();
	}
}
