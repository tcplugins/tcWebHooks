package webhook.teamcity.testing.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.BuildStateEnum;

public class WebHookExecutionRequestGsonBuilder {
	
	private WebHookExecutionRequestGsonBuilder() {}

	public static Gson gsonBuilder() {
		return new GsonBuilder()
					.registerTypeAdapter(BuildStateEnum.class, new BuildStateEnumTypeAdaptor())
					.enableComplexMapKeySerialization()
					.setPrettyPrinting()
					.create();
	}
}
