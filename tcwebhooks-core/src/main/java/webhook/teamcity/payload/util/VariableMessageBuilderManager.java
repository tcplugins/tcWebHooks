package webhook.teamcity.payload.util;

import java.util.HashMap;
import java.util.Map;

public class VariableMessageBuilderManager {
	
	Map<String,VariableMessageBuilderFactory> builders = new HashMap<>();
	
	public void registerBuilderFactory(VariableMessageBuilderFactory factory) {
		this.builders.put(factory.getType(), factory);
	}
	
	public VariableMessageBuilder getFactory(String payloadFormat) {
		return builders.get(payloadFormat).build();
	}

}
