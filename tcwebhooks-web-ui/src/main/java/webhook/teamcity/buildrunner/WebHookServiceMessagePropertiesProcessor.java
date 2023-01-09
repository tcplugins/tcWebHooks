package webhook.teamcity.buildrunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

public class WebHookServiceMessagePropertiesProcessor implements PropertiesProcessor {
	public Collection<InvalidProperty> process(Map<String, String> properties) {
		return new ArrayList<>();
	}

}
